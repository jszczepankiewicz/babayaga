package com.github.jszczepankiewicz.babayaga.sql.test

import com.github.jszczepankiewicz.babayaga.config.JdbcDataSourceConfig
import com.github.jszczepankiewicz.babayaga.sql.JdbcColumn
import com.github.jszczepankiewicz.babayaga.sql.JdbcMetaDataRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExpectedException
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.jdbc.Sql
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner


/**
 * @author jszczepankiewicz
 * *
 * @since 2016-09-16
 */
@RunWith(SpringJUnit4ClassRunner::class)
@ContextConfiguration(classes = arrayOf(JdbcDataSourceConfig::class, JdbcMetaDataRepository::class))
@TestPropertySource("/db-test.properties")
@Sql(scripts = arrayOf("/test-before-ddl.sql", "/ddl-postgresql.sql"))
class JdbcMetaDataRepositoryTest {

    @Autowired
    lateinit var repository: JdbcMetaDataRepository

    @Rule
    @JvmField
    val thrown: ExpectedException = ExpectedException.none()

    @Test
    fun detectIfTableExists() {
        //  entities table should be created by before sql script
        assertThat(repository.tableExists("entities")).isTrue()
    }

    @Test
    fun detectIfTableDoesNotExists() {
        assertThat(repository.tableExists("thisTableDefinitelyDoesNotExist")).isFalse()
    }

    @Test
    fun throwIAEOnGetColumnsCalledOnNonExistingTable() {

        //  then
        thrown.expect(IllegalArgumentException::class.java)
        thrown.expectMessage("Can not retrieve information about columns on non-existing table: thisTableDefinitelyDoesNotExist")

        //  when
        repository.getColumns("thisTableDefinitelyDoesNotExist")

    }

    @Test
    fun retrieveNonEmptyListOfColumnsInExistingTable() {

        //  when
        val columns = repository.getColumns("entities")

        //  then
        assertThat(columns).hasSize(4)
        assertThat(columns[0]).isEqualTo(JdbcColumn(name = "added_id", type = "bigserial", isNullable = false, ordinalPosition = 1))
        assertThat(columns[1]).isEqualTo(JdbcColumn(name = "id", type = "uuid", isNullable = false, ordinalPosition = 2))
        assertThat(columns[2]).isEqualTo(JdbcColumn(name = "updated", type = "timestamp", isNullable = false, ordinalPosition = 3))
        assertThat(columns[3]).isEqualTo(JdbcColumn(name = "body", type = "bytea", isNullable = true, ordinalPosition = 4))

    }


}