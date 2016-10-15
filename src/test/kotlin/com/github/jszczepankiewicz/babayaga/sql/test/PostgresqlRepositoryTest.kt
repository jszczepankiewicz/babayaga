package com.github.jszczepankiewicz.babayaga.sql.test

import com.github.jszczepankiewicz.babayaga.config.JdbcDataSourceConfig
import com.github.jszczepankiewicz.babayaga.sql.Entity
import com.github.jszczepankiewicz.babayaga.sql.JdbcColumn
import com.github.jszczepankiewicz.babayaga.sql.JdbcMetaDataRepository
import com.github.jszczepankiewicz.babayaga.sql.PostgresqlRepository
import org.apache.logging.log4j.LogManager.getLogger
import org.assertj.core.api.Assertions.assertThat
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.jdbc.Sql
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner
import java.time.LocalDateTime.now
import java.util.*
import java.util.UUID.randomUUID
import kotlin.text.Charsets.UTF_8


/**
 * @author jszczepankiewicz
 * *
 * @since 2016-09-06
 */
@RunWith(SpringJUnit4ClassRunner::class)
@ContextConfiguration(classes = arrayOf(JdbcDataSourceConfig::class, PostgresqlRepository::class, JdbcMetaDataRepository::class))
@TestPropertySource("/db-test.properties")
@Sql(scripts = arrayOf("/test-before-ddl.sql", "/ddl-postgresql.sql", "/test-after-ddl.sql"))
class PostgresqlRepositoryTest {

    val existingEntityPK:Long = 99999
    val existingEntityUUID:UUID = UUID.fromString("a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11")

    @Autowired
    lateinit var repository: PostgresqlRepository

    @Autowired
    lateinit var metaRepository: JdbcMetaDataRepository

    @Test
    fun createValidIndexTableIfNotExist() {

        //  when
        repository.createIndexTable("book_of_author")

        //  then
        assertThat(metaRepository.tableExists("index_book_of_author")).isTrue()
        val columns = metaRepository.getColumns("index_book_of_author")
        assertThat(columns).hasSize(2)
        //  TODO: refactor to kotlin style as no clean java collection here
        //  probably need some custom matcher using Kotlin syntax
        assertThat(columns[0]).isEqualTo(JdbcColumn(name = "book_of_author", type = "bytea", isNullable = false, ordinalPosition = 1))
        assertThat(columns[1]).isEqualTo(JdbcColumn(name = "entity_id", type = "bytea", isNullable = false, ordinalPosition = 2))
    }

    @Ignore("unimplemented")
    @Test
    fun updateExistingEntity(){

        val retrieved = repository.getByPk(existingEntityPK)   //  inserted by test-after-ddl.sql in Before

        //  then
        assertThat(retrieved).isNotNull()
        assertThat(retrieved!!.id).isEqualTo(UUID.fromString("a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11"))
        assertThat(retrieved.refKey).isEqualTo(existingEntityPK)
        assertThat(retrieved.updated).isEqualTo("2004-10-19T10:23:54")
        assertThat(retrieved.body).isEqualTo("isBabaYagaTheUgliest:true".toByteArray(Charsets.US_ASCII))


    }

    @Test
    fun insertValidEntityForNonExistingId() {

        val entity = Entity(id = randomUUID(), body = testBody(), updated = now(), refKey = null)

        //  when
        val savedId = repository.insertEntity(entity)

        //  then
        assertThat(savedId).isGreaterThan(0)
    }

    @Test
    fun getExistingEntityById(){
        //  when
        val retrieved = repository.getById(existingEntityUUID)   //  inserted by test-after-ddl.sql in Before

        //  then
        assertThat(retrieved).isNotNull()
        assertThat(retrieved!!.id).isEqualTo(existingEntityUUID)
        assertThat(retrieved.refKey).isEqualTo(existingEntityPK)
        assertThat(retrieved.updated).isEqualTo("2004-10-19T10:23:54")
        assertThat(retrieved.body).isEqualTo("isBabaYagaTheUgliest:true".toByteArray(Charsets.US_ASCII))
    }

    @Test
    fun getExistingEntityByPk(){

        //  when
        val retrieved = repository.getByPk(existingEntityPK)   //  inserted by test-after-ddl.sql in Before

        //  then
        assertThat(retrieved).isNotNull()
        assertThat(retrieved!!.id).isEqualTo(UUID.fromString("a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11"))
        assertThat(retrieved.refKey).isEqualTo(existingEntityPK)
        assertThat(retrieved.updated).isEqualTo("2004-10-19T10:23:54")
        assertThat(retrieved.body).isEqualTo("isBabaYagaTheUgliest:true".toByteArray(Charsets.US_ASCII))
    }

    @Test
    fun getNullForRetrievalOfNonExistingEntityByPK(){

        //  when
        val retrieved = repository.getByPk(Long.MAX_VALUE)

        //  then
        assertThat(retrieved).isNull()
    }

    @Test
    fun fullRoundtripPutAndGetForEntityWithoutOlderVersions() {

        //  given
        val entity = Entity(id = randomUUID(), body = testBody(), updated = now(), refKey = null)

        //  when
        val serialId = repository.insertEntity(entity)
        val retrieved = repository.getByPk(serialId)

        //  then
        assertThat(retrieved).isEqualToIgnoringGivenFields(entity, "refKey")
    }

}