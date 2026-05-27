package edu.cit.tiongzon.lostandfound.feature.payments;

import com.stripe.model.checkout.Session;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/payments")
public class PaymentController {
    private final PaymentService paymentService;

    @Value("${payment.amount:5000}")
    private Long defaultAmount;

    @Value("${payment.currency:php}")
    private String defaultCurrency;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    public static class PaymentRequest {
        public Long amount;
        public String currency;
    }

    @PostMapping("/create-checkout-session")
    public ResponseEntity<?> createCheckoutSession(@RequestBody PaymentRequest request) {
        try {
            long amount = request.amount != null ? request.amount : defaultAmount;
            String currency = (request.currency != null && !request.currency.isBlank())
                    ? request.currency.toLowerCase()
                    : defaultCurrency;
            
            
            String successUrl = "https://example.com/success";
            String cancelUrl = "https://example.com/cancel";
            
            Session session = paymentService.createCheckoutSession(amount, currency, successUrl, cancelUrl);
            return ResponseEntity.ok(Map.of(
                    "sessionId", session.getId(),
                    "url", session.getUrl()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/verify-session/{sessionId}")
    public ResponseEntity<?> verifySession(@PathVariable String sessionId) {
        try {
            Session session = Session.retrieve(sessionId);
            String status = session.getPaymentStatus(); 
            return ResponseEntity.ok(Map.of(
                    "sessionId", sessionId,
                    "status", status,
                    "succeeded", "paid".equals(status)
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage(), "succeeded", false));
        }
    }
}
