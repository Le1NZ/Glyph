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

    @Suppress("DEPRECATION")
    transaction {
        SchemaUtils.createMissingTablesAndColumns(Users, Folders, Notes, Tags, NoteTags)
    }

    // Separate transaction: information_schema query never throws, so no abort risk.
    // Drops NoteShares only once when old email-based schema is detected.
    @Suppress("DEPRECATION")
    transaction {
        val hasOldEmailColumn = exec(
            """SELECT 1 FROM information_schema.columns
               WHERE table_name = 'note_shares' AND column_name = 'email'"""
        ) { it.next() } == true

        if (hasOldEmailColumn) {
            SchemaUtils.drop(NoteShares)
        }
        SchemaUtils.create(NoteShares)
    }

    log.info("Database connected: $url")
}
