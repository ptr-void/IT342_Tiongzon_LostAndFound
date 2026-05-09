package edu.cit.tiongzon.lostandfound.feature.home.data.model

data class UserResponse(
    val userId: Long,
    val username: String,
    val email: String,
    val role: String,
    val avatarUrl: String?,
    val warningMarks: Int,
    val active: Boolean,
    val banned: Boolean
)
