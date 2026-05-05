package edu.cit.tiongzon.lostandfound.feature.auth;

import edu.cit.tiongzon.lostandfound.feature.users.User;
import edu.cit.tiongzon.lostandfound.feature.users.UserRepository;
import edu.cit.tiongzon.lostandfound.shared.utils.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private UserRepository userRepository;

    @Autowired
    private JwtUtils jwtService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public AuthController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody User user) {
        var userFound = userRepository.findByUsername(user.getUsername());
        if (userFound.isPresent()) {
            if (userFound.get().isBanned()) {
                return ResponseEntity.status(403).body(Map.of("message", "Your account has been banned."));
            }
            if (passwordEncoder.matches(user.getPassword(), userFound.get().getPassword())) {
                String token = jwtService.generateToken(userFound.get().getUsername());
                return ResponseEntity.ok(Map.of("token", token));
            } else {
                return ResponseEntity.badRequest().body(Map.of("message", "Invalid credentials!"));
            }
        }

        return ResponseEntity.badRequest().body(Map.of("message", "Username not found!"));
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody User user) {
        if (userRepository.existsByUsername(user.getUsername())) {
            return ResponseEntity.badRequest().body(Map.of("message", "Username already exists!"));
        }

        if (userRepository.existsByEmail(user.getEmail())) {
            return ResponseEntity.badRequest().body(Map.of("message", "Email already exists!"));
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepository.save(user);

        return ResponseEntity.ok(Map.of("message", "Registration success!"));
    }
}
