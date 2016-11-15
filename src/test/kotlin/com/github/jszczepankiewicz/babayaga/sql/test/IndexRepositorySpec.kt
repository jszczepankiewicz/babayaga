package com.github.jszczepankiewicz.babayaga.sql.test

import com.github.jszczepankiewicz.babayaga.config.JdbcDataSourceConfig
import com.github.jszczepankiewicz.babayaga.sql.*
import com.github.jszczepankiewicz.babayaga.sql.ColumnType.*
import io.damo.aspen.Test
import io.damo.aspen.expectException
import io.damo.aspen.spring.SpringTestTreeRunner
import io.damo.aspen.spring.inject
import org.assertj.core.api.Assertions.assertThat
import org.junit.runner.RunWith
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.jdbc.Sql
import java.time.LocalDateTime
import java.time.LocalDateTime.now
import java.time.Month.APRIL
import java.util.*
import java.util.UUID.fromString
import java.util.UUID.randomUUID

/**
 * @author jszczepankiewicz
 * *
 * @since 2016-11-01
 */
@RunWith(SpringTestTreeRunner::class)
@ContextConfiguration(classes = arrayOf(JdbcDataSourceConfig::class, IndexRepository::class, JdbcMetaDataRepository::class, PostgresqlDBDialect::class, JdbcMetaDataRepository::class))
@TestPropertySource("/db-test.properties")
@Sql(scripts = arrayOf("/test-before-ddl.sql", "/ddl-postgresql.sql", "/test-after-ddl.sql"))
class IndexRepositorySpec : Test({

    val indexRepository: IndexRepository = inject(IndexRepository::class)
    val metaRepo: JdbcMetaDataRepository = inject(JdbcMetaDataRepository::class)

    val singleAttribute = listOf(Pair("name", TEXT))
    val multipleAttributes = listOf(
            Pair("name", TEXT),
            Pair("born", TIMESTAMP_WITHOUT_TZ),
            Pair("married", ColumnType.BOOL),
            Pair("picture", ColumnType.BINARY))

    describe("When create index table") {
        test("supports single column index") {
            val indexTable = indexRepository.createIndexTable("veryfamousartists", singleAttribute)

            val columns = metaRepo.getColumns(indexTable)

            assertThat(columns).containsExactly(
                    JdbcColumn(name = "name", type = "text", isNullable = false, ordinalPosition = 1),
                    JdbcColumn(name = "id", type = "uuid", isNullable = false, ordinalPosition = 2))

        }
        test("supports multiple column index") {
            val indexTable = indexRepository.createIndexTable("veryfamousartists", multipleAttributes)

            val columns = metaRepo.getColumns(indexTable)

            assertThat(columns).containsExactly(
                    JdbcColumn(name = "name", type = "text", isNullable = false, ordinalPosition = 1),
                    JdbcColumn(name = "born", type = "timestamp", isNullable = false, ordinalPosition = 2),
                    JdbcColumn(name = "married", type = "bool", isNullable = false, ordinalPosition = 3),
                    JdbcColumn(name = "picture", type = "bytea", isNullable = false, ordinalPosition = 4),
                    JdbcColumn(name = "id", type = "uuid", isNullable = false, ordinalPosition = 5))
        }
    }
    describe("When resolve index table name") {

        test("throw IAE when resolve index name with empty entityName") {
            expectException(IllegalArgumentException::class, "Can not resolve index table name, entityName should not be empty") {
                indexRepository.resolveIndexTableName(" ", singleAttribute)
            }
        }

        test("throw IAE when resolve index name with empty list of attributes") {
            expectException(IllegalArgumentException::class, "Can not resolve index table name, list of attributes should not be empty") {
                indexRepository.resolveIndexTableName("artists", emptyList())
            }
        }

        test("throw IAE when resolve index name with empty name of attribute") {
            expectException(IllegalArgumentException::class, "Can not resolve index table name, one of attribute name is empty") {
                indexRepository.resolveIndexTableName("artists", listOf(Pair("name", TEXT), Pair(" ", TIMESTAMP_WITHOUT_TZ)))
            }
        }

        test("return index + entityName + attribute names lowercase when fits db max table name limit") {
            assertThat(indexRepository.resolveIndexTableName("veryFamousArtists", singleAttribute)).isEqualTo("index_veryfamousartists_on_name")
        }

        test("return index + entityName + atribute names shortened to fit limit") {
            // TODO: add mocks and tests that will test more examples
            //  notice shortened 'on_nam'
            assertThat(indexRepository.resolveIndexTableName("veryFamousArtists", multipleAttributes)).isEqualTo("index_veryfamousartists_on_nam_and_bor_and_marrie_and_pictur")
        }
    }

    describe("When inserting index value") {

        val friends = "friends"
        val indexOnNameAndBorn = listOf(
                Pair("name", TEXT),
                Pair("born", TIMESTAMP_WITHOUT_TZ))

        before {
            indexRepository.createIndexTable(friends, indexOnNameAndBorn)
        }

        test("throw IAE when attempt to insert value for empty arguments") {
            expectException(IllegalArgumentException::class, "Can not insert empty entities arguments into index") {
                indexRepository.insertIndexValue("artists", listOf(Pair("name", TEXT)))
            }
        }

        test("supports inserting single entry") {

            val maryska = mapOf("id" to randomUUID(), "name" to "Maryska Zakaszewska", "born" to now())
            indexRepository.insertIndexValue(friends, indexOnNameAndBorn, maryska)

            val stored = indexRepository.getIndexValuesById(friends, indexOnNameAndBorn, maryska.get("id") as UUID)
            assertThat(stored).isEqualTo(maryska)
        }

        test("supports inserting multiple entries at once") {

            val maryska = mapOf("id" to randomUUID(), "name" to "Maryska Zakaszewska", "born" to now())
            val jacek = mapOf("id" to randomUUID(), "name" to "Jacek Sorgowicki", "born" to LocalDateTime.of(1986, APRIL, 8, 12, 30))

            indexRepository.insertIndexValue(friends, indexOnNameAndBorn, jacek, maryska)

            val storedJacek = indexRepository.getIndexValuesById(friends, indexOnNameAndBorn, jacek.get("id") as UUID)
            val storedMaryska = indexRepository.getIndexValuesById(friends, indexOnNameAndBorn, maryska.get("id") as UUID)

            assertThat(storedJacek).isEqualTo(jacek)
            assertThat(storedMaryska).isEqualTo(maryska)
        }

    }

    describe("When retrieving index values for given id") {
        test("retrieve indexed attributes map") {
            val id = fromString("a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a22")
            val attributes = indexRepository.getIndexValuesById("cars", listOf(Pair("type", TEXT), Pair("active", BOOL)), id)
            assertThat(attributes!!.size).isEqualTo(3)
            assertThat(attributes["type"]).isEqualTo("Syrena Sport")
            assertThat(attributes["id"]).isEqualTo(id)
            assertThat(attributes["active"]).isEqualTo(true)
        }
    }


})