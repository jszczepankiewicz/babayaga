package com.github.jszczepankiewicz.babayaga.sql

/**
 * Container for meta information about jdbc column
 *
 * @since 2016-09-16
 * @author jszczepankiewicz
 */
data class JdbcColumn (val name:String, val type:String, val isNullable:Boolean, val ordinalPosition:Int)