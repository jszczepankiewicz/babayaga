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
    }

    override fun buildCreateIndexTableDDL(tableName: String, columns: List<Pair<String, ColumnType>>): String {
        val sql = StringBuilder().append("CREATE TABLE ").append(tableName).append("(")

        val primaryKeySql = StringBuilder()

        for (column in columns) {
            sql.append(getColumnDDL(column, false)).append(",")
            primaryKeySql.append(column.first).append(",")
        }

        sql.append("id UUID NOT NULL UNIQUE,")
        sql.append("PRIMARY KEY(%sid))".format(primaryKeySql.toString()))
        val retval = sql.toString()
        LOG.debug("Index ddl:\n\t{}", retval)
        return retval
    }

    override fun buildCreateEntityTableDDL(entityName: String): String {
        return ("CREATE TABLE IF NOT EXISTS %s(added_id SERIAL NOT NULL PRIMARY KEY," +
                "id UUID NOT NULL UNIQUE," +
                "updated TIMESTAMP WITHOUT TIME ZONE NOT NULL," +
                "body BYTEA)").format(entityName)
    }
}