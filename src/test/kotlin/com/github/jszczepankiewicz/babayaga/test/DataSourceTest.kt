package com.github.jszczepankiewicz.babayaga.test

import org.junit.Test


/**
 * @author jszczepankiewicz
 * *
 * @since 2016-10-04
 */
class DataSourceTest{

    @Test
    fun createValidEntity(){
        val dict1 = mapOf(
                "firstName" to "John",
                "age" to 31,
                "married" to true
        )
    }
}