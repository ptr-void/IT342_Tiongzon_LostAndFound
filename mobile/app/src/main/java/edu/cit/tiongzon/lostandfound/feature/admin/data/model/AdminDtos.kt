package edu.cit.tiongzon.lostandfound.feature.admin.data.model

data class AdminUserDto(
    val userId: Long? = null,
    val username: String = "",
    val email: String = "",
    val avatarUrl: String? = null,
    val role: String = "USER",
    val active: Boolean = true,
    val warningMarks: Int = 0,
    val banned: Boolean = false,
    val createdAt: String? = null
)

data class AdminItemDto(
    val id: Long? = null,
    val title: String = "",
    val description: String? = null,
    val status: String = "LOST",
    val category: String = "OTHER",
    val imagePath: String? = null,
    val reporterId: Long? = null,
    val reporterName: String? = null,
    val createdAt: String? = null
)

data class AdminClaimDto(
    val id: Long? = null,
    val itemId: Long? = null,
    val itemTitle: String? = null,
    val claimantId: Long? = null,
    val claimantName: String? = null,
    val proofDescription: String? = null,
    val proofImagePath: String? = null,
    val status: String? = null,
    val paymentStatus: String? = null,
    val paymentIntentId: String? = null,
    val createdAt: String? = null
)

data class WarnResponse(
    val message: String = "",
    val warningMarks: Int = 0
)

data class BanResponse(
    val message: String = "",
    val isBanned: Boolean = false
)
