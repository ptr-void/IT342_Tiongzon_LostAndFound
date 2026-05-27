package edu.cit.tiongzon.lostandfound.feature.chat.data.model

data class MessageDto(
    val id: Long? = null,
    val senderId: Long? = null,
    val senderName: String? = null,
    val receiverId: Long? = null,
    val itemId: Long? = null,
    val content: String = "",
    val senderAvatar: String? = null,
    val sentAt: String? = null
)

data class ConversationDto(
    val userId: Long? = null,
    val username: String? = null,
    val avatarUrl: String? = null,
    val itemId: Long? = null,
    val itemTitle: String? = null,
    val lastMessage: String? = null,
    val lastMessageSenderId: Long? = null,
    val lastMessageAt: String? = null
)
