package com.vyxel.launcher

import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.graphics.drawable.Drawable
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import java.text.SimpleDateFormat
import java.util.*

data class AppInfo(
    val label: String,
    val packageName: String,
    val activityName: String,
    val icon: Drawable?
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { VyxelLauncherApp() }
    }
}

@Composable
fun VyxelLauncherApp() {
    val context = LocalContext.current
    var showDrawer by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    val apps = remember {
        loadInstalledApps(context.packageManager)
    }

    val filteredApps = remember(searchQuery, apps) {
        if (searchQuery.isBlank()) apps
        else apps.filter { it.label.contains(searchQuery, ignoreCase = true) }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF0B0E14),
                        Color(0xFF151A24),
                        Color(0xFF1C2333)
                    )
                )
            )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color(0x334A90E2),
                            Color.Transparent
                        ),
                        radius = 900f
                    )
                )
        )

        if (!showDrawer) {
            HomeScreen(
                onOpenDrawer = { showDrawer = true },
                onLaunchApp = { launchApp(context, it) }
            )
        } else {
            AppDrawer(
                apps = filteredApps,
                searchQuery = searchQuery,
                onSearchChange = { searchQuery = it },
                onClose = { showDrawer = false; searchQuery = "" },
                onLaunchApp = {
                    launchApp(context, it)
                    showDrawer = false
                    searchQuery = ""
                }
            )
        }
    }
}

@Composable
fun HomeScreen(
    onOpenDrawer: () -> Unit,
    onLaunchApp: (AppInfo) -> Unit
) {
    val time = remember {
        SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
    }
    val date = remember {
        SimpleDateFormat("EEEE, MMM d", Locale.getDefault()).format(Date())
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .pointerInput(Unit) {
                detectVerticalDragGestures { _, dragAmount ->
                    if (dragAmount < -40) onOpenDrawer()
                }
            },
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        GlassCard(
            modifier = Modifier
                .padding(horizontal = 24.dp, vertical = 32.dp)
                .fillMaxWidth()
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Text(
                    text = time,
                    fontSize = 56.sp,
                    fontWeight = FontWeight.Light,
                    color = Color.White
                )
                Text(
                    text = date,
                    fontSize = 16.sp,
                    color = Color(0xFFB0B8C8)
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        GlassCard(
            modifier = Modifier
                .padding(horizontal = 20.dp, vertical = 24.dp)
                .fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp, horizontal = 12.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                DockIcon(Icons.Default.Search, "Search") { onOpenDrawer() }
                DockIcon(Icons.Default.Apps, "Apps") { onOpenDrawer() }
            }
        }
    }
}

@Composable
fun AppDrawer(
    apps: List<AppInfo>,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    onClose: () -> Unit,
    onLaunchApp: (AppInfo) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(16.dp)
    ) {
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Search,
                    contentDescription = null,
                    tint = Color(0xFFB0B8C8),
                    modifier = Modifier.size(22.dp)
                )
                Spacer(Modifier.width(10.dp))
                TextField(
                    value = searchQuery,
                    onValueChange = onSearchChange,
                    placeholder = { Text("Search apps", color = Color(0xFF8A92A4)) },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        cursorColor = Color(0xFF6BA3F5)
                    ),
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "Close",
                    color = Color(0xFF6BA3F5),
                    modifier = Modifier
                        .clickable { onClose() }
                        .padding(8.dp),
                    fontSize = 14.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(4),
            contentPadding = PaddingValues(8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(apps) { app ->
                AppIconItem(app = app, onClick = { onLaunchApp(app) })
            }
        }
    }
}

@Composable
fun AppIconItem(app: AppInfo, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable { onClick() }
            .padding(4.dp)
    ) {
        val bitmap = remember(app.icon) {
            app.icon?.toBitmap(96, 96)?.asImageBitmap()
        }
        if (bitmap != null) {
            Image(
                bitmap = bitmap,
                contentDescription = app.label,
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(14.dp))
            )
        } else {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFF2A3142)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = app.label.take(1).uppercase(),
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        Spacer(Modifier.height(6.dp))
        Text(
            text = app.label,
            color = Color.White,
            fontSize = 11.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            modifier = Modifier.width(64.dp)
        )
    }
}

@Composable
fun DockIcon(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable { onClick() }
            .padding(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(Color(0x33FFFFFF)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = label, tint = Color.White, modifier = Modifier.size(24.dp))
        }
    }
}

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(28.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        Color(0x44FFFFFF),
                        Color(0x22FFFFFF),
                        Color(0x18FFFFFF)
                    )
                )
            )
            .background(Color(0x1AFFFFFF))
    ) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .blur(20.dp)
                .background(Color(0x12FFFFFF))
        )
        content()
    }
}

fun loadInstalledApps(pm: PackageManager): List<AppInfo> {
    val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
    val resolveInfos: List<ResolveInfo> = pm.queryIntentActivities(intent, 0)
    return resolveInfos
        .map {
            AppInfo(
                label = it.loadLabel(pm).toString(),
                packageName = it.activityInfo.packageName,
                activityName = it.activityInfo.name,
                icon = it.loadIcon(pm)
            )
        }
        .sortedBy { it.label.lowercase() }
}

fun launchApp(context: android.content.Context, app: AppInfo) {
    try {
        val intent = Intent(Intent.ACTION_MAIN).apply {
            setClassName(app.packageName, app.activityName)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    } catch (_: Exception) {
    }
}
