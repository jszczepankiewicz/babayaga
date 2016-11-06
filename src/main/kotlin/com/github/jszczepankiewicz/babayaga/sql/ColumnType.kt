package com.github.jszczepankiewicz.babayaga.sql

/**
 * Represents attribute type in a way not tied up to particular RDBMS.
 *
 * @since 2016-11-01
 * @author jszczepankiewicz
 */
enum class ColumnType {
    TEXT, TIMESTAMP_WITHOUT_TZ, BOOL, BINARY
}