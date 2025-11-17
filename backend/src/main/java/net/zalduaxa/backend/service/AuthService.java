package net.zalduaxa.backend.service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import net.zalduaxa.backend.model.requestUser.RequestUser;
import net.zalduaxa.backend.model.user.User;
import net.zalduaxa.backend.model.user.UserRepository;
import net.zalduaxa.backend.utils.PasswordAuthentication;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepo;
    private PasswordAuthentication passAuth;

    public AuthService() {
        this.passAuth = new PasswordAuthentication();
    }


    public User register(RequestUser req) throws Exception {

        // Check repeated username/email
        if (userRepo.existsByUsername(req.getUsername())) {
            throw new Exception("Username already exists");
        }

        if (userRepo.existsByEmail(req.getEmail())) {
            throw new Exception("Email already exists");
        }

        // Check repeated password
        if (!req.getPassword().equals(req.getRepeated_password())) {
            throw new Exception("Passwords do not match");
        }

        User user = new User();

        user.setUsername(req.getUsername());
        user.setFullName(req.getFullName());
        user.setEmail(req.getEmail());
        user.setPasswordHash(passAuth.hash(req.getPassword().toCharArray()));

        return userRepo.save(user);
    }

    public User login(RequestUser req) throws Exception {
        Optional<User> optionalUser = userRepo.findByUsername(req.getUsername());
        if (optionalUser.isEmpty()) {
            throw new Exception("Username does not exist");
        }

        User user = optionalUser.get();

        if (!passAuth.authenticate(req.getPassword().toCharArray(), user.getPasswordHash())) {
            throw new Exception("Incorrect password");
        }

        return user;
    }

    public User logout(String token, JwtService jwtService) throws Exception {
        if (token == null || token.isEmpty()) {
            throw new Exception("Token missing");
        }

        // validar token
        String username = jwtService.getUsername(token);

        return userRepo.findByUsername(username)
            .orElseThrow(() -> new Exception("User not found"));
    }
}