package org.springframework.samples.petclinic.rest.advice

import jakarta.servlet.http.HttpServletRequest
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.http.HttpStatus
import org.springframework.http.ProblemDetail
import org.springframework.http.ResponseEntity
import org.springframework.samples.petclinic.rest.controller.BindingErrorsResponse
import org.springframework.validation.BindingResult
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseBody
import java.net.URI
import java.time.Instant

@ControllerAdvice
class ExceptionControllerAdvice {
    private fun detailBuild(ex: Exception, status: HttpStatus, url: StringBuffer): ProblemDetail {
        val detail = ProblemDetail.forStatus(status)
        detail.type = URI.create(url.toString())
        detail.title = ex.javaClass.simpleName
        detail.detail = ex.localizedMessage
        detail.setProperty("timestamp", Instant.now())
        return detail
    }

    @ExceptionHandler(Exception::class)
    @ResponseBody
    fun handleGeneralException(e: Exception, request: HttpServletRequest): ResponseEntity<ProblemDetail> {
        val status = HttpStatus.INTERNAL_SERVER_ERROR
        val detail = detailBuild(e, status, request.requestURL)
        return ResponseEntity.status(status).body(detail)
    }

    @ExceptionHandler(DataIntegrityViolationException::class)
    @ResponseBody
    fun handleDataIntegrityViolationException(
        ex: DataIntegrityViolationException,
        request: HttpServletRequest
    ): ResponseEntity<ProblemDetail> {
        val status = HttpStatus.NOT_FOUND
        val detail = ProblemDetail.forStatus(status)
        detail.type = URI.create(request.requestURL.toString())
        detail.title = ex.javaClass.simpleName
        detail.detail = "Request could not be processed"
        detail.setProperty("timestamp", Instant.now())
        return ResponseEntity.status(status).body(detail)
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    @ResponseBody
    fun handleMethodArgumentNotValidException(
        ex: MethodArgumentNotValidException,
        request: HttpServletRequest
    ): ResponseEntity<ProblemDetail> {
        val status = HttpStatus.BAD_REQUEST
        val errors = BindingErrorsResponse()
        val bindingResult: BindingResult = ex.bindingResult
        if (bindingResult.hasErrors()) {
            errors.addAllErrors(bindingResult)
            val detail = detailBuild(ex, status, request.requestURL)
            return ResponseEntity.status(status).body(detail)
        }
        return ResponseEntity.status(status).build()
    }
}
