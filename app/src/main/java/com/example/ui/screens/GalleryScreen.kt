package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.api.FacelessCanvasConfig
import com.example.data.GenerationEntity
import com.example.ui.components.FaceLessCanvasRenderer
import com.example.ui.theme.*
import com.example.viewmodel.StudioViewModel
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

@Composable
fun GalleryScreen(
    viewModel: StudioViewModel,
    modifier: Modifier = Modifier
) {
    val history by viewModel.historyGenerations.collectAsState()
    val favorites by viewModel.favoriteGenerations.collectAsState()

    var activeTab by remember { mutableStateOf("history") } // "history" or "favorites"
    
    val moshi = remember { Moshi.Builder().add(KotlinJsonAdapterFactory()).build() }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(NavyBackground)
            .padding(16.dp)
    ) {
        // Tab Headers
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TabHeaderButton(
                title = "Generation History",
                isActive = activeTab == "history",
                badgeCount = history.size,
                onClick = { activeTab = "history" },
                modifier = Modifier.weight(1f).testTag("tab_history_btn")
            )

            TabHeaderButton(
                title = "Saved Favorites",
                isActive = activeTab == "favorites",
                badgeCount = favorites.size,
                onClick = { activeTab = "favorites" },
                modifier = Modifier.weight(1f).testTag("tab_favorites_btn")
            )
        }

        val activeList = if (activeTab == "history") history else favorites

        if (activeList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(TechOrange.copy(alpha = 0.08f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (activeTab == "history") Icons.Filled.History else Icons.Filled.FavoriteBorder,
                            contentDescription = "Empty",
                            tint = TechOrange,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = if (activeTab == "history") "No history generated yet" else "No saved favorites yet",
                        color = TextWhite,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Your faceless designs will display here.\nGo to the Studio tab to start generating!",
                        color = TextGray,
                        fontSize = 11.sp,
                        textAlign = TextAlign.Center,
                        lineHeight = 15.sp
                    )
                }
            }
        } else {
            // Header actions
            if (activeTab == "history") {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = { viewModel.clearAllHistory() },
                        colors = ButtonDefaults.textButtonColors(contentColor = Color.Red.copy(alpha = 0.8f))
                    ) {
                        Icon(Icons.Filled.DeleteSweep, contentDescription = "Clear All", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Clear All History", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Grid displaying card outputs with live miniatures of custom drawings!
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(activeList, key = { it.id }) { item ->
                    val config = try {
                        moshi.adapter(FacelessCanvasConfig::class.java).fromJson(item.renderJson)
                    } catch (e: Exception) {
                        null
                    }

                    GalleryItemCard(
                        item = item,
                        config = config,
                        onLoad = { viewModel.loadConfig(item) },
                        onToggleFavorite = { viewModel.toggleFavorite(item.id, !item.isFavorite) },
                        onDelete = { viewModel.deleteHistoryId(item.id) },
                        isHistoryView = activeTab == "history"
                    )
                }
            }
        }
    }
}

@Composable
fun TabHeaderButton(
    title: String,
    isActive: Boolean,
    badgeCount: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = if (isActive) ActiveCardNavy else DeepCardNavy,
        border = BorderStroke(1.dp, if (isActive) TechOrange else BorderNavy),
        shape = RoundedCornerShape(10.dp),
        modifier = modifier
            .clickable { onClick() }
            .height(40.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = title,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = if (isActive) TechOrange else TextWhite
            )
            if (badgeCount > 0) {
                Spacer(modifier = Modifier.width(6.dp))
                Box(
                    modifier = Modifier
                        .background(if (isActive) TechOrange else BorderNavy, CircleShape)
                        .padding(horizontal = 6.dp, vertical = 2.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        badgeCount.toString(),
                        color = TextWhite,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun GalleryItemCard(
    item: GenerationEntity,
    config: FacelessCanvasConfig?,
    onLoad: () -> Unit,
    onToggleFavorite: () -> Unit,
    onDelete: () -> Unit,
    isHistoryView: Boolean
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onLoad() }
            .testTag("gallery_item_${item.id}"),
        colors = CardDefaults.cardColors(containerColor = DeepCardNavy),
        border = BorderStroke(1.dp, BorderNavy),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column {
            // Miniature render on top of Card!
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp)
                    .background(ActiveCardNavy)
            ) {
                if (config != null) {
                    FaceLessCanvasRenderer(
                        config = config,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Icon(Icons.Filled.BrokenImage, contentDescription = "Error", tint = TextGray)
                    }
                }

                // Overlay Controls to Like / Delete (mini-floating button row)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Like heart toggle
                    IconButton(
                        onClick = { onToggleFavorite() },
                        modifier = Modifier
                            .background(DeepCardNavy.copy(alpha = 0.8f), CircleShape)
                            .size(26.dp)
                    ) {
                        Icon(
                            imageVector = if (item.isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                            contentDescription = "Save",
                            tint = if (item.isFavorite) Color.Red else TextWhite,
                            modifier = Modifier.size(14.dp)
                        )
                    }

                    // Delete history card
                    if (isHistoryView) {
                        IconButton(
                            onClick = { onDelete() },
                            modifier = Modifier
                                .background(DeepCardNavy.copy(alpha = 0.8f), CircleShape)
                                .size(26.dp)
                        ) {
                            Icon(Icons.Filled.Delete, contentDescription = "Delete", tint = TextWhite, modifier = Modifier.size(13.dp))
                        }
                    }
                }
            }

            // Info panel underneath the card drawing
            Column(modifier = Modifier.padding(8.dp)) {
                Text(
                    text = item.prompt,
                    color = TextWhite,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = item.styleName,
                        color = TechOrange,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                    
                    if (item.mode == "Islamic Kids") {
                        Icon(Icons.Filled.Star, contentDescription = "Islamic", tint = GlowCyan, modifier = Modifier.size(10.dp))
                    }
                }
            }
        }
    }
}
