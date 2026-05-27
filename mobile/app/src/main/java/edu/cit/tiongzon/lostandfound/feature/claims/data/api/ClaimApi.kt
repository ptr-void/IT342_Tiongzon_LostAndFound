package edu.cit.tiongzon.lostandfound.feature.claims.data.api

import edu.cit.tiongzon.lostandfound.feature.claims.data.model.ClaimDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path
import retrofit2.http.POST

interface ClaimApi {
    @GET("claims/mine")
    suspend fun getMyClaims(
        @Header("Authorization") token: String
    ): Response<List<ClaimDto>>

    @GET("claims/received")
    suspend fun getReceivedClaims(
        @Header("Authorization") token: String
    ): Response<List<ClaimDto>>

    @POST("claims")
    suspend fun createClaim(
        @Header("Authorization") token: String,
        @Body body: ClaimDto
    ): Response<ClaimDto>

    @GET("claims/{id}")
    suspend fun getClaim(
        @Header("Authorization") token: String,
        @Path("id") id: Long
    ): Response<ClaimDto>

    @POST("claims/{id}/approve")
    suspend fun approveClaim(
        @Header("Authorization") token: String,
        @Path("id") id: Long
    ): Response<ClaimDto>

    @POST("claims/{id}/reject")
    suspend fun rejectClaim(
        @Header("Authorization") token: String,
        @Path("id") id: Long
    ): Response<ClaimDto>

    @POST("claims/{id}/mark-paid")
    suspend fun markClaimPaid(
        @Header("Authorization") token: String,
        @Path("id") id: Long,
        @Body body: ClaimDto
    ): Response<ClaimDto>
}
