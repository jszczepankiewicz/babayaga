package com.github.jszczepankiewicz.babayaga.sql

/**
 * This interface represents RDBMS specific functions.
 *
 * @since 2016-11-05
 * @author jszczepankiewicz
 */
interface DBDialect {
    /**
     * Generate column ddl for given name & type pair in given RDBMS.
     */
    fun getColumnDDL(column: Pair<String, ColumnType>, nullable: Boolean): String

    fun buildCreateIndexTableDDL(tableName: String, columns: List<Pair<String, ColumnType>>): String

    /**
     * Maximum size of supported table name. For Oracle usually 30 characters, 63 for Postgresql. Used in contexts
     * that require building label names for tables where generated dynamically has to be compliant with RDBMS limits.
     */
    fun getMaximumTableNameLength():Int
}
