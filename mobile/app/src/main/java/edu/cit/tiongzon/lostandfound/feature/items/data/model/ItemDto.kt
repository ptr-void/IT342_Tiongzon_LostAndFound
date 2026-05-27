package edu.cit.tiongzon.lostandfound.feature.items.data.model

data class ItemDto(
    val id: Long? = null,
    val title: String = "",
    val description: String? = null,
    val status: String = "LOST",
    val category: String = "OTHER",
    val locationLat: Double? = null,
    val locationLng: Double? = null,
    val locationDescription: String? = null,
    val imagePath: String? = null,
    val reporterId: Long? = null,
    val reporterName: String? = null,
    val reporterEmail: String? = null,
    val reporterWarningMarks: Int = 0,
    val reporterAvatar: String? = null,
    val createdAt: String? = null,
    val lastUpdate: String? = null
)
