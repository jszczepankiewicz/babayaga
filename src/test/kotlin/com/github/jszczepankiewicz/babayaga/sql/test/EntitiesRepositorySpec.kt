package com.github.jszczepankiewicz.babayaga.sql.test

import com.github.jszczepankiewicz.babayaga.config.JdbcDataSourceConfig
import com.github.jszczepankiewicz.babayaga.sql.EntitiesRepository
import com.github.jszczepankiewicz.babayaga.sql.JdbcColumn
import com.github.jszczepankiewicz.babayaga.sql.JdbcMetaDataRepository
import com.github.jszczepankiewicz.babayaga.sql.PostgresqlDBDialect
import io.damo.aspen.Test
import io.damo.aspen.expectException
import io.damo.aspen.spring.SpringTestTreeRunner
import io.damo.aspen.spring.inject
import org.assertj.core.api.Assertions.assertThat
import org.junit.runner.RunWith
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.jdbc.Sql

/**
 * Repository for managing entity tables.
 *
 * @author jszczepankiewicz
 * *
 * @since 2016-11-07
 */
@RunWith(SpringTestTreeRunner::class)
@ContextConfiguration(classes = arrayOf(EntitiesRepository::class, JdbcDataSourceConfig::class,
        JdbcMetaDataRepository::class, PostgresqlDBDialect::class, JdbcMetaDataRepository::class))
@TestPropertySource("/db-test.properties")
@Sql(scripts = arrayOf("/test-before-ddl.sql", "/ddl-postgresql.sql", "/test-after-ddl.sql"))
class EntitiesRepositorySpec : Test({

    val repo: EntitiesRepository = inject(EntitiesRepository::class)
    val metaRepo: JdbcMetaDataRepository = inject(JdbcMetaDataRepository::class)

    describe("When create entities table") {
        test("create table in database") {

            val table = repo.createEntityTable("veryFamousArtists")
            val columns = metaRepo.getColumns(table)

            assertThat(table).isEqualTo("veryfamousartists")
            assertThat(columns).containsExactly(
                    JdbcColumn(name = "added_id", type = "serial", isNullable = false, ordinalPosition = 1),
                    JdbcColumn(name = "id", type = "uuid", isNullable = false, ordinalPosition = 2),
                    JdbcColumn(name = "updated", type = "timestamp", isNullable = false, ordinalPosition = 3),
                    JdbcColumn(name = "body", type = "bytea", isNullable = true, ordinalPosition = 4))
        }
        test("throw IAE when create entity table with empty entityName") {
            expectException(IllegalArgumentException::class, "Can not create entity name, entityName should not be empty") {
                repo.createEntityTable(" ")
            }
        }
    }
})