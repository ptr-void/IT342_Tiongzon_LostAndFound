package edu.cit.tiongzon.lostandfound.data.model

data class UserResponse(
    val userId: Long? = null,
    val username: String? = null,
    val email: String? = null,
    val avatarUrl: String? = null,
    val role: String? = null,
    val isActive: Boolean = false,
    val warningMarks: Int = 0,
    val isBanned: Boolean = false
)
