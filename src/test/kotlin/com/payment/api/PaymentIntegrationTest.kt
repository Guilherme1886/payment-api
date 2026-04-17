package com.payment.api

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import java.math.BigDecimal

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class PaymentIntegrationTest {

    companion object {
        @Container
        @JvmStatic
        val postgres = PostgreSQLContainer("postgres:16-alpine")

        @DynamicPropertySource
        @JvmStatic
        fun configureProperties(registry: DynamicPropertyRegistry) {
            registry.add("spring.datasource.url", postgres::getJdbcUrl)
            registry.add("spring.datasource.username", postgres::getUsername)
            registry.add("spring.datasource.password", postgres::getPassword)
        }
    }

    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var objectMapper: ObjectMapper

    @Autowired
    lateinit var jdbcTemplate: JdbcTemplate

    @BeforeEach
    fun cleanDatabase() {
        jdbcTemplate.execute("TRUNCATE transactions, users, accounts CASCADE")
    }

    private fun registerUser(email: String, cpf: String, nome: String): JsonNode {
        val body = """
            {
                "email": "$email",
                "password": "senha123",
                "agencia": "0001",
                "numeroConta": "${cpf.substring(0, 5)}-0",
                "titularNome": "$nome",
                "cpf": "$cpf"
            }
        """.trimIndent()

        val result = mockMvc.perform(
            post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body)
        )
            .andExpect(status().isCreated)
            .andReturn()

        return objectMapper.readTree(result.response.contentAsString)
    }

    private fun deposit(accountId: String, amount: BigDecimal, token: String) {
        mockMvc.perform(
            post("/accounts/$accountId/deposit")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer $token")
                .content("""{"amount": $amount}""")
        )
            .andExpect(status().isOk)
    }

    private fun transfer(
        payerId: String,
        receiverId: String,
        amount: BigDecimal,
        idempotencyKey: String,
        token: String
    ) = mockMvc.perform(
        post("/transactions")
            .contentType(MediaType.APPLICATION_JSON)
            .header("Authorization", "Bearer $token")
            .content(
                """
                {
                    "idempotencyKey": "$idempotencyKey",
                    "payerId": "$payerId",
                    "receiverId": "$receiverId",
                    "amount": $amount
                }
                """.trimIndent()
            )
    )

    private fun queryBalance(accountId: String): BigDecimal =
        jdbcTemplate.queryForObject(
            "SELECT balance FROM accounts WHERE id = ?::uuid",
            BigDecimal::class.java,
            accountId
        )!!

    private fun countTransactions(): Long =
        jdbcTemplate.queryForObject("SELECT count(*) FROM transactions", Long::class.java)!!

    @Nested
    @DisplayName("1. Transferência completa")
    inner class FullTransferTest {

        @Test
        fun `deve transferir entre contas e atualizar saldos`() {
            val userA = registerUser("a@test.com", "11111111111", "User A")
            val userB = registerUser("b@test.com", "22222222222", "User B")
            val tokenA = userA["token"].asText()
            val accountA = userA["accountId"].asText()
            val accountB = userB["accountId"].asText()

            deposit(accountA, BigDecimal("1000.00"), tokenA)

            transfer(accountA, accountB, BigDecimal("300.00"), "txn-full-001", tokenA)
                .andExpect(status().isCreated)
                .andExpect(jsonPath("$.status").value("COMPLETED"))
                .andExpect(jsonPath("$.amount").value(300.0))

            assert(queryBalance(accountA).compareTo(BigDecimal("700.00")) == 0) {
                "Saldo de A deveria ser 700.00, mas é ${queryBalance(accountA)}"
            }
            assert(queryBalance(accountB).compareTo(BigDecimal("300.00")) == 0) {
                "Saldo de B deveria ser 300.00, mas é ${queryBalance(accountB)}"
            }
            assert(countTransactions() == 1L) {
                "Deveria ter exatamente 1 transação, mas tem ${countTransactions()}"
            }
        }
    }

    @Nested
    @DisplayName("2. Saldo insuficiente")
    inner class InsufficientFundsTest {

        @Test
        fun `deve retornar 422 e nao salvar transacao`() {
            val userA = registerUser("a@test.com", "11111111111", "User A")
            val userB = registerUser("b@test.com", "22222222222", "User B")
            val tokenA = userA["token"].asText()
            val accountA = userA["accountId"].asText()
            val accountB = userB["accountId"].asText()

            deposit(accountA, BigDecimal("100.00"), tokenA)

            transfer(accountA, accountB, BigDecimal("500.00"), "txn-insuf-001", tokenA)
                .andExpect(status().isUnprocessableEntity)
                .andExpect(jsonPath("$.error").value("Insufficient funds"))

            assert(countTransactions() == 0L) {
                "Nenhuma transação deveria ser salva, mas tem ${countTransactions()}"
            }
            assert(queryBalance(accountA).compareTo(BigDecimal("100.00")) == 0) {
                "Saldo de A deveria permanecer 100.00, mas é ${queryBalance(accountA)}"
            }
        }
    }

    @Nested
    @DisplayName("3. Idempotência")
    inner class IdempotencyTest {

        @Test
        fun `deve processar apenas uma vez com mesmo idempotencyKey`() {
            val userA = registerUser("a@test.com", "11111111111", "User A")
            val userB = registerUser("b@test.com", "22222222222", "User B")
            val tokenA = userA["token"].asText()
            val accountA = userA["accountId"].asText()
            val accountB = userB["accountId"].asText()

            deposit(accountA, BigDecimal("1000.00"), tokenA)

            transfer(accountA, accountB, BigDecimal("200.00"), "txn-idemp-001", tokenA)
                .andExpect(status().isCreated)
                .andExpect(jsonPath("$.status").value("COMPLETED"))

            transfer(accountA, accountB, BigDecimal("200.00"), "txn-idemp-001", tokenA)
                .andExpect(status().isCreated)
                .andExpect(jsonPath("$.status").value("COMPLETED"))

            assert(countTransactions() == 1L) {
                "Deveria ter exatamente 1 transação, mas tem ${countTransactions()}"
            }
            assert(queryBalance(accountA).compareTo(BigDecimal("800.00")) == 0) {
                "Saldo de A deveria ser 800.00 (debitado apenas uma vez), mas é ${queryBalance(accountA)}"
            }
            assert(queryBalance(accountB).compareTo(BigDecimal("200.00")) == 0) {
                "Saldo de B deveria ser 200.00, mas é ${queryBalance(accountB)}"
            }
        }
    }

    @Nested
    @DisplayName("4. Autenticação")
    inner class AuthenticationTest {

        @Test
        fun `deve retornar 403 sem token`() {
            mockMvc.perform(
                get("/accounts/00000000-0000-0000-0000-000000000000")
                    .contentType(MediaType.APPLICATION_JSON)
            )
                .andExpect(status().isForbidden)
        }

        @Test
        fun `deve processar normalmente com token valido`() {
            val userA = registerUser("a@test.com", "11111111111", "User A")
            val tokenA = userA["token"].asText()
            val accountA = userA["accountId"].asText()

            mockMvc.perform(
                get("/accounts/$accountA")
                    .header("Authorization", "Bearer $tokenA")
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.id").value(accountA))
                .andExpect(jsonPath("$.balance").value(0))
        }
    }
}
