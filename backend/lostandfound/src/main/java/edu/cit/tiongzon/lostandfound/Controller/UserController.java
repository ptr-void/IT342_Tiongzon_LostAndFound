package edu.cit.tiongzon.lostandfound.Controller;

import edu.cit.tiongzon.lostandfound.Entity.User;
import edu.cit.tiongzon.lostandfound.Repository.UserRepository;
import edu.cit.tiongzon.lostandfound.Utils.JwtUtils;
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
}
