package com.github.jszczepankiewicz.babayaga.sql

import com.github.jszczepankiewicz.babayaga.sql.ColumnType.*
import org.apache.logging.log4j.LogManager.getLogger
import org.springframework.stereotype.Component

/**
 * @since 2016-11-05
 * @author jszczepankiewicz
 */
@Component
class PostgresqlDBDialect : DBDialect {

    private val LOG = getLogger()

    override fun getMaximumTableNameLength(): Int {
        return 63
    }

    override fun getColumnDDL(column: Pair<String, ColumnType>, nullable: Boolean): String {

        val columnName = column.first.toLowerCase()
        val columnType = column.second
        val notNull = if (!nullable) " NOT NULL" else ""

        when (columnType) {
            BOOL -> return columnName + " BOOLEAN" + notNull
            TEXT -> return columnName + " TEXT" + notNull
            TIMESTAMP_WITHOUT_TZ -> return columnName + " TIMESTAMP WITHOUT TIME ZONE" + notNull
            BINARY -> return columnName + " BYTEA" + notNull
        }

        throw IllegalArgumentException("Unsupported ColumnType: " + column.second)
    }

    override fun buildCreateIndexTableDDL(tableName: String, columns: List<Pair<String, ColumnType>>): String {
        val sql = StringBuilder().append("CREATE TABLE ").append(tableName).append("(")

        val primaryKeySql = StringBuilder()

        for (column in columns) {
            sql.append(getColumnDDL(column, false)).append(",")
            primaryKeySql.append(column.first).append(",")
        }

        sql.append("id BYTEA NOT NULL UNIQUE,")
        sql.append("PRIMARY KEY(%sid))".format(primaryKeySql.toString()))
        val retval = sql.toString()
        LOG.debug("Index ddl:\n\t{}", retval)
        return retval
    }
}