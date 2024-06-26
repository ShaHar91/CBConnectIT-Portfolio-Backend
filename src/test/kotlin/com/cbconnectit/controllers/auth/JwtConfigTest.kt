package com.cbconnectit.controllers.auth

import com.auth0.jwt.JWT
import com.auth0.jwt.exceptions.SignatureVerificationException
import com.cbconnectit.domain.models.user.User
import com.cbconnectit.modules.auth.JwtConfig
import io.ktor.server.config.*
import io.mockk.coEvery
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertThrows
import java.util.*
import java.util.concurrent.TimeUnit

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class JwtConfigTest {

    @Test
    fun `when verifying a token with wrong secret key, we throw exception`() {
        val config = JwtConfig("https://127.0.0.1:8081", "users", "some-secret-key")

        assertThrows<SignatureVerificationException> {
            config.verifyToken("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.i1DDVAVnfKjFv8T2JZbASZ2KcIoJYTQrqmDyv-LLiro")
        }
    }

    @Test
    fun `when verifying correct tokens, we return the associated user id`() {
        val config = JwtConfig("https://127.0.0.1:8081", "users", "some-secret-key")

        val tokens = config.createTokens(User(UUID.fromString("00000000-0000-0000-0000-000000000001")))

        val userId = config.verifyToken(tokens.accessToken)
        val userIdRefresh = config.verifyToken(tokens.refreshToken)

        assertThat(userId).isEqualTo("00000000-0000-0000-0000-000000000001")
        assertThat(userIdRefresh).isEqualTo("00000000-0000-0000-0000-000000000001")
    }

    @Test
    fun `when creating tokens, we return tokens with the correct expiration date`() {
        val config = JwtConfig("https://127.0.0.1:8081", "users", "some-secret-key")

        val tokens = config.createTokens(User(UUID.fromString("00000000-0000-0000-0000-000000000001")))

        val decodeToken = JWT.decode(tokens.accessToken)
        val decodeRefreshToken = JWT.decode(tokens.refreshToken)

        assertThat(decodeToken.expiresAt).isCloseTo(Date(System.currentTimeMillis() + TimeUnit.HOURS.toMillis(24)), 5000)
        assertThat(decodeRefreshToken.expiresAt).isCloseTo(Date(System.currentTimeMillis() + TimeUnit.DAYS.toMillis(30)), 5000)
    }

    @Test
    fun hello() {
        val appConfig = mockk<ApplicationConfig>()

        coEvery { appConfig.property("jwt.issuer").getString() } returns "http://1.1.1.1:8080"
        coEvery { appConfig.property("jwt.audience").getString() } returns "users"

        val config = JwtConfig(appConfig, "some-secret-key")

        val tokens = config.createTokens(User(UUID.fromString("00000000-0000-0000-0000-000000000001")))

        val userId = config.verifyToken(tokens.accessToken)
        val userIdRefresh = config.verifyToken(tokens.refreshToken)

        assertThat(userId).isEqualTo("00000000-0000-0000-0000-000000000001")
        assertThat(userIdRefresh).isEqualTo("00000000-0000-0000-0000-000000000001")
    }
}
