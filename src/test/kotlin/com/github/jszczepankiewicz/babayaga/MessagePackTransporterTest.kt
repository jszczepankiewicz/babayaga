package com.github.jszczepankiewicz.babayaga

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

/**
 * @author jszczepankiewicz
 * *
 * @since 2016-10-05
 */
class MessagePackTransporterTest {

    private val transporter = MessagePackTransporter()
    private val entity = mapOf("byteMin" to Byte.MIN_VALUE, "byteMax" to Byte.MAX_VALUE, "shortMin" to Short.MIN_VALUE,
            "shortMax" to Short.MAX_VALUE, "integerMin" to Integer.MIN_VALUE, "integerMax" to Integer.MAX_VALUE,
            "longMin" to Long.MIN_VALUE, "longMax" to Long.MAX_VALUE, "floatMin" to Float.MIN_VALUE,
            "floatMax" to Float.MAX_VALUE, "doubleMin" to Double.MIN_VALUE, "doubleMax" to Double.MAX_VALUE,
            "boolTrue" to true, "boolFalse" to false, "someString" to "someStringValue", "nulledField" to null)

    private val entityEncoded = byteArrayOf(-89, 98, 121, 116, 101, 77, 105, 110, -48, -128, -89, 98, 121, 116, 101, 77,
            97, 120, 127, -88, 115, 104, 111, 114, 116, 77, 105, 110, -47, -128, 0, -88, 115, 104, 111, 114, 116, 77, 97,
            120, -51, 127, -1, -86, 105, 110, 116, 101, 103, 101, 114, 77, 105, 110, -46, -128, 0, 0, 0, -86, 105, 110,
            116, 101, 103, 101, 114, 77, 97, 120, -50, 127, -1, -1, -1, -89, 108, 111, 110, 103, 77, 105, 110, -45, -128,
            0, 0, 0, 0, 0, 0, 0, -89, 108, 111, 110, 103, 77, 97, 120, -49, 127, -1, -1, -1, -1, -1, -1, -1, -88, 102,
            108, 111, 97, 116, 77, 105, 110, -54, 0, 0, 0, 1, -88, 102, 108, 111, 97, 116, 77, 97, 120, -54, 127, 127,
            -1, -1, -87, 100, 111, 117, 98, 108, 101, 77, 105, 110, -53, 0, 0, 0, 0, 0, 0, 0, 1, -87, 100, 111, 117, 98,
            108, 101, 77, 97, 120, -53, 127, -17, -1, -1, -1, -1, -1, -1, -88, 98, 111, 111, 108, 84, 114, 117, 101,
            -61, -87, 98, 111, 111, 108, 70, 97, 108, 115, 101, -62, -86, 115, 111, 109, 101, 83, 116, 114, 105, 110,
            103, -81, 115, 111, 109, 101, 83, 116, 114, 105, 110, 103, 86, 97, 108, 117, 101, -85, 110, 117, 108, 108,
            101, 100, 70, 105, 101, 108, 100, -64)

    @Test
    fun excludeSpecialFieldsFromEncoding() {

        //  given
        val excluded = setOf("shortMin", "doubleMax")

        //  when
        val encoded = transporter.encode(entity, excluded)
        val deserialized = transporter.decode(encoded)

        //  then
        assertThat(encoded).isNotEqualTo(entityEncoded)
        assertThat(deserialized).doesNotContainKeys("shortMin", "doubleMax").hasSize(14)
        assertThat(deserialized["boolFalse"]).isEqualTo(false)
        assertThat(deserialized["boolTrue"]).isEqualTo(true)
        assertThat(deserialized["nulledField"]).isNull()
        assertThat(deserialized).containsKey("nulledField")
        assertThat(deserialized["someString"]).isEqualTo("someStringValue")

        //  potentially lossy assertions
        assertThat(deserialized["byteMin"]).isEqualTo(Byte.MIN_VALUE)
        assertThat(deserialized["byteMax"]).isEqualTo(Byte.MAX_VALUE)

        assertThat(deserialized["shortMax"]).isEqualTo(Short.MAX_VALUE)

        assertThat(deserialized["integerMin"]).isEqualTo(Integer.MIN_VALUE)
        assertThat(deserialized["integerMax"]).isEqualTo(Integer.MAX_VALUE)

        assertThat(deserialized["longMin"]).isEqualTo(Long.MIN_VALUE)
        assertThat(deserialized["longMax"]).isEqualTo(Long.MAX_VALUE)

        assertThat(deserialized["floatMin"]).isEqualTo(Float.MIN_VALUE)
        assertThat(deserialized["floatMax"]).isEqualTo(Float.MAX_VALUE)

        assertThat(deserialized["doubleMin"]).isEqualTo(Double.MIN_VALUE)


    }


    @Test
    fun encodeEmptyMap() {

        //  given
        val from = emptyMap<String, Any?>()

        //  when
        val encoded = transporter.encode(from, emptySet())

        //  then
        assertThat(encoded).isEmpty()
    }

    @Test
    fun decodeEmptyMap() {

        //  given
        val from = ByteArray(0)

        //  when
        val decoded = transporter.decode(from)

        //  assertThat
        assertThat(decoded).isEmpty()
    }

    @Test
    fun decodeNonEmptyMap() {

        //  given
        val from = entityEncoded

        //  when
        val decoded = transporter.decode(from)

        //  then

        //  simple non-lossy assertions
        assertThat(decoded).hasSize(16)
        assertThat(decoded["boolFalse"]).isEqualTo(false)
        assertThat(decoded["boolTrue"]).isEqualTo(true)
        assertThat(decoded["nulledField"]).isNull()
        assertThat(decoded).containsKey("nulledField")
        assertThat(decoded["someString"]).isEqualTo("someStringValue")

        //  potentially lossy assertions
        assertThat(decoded["byteMin"]).isEqualTo(Byte.MIN_VALUE)
        assertThat(decoded["byteMax"]).isEqualTo(Byte.MAX_VALUE)

        assertThat(decoded["shortMin"]).isEqualTo(Short.MIN_VALUE)
        assertThat(decoded["shortMax"]).isEqualTo(Short.MAX_VALUE)

        assertThat(decoded["integerMin"]).isEqualTo(Integer.MIN_VALUE)
        assertThat(decoded["integerMax"]).isEqualTo(Integer.MAX_VALUE)

        assertThat(decoded["longMin"]).isEqualTo(Long.MIN_VALUE)
        assertThat(decoded["longMax"]).isEqualTo(Long.MAX_VALUE)

        assertThat(decoded["floatMin"]).isEqualTo(Float.MIN_VALUE)
        assertThat(decoded["floatMax"]).isEqualTo(Float.MAX_VALUE)

        assertThat(decoded["doubleMin"]).isEqualTo(Double.MIN_VALUE)
        assertThat(decoded["doubleMax"]).isEqualTo(Double.MAX_VALUE)
    }


    @Test
    fun encodeNonEmptyMap() {

        //  given
        val entity = entity

        //  when
        val compressed = transporter.encode(entity, emptySet())

        //  then
        assertThat(compressed).isNotNull().isEqualTo(entityEncoded)
    }

}