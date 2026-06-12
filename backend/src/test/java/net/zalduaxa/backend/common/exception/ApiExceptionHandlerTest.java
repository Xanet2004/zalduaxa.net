package net.zalduaxa.backend.common.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;
import net.zalduaxa.backend.common.exception.ApiExceptionHandler.ErrorResponse;

class ApiExceptionHandlerTest {

    private final ApiExceptionHandler handler = new ApiExceptionHandler();

    @Test
    void handleBadRequest_returns400() {
        ResponseEntity<ErrorResponse> response = handler.handleBadRequest(new BadRequestException("Bad request"));

        assertErrorResponse(response, HttpStatus.BAD_REQUEST, 400, "Bad request", 0);
    }

    @Test
    void handleUnauthorized_returns401() {
        ResponseEntity<ErrorResponse> response = handler.handleUnauthorized(new UnauthorizedException("Unauthorized"));

        assertErrorResponse(response, HttpStatus.UNAUTHORIZED, 401, "Unauthorized", 0);
    }

    @Test
    void handleForbidden_returns403() {
        ResponseEntity<ErrorResponse> response = handler.handleForbidden(new ForbiddenException("Forbidden"));

        assertErrorResponse(response, HttpStatus.FORBIDDEN, 403, "Forbidden", 0);
    }

    @Test
    void handleNotFound_returns404() {
        ResponseEntity<ErrorResponse> response = handler.handleNotFound(new NotFoundException("Not found"));

        assertErrorResponse(response, HttpStatus.NOT_FOUND, 404, "Not found", 0);
    }

    @Test
    void handleMethodArgumentNotValid_returnsValidationErrors() throws Exception {
        Method method = TestController.class.getDeclaredMethod("create", TestRequest.class);
        MethodParameter parameter = new MethodParameter(method, 0);

        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new TestRequest(), "request");
        bindingResult.addError(new FieldError("request", "name", "Name is required"));

        MethodArgumentNotValidException exception = new MethodArgumentNotValidException(parameter, bindingResult);

        ResponseEntity<ErrorResponse> response = handler.handleMethodArgumentNotValid(exception);

        assertErrorResponse(response, HttpStatus.BAD_REQUEST, 400, "Validation failed", 1);
        assertEquals("name", response.getBody().errors().get(0).field());
        assertEquals("Name is required", response.getBody().errors().get(0).message());
    }

    @Test
    void handleBindException_returnsValidationErrors() {
        BindException exception = new BindException(new TestRequest(), "request");
        exception.addError(new FieldError("request", "slug", "Slug is required"));

        ResponseEntity<ErrorResponse> response = handler.handleBindException(exception);

        assertErrorResponse(response, HttpStatus.BAD_REQUEST, 400, "Validation failed", 1);
        assertEquals("slug", response.getBody().errors().get(0).field());
        assertEquals("Slug is required", response.getBody().errors().get(0).message());
    }

    @Test
    void handleConstraintViolation_returnsValidationErrors() {
        ConstraintViolation<?> violation = mock(ConstraintViolation.class);
        Path path = mock(Path.class);

        when(path.toString()).thenReturn("name");
        when(violation.getPropertyPath()).thenReturn(path);
        when(violation.getMessage()).thenReturn("must not be blank");

        ConstraintViolationException exception = new ConstraintViolationException(Set.of(violation));

        ResponseEntity<ErrorResponse> response = handler.handleConstraintViolation(exception);

        assertErrorResponse(response, HttpStatus.BAD_REQUEST, 400, "Validation failed", 1);
        assertEquals("name", response.getBody().errors().get(0).field());
        assertEquals("must not be blank", response.getBody().errors().get(0).message());
    }

    @Test
    void handleMissingServletRequestParameter_returnsFieldError() {
        MissingServletRequestParameterException exception =
                new MissingServletRequestParameterException("typeSlug", "String");

        ResponseEntity<ErrorResponse> response = handler.handleMissingServletRequestParameter(exception);

        assertErrorResponse(response, HttpStatus.BAD_REQUEST, 400, "Missing required request parameter", 1);
        assertEquals("typeSlug", response.getBody().errors().get(0).field());
        assertEquals("Parameter is required", response.getBody().errors().get(0).message());
    }

    @Test
    void handleHttpMessageNotReadable_returnsMalformedJson() {
        HttpMessageNotReadableException exception = new HttpMessageNotReadableException("Malformed JSON");

        ResponseEntity<ErrorResponse> response = handler.handleHttpMessageNotReadable(exception);

        assertErrorResponse(response, HttpStatus.BAD_REQUEST, 400, "Malformed JSON request", 0);
    }

    @Test
    void handleHttpMediaTypeNotSupported_returnsUnsupportedMediaType() {
        HttpMediaTypeNotSupportedException exception =
                new HttpMediaTypeNotSupportedException(MediaType.TEXT_PLAIN, List.of(MediaType.APPLICATION_JSON));

        ResponseEntity<ErrorResponse> response = handler.handleHttpMediaTypeNotSupported(exception);

        assertErrorResponse(response, HttpStatus.UNSUPPORTED_MEDIA_TYPE, 415, "Unsupported media type", 0);
    }

    @Test
    void handleAccessDenied_returnsForbidden() {
        ResponseEntity<ErrorResponse> response = handler.handleAccessDenied(new AccessDeniedException("Denied"));

        assertErrorResponse(response, HttpStatus.FORBIDDEN, 403, "Access denied", 0);
    }

    @Test
    void handleGeneric_returnsInternalServerErrorWithoutLeakingMessage() {
        ResponseEntity<ErrorResponse> response = handler.handleGeneric(new RuntimeException("Database exploded"));

        assertErrorResponse(response, HttpStatus.INTERNAL_SERVER_ERROR, 500, "Unexpected server error", 0);
    }

    private void assertErrorResponse(
            ResponseEntity<ErrorResponse> response,
            HttpStatus expectedHttpStatus,
            int expectedBodyStatus,
            String expectedMessage,
            int expectedErrorsSize) {

        assertEquals(expectedHttpStatus, response.getStatusCode());
        assertEquals(expectedBodyStatus, response.getBody().status());
        assertEquals(expectedMessage, response.getBody().message());
        assertFalse(response.getBody().timestamp().isBlank());
        assertEquals(expectedErrorsSize, response.getBody().errors().size());
    }

    private static class TestRequest {
        private String name;

        @SuppressWarnings("unused")
        public String getName() {
            return name;
        }

        @SuppressWarnings("unused")
        public void setName(String name) {
            this.name = name;
        }
    }

    private static class TestController {
        @SuppressWarnings("unused")
        void create(TestRequest request) {
            throw new UnsupportedOperationException("Test-only method used only to build MethodParameter metadata");
        }
    }
}