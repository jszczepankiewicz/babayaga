package com.github.jszczepankiewicz.babayaga.sql

import org.apache.logging.log4j.LogManager.getLogger
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Repository
import java.sql.*
import java.sql.Statement.RETURN_GENERATED_KEYS
import java.util.*
import javax.sql.DataSource

/**
 *
 * @since 2016-09-06
 * @author jszczepankiewicz
 */
@Repository
class EntitiesRepository(val dataSource: DataSource, val dbDialect: DBDialect) {

    private val LOG = getLogger()

    private var sql: JdbcTemplate

    init {
        sql = JdbcTemplate(dataSource)
        LOG.info("PostgresqlRepository initialized")
    }

    fun updateEntity(entityName: String, entity: Entity) {

        val tableName = getTableName(entityName)

        LOG.debug("Update {}", entity.id)

        var insert: PreparedStatement? = null
        var conn: Connection? = null

        try {
            val query = "UPDATE %s SET updated=?, body=? WHERE id=?".format(tableName)
            conn = dataSource.connection
            insert = conn.prepareStatement(query)

            insert.setTimestamp(1, Timestamp.valueOf(entity.updated))
            insert.setBinaryStream(2, entity.body.inputStream())
            insert.setObject(3, entity.id)
            insert.executeUpdate()

        } catch (e: SQLException) {
            throw IllegalStateException(e)
        } finally {
            insert?.close()
            conn?.close()
        }
    }

    fun getById(entityName: String, id: UUID): Entity? {

        val tableName = getTableName(entityName)

        LOG.debug("getById {}.{}", entityName, id)

        var get: PreparedStatement? = null
        var conn: Connection? = null

        try {
            conn = dataSource.connection
            get = conn.prepareStatement("SELECT * FROM %s WHERE id=?".format(tableName))
            get.setObject(1, id)
            val results = get.executeQuery()
            var entity: Entity? = null
            while (results.next()) {
                entity = map(results)
            }
            return entity
        } catch (e: SQLException) {
            throw IllegalStateException(e)
        } finally {
            get?.close()
            conn?.close()
        }
    }

    /**
     * Insert entity into given table
     */
    fun insertEntity(entityName: String, entity: Entity): Long {
        val tableName = getTableName(entityName)

        LOG.debug("Before inserting tuple of id {}", entity.id)
        var insert: PreparedStatement? = null
        var conn: Connection? = null
        var key: Long = 0
        try {
            val query = "INSERT INTO %s(id, updated, body) VALUES (?,?,?)".format(tableName)
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
        } catch (e: SQLException) {
            throw IllegalStateException(e)
        } finally {
            insert?.close()
            conn?.close()
        }

        LOG.debug("Put {} at {}", entity.id, key)
        return key
    }

    /**
     * TODO: add cache
     */
    fun getTableName(entityName: String): String {

        if (entityName.trim().length == 0) {
            throw IllegalArgumentException("Can not resolve table name, entityName should not be empty")
        }

        return entityName.trim().toLowerCase()
    }

    /**
     * Creates standard entities table
     *
     * @return entity table name
     */
    fun createEntityTable(entityName: String): String {

        val tableName = getTableName(entityName)
        val ddl = dbDialect.buildCreateEntityTableDDL(tableName)
        var insert: Statement? = null
        var conn: Connection? = null

        try {
            conn = dataSource.connection
            insert = conn.prepareStatement(ddl)
            insert.executeUpdate()

        } catch (e: SQLException) {
            throw IllegalStateException(e)
        } finally {
            insert?.close()
            conn?.close()
        }

        LOG.info("Entity table {} created", tableName)
        return tableName
    }

    /**
     *
     */
    @Deprecated("old api")
    fun getById(id: UUID): Entity? {

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

    @Deprecated("old api")
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

    @Deprecated("old api")
    fun updateEntity(entity: Entity) {

        LOG.debug("Update {}", entity.id)

        var insert: PreparedStatement? = null
        var conn: Connection? = null

        try {
            val query = "UPDATE entities SET updated=?, body=? WHERE id=?"
            conn = dataSource.connection
            insert = conn.prepareStatement(query)

            insert.setTimestamp(1, Timestamp.valueOf(entity.updated))
            insert.setBinaryStream(2, entity.body.inputStream())
            insert.setObject(3, entity.id)
            insert.executeUpdate()

        } finally {
            insert?.close()
            conn?.close()
        }
    }

    @Deprecated("old api")
    fun deleteEntity(id: UUID): Boolean {
        LOG.debug("Delete {}", id)
        var delete: PreparedStatement? = null
        var conn: Connection? = null
        try {
            val query = "DELETE FROM entities WHERE id=?"
            conn = dataSource.connection
            delete = conn.prepareStatement(query)
            delete.setObject(1, id)
            val results = delete.executeUpdate()
            if (results == 0) {
                return false
            }

            return true
        } finally {
            delete?.close()
            conn?.close()
        }
    }

    /**
     * Create entry in entities table returning native db pk. Please note this function does not replace existing
     * entries. Just put new version into entities table without doing anything related to indexes
     */
    @Deprecated("old api")
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

    fun map(rs: ResultSet): Entity {
        return Entity(refKey = rs.getLong("added_id"), id = rs.getObject("id") as UUID, updated = rs.getTimestamp("updated").toLocalDateTime(), body = rs.getBytes("body"))
    }


}