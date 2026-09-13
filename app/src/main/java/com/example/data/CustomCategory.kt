package com.example.data

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material.icons.filled.ChildCare
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalCafe
import androidx.compose.material.icons.filled.LocalGroceryStore
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Subscriptions
import androidx.compose.material.icons.filled.Work
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "custom_categories")
data class CustomCategory(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val iconKey: String = "Category",
    val colorHex: String = "#2563EB",
    val isIncome: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
) {
    val color: Color
        get() = try {
            Color(android.graphics.Color.parseColor(colorHex))
        } catch (e: Exception) {
            Color(0xFF2563EB)
        }

    val icon: ImageVector
        get() = CategoryIconCatalog.getIcon(iconKey)
}

object CategoryIconCatalog {
    data class IconItem(
        val key: String,
        val label: String,
        val icon: ImageVector
    )

    val availableIcons: List<IconItem> = listOf(
        IconItem("School", "Education", Icons.Filled.School),
        IconItem("Flight", "Travel", Icons.Filled.Flight),
        IconItem("LocalCafe", "Coffee", Icons.Filled.LocalCafe),
        IconItem("Pets", "Pets", Icons.Filled.Pets),
        IconItem("FitnessCenter", "Fitness", Icons.Filled.FitnessCenter),
        IconItem("Savings", "Savings", Icons.Filled.Savings),
        IconItem("CardGiftcard", "Gifts", Icons.Filled.CardGiftcard),
        IconItem("Spa", "Beauty", Icons.Filled.Spa),
        IconItem("DirectionsCar", "Car", Icons.Filled.DirectionsCar),
        IconItem("LocalGroceryStore", "Groceries", Icons.Filled.LocalGroceryStore),
        IconItem("Computer", "Tech", Icons.Filled.Computer),
        IconItem("MusicNote", "Music", Icons.Filled.MusicNote),
        IconItem("MenuBook", "Books", Icons.Filled.MenuBook),
        IconItem("Home", "Home", Icons.Filled.Home),
        IconItem("Restaurant", "Food", Icons.Filled.Restaurant),
        IconItem("SportsEsports", "Gaming", Icons.Filled.SportsEsports),
        IconItem("LocalHospital", "Health", Icons.Filled.LocalHospital),
        IconItem("Bolt", "Utilities", Icons.Filled.Bolt),
        IconItem("ShoppingBag", "Shopping", Icons.Filled.ShoppingBag),
        IconItem("DirectionsBus", "Transit", Icons.Filled.DirectionsBus),
        IconItem("TrendingUp", "Income", Icons.AutoMirrored.Filled.TrendingUp),
        IconItem("Receipt", "Bills", Icons.Filled.Receipt),
        IconItem("Work", "Work", Icons.Filled.Work),
        IconItem("ChildCare", "Family", Icons.Filled.ChildCare),
        IconItem("Subscriptions", "Streaming", Icons.Filled.Subscriptions),
        IconItem("Build", "Repairs", Icons.Filled.Build),
        IconItem("Movie", "Movies", Icons.Filled.Movie),
        IconItem("Celebration", "Events", Icons.Filled.Celebration),
        IconItem("AttachMoney", "Finance", Icons.Filled.AttachMoney),
        IconItem("Category", "General", Icons.Filled.Category)
    )

    val availableColors: List<String> = listOf(
        "#2563EB", // Blue
        "#10B981", // Emerald
        "#8B5CF6", // Purple
        "#F43F5E", // Rose
        "#F59E0B", // Amber
        "#0D9488", // Teal
        "#6366F1", // Indigo
        "#D946EF", // Fuchsia
        "#EA580C", // Orange
        "#0284C7"  // Sky
    )

    fun getIcon(key: String): ImageVector {
        return availableIcons.firstOrNull { it.key.equals(key, ignoreCase = true) }?.icon
            ?: Icons.Filled.Category
    }
}
