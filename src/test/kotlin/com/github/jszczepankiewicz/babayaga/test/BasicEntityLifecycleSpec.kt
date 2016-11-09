package com.github.jszczepankiewicz.babayaga.test

import com.github.jszczepankiewicz.babayaga.MessagePackTransporter
import com.github.jszczepankiewicz.babayaga.Storage
import com.github.jszczepankiewicz.babayaga.config.JdbcDataSourceConfig
import com.github.jszczepankiewicz.babayaga.sql.JdbcMetaDataRepository
import com.github.jszczepankiewicz.babayaga.sql.EntitiesRepository
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
import java.time.LocalDateTime
import java.time.LocalDateTime.now
import java.util.*
import java.util.UUID.randomUUID

/**
 * @author jszczepankiewicz
 * *
 * @since 2016-10-17
 */
@RunWith(SpringTestTreeRunner::class)
@ContextConfiguration(classes = arrayOf(PostgresqlDBDialect::class, JdbcDataSourceConfig::class, EntitiesRepository::class, JdbcMetaDataRepository::class, Storage::class, MessagePackTransporter::class))
@TestPropertySource("/db-test.properties")
@Sql(scripts = arrayOf("/test-before-ddl.sql", "/ddl-postgresql.sql", "/test-after-ddl.sql"))
class BasicEntityLifecycleSpec : Test({

    val storage: Storage = inject(Storage::class)
    val entityRepo:EntitiesRepository = inject(EntitiesRepository::class)

    val veryFamousArtists = "VeryFamousArtists"

    val person = mapOf(
            "firstName" to "John",
            "age" to 31,
            "married" to true
    )

    var stored: Map<String, Any?>

    var mutablePerson: HashMap<String, Any?> = HashMap(person)

    before {
        entityRepo.createEntityTable(veryFamousArtists)
        mutablePerson = HashMap(person)
        stored = storage.put(veryFamousArtists, person)
    }

    describe("When put entity") {

        test("should throw IAE when id field present without updated") {
            expectException(IllegalArgumentException::class, "Inconsistent entity - id field present without updated") {
                mutablePerson.put("id", randomUUID())
                storage.put(veryFamousArtists, mutablePerson)
            }
        }

        test("should throw IAE when updated field present without id") {
            expectException(IllegalArgumentException::class, "Inconsistent entity - updated field present without id") {
                mutablePerson.put("updated", now())
                storage.put(veryFamousArtists, mutablePerson)
            }
        }

        test("should throw IAE when id field present with updated null") {
            expectException(IllegalArgumentException::class, "Inconsistent entity - id field present with updated empty") {
                mutablePerson.put("updated", null)
                mutablePerson.put("id", randomUUID())
                storage.put(veryFamousArtists, mutablePerson)
            }
        }

        test("should throw IAE when updated field present with id null") {
            expectException(IllegalArgumentException::class, "Inconsistent entity - updated field present with id empty") {
                mutablePerson.put("updated", now())
                mutablePerson.put("id", null)
                storage.put(veryFamousArtists, mutablePerson)
            }
        }

        test("should throw IAE when id field not UUID") {
            expectException(IllegalArgumentException::class, "Inconsistent entity - id field is not UUID") {
                mutablePerson.put("updated", now())
                mutablePerson.put("id", "somethingElseThanUUID")
                storage.put(veryFamousArtists, mutablePerson)
            }
        }

        test("should throw IAE when updated field not LocalDateTime") {
            expectException(IllegalArgumentException::class, "Inconsistent entity - updated field is not LocalDateTime") {
                mutablePerson.put("updated", "inThePast")
                mutablePerson.put("id", randomUUID())
                storage.put(veryFamousArtists, mutablePerson)
            }
        }
    }

    describe("When insert entity") {

        test("should return map with non-modified original fields") {
            stored = storage.put(veryFamousArtists, person)
            assertThat(stored!!["firstName"]).isEqualTo("John")
            assertThat(stored["age"]).isEqualTo(31)
            assertThat(stored["married"]).isEqualTo(true)
        }

        test("should return object with id and updated initialized") {
            stored = storage.put(veryFamousArtists, person)
            assertThat(stored["id"]).isNotNull().isInstanceOf(UUID::class.java)
            assertThat(stored["updated"]).isNotNull().isInstanceOf(LocalDateTime::class.java)
        }
    }

    describe("When retrieve inserted entity") {
        val stored = storage.put(veryFamousArtists, person)
        val retrieved = storage.find(veryFamousArtists, stored["id"] as UUID)

        test("should retrieve non-modified fields") {
            assertThat(retrieved!!["firstName"]).isEqualTo("John")
            assertThat(retrieved["age"]).isEqualTo(31.toByte()) //  numbers are lossless compressed
            assertThat(retrieved["married"]).isEqualTo(true)
        }

        test("should retrieve id and updated initialized") {
            assertThat(retrieved!!["id"]).isNotNull().isEqualTo(stored["id"] as UUID)
            assertThat(retrieved["updated"]).isNotNull().isInstanceOf(LocalDateTime::class.java)
        }
    }

    describe("When update existing entity") {
        var stored: Map<String, Any?> = storage.put(veryFamousArtists, person)
        val retrieved = storage.find(veryFamousArtists, stored["id"] as UUID)
        mutablePerson = HashMap(retrieved)
        mutablePerson.put("newField", "somethingCompletelyNew")
        mutablePerson.remove("married")
        mutablePerson.put("firstName", "JetztMainNameIsWurst")

        val updated = storage.put(veryFamousArtists, mutablePerson)
        val updatedRetrieved = storage.find(veryFamousArtists, updated["id"] as UUID)

        test("should return object with modified fields") {
            assertThat(updated["id"]).isInstanceOf(UUID::class.java).isEqualTo(stored["id"])
            assertThat(updated["id"]).isEqualTo(stored["id"])
            assertThat(updated["firstName"]).isEqualTo("JetztMainNameIsWurst")
            assertThat(updated["newField"]).isEqualTo("somethingCompletelyNew")
            assertThat(updated.containsKey("married")).isFalse()
            assertThat(updated["age"]).isEqualTo(31.toByte())
            assertThat(updated.size).isEqualTo(5) //    3 built-in + 2 meta
        }

        test("should return object with timestamp updated") {
            assertThat(updated["updated"]).isInstanceOf(LocalDateTime::class.java)
            assertThat(updated["updated"]).isNotEqualTo(mutablePerson["updated"])
        }

        test("should retrieve object same as returned") {
            assertThat(updatedRetrieved).isEqualTo(updated)
        }
    }

})