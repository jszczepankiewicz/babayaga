package com.github.jszczepankiewicz.babayaga

import org.msgpack.core.MessageFormat.FLOAT32
import org.msgpack.core.MessageFormat.FLOAT64
import org.msgpack.core.MessagePack.newDefaultBufferPacker
import org.msgpack.core.MessagePack.newDefaultUnpacker
import org.msgpack.value.ValueType.*

/**
 * (De)serialization of dictionary maps from/to binary buffer using messagepack.org library. Please note that using
 * this transport some values (integers, floats) will be downgraded if they are in range of "lower" type i.e.
 * (Integer) of value 1 after serialization -> deserialization will be converted to (Byte) of value 1. This is per
 * design to minimize memory consumption (TO BE IMPLEMENTED)
 *
 * @since 2016-10-05
 * @author jszczepankiewicz
 */
class MessagePackTransporter : Transporter {

    override fun encode(entity: Map<String, Any?>): ByteArray {

        val packer = newDefaultBufferPacker()

        for ((key, value) in entity) {

            packer.packString(key)

            if (value == null) {
                packer.packNil()
                continue
            }

            when (value) {
                is Byte -> packer.packByte(value)
                is Short -> packer.packShort(value)
                is Int -> packer.packInt(value)
                is Long -> packer.packLong(value)
                is Float -> packer.packFloat(value)
                is Double -> packer.packDouble(value)
                is String -> packer.packString(value)
                is Boolean -> packer.packBoolean(value)
                else -> {
                    throw IllegalArgumentException("Type: %s is not supported to transport".format(value.javaClass))
                }
            }
        }

        packer.close()
        return packer.toByteArray()
    }

    override fun decode(bytes: ByteArray): Map<String, Any?> {

        val unpacker = newDefaultUnpacker(bytes)
        val map = mutableMapOf<String, Any?>()
        var key: String? = null

        while (unpacker.hasNext()) {
            val format = unpacker.nextFormat
            val v = unpacker.unpackValue()

            when (v.valueType) {
                STRING -> {
                    if (key == null) {
                        key = v.asStringValue().asString()
                    } else {
                        map.put(key, v.asStringValue().asString())
                        key = null
                    }
                }
                BOOLEAN -> {
                    if (key == null) {
                        throw IllegalStateException("Boolean pair value found but no preceding String key name found")
                    }
                    map.put(key, v.asBooleanValue().boolean)
                    key = null
                }
                INTEGER -> {
                    if (key == null) {
                        throw IllegalStateException("Integer pair value found but no preceding String key name found")
                    }

                    val value = v.asIntegerValue()

                    if (value.isInByteRange) {
                        map.put(key, value.toByte())
                    } else if (value.isInShortRange) {
                        map.put(key, value.toShort())
                    } else if (value.isInIntRange) {
                        map.put(key, value.toInt())
                    } else {
                        map.put(key, value.toLong())
                    }
                    key = null
                }
                FLOAT -> {
                    if (key == null) {
                        throw IllegalStateException("Float/Double pair value found but no preceding String key name found")
                    }

                    if (format == FLOAT32) {
                        map.put(key, v.asFloatValue().toFloat())
                    } else if (format == FLOAT64) {
                        map.put(key, v.asFloatValue().toDouble())
                    }

                    key = null
                }
                NIL -> {
                    if (key == null) {
                        throw IllegalStateException("NIL pair value found but no preceding String key name found")
                    }
                    map.put(key, null)
                    key = null
                }
                else -> {
                    throw IllegalArgumentException("Unexpected type")
                }
            }
        }

        if (key != null) {
            throw IllegalStateException("Inconsistent byte array structure, key '%s' present without value".format(key))
        }

        unpacker.close()

        return map
    }
}
