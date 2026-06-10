package net.zalduaxa.backend.service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import net.zalduaxa.backend.exception.BadRequestException;
import net.zalduaxa.backend.exception.UnauthorizedException;
import net.zalduaxa.backend.model.requestUser.RequestUser;
import net.zalduaxa.backend.model.role.RoleRepository;
import net.zalduaxa.backend.model.session.Session;
import net.zalduaxa.backend.model.session.SessionRepository;
import net.zalduaxa.backend.model.user.User;
import net.zalduaxa.backend.model.user.UserRepository;
import net.zalduaxa.backend.utils.PasswordAuthentication;

@Service
public class AuthService {

    private final UserRepository userRepo;
    private final RoleRepository roleRepo;
    private final SessionRepository sessionRepo;
    private final JwtService jwtService;
    private final PasswordAuthentication passAuth;

    public AuthService(
        UserRepository userRepo,
        RoleRepository roleRepo,
        SessionRepository sessionRepo,
        JwtService jwtService
    ) {
        this.userRepo = userRepo;
        this.roleRepo = roleRepo;
        this.sessionRepo = sessionRepo;
        this.jwtService = jwtService;
        this.passAuth = new PasswordAuthentication();
    }

    @PostConstruct
    private void init() {
        defaultUsers();
    }

    // ------------------------
    // Signup
    // ------------------------
    // TODO: Create require methods for more readibility
    public User register(RequestUser req) {
        if (req.getUsername() == null || req.getUsername().isBlank()) {
            throw new BadRequestException("Username is required");
        }
        if (req.getEmail() == null || req.getEmail().isBlank()) {
            throw new BadRequestException("Email is required");
        }
        if (req.getPassword() == null || req.getPassword().isBlank()) {
            throw new BadRequestException("Password is required");
        }

        if (userRepo.existsByUsername(req.getUsername())) {
            throw new BadRequestException("Username already exists");
        }
        if (userRepo.existsByEmail(req.getEmail())) {
            throw new BadRequestException("Email already exists");
        }
        if (!req.getPassword().equals(req.getRepeated_password())) {
            throw new BadRequestException("Passwords do not match");
        }

        User user = new User();
        user.setUsername(req.getUsername());
        user.setFullName(req.getFullName());
        user.setEmail(req.getEmail());
        user.setPasswordHash(passAuth.hash(req.getPassword().toCharArray()));

        user.setRole(roleRepo.findByName("guest"));

        return userRepo.save(user);
    }

    // ------------------------
    // Login + session creation
    // ------------------------
    // TODO: Create require methods for more readibility
    public User loginAndCreateSession(RequestUser req) {
        User user = authenticateCredentials(req);

        // Enforce “only one session per user”
        if (sessionRepo.findByUserId(user.getId().longValue()).isPresent()) {
            throw new BadRequestException("User already is in session");
        }

        // Create session after issuing JWT (token stored in DB)
        String token = jwtService.generateToken(user.getUsername());

        try {
            sessionRepo.save(new Session(user.getId(), token));
        } catch (DataIntegrityViolationException e) {
            // In case you add a UNIQUE(user_id) constraint and there’s a race condition
            throw new BadRequestException("User already is in session");
        }

        return user;
    }

    // Controller expects to call this separately
    public String issueJwt(User user) {
        return jwtService.generateToken(user.getUsername());
    }

    // TODO: Create require methods for more readibility
    private User authenticateCredentials(RequestUser req) {
        if (req.getUsername() == null || req.getUsername().isBlank()) {
            throw new BadRequestException("Username is required");
        }
        if (req.getPassword() == null || req.getPassword().isBlank()) {
            throw new BadRequestException("Password is required");
        }

        User user = userRepo.findByUsername(req.getUsername())
            .orElseThrow(() -> new UnauthorizedException("Invalid username or password"));

        boolean ok = passAuth.authenticate(req.getPassword().toCharArray(), user.getPasswordHash());
        if (!ok) {
            throw new UnauthorizedException("Invalid username or password");
        }

        return user;
    }

    // ------------------------
    // Session validation
    // ------------------------
    public User getUserFromRequest(HttpServletRequest request) {
        String token = extractToken(request);
        return getUserFromToken(token);
    }

    private User getUserFromToken(String token) {
        if (token == null || token.isBlank()) {
            throw new UnauthorizedException("Missing auth token");
        }

        String username;
        try {
            username = jwtService.getUsername(token);
        } catch (Exception e) {
            throw new UnauthorizedException("Invalid token");
        }

        return userRepo.findByUsername(username)
            .orElseThrow(() -> new UnauthorizedException("User not found"));
    }

    public void assertHasActiveSession(Number userId) {
        if (userId == null) throw new UnauthorizedException("User id missing");

        if (sessionRepo.findByUserId(userId.longValue()).isEmpty()) {
            throw new UnauthorizedException("User is not in session");
        }
    }

    // ------------------------
    // Logout
    // ------------------------
    public void logoutByRequest(HttpServletRequest request) {
        String token = extractToken(request);
        logoutByToken(token);
    }

    // TODO: Create require methods for more readibility
    private void logoutByToken(String token) {
        if (token == null || token.isBlank()) {
            throw new UnauthorizedException("Missing auth token");
        }

        // Best: implement SessionRepository.findByToken(token)
        Optional<Session> session = sessionRepo.findByToken(token);
        if (session.isEmpty()) {
            throw new BadRequestException("User is not in session");
        }

        sessionRepo.delete(session.get());
    }


    // ------------------------
    // Extract token from header or cookie (for logout endpoint)
    // ------------------------
    private String extractToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer "))
            return authHeader.substring(7);

        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("token".equals(cookie.getName()))
                    return cookie.getValue();
            }
        }
        return null;
    }


    // ------------------------
    // Create default users (seed)
    // ------------------------
    @Value("${app.seed.enabled:true}")
    private boolean seedEnabled;

    @Value("${app.seed.admin.username}")
    private String adminUsername;

    @Value("${app.seed.admin.password}")
    private String adminPassword;

    @Value("${app.seed.admin.email}")
    private String adminEmail;

    @Value("${app.seed.guest.username}")
    private String guestUsername;

    @Value("${app.seed.guest.password}")
    private String guestPassword;

    @Value("${app.seed.guest.email}")
    private String guestEmail;

    private void defaultUsers() {
        if (!seedEnabled) return;

        if (userRepo.count() == 0) {
            User admin = new User();
            admin.setUsername(adminUsername);
            admin.setFullName("Admin User");
            admin.setEmail(adminEmail);
            admin.setRole(roleRepo.findByName("admin"));
            admin.setPasswordHash(passAuth.hash(adminPassword.toCharArray()));
            userRepo.save(admin);

            User guest = new User();
            guest.setUsername(guestUsername);
            guest.setFullName("Guest User");
            guest.setEmail(guestEmail);
            guest.setRole(roleRepo.findByName("guest"));
            guest.setPasswordHash(passAuth.hash(guestPassword.toCharArray()));
            userRepo.save(guest);
        }
    }
}