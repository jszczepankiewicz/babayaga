package com.github.jszczepankiewicz.babayaga

import com.github.jszczepankiewicz.babayaga.sql.EntitiesRepository
import com.github.jszczepankiewicz.babayaga.sql.Entity
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
class Storage(val repository: EntitiesRepository, val transporter: Transporter) {

    val id = "id"
    val updated = "updated"
    val excludedFromSerialization = setOf(id, updated)

    fun put(entityName:String, entity: Map<String, Any?>): Map<String, Any?> {
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

            return update(entityName, entity)
        }

        return insert(entityName, entity)
    }

    private fun update(entityName:String, entity: Map<String, Any?>): Map<String, Any?> {
        var tuple = Entity(refKey = null, id = entity["id"] as UUID, body = transporter.encode(entity, excludedFromSerialization), updated = now())
        repository.updateEntity(entityName, tuple)
        val retval = HashMap(entity)
        retval.put("updated", tuple.updated)
        return retval
    }

    private fun insert(entityName:String, entity: Map<String, Any?>): Map<String, Any?> {
        var tuple = Entity(refKey = null, id = randomUUID(), body = transporter.encode(entity, excludedFromSerialization), updated = now())
        repository.insertEntity(entityName, tuple)
        val retval = HashMap(entity)
        retval.put("id", tuple.id)
        retval.put("updated", tuple.updated)
        return retval
    }

    fun find(entityName:String, id: UUID): Map<String, Any?>? {
        val tuple = repository.getById(entityName, id) ?: return null

        val retval = HashMap(transporter.decode(tuple.body))
        retval.put("id", tuple.id)
        retval.put("updated", tuple.updated)
        return retval
    }
}
