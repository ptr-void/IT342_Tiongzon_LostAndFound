package edu.cit.tiongzon.lostandfound.feature.catalog.data.model

import edu.cit.tiongzon.lostandfound.feature.items.data.model.ItemDto

data class MobileItem(
    val id: Int,
    val title: String,
    val category: String,
    val status: String,
    val description: String,
    val date: String
)

val sampleItems = listOf(
    ItemDto(1, "Blue Hydro Flask", "Lost near the library entrance, blue color with stickers.", "LOST", "VALUABLES", reporterId = 1, reporterName = "john_doe", createdAt = "2/25/2026"),
    ItemDto(2, "Found iPhone 13", "Found near Room 301, has a black silicone case.", "FOUND", "ELECTRONICS", reporterId = 2, reporterName = "maria_g", createdAt = "2/25/2026"),
    ItemDto(3, "Student ID Card", "CIT-U ID with red lanyard.", "LOST", "DOCUMENTS", reporterId = 3, reporterName = "alice_99", createdAt = "2/24/2026")
)
