package net.zalduaxa.backend.auth.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.access.AccessDeniedException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

class RestAccessDeniedHandlerTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final RestAccessDeniedHandler handler = new RestAccessDeniedHandler(objectMapper);

    @Test
    void handle_returnsForbiddenJsonErrorResponse() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        handler.handle(request, response, new AccessDeniedException("Forbidden"));

        assertEquals(403, response.getStatus());
        assertEquals("application/json", response.getContentType());

        JsonNode body = objectMapper.readTree(response.getContentAsString(StandardCharsets.UTF_8));

        assertEquals(403, body.get("status").asInt());
        assertEquals("Access denied", body.get("message").asText());
        assertTrue(body.hasNonNull("timestamp"));
        assertFalse(body.get("timestamp").asText().isBlank());
        assertTrue(body.get("errors").isArray());
        assertEquals(0, body.get("errors").size());
    }
}