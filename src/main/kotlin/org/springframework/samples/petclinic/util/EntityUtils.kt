package org.springframework.samples.petclinic.util

import org.springframework.samples.petclinic.model.BaseEntity
import org.springframework.orm.ObjectRetrievalFailureException

object EntityUtils {
    @JvmStatic
    fun <T : BaseEntity> getById(entities: Collection<T>, entityClass: Class<T>, entityId: Int): T {
        for (entity in entities) {
            if (entity.id == entityId && entityClass.isInstance(entity)) {
                return entity
            }
        }
        throw ObjectRetrievalFailureException(entityClass, entityId)
    }
}
