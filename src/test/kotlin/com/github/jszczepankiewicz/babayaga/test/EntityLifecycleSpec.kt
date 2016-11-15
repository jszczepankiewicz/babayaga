package com.github.jszczepankiewicz.babayaga.test

import com.github.jszczepankiewicz.babayaga.MessagePackTransporter
import com.github.jszczepankiewicz.babayaga.Storage
import com.github.jszczepankiewicz.babayaga.config.JdbcDataSourceConfig
import com.github.jszczepankiewicz.babayaga.sql.EntitiesRepository
import com.github.jszczepankiewicz.babayaga.sql.IndexRepository
import com.github.jszczepankiewicz.babayaga.sql.JdbcMetaDataRepository
import com.github.jszczepankiewicz.babayaga.sql.PostgresqlDBDialect
import io.damo.aspen.Test
import io.damo.aspen.expectException
import io.damo.aspen.spring.SpringTestTreeRunner
import io.damo.aspen.spring.inject
import org.apache.logging.log4j.LogManager.getLogger
import org.assertj.core.api.Assertions
import org.junit.runner.RunWith
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.jdbc.Sql
import java.time.LocalDateTime
import java.util.*

/**
 * @since 2016-11-11
 * @author jszczepankiewicz
 */
@RunWith(SpringTestTreeRunner::class)
@ContextConfiguration(classes = arrayOf(IndexRepository::class, EntitiesRepository::class, JdbcDataSourceConfig::class, MessagePackTransporter::class,
        JdbcMetaDataRepository::class, PostgresqlDBDialect::class, JdbcMetaDataRepository::class, Storage::class))
