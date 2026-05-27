package edu.cit.tiongzon.lostandfound.feature.claims;

import edu.cit.tiongzon.lostandfound.feature.items.Item;
import edu.cit.tiongzon.lostandfound.feature.items.ItemRepository;
import edu.cit.tiongzon.lostandfound.feature.users.Role;
import edu.cit.tiongzon.lostandfound.feature.users.User;
import edu.cit.tiongzon.lostandfound.feature.users.UserRepository;
import edu.cit.tiongzon.lostandfound.feature.notifications.EmailService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/claims")
public class ClaimController {
    private final ClaimRepository claimRepository;
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;

    public ClaimController(ClaimRepository claimRepository, ItemRepository itemRepository,
            UserRepository userRepository, EmailService emailService) {
        this.claimRepository = claimRepository;
        this.itemRepository = itemRepository;
        this.userRepository = userRepository;
        this.emailService = emailService;
    }

    @GetMapping
    public ResponseEntity<List<ClaimDTO>> getAllClaims() {
        return ResponseEntity.ok(claimRepository.findAll().stream().map(this::convertToDto).toList());
    }

    @GetMapping("/mine")
    public ResponseEntity<List<ClaimDTO>> getMyClaims(Authentication authentication) {
        User currentUser = currentUser(authentication);
        if (currentUser == null) return ResponseEntity.ok(List.of());
        return ResponseEntity.ok(claimRepository.findByClaimant_UserId(currentUser.getUserId()).stream().map(this::convertToDto).toList());
    }

