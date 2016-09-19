package com.github.jszczepankiewicz.babayaga

import org.apache.logging.log4j.Logger

/**
 * @since 2016-09-09
 * @author jszczepankiewicz
 */
public fun <T: Any> T.logger(): Logger {
    //return org.apache.logging.log4j.LogManager.getLogger(unwrapCompanionClass(this.javaClass).name)
}
