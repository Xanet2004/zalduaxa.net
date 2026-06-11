package net.zalduaxa.backend.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import net.zalduaxa.backend.exception.BadRequestException;
import net.zalduaxa.backend.exception.UnauthorizedException;
import net.zalduaxa.backend.dto.request.LoginRequest;
import net.zalduaxa.backend.dto.request.SignupRequest;
import net.zalduaxa.backend.model.role.RoleRepository;
import net.zalduaxa.backend.model.user.User;
import net.zalduaxa.backend.model.user.UserRepository;
import net.zalduaxa.backend.utils.PasswordAuthentication;

@Service
public class AuthService {

    private final UserRepository userRepo;
    private final RoleRepository roleRepo;
    private final JwtService jwtService;
    private final SessionService sessionService;
    private final PasswordAuthentication passAuth;

    public AuthService(
            UserRepository userRepo,
            RoleRepository roleRepo,
            JwtService jwtService,
            SessionService sessionService) {
        this.userRepo = userRepo;
        this.roleRepo = roleRepo;
        this.jwtService = jwtService;
        this.sessionService = sessionService;
        this.passAuth = new PasswordAuthentication();
    }

    @PostConstruct
    private void init() {
        defaultUsers();
    }

    public User register(SignupRequest req) {
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
        if (!req.getPassword().equals(req.getRepeatedPassword())) {
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

    public LoginSession loginAndCreateSession(LoginRequest req) {
        User user = authenticateCredentials(req);

        String token = jwtService.generateToken(user.getUsername());
        sessionService.createSession(user.getId(), token);

        return new LoginSession(user, token);
    }

    private User authenticateCredentials(LoginRequest req) {
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

    public User getUserFromRequest(HttpServletRequest request) {
        String token = sessionService.extractToken(request);
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
        if (!seedEnabled) {
            return;
        }

        if (adminPassword == null || adminPassword.isBlank()) {
            throw new IllegalStateException("Admin seed password must be configured when seed is enabled");
        }

        if (guestPassword == null || guestPassword.isBlank()) {
            throw new IllegalStateException("Guest seed password must be configured when seed is enabled");
        }

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