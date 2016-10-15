package com.github.jszczepankiewicz.babayaga

/**
 * Used to (de/en)code attributes on entities. Implementors are free to use different formats (text/binary).
 *
 * @since 2016-10-10
 * @author jszczepankiewicz
 */
interface Transporter {
    fun decode(bytes: ByteArray): Map<String, Any?>
    fun encode(entity: Map<String, Any?>): ByteArray
}