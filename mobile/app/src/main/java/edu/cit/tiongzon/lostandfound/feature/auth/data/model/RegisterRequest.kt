package edu.cit.tiongzon.lostandfound.feature.auth.data.model

data class RegisterRequest(
    val username: String,
    val email: String,
    val password: String
)
