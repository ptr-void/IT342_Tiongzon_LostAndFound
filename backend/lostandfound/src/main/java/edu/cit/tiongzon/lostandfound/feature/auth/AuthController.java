package edu.cit.tiongzon.lostandfound.feature.auth;

import edu.cit.tiongzon.lostandfound.feature.users.User;
import edu.cit.tiongzon.lostandfound.feature.users.UserRepository;
import edu.cit.tiongzon.lostandfound.feature.notifications.EmailService;
import edu.cit.tiongzon.lostandfound.shared.utils.JwtUtils;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private UserRepository userRepository;

    @Autowired
    private JwtUtils jwtService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired(required = false)
    private EmailService emailService;

    @Value("${google.client.id:}")
    private String googleClientId;

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

        try {
            if (emailService != null) emailService.sendWelcomeEmail(user.getEmail(), user.getUsername());
        } catch (Exception ignored) {
        }

        return ResponseEntity.ok(Map.of("message", "Registration success!"));
    }

    public static class GoogleLoginRequest {
        public String idToken;
    }

    @PostMapping("/google")
    public ResponseEntity<?> googleLogin(@RequestBody GoogleLoginRequest request) {
        if (googleClientId == null || googleClientId.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Google OAuth is not configured."));
        }

        try {
            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), new GsonFactory())
                    .setAudience(Collections.singletonList(googleClientId))
                    .build();

            GoogleIdToken idToken = verifier.verify(request.idToken);
            if (idToken == null) {
                return ResponseEntity.badRequest().body(Map.of("message", "Invalid ID token."));
            }

            GoogleIdToken.Payload payload = idToken.getPayload();
            String email = payload.getEmail();
            String pictureUrl = (String) payload.get("picture");
            Optional<User> existingUser = userRepository.findByEmail(email);
            User user;

            if (existingUser.isPresent()) {
                user = existingUser.get();
                if (user.isBanned()) {
                    return ResponseEntity.status(403).body(Map.of("message", "Your account has been banned."));
                }
            } else {
                user = new User();
                user.setEmail(email);
                user.setUsername(email.split("@")[0] + "_" + System.currentTimeMillis());
                user.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));
                user.setAvatarUrl(pictureUrl);
                userRepository.save(user);
                try {
                    if (emailService != null) emailService.sendWelcomeEmail(user.getEmail(), user.getUsername());
                } catch (Exception ignored) {
                }
            }

            return ResponseEntity.ok(Map.of("token", jwtService.generateToken(user.getUsername())));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("message", "Verification failed", "error", e.getMessage()));
        }
    }
}
