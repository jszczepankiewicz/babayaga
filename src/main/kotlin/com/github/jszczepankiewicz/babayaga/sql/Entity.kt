package com.github.jszczepankiewicz.babayaga.sql

import java.time.LocalDateTime
import java.util.*

/**
 * Low level container representing entity.
 * added_id SERIAL NOT NULL PRIMARY KEY,
 * id UUID NOT NULL UNIQUE,
 * updated TIMESTAMP WITHOUT TIME ZONE NOT NULL,
 * body BYTEA
 *
 * @since 2016-09-06
 * @author jszczepankiewicz
 */
data class Entity(val refKey: Long?, val id: UUID, val body: ByteArray, val updated: LocalDateTime)