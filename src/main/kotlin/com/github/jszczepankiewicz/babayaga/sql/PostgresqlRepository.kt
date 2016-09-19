package com.github.jszczepankiewicz.babayaga.sql

import org.apache.logging.log4j.LogManager.getLogger
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Repository
import javax.sql.DataSource

/**
 * @since 2016-09-06
 * @author jszczepankiewicz
 */
@Repository
class PostgresqlRepository(dataSource: DataSource) {

    private val LOG = getLogger(this.javaClass.name)

    private lateinit var jdbcTemplate: JdbcTemplate

    init {
        jdbcTemplate = JdbcTemplate(dataSource)
        LOG.info("PostgresqlRepository initialized")
    }

    fun get(id: String): Entity? {
        return null
    }

    /**
     * Create index table which will have following schema:
     * table name: index_attributeName
     * column 1: attributeName_id
     * column 2: entity_id
     * PK(attributeName_id, entity_id)
     */
    fun createIndexTable(attributeName: String) {
        LOG.debug("Creating index table index_{}", attributeName)
        jdbcTemplate.execute(String.format("CREATE TABLE index_%s (%s BYTEA NOT NULL, entity_id BYTEA NOT NULL UNIQUE, PRIMARY KEY(%s, entity_id))",
                attributeName, attributeName, attributeName))
        LOG.info("Created index table index_{}", attributeName)
    }


}