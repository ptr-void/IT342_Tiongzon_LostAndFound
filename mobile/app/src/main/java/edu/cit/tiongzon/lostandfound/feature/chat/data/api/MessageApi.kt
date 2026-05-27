package edu.cit.tiongzon.lostandfound.feature.chat.data.api

import edu.cit.tiongzon.lostandfound.feature.chat.data.model.ConversationDto
import edu.cit.tiongzon.lostandfound.feature.chat.data.model.MessageDto
import retrofit2.Response
import retrofit2.http.*

interface MessageApi {
    @GET("messages/global")
    suspend fun getGlobalMessages(
        @Header("Authorization") token: String
    ): Response<List<MessageDto>>

    @GET("messages/conversations")
    suspend fun getConversations(
        @Header("Authorization") token: String
    ): Response<List<ConversationDto>>

    @GET("messages/private/{userId}")
    suspend fun getPrivateMessages(
        @Header("Authorization") token: String,
        @Path("userId") userId: Long,
        @Query("itemId") itemId: Long
    ): Response<List<MessageDto>>

}