@TestPropertySource("/db-test.properties")
@Sql(scripts = arrayOf("/test-before-ddl.sql", "/ddl-postgresql.sql", "/test-after-ddl.sql"))
class EntityLifecycleSpec : Test({

    val LOG = getLogger()

    val storage: Storage = inject(Storage::class)
    val entityRepo: EntitiesRepository = inject(EntitiesRepository::class)

    val veryFamousArtists = "VeryFamousArtists"
    val person = mapOf(
            "firstName" to "John",
            "age" to 31,
            "married" to true)

    describe("When put entity") {

        before {
            entityRepo.createEntityTable(veryFamousArtists)
        }

        test("should throw IAE when id field present without updated") {
            expectException(IllegalArgumentException::class, "Inconsistent entity - id field present without updated") {
                var mutablePerson = HashMap(person)
                mutablePerson.put("id", UUID.randomUUID())
                storage.put(veryFamousArtists, mutablePerson)
            }
        }

        test("should throw IAE when updated field present without id") {
            expectException(IllegalArgumentException::class, "Inconsistent entity - updated field present without id") {
                var mutablePerson = HashMap(person)
                mutablePerson.put("updated", LocalDateTime.now())
                storage.put(veryFamousArtists, mutablePerson)
            }
        }

        test("should throw IAE when id field present with updated null") {
            expectException(IllegalArgumentException::class, "Inconsistent entity - id field present with updated empty") {
                var mutablePerson = HashMap(person)
                mutablePerson.put("updated", null)
                mutablePerson.put("id", UUID.randomUUID())
                storage.put(veryFamousArtists, mutablePerson)
            }
        }

        test("should throw IAE when updated field present with id null") {
            expectException(IllegalArgumentException::class, "Inconsistent entity - updated field present with id empty") {
                var mutablePerson = HashMap(person)
                mutablePerson.put("updated", LocalDateTime.now())
                mutablePerson.put("id", null)
                storage.put(veryFamousArtists, mutablePerson)
            }
        }

        test("should throw IAE when id field not UUID") {
            expectException(IllegalArgumentException::class, "Inconsistent entity - id field is not UUID") {
                var mutablePerson = HashMap(person)
                mutablePerson.put("updated", LocalDateTime.now())
                mutablePerson.put("id", "somethingElseThanUUID")
                storage.put(veryFamousArtists, mutablePerson)
            }
        }

        test("should throw IAE when updated field not LocalDateTime") {
            expectException(IllegalArgumentException::class, "Inconsistent entity - updated field is not LocalDateTime") {
                var mutablePerson = HashMap(person)
                mutablePerson.put("updated", "inThePast")
                mutablePerson.put("id", UUID.randomUUID())
                storage.put(veryFamousArtists, mutablePerson)
            }
        }
    }

    describe("When successfully put new entity") {

        before {
            entityRepo.createEntityTable(veryFamousArtists)
        }

        test("should return map with non-modified original fields") {
            val stored = storage.put(veryFamousArtists, person)
            Assertions.assertThat(stored!!["firstName"]).isEqualTo("John")
            Assertions.assertThat(stored["age"]).isEqualTo(31)
            Assertions.assertThat(stored["married"]).isEqualTo(true)
        }

        test("should return object with id and updated initialized") {
            val stored = storage.put(veryFamousArtists, person)
            Assertions.assertThat(stored["id"]).isNotNull().isInstanceOf(UUID::class.java)
            Assertions.assertThat(stored["updated"]).isNotNull().isInstanceOf(LocalDateTime::class.java)
        }
    }

    describe("When retrieve inserted entity") {

        entityRepo.createEntityTable(veryFamousArtists)

        val stored = storage.put(veryFamousArtists, person)
        val retrieved = storage.find(veryFamousArtists, stored["id"] as UUID)

        test("should retrieve non-modified fields") {
            Assertions.assertThat(retrieved!!["firstName"]).isEqualTo("John")
            Assertions.assertThat(retrieved["age"]).isEqualTo(31.toByte()) //  numbers are lossless compressed
            Assertions.assertThat(retrieved["married"]).isEqualTo(true)
        }

        test("should retrieve id and updated initialized") {
            Assertions.assertThat(retrieved!!["id"]).isNotNull().isEqualTo(stored["id"] as UUID)
            Assertions.assertThat(retrieved["updated"]).isNotNull().isInstanceOf(LocalDateTime::class.java)
        }
    }

    describe("When update existing entity") {

        before {
            entityRepo.createEntityTable(veryFamousArtists)
        }

        var stored: Map<String, Any?> = storage.put(veryFamousArtists, person)
        val retrieved = storage.find(veryFamousArtists, stored["id"] as UUID)

        var mutablePerson = HashMap(retrieved)
        mutablePerson.put("newField", "somethingCompletelyNew")
        mutablePerson.remove("married")
        mutablePerson.put("firstName", "JetztMainNameIsWurst")

        val updated = storage.put(veryFamousArtists, mutablePerson)
        val updatedRetrieved = storage.find(veryFamousArtists, updated["id"] as UUID)

        test("should return object with modified fields") {
            Assertions.assertThat(updated["id"]).isInstanceOf(UUID::class.java).isEqualTo(stored["id"])
            Assertions.assertThat(updated["id"]).isEqualTo(stored["id"])
            Assertions.assertThat(updated["firstName"]).isEqualTo("JetztMainNameIsWurst")
            Assertions.assertThat(updated["newField"]).isEqualTo("somethingCompletelyNew")
            Assertions.assertThat(updated.containsKey("married")).isFalse()
            Assertions.assertThat(updated["age"]).isEqualTo(31.toByte())
            Assertions.assertThat(updated.size).isEqualTo(5) //    3 built-in + 2 meta
        }

        test("should return object with timestamp updated") {
            Assertions.assertThat(updated["updated"]).isInstanceOf(LocalDateTime::class.java)
            Assertions.assertThat(updated["updated"]).isNotEqualTo(mutablePerson["updated"])
        }

        test("should retrieve object same as returned") {
            Assertions.assertThat(updatedRetrieved).isEqualTo(updated)
        }
    }
})