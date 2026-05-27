package edu.cit.tiongzon.lostandfound.feature.claims.data.model

data class ClaimDto(
    val id: Long? = null,
    val itemId: Long? = null,
    val itemTitle: String? = null,
    val itemStatus: String? = null,
    val itemReporterId: Long? = null,
    val claimantId: Long? = null,
    val claimantName: String? = null,
    val proofDescription: String = "",
    val proofImagePath: String? = null,
    val status: String? = null,
    val paymentStatus: String? = null,
    val paymentIntentId: String? = null,
    val createdAt: String? = null,
    val lastUpdate: String? = null
)
