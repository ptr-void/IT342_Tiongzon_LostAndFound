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

    @GetMapping
    public ResponseEntity<?> getAllUsers(@RequestHeader("Authorization") String authorizationHeader) {
        ResponseEntity<?> invalid = validateToken(authorizationHeader);
        if (invalid != null) return invalid;
        return ResponseEntity.ok(userRepository.findAll().stream().map(this::convertToDto).toList());
    }

    @GetMapping("/{userId}")
    public ResponseEntity<?> getUserById(@PathVariable Long userId, @RequestHeader("Authorization") String authorizationHeader) {
        ResponseEntity<?> invalid = validateToken(authorizationHeader);
        if (invalid != null) return invalid;
        return userRepository.findByUserId(userId)
                .<ResponseEntity<?>>map(user -> ResponseEntity.ok(convertToDto(user)))
                .orElseGet(() -> ResponseEntity.notFound().build());
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
        return ResponseEntity.ok(convertToDto(user.get()));
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

    private ResponseEntity<?> validateToken(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(401).body(Map.of("message", "Missing or invalid Authorization header"));
        }
        try {
            jwtService.extractUsername(authorizationHeader.substring(7));
            return null;
        } catch (Exception e) {
            return ResponseEntity.status(401).body(Map.of("message", "Invalid token"));
        }
    }

    private UserDTO convertToDto(User user) {
        UserDTO dto = new UserDTO();
        dto.setUserId(user.getUserId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setAvatarUrl(user.getAvatarUrl());
        dto.setRole(user.getRole());
        dto.setActive(user.isActive());
        dto.setWarningMarks(user.getWarningMarks());
        dto.setBanned(user.isBanned());
        dto.setCreatedAt(user.getCreatedAt());
        return dto;
    }
}
