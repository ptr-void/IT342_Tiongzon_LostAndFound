package edu.cit.tiongzon.lostandfound.feature.claims;

import edu.cit.tiongzon.lostandfound.feature.items.Item;
import edu.cit.tiongzon.lostandfound.feature.users.User;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "CLAIMS")
public class Claim {
    public enum ClaimStatus { PENDING, APPROVED, REJECTED }

    public enum PaymentStatus { PENDING, PAID, FAILED, NOT_APPLICABLE }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id", nullable = false)
    private Item item;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "claimant_id", nullable = false)
    private User claimant;

    @Column(name = "proof_description", columnDefinition = "TEXT")
    private String proofDescription;

    @Column(name = "proof_image_path", length = 1024)
    private String proofImagePath;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ClaimStatus status = ClaimStatus.PENDING;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", nullable = false)
    private PaymentStatus paymentStatus = PaymentStatus.NOT_APPLICABLE;

    @Column(name = "payment_intent_id", length = 255)
    private String paymentIntentId;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "last_update")
    private LocalDateTime lastUpdate;
}
