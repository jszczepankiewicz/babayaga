package com.github.jszczepankiewicz.babayaga.sql.test

import com.github.jszczepankiewicz.babayaga.config.JdbcDataSourceConfig
import com.github.jszczepankiewicz.babayaga.sql.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.jdbc.Sql
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner
import java.time.LocalDateTime.now
import java.time.LocalDateTime.parse
import java.util.*
import java.util.UUID.randomUUID


/**
 * @author jszczepankiewicz
 * *
 * @since 2016-09-06
 */
@RunWith(SpringJUnit4ClassRunner::class)
@ContextConfiguration(classes = arrayOf(JdbcDataSourceConfig::class, EntitiesRepository::class, JdbcMetaDataRepository::class, PostgresqlDBDialect::class))
@TestPropertySource("/db-test.properties")
@Sql(scripts = arrayOf("/test-before-ddl.sql", "/ddl-postgresql.sql", "/test-after-ddl.sql"))
class EntitiesRepositoryTest {

    val existingEntityPK: Long = 99999
    val existingEntityUUID: UUID = UUID.fromString("a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11")
    val nonExistingEntityUUID: UUID = UUID.fromString("aaaaaaaa-9c0b-4ef8-bb6d-6bb9bd380a11")

    @Autowired
    lateinit var repository: EntitiesRepository

    @Autowired
    lateinit var metaRepository: JdbcMetaDataRepository

    @Test
    fun deleteNonExistingEntity() {

        //  when
        val deleted = repository.deleteEntity(nonExistingEntityUUID)

        //  then
        assertThat(deleted).isFalse()
    }

    @Test
    fun deleteExistingEntity() {

        //  when
        val deleted = repository.deleteEntity(existingEntityUUID)

        //  then
        assertThat(deleted).isTrue()
    }

    @Test
    fun updateExistingEntity() {

        //  given
        val entity = Entity(id = existingEntityUUID, body = ByteArray(100), updated = parse("2034-12-19T10:23:54"), refKey = null)

        //  when
        repository.updateEntity(entity)

        //  then
        val retrieved = repository.getById(existingEntityUUID)
        assertThat(retrieved).isNotNull()
        assertThat(retrieved!!.refKey).isEqualTo(existingEntityPK)
        assertThat(retrieved.updated).isEqualTo("2034-12-19T10:23:54")
        assertThat(retrieved.body).isEqualTo(ByteArray(100))
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
    fun getExistingEntityById() {
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
    fun getExistingEntityByPk() {

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
    fun getNullForRetrievalOfNonExistingEntityByPK() {

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