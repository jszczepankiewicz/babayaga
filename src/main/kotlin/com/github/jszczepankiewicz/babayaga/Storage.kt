package com.github.jszczepankiewicz.babayaga

import com.github.jszczepankiewicz.babayaga.sql.Entity
import com.github.jszczepankiewicz.babayaga.sql.PostgresqlRepository
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.time.LocalDateTime.now
import java.util.*
import java.util.UUID.randomUUID

/**
 * @since 2016-10-17
 * @author jszczepankiewicz
 */
@Service
class Storage(val repository: PostgresqlRepository, val transporter: Transporter) {

    val id = "id"
    val updated = "updated"
    val excludedFromSerialization = setOf(id, updated)

    fun put(entity: Map<String, Any?>): Map<String, Any?> {
        //  basic entity consistency validation
        val containsId = entity.contains(id)
        val containsUpdated = entity.contains(updated)

        if (containsId && (!containsUpdated)) {
            throw IllegalArgumentException("Inconsistent entity - id field present without updated")
        }

        if (containsUpdated && (!containsId)) {
            throw IllegalArgumentException("Inconsistent entity - updated field present without id")
        }

        if (containsUpdated) {

            val entityId = entity[id]
            val entityUpdated = entity[updated]

            if (entityId == null) {
                throw IllegalArgumentException("Inconsistent entity - updated field present with id empty")
            }

            if (entityId !is UUID) {
                throw IllegalArgumentException("Inconsistent entity - id field is not UUID")
            }

            if (entityUpdated == null) {
                throw IllegalArgumentException("Inconsistent entity - id field present with updated empty")
            }

            if (entityUpdated !is LocalDateTime) {
                throw IllegalArgumentException("Inconsistent entity - updated field is not LocalDateTime")
            }

            return update(entity)
        }

        return insert(entity)
    }

    private fun update(entity: Map<String, Any?>): Map<String, Any?> {
        var tuple = Entity(refKey = null, id = entity["id"] as UUID, body = transporter.encode(entity, excludedFromSerialization), updated = now())
        repository.updateEntity(tuple)
        val retval = HashMap(entity)
        retval.put("updated", tuple.updated)
        return retval
    }

    private fun insert(entity: Map<String, Any?>): Map<String, Any?> {
        var tuple = Entity(refKey = null, id = randomUUID(), body = transporter.encode(entity, excludedFromSerialization), updated = now())
        repository.insertEntity(tuple)
        val retval = HashMap(entity)
        retval.put("id", tuple.id)
        retval.put("updated", tuple.updated)
        return retval
    }

    fun find(id: UUID): Map<String, Any?>? {
        val tuple = repository.getById(id) ?: return null

        val retval = HashMap(transporter.decode(tuple.body))
        retval.put("id", tuple.id)
        retval.put("updated", tuple.updated)
        return retval
    }
}
