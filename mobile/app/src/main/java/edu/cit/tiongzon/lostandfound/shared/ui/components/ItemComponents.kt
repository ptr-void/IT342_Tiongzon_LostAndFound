package edu.cit.tiongzon.lostandfound.shared.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import edu.cit.tiongzon.lostandfound.feature.items.data.model.ItemDto
import edu.cit.tiongzon.lostandfound.shared.ui.theme.*

private const val BASE_URL = "http://10.0.2.2:8080"

@Composable
fun CardPanel(padding: Int = 16, content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(Modifier.padding(padding.dp), content = content)
    }
}

@Composable
fun SearchAndFilters(
    query: String = "",
    onQueryChange: (String) -> Unit = {},
    selectedStatus: String = "All",
    onStatusChange: (String) -> Unit = {},
    selectedCategory: String = "All Categories",
    onCategoryChange: (String) -> Unit = {}
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        placeholder = { Text("Search items...", fontSize = 13.sp, color = Slate400) },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Slate400, modifier = Modifier.size(20.dp)) },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedBorderColor = Slate200,
            focusedBorderColor = Rose800,
            unfocusedContainerColor = Color.White,
            focusedContainerColor = Color.White
        )
    )
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        listOf("All", "Lost", "Found").forEach { status ->
            val active = selectedStatus == status
            FilterChip(
                selected = active,
                onClick = { onStatusChange(status) },
                label = { Text(status, fontSize = 12.sp, fontWeight = if (active) FontWeight.Bold else FontWeight.Normal) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Rose800,
                    selectedLabelColor = Color.White,
                    containerColor = Color.White,
                    labelColor = Slate600
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = active,
                    selectedBorderColor = Rose800,
                    borderColor = Slate200
                )
            )
        }
    }
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState())) {
        listOf("All Categories", "Electronics", "Documents", "Clothing", "Other").forEach { category ->
            val active = selectedCategory == category
            FilterChip(
                selected = active,
                onClick = { onCategoryChange(category) },
                label = { Text(category, fontSize = 11.sp, fontWeight = if (active) FontWeight.Bold else FontWeight.Normal) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Amber500,
                    selectedLabelColor = Color.White,
                    containerColor = Color.White,
                    labelColor = Slate600
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = active,
                    selectedBorderColor = Amber500,
                    borderColor = Slate200
                )
            )
        }
    }
}

@Composable
fun SmallOutlineButton(text: String, modifier: Modifier = Modifier) {
    OutlinedButton(
        onClick = {},
        modifier = modifier.height(34.dp),
        contentPadding = PaddingValues(horizontal = 10.dp),
        shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = Slate700)
    ) { Text(text, fontSize = 11.sp, maxLines = 1) }
}

@Composable
fun ItemCard(item: ItemDto, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        
        ItemImage(imagePath = item.imagePath, height = 160)

        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                StatusPill(item.status)
                CategoryPill(item.category)
            }
            Text(
                item.title,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = Slate900,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                item.description ?: "No description provided.",
                fontSize = 12.sp,
                color = Slate500,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 18.sp
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                item.reporterName?.let {
                    Text("by $it", fontSize = 11.sp, color = Slate400)
                }
                Spacer(Modifier.weight(1f))
                Text(item.createdAt?.take(10) ?: "", fontSize = 10.sp, color = Slate400)
            }
        }
    }
}

