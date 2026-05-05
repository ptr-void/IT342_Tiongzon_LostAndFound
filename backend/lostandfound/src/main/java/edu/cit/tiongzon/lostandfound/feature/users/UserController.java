package edu.cit.tiongzon.lostandfound.feature.users;

import edu.cit.tiongzon.lostandfound.shared.utils.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/users")
public class UserController {
    private UserRepository userRepository;

    @Autowired
    private JwtUtils jwtService;

    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/me")
    public ResponseEntity<?> getMe(@RequestHeader("Authorization") String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            return ResponseEntity
                    .status(401)
                    .body(Map.of("message", "Missing or invalid Authorization header"));
        }

        String token = authorizationHeader.substring(7);
        String username;

        try {
            username = jwtService.extractUsername(token);
        } catch (Exception e) {
            return ResponseEntity
                    .status(401)
                    .body(Map.of("message", "Invalid token"));
        }

        Optional<User> user = userRepository.findByUsername(username);

        if (user.isEmpty()) {
            return ResponseEntity
                    .status(404)
                    .body(Map.of("message", "User not found"));
        }
        return ResponseEntity.ok(user);
    }

    @PatchMapping("/me/avatar")
    public ResponseEntity<?> updateMyAvatar(
            @RequestHeader("Authorization") String authorizationHeader,
            @RequestBody Map<String, String> body) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(401).body(Map.of("message", "Missing or invalid Authorization header"));
        }
        String token = authorizationHeader.substring(7);
        String username;
        try {
            username = jwtService.extractUsername(token);
        } catch (Exception e) {
            return ResponseEntity.status(401).body(Map.of("message", "Invalid token"));
        }
        String avatarUrl = body.get("avatarUrl");
        if (avatarUrl == null || avatarUrl.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("message", "avatarUrl is required"));
        }
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(404).body(Map.of("message", "User not found"));
        }
        User user = userOpt.get();
        user.setAvatarUrl(avatarUrl);
        userRepository.save(user);
        return ResponseEntity.ok(Map.of("avatarUrl", avatarUrl, "message", "Avatar updated successfully"));
    }
}
