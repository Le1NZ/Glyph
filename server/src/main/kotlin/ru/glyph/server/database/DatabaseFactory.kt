package ru.glyph.server.database

import io.ktor.server.application.Application
import io.ktor.server.application.log
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

fun Application.configureDatabase() {
    val url = checkNotNull(System.getenv("DATABASE_URL")) { "DATABASE_URL is not set" }
    val user = checkNotNull(System.getenv("DATABASE_USER")) { "DATABASE_USER is not set" }
    val password = checkNotNull(System.getenv("DATABASE_PASSWORD")) { "DATABASE_PASSWORD is not set" }

    Database.connect(
        url = url,
        driver = "org.postgresql.Driver",
        user = user,
        password = password,
    )

    transaction {
        SchemaUtils.create(Users, Notes)
    }

    log.info("Database connected: $url")
}