@Composable
fun DashboardItemCard(
    item: ItemDto,
    onDelete: (() -> Unit)? = null,
    onUpdateStatus: ((String) -> Unit)? = null
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        ItemImage(imagePath = item.imagePath, height = 140)
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                StatusPill(item.status)
                CategoryPill(item.category)
            }
            Text(item.title, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Slate900)
            Text(item.description ?: "No description.", fontSize = 12.sp, color = Slate500, lineHeight = 18.sp)
            HorizontalDivider(color = Slate100)
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Posted ${item.createdAt?.take(10) ?: "recently"}", fontSize = 11.sp, color = Slate400)
                
                if (onUpdateStatus != null || onDelete != null) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            if (onUpdateStatus != null) {
                                SmallStatusBtn("LOST", active = item.status == "LOST") { onUpdateStatus("LOST") }
                                SmallStatusBtn("FOUND", active = item.status == "FOUND") { onUpdateStatus("FOUND") }
                                SmallStatusBtn("RESOLVED", active = item.status == "RESOLVED") { onUpdateStatus("RESOLVED") }
                            }
                        }
                        if (onDelete != null) {
                            IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Rose700, modifier = Modifier.size(20.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SmallStatusBtn(text: String, active: Boolean, onClick: () -> Unit) {
    val containerColor = if (active) {
        when (text) {
            "LOST" -> Rose100
            "FOUND" -> Emerald100
            else -> Slate100
        }
    } else Color.Transparent
    
    val contentColor = if (active) {
        when (text) {
            "LOST" -> Rose800
            "FOUND" -> Emerald700
            else -> Slate800
        }
    } else Slate500
    
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier.height(30.dp),
        contentPadding = PaddingValues(horizontal = 8.dp),
        shape = RoundedCornerShape(6.dp),
        colors = ButtonDefaults.outlinedButtonColors(containerColor = containerColor, contentColor = contentColor),
        border = if (active) null else ButtonDefaults.outlinedButtonBorder.copy(brush = androidx.compose.ui.graphics.SolidColor(Slate200))
    ) {
        Text(text, fontSize = 10.sp, fontWeight = if (active) FontWeight.Bold else FontWeight.Medium)
    }
}

@Composable
fun CompactItemCard(item: ItemDto) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            
            val imageUrl = item.imagePath?.let { p -> if (p.startsWith("http")) p else "$BASE_URL$p" }
            if (!imageUrl.isNullOrBlank()) {
                SubcomposeAsyncImage(
                    model = imageUrl,
                    contentDescription = item.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.size(48.dp).clip(RoundedCornerShape(10.dp)),
                    loading = {
                        Box(Modifier.size(48.dp).clip(RoundedCornerShape(10.dp)).background(Slate100))
                    },
                    error = {
                        Box(
                            Modifier.size(48.dp).clip(RoundedCornerShape(10.dp)).background(Slate100),
                            contentAlignment = Alignment.Center
                        ) { Icon(Icons.Default.Inventory2, contentDescription = null, tint = Slate400, modifier = Modifier.size(20.dp)) }
                    }
                )
            } else {
                Box(
                    Modifier.size(48.dp).clip(RoundedCornerShape(10.dp)).background(Rose50),
                    contentAlignment = Alignment.Center
                ) { Icon(Icons.Default.Inventory2, contentDescription = null, tint = Slate400, modifier = Modifier.size(20.dp)) }
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(item.title, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = Slate900, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text("Reported ${item.createdAt?.take(10) ?: "recently"}", fontSize = 11.sp, color = Slate500)
            }
            Spacer(Modifier.width(8.dp))
            StatusPill(item.status)
        }
    }
}

@Composable
fun ItemImage(imagePath: String?, height: Int) {
    val imageUrl = imagePath?.let { p -> if (p.startsWith("http")) p else "$BASE_URL$p" }
    if (!imageUrl.isNullOrBlank()) {
        SubcomposeAsyncImage(
            model = imageUrl,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxWidth().height(height.dp).clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)),
            loading = {
                Box(
                    Modifier.fillMaxWidth().height(height.dp)
                        .background(Brush.linearGradient(listOf(Rose100, Amber100))),
                    contentAlignment = Alignment.Center
                ) { CircularProgressIndicator(color = Rose800, modifier = Modifier.size(28.dp), strokeWidth = 2.dp) }
            },
            error = { ImageFallback(height) }
        )
    } else {
        ImageFallback(height)
    }
}

@Composable
private fun ImageFallback(height: Int) {
    Box(
        modifier = Modifier.fillMaxWidth().height(height.dp)
            .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
            .background(Brush.linearGradient(listOf(Rose50, Amber50))),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.Inventory2, contentDescription = null, tint = Slate300, modifier = Modifier.size(28.dp))
            Spacer(Modifier.height(4.dp))
            Text("No Image", fontSize = 11.sp, color = Slate400)
        }
    }
}

@Composable
fun StatusPill(status: String) {
    val bg = when (status) {
        "FOUND" -> Emerald100
        "LOST" -> Rose100
        "RESOLVED" -> Slate200
        "PENDING" -> Amber100
        "APPROVED" -> Emerald100
        "REJECTED" -> Rose100
        "PAID" -> Emerald100
        "FAILED" -> Rose100
        "NOT_APPLICABLE" -> Slate100
        else -> Slate100
    }
    val fg = when (status) {
        "FOUND" -> Emerald600
        "LOST" -> Rose800
        "RESOLVED" -> Slate800
        "PENDING" -> Amber600
        "APPROVED" -> Emerald600
        "REJECTED" -> Rose800
        "PAID" -> Emerald600
        "FAILED" -> Rose800
        "NOT_APPLICABLE" -> Slate600
        else -> Slate600
    }
    Text(
        status,
        modifier = Modifier.clip(RoundedCornerShape(20.dp)).background(bg).padding(horizontal = 10.dp, vertical = 4.dp),
        fontSize = 10.sp,
        color = fg,
        fontWeight = FontWeight.Bold
    )
}

@Composable
fun CategoryPill(category: String) {
    Text(
        category,
        modifier = Modifier.clip(RoundedCornerShape(20.dp)).background(Slate100).padding(horizontal = 10.dp, vertical = 4.dp),
        fontSize = 10.sp,
        color = Slate600,
        fontWeight = FontWeight.Medium
    )
}

@Composable
fun Pill(text: String, modifier: Modifier = Modifier) {
    Text(
        text,
        modifier = modifier.clip(RoundedCornerShape(20.dp)).background(Slate100).padding(horizontal = 9.dp, vertical = 4.dp),
        fontSize = 9.sp,
        color = Slate700,
        fontWeight = FontWeight.Bold
    )
}
