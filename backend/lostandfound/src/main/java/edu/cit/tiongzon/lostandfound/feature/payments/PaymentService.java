package edu.cit.tiongzon.lostandfound.feature.payments;

import com.stripe.Stripe;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class PaymentService {
    @Value("${stripe.api.key:}")
    private String stripeApiKey;

    @PostConstruct
    public void init() {
        if (stripeApiKey != null && !stripeApiKey.isBlank()) Stripe.apiKey = stripeApiKey;
    }

    public Session createCheckoutSession(Long amount, String currency, String successUrl, String cancelUrl) throws Exception {
        if (stripeApiKey == null || stripeApiKey.isBlank()) throw new IllegalStateException("Stripe API key is not configured");
        SessionCreateParams params = SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setSuccessUrl(successUrl)
                .setCancelUrl(cancelUrl)
                .addLineItem(
                        SessionCreateParams.LineItem.builder()
                                .setQuantity(1L)
                                .setPriceData(
                                        SessionCreateParams.LineItem.PriceData.builder()
                                                .setCurrency(currency)
                                                .setUnitAmount(amount)
                                                .setProductData(
                                                        SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                                .setName("Lost and Found Claim Payment")
                                                                .build()
                                                )
                                                .build()
                                )
                                .build()
                )
                .build();
        return Session.create(params);
    }
}
