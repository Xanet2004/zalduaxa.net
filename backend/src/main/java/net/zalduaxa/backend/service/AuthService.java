package net.zalduaxa.backend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import net.zalduaxa.backend.model.RequestUser;
import net.zalduaxa.backend.model.User;
import net.zalduaxa.backend.model.UserRepository;
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
}