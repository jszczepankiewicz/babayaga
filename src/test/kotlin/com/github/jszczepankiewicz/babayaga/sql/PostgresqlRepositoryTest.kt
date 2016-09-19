package com.github.jszczepankiewicz.babayaga.sql

import com.github.jszczepankiewicz.babayaga.config.JdbcDataSourceConfig
import org.apache.logging.log4j.LogManager.getLogger
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.jdbc.Sql
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner


/**
 * @author jszczepankiewicz
 * *
 * @since 2016-09-06
 */
@RunWith(SpringJUnit4ClassRunner::class)
@ContextConfiguration(classes = arrayOf(JdbcDataSourceConfig::class, PostgresqlRepository::class))
@TestPropertySource("/db-test.properties")
@Sql(scripts = arrayOf("/prepare-for-test.sql", "/ddl-postgresql.sql"))
class PostgresqlRepositoryTest {

    private val LOG = getLogger(this.javaClass.name)

    @Autowired
    lateinit var repository: PostgresqlRepository

    @Test
    fun firstTest() {
        LOG.info("executing first test")
        assertThat(repository.get("something")).isNull()
    }

    @Test
    fun createValidIndexTableIfNotExist() {

        repository.createIndexTable("test_create")
    }
}