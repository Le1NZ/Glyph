package ru.glyph.server.database

import io.ktor.server.application.Application
import io.ktor.server.application.log
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

fun Application.configureDatabase() {
    val url = environment.config.property("database.url").getString()
    val user = environment.config.property("database.user").getString()
    val password = environment.config.property("database.password").getString()

    Database.connect(
        url = url,
        driver = "org.postgresql.Driver",
        user = user,
        password = password,
    )

    transaction {
        SchemaUtils.createMissingTablesAndColumns(Users, Notes)
    }

    log.info("Database connected: $url")
}
