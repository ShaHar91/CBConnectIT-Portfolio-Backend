package com.cbconnectit.plugins

import com.auth0.jwt.JWTVerifier
import com.cbconnectit.data.database.dao.UserDaoImpl
import com.cbconnectit.domain.interfaces.IUserDao
import com.cbconnectit.modules.auth.AuthController
import com.cbconnectit.modules.auth.AuthControllerImpl
import com.cbconnectit.modules.auth.JwtConfig
import com.cbconnectit.modules.auth.TokenProvider
import com.cbconnectit.utils.PasswordManager
import com.cbconnectit.utils.PasswordManagerContract
import io.ktor.server.application.*
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module
import org.koin.ktor.plugin.Koin

fun Application.configureKoin() {
    module {
        install(Koin) {
            modules(
                module {
                    single<PasswordManagerContract> { PasswordManager }
                    single<TokenProvider> {
                        JwtConfig("https://jwt-provider-domain/", JwtConfig.USERS_AUDIENCE, System.getenv("JWT_SECRET"))
                    }
                    single<JWTVerifier> {
                        val tokenProvider = get<TokenProvider>()
                        tokenProvider.verifier
                    }
                },
                routeModule(),
                daoModule()
            )
        }
    }
}

fun routeModule() = module {
    singleOf(::AuthControllerImpl) { bind<AuthController>() }
}

fun daoModule() = module {
    singleOf(::UserDaoImpl) { bind<IUserDao>() }
}