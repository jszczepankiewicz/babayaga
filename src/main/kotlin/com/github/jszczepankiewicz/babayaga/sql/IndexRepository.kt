package com.github.jszczepankiewicz.babayaga.sql

import com.github.jszczepankiewicz.babayaga.sql.ColumnType.*
import org.apache.logging.log4j.LogManager.getLogger
import org.springframework.stereotype.Repository
import java.sql.*
import java.time.LocalDateTime
import java.util.*
import javax.sql.DataSource

/**
 * @since 2016-11-01
 * @author jszczepankiewicz
 */
@Repository
class IndexRepository(val dataSource: DataSource, val dbDialect: DBDialect) {

    private val LOG = getLogger()

    /**
     * Index table name builder. It builds the table name by default from "index_{entityName}_on_{nameOfColumn...n}.
     * If name exceeds limits of RDBMS platform name will be shorten to still meet the goals. Note that this method
     * does NOT check whether after shortening given index table name exists in db. This is possible especially
     * if table name length adjustment was applied. Might be implemented in future.
     * TODO: add caching
     * @return table name
     */
    fun resolveIndexTableName(entityName: String, columns: List<Pair<String, ColumnType>>): String {

        if (entityName.trim().length == 0) {
            throw IllegalArgumentException("Can not resolve index table name, entityName should not be empty")
        }

        if (columns.size == 0) {
            throw IllegalArgumentException("Can not resolve index table name, list of attributes should not be empty")
        }

        var unfinished = true
        var cutTheAtributeNameBy = 0

        while (unfinished) {
            val name = StringBuilder().append("index_").append(entityName.toLowerCase())

            var isFirst = true

            for ((columnName, columnType) in columns) {
                var columnNormalized = columnName.trim()
                if (columnNormalized.length == 0) {
                    throw IllegalArgumentException("Can not resolve index table name, one of attribute name is empty")
                }

                if (cutTheAtributeNameBy > 0) {
                    if (columnNormalized.length > cutTheAtributeNameBy) {
                        columnNormalized = columnNormalized.substring(0, columnNormalized.length - cutTheAtributeNameBy)
                    }
                }

                name.append(if (isFirst) "_on_" else "_and_").append(columnNormalized.toLowerCase())
                isFirst = false
            }

            if (name.length > dbDialect.getMaximumTableNameLength()) {
                cutTheAtributeNameBy++
                // FIXME: need to add control to whether infinite loop might happen
                continue
            }


            return name.toString()
        }

        throw IllegalStateException("Unable to build index table name")
    }

    /**
     * Create index table in database.
     * @return name of index table in db
     */
    fun createIndexTable(entityName: String, columns: List<Pair<String, ColumnType>>): String {

        val tableName = resolveIndexTableName(entityName, columns)
        val ddl = dbDialect.buildCreateIndexTableDDL(tableName, columns)
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

        LOG.info("Index table {} created", tableName)
        return tableName
    }

    fun dropIndexTable(entityName: String, columns: List<Pair<String, ColumnType>>) {
        val tableName = resolveIndexTableName(entityName, columns)
    }

    /**
     * Retrieve values stored in index table which are assigned to given id. Although not used directly from client api
     * usefull in low level index entityRepo tests.
     */
    fun getIndexValuesById(entityName: String, columns: List<Pair<String, ColumnType>>, id: UUID): Map<String, Any?>? {

        val tableName = resolveIndexTableName(entityName, columns)

        var get: PreparedStatement? = null
        var conn: Connection? = null

        try {
            conn = dataSource.connection
            get = conn.prepareStatement("SELECT * FROM %s WHERE id=?".format(tableName))
            get.setObject(1, id)
            val results = get.executeQuery()
            var entity: Map<String, Any?>? = null

            while (results.next()) {

                entity = HashMap()
                entity.put("id", results.getObject("id") as UUID)
                for ((column, type) in columns) {

                    when (type) {
                        BOOL -> entity.put(column, results.getBoolean(column))
                        TEXT -> entity.put(column, results.getString(column))
                        TIMESTAMP_WITHOUT_TZ -> entity.put(column, results.getTimestamp(column).toLocalDateTime())
                        BINARY -> entity.put(column, results.getBytes("body"))
                    }
                }
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
     * TODO: cache
     */
    fun getInsertIntoIndexSql(entityName: String, columns: List<Pair<String, ColumnType>>): String {

        val tableName = resolveIndexTableName(entityName, columns)
        val builder = StringBuilder(1024)
        builder.append("INSERT INTO ").append(tableName).append("(id")

        for ((columnName, columnType) in columns) {
            builder.append(",").append(columnName)
        }

        builder.append(") VALUES (?").append(",?".repeat(columns.size)).append(")")
        return builder.toString()
    }

    fun insertIndexValue(entityName: String, columns: List<Pair<String, ColumnType>>, vararg entities: Map<String, Any?>) {

        if (entities.size == 0) {
            throw IllegalArgumentException("Can not insert empty entities arguments into index")
        }

        var insert: PreparedStatement? = null
        var conn: Connection? = null

        try {
            val query = getInsertIntoIndexSql(entityName, columns)

            conn = dataSource.connection
            insert = conn.prepareStatement(query)
            LOG.debug("batch insert query: {}", query)
            for (entity in entities) {
                var i = 1
                LOG.debug("for id: " + entity["id"])
                insert.setObject(i++, (entity["id"] as UUID), Types.OTHER)

                for ((columnName, columnType) in columns) {
                    LOG.warn("XX: i: {} {} => {}", i, columnName, columnType)
                    when (columnType) {
                        BOOL -> insert.setBoolean(i++, entity[columnName] as Boolean)
                        TEXT -> insert.setString(i++, entity[columnName] as String)
                        TIMESTAMP_WITHOUT_TZ -> insert.setTimestamp(i++, Timestamp.valueOf(entity[columnName] as LocalDateTime))
                        BINARY -> insert.setBinaryStream(i++, (entity[columnName] as ByteArray).inputStream())
                    }
                }
                insert.addBatch()
            }

            insert.executeBatch()

        } catch (e: SQLException) {
            throw IllegalStateException(e)
        } finally {
            insert?.close()
            conn?.close()
        }
    }


}