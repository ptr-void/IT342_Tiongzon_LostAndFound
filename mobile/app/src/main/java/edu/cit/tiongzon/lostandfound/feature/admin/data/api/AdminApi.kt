package edu.cit.tiongzon.lostandfound.feature.admin.data.api

import edu.cit.tiongzon.lostandfound.feature.admin.data.model.AdminClaimDto
import edu.cit.tiongzon.lostandfound.feature.admin.data.model.AdminItemDto
import edu.cit.tiongzon.lostandfound.feature.admin.data.model.AdminUserDto
import edu.cit.tiongzon.lostandfound.feature.admin.data.model.BanResponse
import edu.cit.tiongzon.lostandfound.feature.admin.data.model.WarnResponse
import retrofit2.Response
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path

interface AdminApi {
    @GET("admin/users")
    suspend fun getUsers(@Header("Authorization") token: String): Response<List<AdminUserDto>>

    @GET("admin/items")
    suspend fun getItems(@Header("Authorization") token: String): Response<List<AdminItemDto>>

    @GET("admin/claims")
    suspend fun getClaims(@Header("Authorization") token: String): Response<List<AdminClaimDto>>

    @POST("admin/users/{userId}/warn")
    suspend fun warnUser(@Header("Authorization") token: String, @Path("userId") userId: Long): Response<WarnResponse>

    @POST("admin/users/{userId}/ban")
    suspend fun toggleBan(@Header("Authorization") token: String, @Path("userId") userId: Long): Response<BanResponse>

    @DELETE("admin/items/{itemId}")
    suspend fun deleteItem(@Header("Authorization") token: String, @Path("itemId") itemId: Long): Response<Map<String, String>>
}
