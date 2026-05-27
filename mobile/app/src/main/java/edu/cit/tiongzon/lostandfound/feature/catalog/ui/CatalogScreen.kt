package edu.cit.tiongzon.lostandfound.feature.catalog.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import edu.cit.tiongzon.lostandfound.feature.catalog.data.model.sampleItems
import edu.cit.tiongzon.lostandfound.feature.items.data.model.ItemDto
import edu.cit.tiongzon.lostandfound.shared.api.RetrofitClient
import edu.cit.tiongzon.lostandfound.shared.ui.components.*
import edu.cit.tiongzon.lostandfound.shared.ui.theme.*

@Composable
fun CatalogScreen(
    token: String,
    onNavigate: (String) -> Unit,
    onLogout: () -> Unit,
    onItemClick: (Long) -> Unit = {}
) {
    var items by remember { mutableStateOf<List<ItemDto>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var query by remember { mutableStateOf("") }
    var selectedStatus by remember { mutableStateOf("All") }
    var selectedCategory by remember { mutableStateOf("All Categories") }

    LaunchedEffect(token) {
        loading = true
        error = null
        try {
            val response = RetrofitClient.itemApi.getItems("Bearer $token")
            if (response.isSuccessful) {
                items = response.body().orEmpty()
            } else {
                error = "Failed to load items."
                items = sampleItems
            }
        } catch (e: Exception) {
            error = "Backend unavailable. Showing samples."
            items = sampleItems
        } finally {
            loading = false
        }
    }

    val filtered = items.filter { item ->
        val matchesQuery = query.isBlank() ||
            item.title.contains(query, ignoreCase = true) ||
            item.description.orEmpty().contains(query, ignoreCase = true)
        val matchesStatus = selectedStatus == "All" || item.status == selectedStatus.uppercase()
        val matchesCategory = selectedCategory == "All Categories" || item.category.equals(selectedCategory, ignoreCase = true)
        matchesQuery && matchesStatus && matchesCategory
    }

    MobileShell(token = token, onNavigate = onNavigate, onLogout = onLogout, currentRoute = "catalog") {
        
        Box(
            modifier = Modifier.fillMaxWidth()
                .background(Brush.linearGradient(listOf(Rose900, Rose700)), shape = RoundedCornerShape(20.dp))
                .padding(horizontal = 20.dp, vertical = 20.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Lost & Found", fontWeight = FontWeight.ExtraBold, fontSize = 22.sp, color = Color.White)
                    Text("Catalog", fontWeight = FontWeight.Light, fontSize = 16.sp, color = Color.White.copy(alpha = 0.8f))
                    Spacer(Modifier.height(4.dp))
                    Text("${filtered.size} items reported", fontSize = 12.sp, color = Color.White.copy(alpha = 0.65f))
                }
                Button(
                    onClick = { onNavigate("report") },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Rose900),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Report", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        
        SearchAndFilters(
            query = query,
            onQueryChange = { query = it },
            selectedStatus = selectedStatus,
            onStatusChange = { selectedStatus = it },
            selectedCategory = selectedCategory,
            onCategoryChange = { selectedCategory = it }
        )

        
        if (loading) {
            LinearProgressIndicator(
                modifier = Modifier.fillMaxWidth(),
                color = Rose800,
                trackColor = Rose100
            )
        }
        error?.let {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Amber100)
            ) {
                Text(it, modifier = Modifier.padding(12.dp), color = Amber600, fontSize = 12.sp)
            }
        }

        
        if (filtered.isEmpty() && !loading) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Default.Search, contentDescription = null, tint = Slate300, modifier = Modifier.size(40.dp))
                    Spacer(Modifier.height(8.dp))
                    Text("No items found", fontWeight = FontWeight.Bold, color = Slate700)
                    Text("Try adjusting your filters.", fontSize = 12.sp, color = Slate500)
                }
            }
        } else {
            filtered.forEach { item ->
                ItemCard(item = item, onClick = {
                    val id = item.id
                    if (id != null) onItemClick(id) else onNavigate("catalog")
                })
            }
        }
    }
}
