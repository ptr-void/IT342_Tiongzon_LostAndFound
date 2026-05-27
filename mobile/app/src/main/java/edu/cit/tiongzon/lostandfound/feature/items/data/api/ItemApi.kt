package edu.cit.tiongzon.lostandfound.feature.items.data.api

import edu.cit.tiongzon.lostandfound.feature.items.data.model.ItemDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface ItemApi {
    @GET("items")
    suspend fun getItems(@Header("Authorization") token: String? = null): Response<List<ItemDto>>

    @GET("items/{id}")
    suspend fun getItem(@Path("id") id: Long): Response<ItemDto>

    @POST("items")
    suspend fun createItem(@Header("Authorization") token: String, @Body item: ItemDto): Response<ItemDto>

    @PUT("items/{id}")
    suspend fun updateItem(@Header("Authorization") token: String, @Path("id") id: Long, @Body item: ItemDto): Response<ItemDto>

    @DELETE("items/{id}")
    suspend fun deleteItem(@Header("Authorization") token: String, @Path("id") id: Long): Response<Unit>
}
