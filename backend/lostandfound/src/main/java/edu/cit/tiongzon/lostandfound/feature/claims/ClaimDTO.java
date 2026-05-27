package edu.cit.tiongzon.lostandfound.feature.claims;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ClaimDTO {
    private Long id;
    private Long itemId;
    private String itemTitle;
    private String itemStatus;
    private Long itemReporterId;
    private Long claimantId;
    private String claimantName;
    private String proofDescription;
    private String proofImagePath;
    private Claim.ClaimStatus status;
    private Claim.PaymentStatus paymentStatus;
    private String paymentIntentId;
    private LocalDateTime createdAt;
    private LocalDateTime lastUpdate;
}
