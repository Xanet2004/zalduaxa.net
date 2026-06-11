package net.zalduaxa.backend.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.util.ReflectionTestUtils;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import net.zalduaxa.backend.exception.BadRequestException;
import net.zalduaxa.backend.exception.UnauthorizedException;
import net.zalduaxa.backend.model.session.Session;
import net.zalduaxa.backend.model.session.SessionRepository;

@ExtendWith(MockitoExtension.class)
class SessionServiceTest {

    @Mock
    private SessionRepository sessionRepository;

    @Mock
    private HttpServletRequest request;

    @InjectMocks
    private SessionService sessionService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(sessionService, "cookieName", "token");
    }

    @Test
    void extractToken_fromAuthorizationHeader_returnsBearerToken() {
        when(request.getHeader("Authorization")).thenReturn("Bearer jwt-token");

        String token = sessionService.extractToken(request);

        assertEquals("jwt-token", token);
    }

    @Test
    void extractToken_fromCookie_returnsCookieToken() {
        when(request.getHeader("Authorization")).thenReturn(null);
        when(request.getCookies()).thenReturn(new Cookie[] {
                new Cookie("token", "cookie-token")
        });

        String token = sessionService.extractToken(request);

        assertEquals("cookie-token", token);
    }

    @Test
    void extractToken_headerPresentButNotBearer_checksCookies() {
        when(request.getHeader("Authorization")).thenReturn("Basic abc");
        when(request.getCookies()).thenReturn(new Cookie[] {
                new Cookie("token", "cookie-token")
        });

        String token = sessionService.extractToken(request);

        assertEquals("cookie-token", token);
    }

    @Test
    void extractToken_noHeaderAndNoCookies_returnsNull() {
        when(request.getHeader("Authorization")).thenReturn(null);
        when(request.getCookies()).thenReturn(null);

        String token = sessionService.extractToken(request);

        assertNull(token);
    }

    @Test
    void extractToken_cookieArrayWithoutAuthCookie_returnsNull() {
        when(request.getHeader("Authorization")).thenReturn(null);
        when(request.getCookies()).thenReturn(new Cookie[] {
                new Cookie("other-cookie", "value")
        });

        String token = sessionService.extractToken(request);

        assertNull(token);
    }

    @Test
    void extractToken_nullRequest_returnsNull() {
        String token = sessionService.extractToken(null);

        assertNull(token);
    }

    @Test
    void createSession_savesAndReturnsSession() {
        Session saved = new Session(1, "jwt-token");
        when(sessionRepository.save(any(Session.class))).thenReturn(saved);

        Session result = sessionService.createSession(1, "jwt-token");

        assertSame(saved, result);

        ArgumentCaptor<Session> captor = ArgumentCaptor.forClass(Session.class);
        verify(sessionRepository).deleteByUserId(1);
        verify(sessionRepository).save(captor.capture());

        assertEquals(1, captor.getValue().getUserId());
        assertEquals("jwt-token", captor.getValue().getToken());
    }

    @Test
    void createSession_existingSession_replacesOldSessionAndCreatesNewOne() {
        Session saved = new Session(1, "new-token");
        when(sessionRepository.save(any(Session.class))).thenReturn(saved);

        Session result = sessionService.createSession(1, "new-token");

        assertSame(saved, result);
        verify(sessionRepository).deleteByUserId(1);
        verify(sessionRepository).save(any(Session.class));
    }

    @Test
    void createSession_saveThrowsDataIntegrityViolation_throwsBadRequestException() {
        when(sessionRepository.save(any(Session.class)))
                .thenThrow(new DataIntegrityViolationException("duplicate"));

        assertThrows(
                BadRequestException.class,
                () -> sessionService.createSession(1, "jwt-token"));

        verify(sessionRepository).deleteByUserId(1);
    }

    @Test
    void createSession_nullUserId_throwsUnauthorizedException() {
        assertThrows(
                UnauthorizedException.class,
                () -> sessionService.createSession(null, "jwt-token"));
    }

    @Test
    void createSession_nullToken_throwsUnauthorizedException() {
        assertThrows(
                UnauthorizedException.class,
                () -> sessionService.createSession(1, null));
    }

    @Test
    void createSession_blankToken_throwsUnauthorizedException() {
        assertThrows(
                UnauthorizedException.class,
                () -> sessionService.createSession(1, " "));
    }

    @Test
    void assertHasActiveSession_found_doesNotThrow() {
        when(sessionRepository.findByUserId(1)).thenReturn(Optional.of(new Session(1, "jwt-token")));

        assertDoesNotThrow(() -> sessionService.assertHasActiveSession(1));
    }

    @Test
    void assertHasActiveSession_notFound_throwsUnauthorizedException() {
        when(sessionRepository.findByUserId(1)).thenReturn(Optional.empty());

        assertThrows(
                UnauthorizedException.class,
                () -> sessionService.assertHasActiveSession(1));
    }

    @Test
    void assertHasActiveSession_nullUserId_throwsUnauthorizedException() {
        assertThrows(
                UnauthorizedException.class,
                () -> sessionService.assertHasActiveSession(null));
    }

    @Test
    void hasActiveSession_found_returnsTrue() {
        when(sessionRepository.findByUserId(1)).thenReturn(Optional.of(new Session(1, "jwt-token")));

        assertTrue(sessionService.hasActiveSession(1));
    }

    @Test
    void hasActiveSession_notFound_returnsFalse() {
        when(sessionRepository.findByUserId(1)).thenReturn(Optional.empty());

        assertFalse(sessionService.hasActiveSession(1));
    }

    @Test
    void hasActiveSession_nullUserId_returnsFalse() {
        assertFalse(sessionService.hasActiveSession(null));
    }

    @Test
    void logoutByToken_found_deletesSession() {
        Session session = new Session(1, "jwt-token");
        when(sessionRepository.findByToken("jwt-token")).thenReturn(Optional.of(session));

        sessionService.logoutByToken("jwt-token");

        verify(sessionRepository).delete(session);
    }

    @Test
    void logoutByToken_notFound_throwsBadRequestException() {
        when(sessionRepository.findByToken("missing-token")).thenReturn(Optional.empty());

        assertThrows(
                BadRequestException.class,
                () -> sessionService.logoutByToken("missing-token"));
    }

    @Test
    void logoutByToken_nullToken_throwsUnauthorizedException() {
        assertThrows(
                UnauthorizedException.class,
                () -> sessionService.logoutByToken(null));
    }

    @Test
    void logoutByToken_blankToken_throwsUnauthorizedException() {
        assertThrows(
                UnauthorizedException.class,
                () -> sessionService.logoutByToken(" "));
    }

    @Test
    void logoutByRequest_extractsTokenAndDeletesSession() {
        Session session = new Session(1, "cookie-token");

        when(request.getHeader("Authorization")).thenReturn(null);
        when(request.getCookies()).thenReturn(new Cookie[] {
                new Cookie("token", "cookie-token")
        });
        when(sessionRepository.findByToken("cookie-token")).thenReturn(Optional.of(session));

        sessionService.logoutByRequest(request);

        verify(sessionRepository).delete(session);
    }

    @Test
    void logoutByRequest_missingToken_throwsUnauthorizedException() {
        when(request.getHeader("Authorization")).thenReturn(null);
        when(request.getCookies()).thenReturn(null);

        assertThrows(
                UnauthorizedException.class,
                () -> sessionService.logoutByRequest(request));
    }
}