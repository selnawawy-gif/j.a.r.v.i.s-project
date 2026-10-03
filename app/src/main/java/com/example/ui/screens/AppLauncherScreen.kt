package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Launch
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.apps.InstalledAppItem
import com.example.ui.theme.StarkArcCyan
import com.example.ui.theme.StarkBorder
import com.example.ui.theme.StarkBorderBright
import com.example.ui.theme.StarkDarkBg
import com.example.ui.theme.StarkSurface
import com.example.ui.theme.StarkSurfaceVariant
import com.example.ui.theme.StarkTextPrimary
import com.example.ui.theme.StarkTextSecondary

@Composable
fun AppLauncherScreen(
    installedApps: List<InstalledAppItem>,
    onLaunchApp: (InstalledAppItem) -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }

    val filteredApps = if (searchQuery.isBlank()) {
        installedApps
    } else {
        installedApps.filter {
            it.name.contains(searchQuery, ignoreCase = true) ||
            it.packageName.contains(searchQuery, ignoreCase = true)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(StarkDarkBg)
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        // Title
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "SUBSYSTEM APP UPLINK",
                    color = StarkArcCyan,
                    fontSize = 18.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "VOICE TRIGGER: 'HEY JARVIS, OPEN [APP NAME]'",
                    color = StarkTextSecondary,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
            Text(
                text = "${filteredApps.size} APPS",
                color = StarkArcCyan,
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search installed applications...", color = StarkTextSecondary, fontSize = 13.sp) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    tint = StarkArcCyan,
                    modifier = Modifier.size(18.dp)
                )
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = StarkTextPrimary,
                unfocusedTextColor = StarkTextPrimary,
                focusedBorderColor = StarkArcCyan,
                unfocusedBorderColor = StarkBorder,
                focusedContainerColor = StarkSurface,
                unfocusedContainerColor = StarkSurface
            ),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("app_search_field")
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Apps Grid
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(bottom = 90.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(filteredApps) { app ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .clickable { onLaunchApp(app) }
                        .testTag("app_item_${app.packageName}"),
                    colors = CardDefaults.cardColors(containerColor = StarkSurface),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = androidx.compose.ui.graphics.SolidColor(StarkBorderBright)
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(StarkSurfaceVariant)
                                .border(1.dp, StarkArcCyan.copy(alpha = 0.5f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Apps,
                                contentDescription = null,
                                tint = StarkArcCyan,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = app.name,
                                color = StarkTextPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "TAP TO LAUNCH",
                                color = StarkTextSecondary,
                                fontSize = 9.sp,
                                fontFamily = FontFamily.Monospace,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }

                        Icon(
                            imageVector = Icons.Default.Launch,
                            contentDescription = "Launch",
                            tint = StarkArcCyan,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}
