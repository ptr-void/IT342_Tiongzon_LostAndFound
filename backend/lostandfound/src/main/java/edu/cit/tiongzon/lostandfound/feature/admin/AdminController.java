package edu.cit.tiongzon.lostandfound.feature.admin;

import edu.cit.tiongzon.lostandfound.feature.claims.Claim;
import edu.cit.tiongzon.lostandfound.feature.claims.ClaimDTO;
import edu.cit.tiongzon.lostandfound.feature.claims.ClaimRepository;
import edu.cit.tiongzon.lostandfound.feature.items.Item;
import edu.cit.tiongzon.lostandfound.feature.items.ItemDTO;
import edu.cit.tiongzon.lostandfound.feature.items.ItemRepository;
import edu.cit.tiongzon.lostandfound.feature.users.Role;
import edu.cit.tiongzon.lostandfound.feature.users.User;
import edu.cit.tiongzon.lostandfound.feature.users.UserDTO;
import edu.cit.tiongzon.lostandfound.feature.users.UserRepository;
import edu.cit.tiongzon.lostandfound.shared.utils.JwtUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/admin")
public class AdminController {
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final ClaimRepository claimRepository;
    private final JwtUtils jwtService;

    public AdminController(UserRepository userRepository, ItemRepository itemRepository, ClaimRepository claimRepository, JwtUtils jwtService) {
        this.userRepository = userRepository;
        this.itemRepository = itemRepository;
        this.claimRepository = claimRepository;
        this.jwtService = jwtService;
    }

    @GetMapping("/users")
    public ResponseEntity<?> getAllUsers(@RequestHeader("Authorization") String authorizationHeader) {
        if (!isAdmin(authorizationHeader)) return forbidden();
        return ResponseEntity.ok(userRepository.findAll().stream().map(this::convertToDto).toList());
    }

    @GetMapping("/items")
    public ResponseEntity<?> getAllItems(@RequestHeader("Authorization") String authorizationHeader) {
        if (!isAdmin(authorizationHeader)) return forbidden();
        return ResponseEntity.ok(itemRepository.findAll().stream().map(this::convertItemToDto).toList());
    }

    @GetMapping("/claims")
    public ResponseEntity<?> getAllClaims(@RequestHeader("Authorization") String authorizationHeader) {
        if (!isAdmin(authorizationHeader)) return forbidden();
        return ResponseEntity.ok(claimRepository.findAll().stream().map(this::convertClaimToDto).toList());
    }

    @PostMapping("/users/{userId}/warn")
    public ResponseEntity<?> warnUser(@PathVariable Long userId, @RequestHeader("Authorization") String authorizationHeader) {
        if (!isAdmin(authorizationHeader)) return forbidden();
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) return ResponseEntity.notFound().build();
        User user = userOpt.get();
        if (user.getRole() == Role.ADMIN) return ResponseEntity.badRequest().body(Map.of("message", "Cannot warn another admin"));
        user.setWarningMarks(user.getWarningMarks() + 1);
        userRepository.save(user);
        return ResponseEntity.ok(Map.of("message", "User warned successfully", "warningMarks", user.getWarningMarks()));
    }

    @PostMapping("/users/{userId}/ban")
    public ResponseEntity<?> toggleBanUser(@PathVariable Long userId, @RequestHeader("Authorization") String authorizationHeader) {
        if (!isAdmin(authorizationHeader)) return forbidden();
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) return ResponseEntity.notFound().build();
        User user = userOpt.get();
        if (user.getRole() == Role.ADMIN) return ResponseEntity.badRequest().body(Map.of("message", "Cannot ban another admin"));
        user.setBanned(!user.isBanned());
        userRepository.save(user);
        return ResponseEntity.ok(Map.of("message", "User " + (user.isBanned() ? "banned" : "unbanned") + " successfully", "isBanned", user.isBanned()));
    }

    @DeleteMapping("/items/{itemId}")
    public ResponseEntity<?> deleteItem(@PathVariable Long itemId, @RequestHeader("Authorization") String authorizationHeader) {
        if (!isAdmin(authorizationHeader)) return forbidden();
        if (!itemRepository.existsById(itemId)) return ResponseEntity.notFound().build();
        itemRepository.deleteById(itemId);
        return ResponseEntity.ok(Map.of("message", "Post deleted successfully"));
    }

    private boolean isAdmin(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) return false;
        try {
            String username = jwtService.extractUsername(authorizationHeader.substring(7));
            return userRepository.findByUsername(username).map(user -> user.getRole() == Role.ADMIN).orElse(false);
        } catch (Exception e) {
            return false;
        }
    }

    private ResponseEntity<Map<String, String>> forbidden() {
        return ResponseEntity.status(403).body(Map.of("message", "Forbidden - Admin access required"));
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

    private ItemDTO convertItemToDto(Item item) {
        ItemDTO dto = new ItemDTO();
        dto.setId(item.getId());
        dto.setTitle(item.getTitle());
        dto.setDescription(item.getDescription());
        dto.setStatus(item.getStatus());
        dto.setCategory(item.getCategory());
        dto.setLocationLat(item.getLocationLat());
        dto.setLocationLng(item.getLocationLng());
        dto.setLocationDescription(item.getLocationDescription());
        dto.setImagePath(item.getImagePath());
        if (item.getReporter() != null) {
            dto.setReporterId(item.getReporter().getUserId());
            dto.setReporterName(item.getReporter().getUsername());
            dto.setReporterEmail(item.getReporter().getEmail());
            dto.setReporterWarningMarks(item.getReporter().getWarningMarks());
            dto.setReporterAvatar(item.getReporter().getAvatarUrl());
        }
        dto.setCreatedAt(item.getCreatedAt());
        dto.setLastUpdate(item.getLastUpdate());
        return dto;
    }

    private ClaimDTO convertClaimToDto(Claim claim) {
        ClaimDTO dto = new ClaimDTO();
        dto.setId(claim.getId());
        dto.setItemId(claim.getItem().getId());
        dto.setItemTitle(claim.getItem().getTitle());
        dto.setClaimantId(claim.getClaimant().getUserId());
        dto.setClaimantName(claim.getClaimant().getUsername());
        dto.setProofDescription(claim.getProofDescription());
        dto.setProofImagePath(claim.getProofImagePath());
        dto.setStatus(claim.getStatus());
        dto.setPaymentStatus(claim.getPaymentStatus());
        dto.setPaymentIntentId(claim.getPaymentIntentId());
        dto.setCreatedAt(claim.getCreatedAt());
        dto.setLastUpdate(claim.getLastUpdate());
        return dto;
    }
}