    @GetMapping("/received")
    public ResponseEntity<List<ClaimDTO>> getClaimsForMyItems(Authentication authentication) {
        User currentUser = currentUser(authentication);
        if (currentUser == null) return ResponseEntity.ok(List.of());
        return ResponseEntity.ok(claimRepository.findByItem_Reporter_UserId(currentUser.getUserId()).stream().map(this::convertToDto).toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getClaimById(@PathVariable Long id) {
        return claimRepository.findById(id).<ResponseEntity<?>>map(claim -> ResponseEntity.ok(convertToDto(claim)))
                .orElseGet(() -> ResponseEntity.status(404).body(Map.of("message", "Claim not found")));
    }

    @PostMapping
    public ResponseEntity<?> createClaim(@RequestBody ClaimDTO dto) {
        Optional<Item> item = itemRepository.findById(dto.getItemId());
        Optional<User> claimant = userRepository.findByUserId(dto.getClaimantId());
        if (item.isEmpty()) return ResponseEntity.badRequest().body(Map.of("message", "Invalid item ID"));
        if (claimant.isEmpty()) return ResponseEntity.badRequest().body(Map.of("message", "Invalid claimant ID"));
        if (item.get().getReporter() != null && item.get().getReporter().getUserId().equals(claimant.get().getUserId())) {
            return ResponseEntity.badRequest().body(Map.of("message", "You cannot claim your own item"));
        }

        Claim claim = new Claim();
        claim.setItem(item.get());
        claim.setClaimant(claimant.get());
        claim.setProofDescription(dto.getProofDescription());
        claim.setProofImagePath(dto.getProofImagePath());
        claim.setStatus(Claim.ClaimStatus.PENDING);
        claim.setPaymentStatus(dto.getPaymentStatus() != null ? dto.getPaymentStatus() : Claim.PaymentStatus.NOT_APPLICABLE);
        claim.setPaymentIntentId(dto.getPaymentIntentId());
        Claim saved = claimRepository.save(claim);

        try {
            emailService.sendClaimNotification(item.get().getReporter().getEmail(), item.get().getTitle(), claimant.get().getUsername());
        } catch (Exception ignored) {
        }

        return ResponseEntity.ok(convertToDto(saved));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateClaim(@PathVariable Long id, @RequestBody ClaimDTO dto) {
        Optional<Claim> claimOpt = claimRepository.findById(id);
        if (claimOpt.isEmpty()) return ResponseEntity.status(404).body(Map.of("message", "Claim not found"));
        Claim claim = claimOpt.get();
        if (dto.getProofDescription() != null) claim.setProofDescription(dto.getProofDescription());
        if (dto.getProofImagePath() != null) claim.setProofImagePath(dto.getProofImagePath());
        if (dto.getStatus() != null) claim.setStatus(dto.getStatus());
        if (dto.getPaymentStatus() != null) claim.setPaymentStatus(dto.getPaymentStatus());
        if (dto.getPaymentIntentId() != null) claim.setPaymentIntentId(dto.getPaymentIntentId());
        return ResponseEntity.ok(convertToDto(claimRepository.save(claim)));
    }

    @PostMapping("/{id}/approve")
    public ResponseEntity<?> approveClaim(@PathVariable Long id, Authentication authentication) {
        return updateClaimStatus(id, Claim.ClaimStatus.APPROVED, authentication);
    }

    @PostMapping("/{id}/reject")
    public ResponseEntity<?> rejectClaim(@PathVariable Long id, Authentication authentication) {
        return updateClaimStatus(id, Claim.ClaimStatus.REJECTED, authentication);
    }

    @PostMapping("/{id}/mark-paid")
    public ResponseEntity<?> markClaimPaid(@PathVariable Long id, @RequestBody ClaimDTO dto, Authentication authentication) {
        Optional<Claim> claimOpt = claimRepository.findById(id);
        if (claimOpt.isEmpty()) return ResponseEntity.status(404).body(Map.of("message", "Claim not found"));
        User currentUser = currentUser(authentication);
        if (currentUser == null) return ResponseEntity.status(401).body(Map.of("message", "Authentication required"));

        Claim claim = claimOpt.get();
        boolean isClaimant = claim.getClaimant().getUserId().equals(currentUser.getUserId());
        User reporter = claim.getItem().getReporter();
        boolean isReporter = reporter != null && reporter.getUserId().equals(currentUser.getUserId());
        boolean isAdmin = currentUser.getRole() == Role.ADMIN;
        if (!isClaimant && !isReporter && !isAdmin) return ResponseEntity.status(403).body(Map.of("message", "Only the claimant, item reporter, or admin can mark this claim paid"));
        if (claim.getStatus() != Claim.ClaimStatus.APPROVED) return ResponseEntity.badRequest().body(Map.of("message", "Only approved claims can be paid"));

        claim.setPaymentStatus(Claim.PaymentStatus.PAID);
        claim.setPaymentIntentId(dto.getPaymentIntentId() != null && !dto.getPaymentIntentId().isBlank() ? dto.getPaymentIntentId() : "QR_PAYMENT");
        Claim saved = claimRepository.save(claim);
        
        try {
            
            emailService.sendPaymentNotification(claim.getClaimant().getEmail(), claim.getItem().getTitle(), saved.getPaymentIntentId());
            
            if (claim.getItem().getReporter() != null) {
                emailService.sendPaymentNotification(claim.getItem().getReporter().getEmail(), claim.getItem().getTitle(), saved.getPaymentIntentId());
            }
        } catch (Exception ignored) {
        }
        
        return ResponseEntity.ok(convertToDto(saved));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteClaim(@PathVariable Long id) {
        if (!claimRepository.existsById(id)) return ResponseEntity.status(404).body(Map.of("message", "Claim not found"));
        claimRepository.deleteById(id);
        return ResponseEntity.ok(Map.of("message", "Claim deleted successfully"));
    }

    private User currentUser(Authentication authentication) {
        if (authentication == null) return null;
        return userRepository.findByUsername(authentication.getName()).orElse(null);
    }

    private ResponseEntity<?> updateClaimStatus(Long id, Claim.ClaimStatus status, Authentication authentication) {
        Optional<Claim> claimOpt = claimRepository.findById(id);
        if (claimOpt.isEmpty()) return ResponseEntity.status(404).body(Map.of("message", "Claim not found"));
        User currentUser = currentUser(authentication);
        if (currentUser == null) return ResponseEntity.status(401).body(Map.of("message", "Authentication required"));

        Claim claim = claimOpt.get();
        User reporter = claim.getItem().getReporter();
        boolean isReporter = reporter != null && reporter.getUserId().equals(currentUser.getUserId());
        boolean isAdmin = currentUser.getRole() == Role.ADMIN;
        if (!isReporter && !isAdmin) return ResponseEntity.status(403).body(Map.of("message", "Only the item reporter or an admin can update this claim"));

        claim.setStatus(status);
        claim.setPaymentStatus(status == Claim.ClaimStatus.APPROVED ? Claim.PaymentStatus.PENDING : Claim.PaymentStatus.NOT_APPLICABLE);
        Claim saved = claimRepository.save(claim);
        if (status == Claim.ClaimStatus.APPROVED) {
            claimRepository.findByItem_Id(claim.getItem().getId()).stream()
                    .filter(other -> !other.getId().equals(claim.getId()))
                    .filter(other -> other.getStatus() == Claim.ClaimStatus.PENDING)
                    .forEach(other -> {
                        other.setStatus(Claim.ClaimStatus.REJECTED);
                        claimRepository.save(other);
                    });
        }
        
        try {
            emailService.sendClaimStatusUpdate(claim.getClaimant().getEmail(), claim.getItem().getTitle(), status == Claim.ClaimStatus.APPROVED);
        } catch (Exception ignored) {
        }
        
        return ResponseEntity.ok(convertToDto(saved));
    }

    private ClaimDTO convertToDto(Claim claim) {
        ClaimDTO dto = new ClaimDTO();
        dto.setId(claim.getId());
        dto.setItemId(claim.getItem().getId());
        dto.setItemTitle(claim.getItem().getTitle());
        dto.setItemStatus(claim.getItem().getStatus() != null ? claim.getItem().getStatus().toString() : null);
        dto.setItemReporterId(claim.getItem().getReporter() != null ? claim.getItem().getReporter().getUserId() : null);
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
