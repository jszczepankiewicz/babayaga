package com.github.jszczepankiewicz.babayaga.sql.test

import java.util.*
import kotlin.text.Charsets.UTF_8

/**
 * @since 2016-10-02
 * @author jszczepankiewicz
 */
/**
 * Generate UUID based id that can be used as entity id.
 */
fun randomEntityId():String{
    return UUID.randomUUID().toString()
}

/**
 * Return some small ByteArray
 */
fun testBody():ByteArray{
    return "someting:isSomething".toByteArray(UTF_8)
}
