package org.springframework.samples.petclinic.rest.controller

import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility
import org.springframework.validation.BindingResult
import org.springframework.validation.FieldError
import tools.jackson.core.JacksonException
import tools.jackson.databind.ObjectMapper
import tools.jackson.databind.json.JsonMapper

class BindingErrorsResponse() {
    constructor(id: Int?) : this(null, id)

    constructor(pathId: Int?, bodyId: Int?) : this() {
        val onlyBodyIdSpecified = pathId == null && bodyId != null
        if (onlyBodyIdSpecified) {
            addBodyIdError(bodyId, "must not be specified")
        }
        val bothIdsSpecified = pathId != null && bodyId != null
        if (bothIdsSpecified && pathId != bodyId) {
            addBodyIdError(bodyId, "does not match pathId: $pathId")
        }
    }

    private fun addBodyIdError(bodyId: Int, message: String) {
        val error = BindingError()
        error.objectName = "body"
        error.fieldName = "id"
        error.fieldValue = bodyId.toString()
        error.errorMessage = message
        addError(error)
    }

    private val bindingErrors: MutableList<BindingError> = arrayListOf()

    fun addError(bindingError: BindingError) {
        bindingErrors.add(bindingError)
    }

    fun addAllErrors(bindingResult: BindingResult) {
        for (fieldError: FieldError in bindingResult.fieldErrors) {
            val error = BindingError()
            error.objectName = fieldError.objectName
            error.fieldName = fieldError.field
            error.fieldValue = fieldError.rejectedValue.toString()
            error.errorMessage = fieldError.defaultMessage
            addError(error)
        }
    }

    fun toJSON(): String {
        val mapper: ObjectMapper =
            JsonMapper.builder()
                .changeDefaultVisibility { v -> v.withFieldVisibility(Visibility.ANY) }
                .build()
        var errorsAsJSON = ""
        try {
            errorsAsJSON = mapper.writeValueAsString(bindingErrors)
        } catch (e: JacksonException) {
            e.printStackTrace()
        }
        return errorsAsJSON
    }

    override fun toString(): String = "BindingErrorsResponse [bindingErrors=$bindingErrors]"

    class BindingError {
        var objectName: String = ""
        var fieldName: String = ""
        var fieldValue: String = ""
        var errorMessage: String? = ""

        override fun toString(): String =
            "BindingError [objectName=$objectName, fieldName=$fieldName, fieldValue=$fieldValue, errorMessage=$errorMessage]"
    }
}
