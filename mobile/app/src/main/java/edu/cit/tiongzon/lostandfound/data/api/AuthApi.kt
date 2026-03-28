package edu.cit.tiongzon.lostandfound.data.api

import edu.cit.tiongzon.lostandfound.data.model.AuthResponse
import edu.cit.tiongzon.lostandfound.data.model.LoginRequest
import edu.cit.tiongzon.lostandfound.data.model.RegisterRequest
import edu.cit.tiongzon.lostandfound.data.model.UserResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

interface AuthApi {

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>

    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<AuthResponse>

    @GET("users/me")
    suspend fun getCurrentUser(@Header("Authorization") token: String): Response<UserResponse>
}
