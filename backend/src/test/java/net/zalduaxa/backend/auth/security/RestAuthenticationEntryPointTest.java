package net.zalduaxa.backend.auth.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.BadCredentialsException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

class RestAuthenticationEntryPointTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final RestAuthenticationEntryPoint entryPoint = new RestAuthenticationEntryPoint(objectMapper);

    @Test
    void commence_returnsUnauthorizedJsonErrorResponse() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        entryPoint.commence(request, response, new BadCredentialsException("Invalid credentials"));

        assertEquals(401, response.getStatus());
        assertEquals("application/json", response.getContentType());

        JsonNode body = objectMapper.readTree(response.getContentAsString(StandardCharsets.UTF_8));

        assertEquals(401, body.get("status").asInt());
        assertEquals("Missing auth token", body.get("message").asText());
        assertTrue(body.hasNonNull("timestamp"));
        assertFalse(body.get("timestamp").asText().isBlank());
        assertTrue(body.get("errors").isArray());
        assertEquals(0, body.get("errors").size());
    }
}