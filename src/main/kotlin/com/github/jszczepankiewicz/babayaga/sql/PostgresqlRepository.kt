package com.github.jszczepankiewicz.babayaga.sql

import org.apache.logging.log4j.LogManager.getLogger
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Repository
import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.Statement.RETURN_GENERATED_KEYS
import java.sql.Timestamp
import java.util.*
import javax.sql.DataSource

/**
 * TODO: replace to use PreparedStatements
 *
 * @since 2016-09-06
 * @author jszczepankiewicz
 */
@Repository
class PostgresqlRepository(val dataSource: DataSource) {

    private val LOG = getLogger()

    private var sql: JdbcTemplate

    init {
        sql = JdbcTemplate(dataSource)
        LOG.info("PostgresqlRepository initialized")
    }

    fun getById(id:UUID):Entity?{

        LOG.debug("getById {}", id)

        var get: PreparedStatement? = null
        var conn: Connection? = null

        try {
            conn = dataSource.connection
            get = conn.prepareStatement("SELECT * FROM entities WHERE id=?")
            get.setObject(1, id)
            val results = get.executeQuery()
            var entity: Entity? = null
            while (results.next()) {
                entity = map(results)
            }
            return entity
        } finally {
            get?.close()
            conn?.close()
        }
    }

    fun getByPk(pk: Long): Entity? {

        LOG.debug("GetByPK {}", pk)

        var get: PreparedStatement? = null
        var conn: Connection? = null

        try {
            conn = dataSource.connection
            get = conn.prepareStatement("SELECT * FROM entities WHERE added_id=?")
            get.setLong(1, pk)
            val results = get.executeQuery()
            var entity: Entity? = null
            while (results.next()) {
                entity = map(results)
            }
            return entity
        } finally {
            get?.close()
            conn?.close()
        }
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
        sql.execute("CREATE TABLE index_%s (%s BYTEA NOT NULL, entity_id BYTEA NOT NULL UNIQUE, PRIMARY KEY(%s, entity_id))".format(
                attributeName, attributeName, attributeName))
        LOG.info("Created index table index_{}", attributeName)
    }

    fun updateEntity(entity:Entity){
        LOG.debug("Update {}", entity.id)
        throw UnsupportedOperationException("Unimplemented")
    }

    /**
     * Create entry in entities table returning native db pk. Please note this function does not replace existing
     * entries. Just put new version into entities table without doing anything related to indexes
     */
    fun insertEntity(entity: Entity): Long {

        LOG.debug("Before inserting tuple of id {}", entity.id)
        var insert: PreparedStatement? = null
        var conn: Connection? = null
        var key: Long = 0
        try {
            val query = "INSERT INTO entities(id, updated, body) VALUES (?,?,?)"
            conn = dataSource.connection
            insert = conn.prepareStatement(query, RETURN_GENERATED_KEYS)
            insert.setObject(1, entity.id)
            insert.setTimestamp(2, Timestamp.valueOf(entity.updated))
            insert.setBinaryStream(3, entity.body.inputStream())
            insert.executeUpdate()
            val rs = insert.generatedKeys

            if (rs.next()) {
                key = rs.getLong(1)
            }
            if (key.equals(0)) {
                throw IllegalStateException("No generated key info")
            }
        } finally {
            insert?.close()
            conn?.close()
        }

        LOG.debug("Put {} at {}", entity.id, key)
        return key
    }



    fun putIndexEntry(indexName: String, indexId: String, entityId: String) {
        //  implement me
    }

    fun dropIndexTable(attributeName: String) {
        //  implement me
    }

    fun map(rs: ResultSet): Entity {
        return Entity(refKey = rs.getLong("added_id"), id = rs.getObject("id") as UUID, updated = rs.getTimestamp("updated").toLocalDateTime(), body = rs.getBytes("body"))
    }


}