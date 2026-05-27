package edu.cit.tiongzon.lostandfound.feature.payments.data.model

data class SessionVerifyResponse(
    val sessionId: String,
    val status: String,
    val succeeded: Boolean
)
