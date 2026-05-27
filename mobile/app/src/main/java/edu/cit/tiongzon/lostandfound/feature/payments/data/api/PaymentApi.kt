package edu.cit.tiongzon.lostandfound.feature.payments.data.api

import edu.cit.tiongzon.lostandfound.feature.payments.data.model.CheckoutSessionResponse
import edu.cit.tiongzon.lostandfound.feature.payments.data.model.PaymentQrDto
import edu.cit.tiongzon.lostandfound.feature.payments.data.model.PaymentRequest
import edu.cit.tiongzon.lostandfound.feature.payments.data.model.SessionVerifyResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path

interface PaymentApi {
    @GET("payments/qr")
    suspend fun getPaymentQr(
        @Header("Authorization") token: String
    ): Response<PaymentQrDto>

    @POST("payments/create-checkout-session")
    suspend fun createCheckoutSession(
        @Header("Authorization") token: String,
        @Body request: PaymentRequest
    ): Response<CheckoutSessionResponse>

    @GET("payments/verify-session/{sessionId}")
    suspend fun verifySession(
        @Header("Authorization") token: String,
        @Path("sessionId") sessionId: String
    ): Response<SessionVerifyResponse>
}
