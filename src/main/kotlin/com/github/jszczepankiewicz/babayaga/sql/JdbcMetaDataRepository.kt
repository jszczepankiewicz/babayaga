package com.github.jszczepankiewicz.babayaga.sql

import org.springframework.stereotype.Repository
import java.sql.Connection
import java.sql.DatabaseMetaData.typeNoNulls
import java.sql.ResultSet
import javax.sql.DataSource

/**
 * Repository for metadata retrieval. Helpful for low level db tests.
 *
 * @since 2016-09-16
 * @author jszczepankiewicz
 */
@Repository
class JdbcMetaDataRepository(val dataSource: DataSource) {

    fun tableExists(name: String): Boolean {

        var connection: Connection? = null
        var results: ResultSet? = null

        try {
            connection = dataSource.connection
            results = connection.metaData.getTables(null, null, name, null)
            while (results.next()) {
                return true
            }
            return false
        } finally {
            results?.close()
            connection?.close()
        }
    }

    /**
     * Retrieve information about columns or db table sorted asc by ordinalPosition.
     */
    fun getColumns(tableName: String): Array<JdbcColumn> {

        if (!tableExists(tableName)) {
            throw IllegalArgumentException("Can not retrieve information about columns on non-existing table: %s".format(tableName))
        }

        var connection: Connection? = null
        var rs: ResultSet? = null

        try {
            connection = dataSource.connection
            rs = connection.metaData.getColumns(null, null, tableName, null)
            var columns = mutableListOf<JdbcColumn>()

            while (rs.next()) {
                columns.add(JdbcColumn(
                        name = rs.getString("COLUMN_NAME"),
                        ordinalPosition = rs.getInt("ORDINAL_POSITION"),
                        isNullable = rs.getInt("NULLABLE") != typeNoNulls, //  flatten 3state into 2state but enough for our tests
                        type = rs.getString("TYPE_NAME")
                ))
            }

            columns.sortBy { it.ordinalPosition }
            return columns.toTypedArray()
        } finally {
            rs?.close()
            connection?.close()
        }
    }
}