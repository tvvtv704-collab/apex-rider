package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.CompareArrows
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.DirectionsBike
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.ScreenRotation
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.TwoWheeler
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.domain.model.HighwayMap
import com.example.domain.model.PlayerState
import com.example.domain.model.SteeringMode
import com.example.domain.model.TrafficMode
import com.example.ui.components.BikeIllustrationCanvas
import com.example.ui.components.MapPreviewCanvas
import com.example.ui.theme.AsphaltSurfaceVariant
import com.example.ui.theme.CarbonSurface
import com.example.ui.theme.CyanTelemetry
import com.example.ui.theme.EmeraldCash
import com.example.ui.theme.ObsidianBg
import com.example.ui.theme.RacingAmber
import com.example.ui.theme.SteelBorder

@Composable
fun MainMenuScreen(
    playerState: PlayerState,
    onStartRide: () -> Unit,
    onOpenGarage: () -> Unit,
    onOpenCareer: () -> Unit,
    onSelectMap: (HighwayMap) -> Unit,
    onToggleSteering: (SteeringMode) -> Unit,
    onToggleTraffic: (TrafficMode) -> Unit
) {
    val selectedBike = playerState.selectedBike

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ObsidianBg)
    ) {
        // Atmospheric Garage Backdrop at Top
        Image(
            painter = painterResource(id = R.drawable.img_garage_hero),
            contentDescription = "Garage bay backdrop",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .height(270.dp)
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(275.dp)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            ObsidianBg.copy(alpha = 0.55f),
                            ObsidianBg.copy(alpha = 0.72f),
                            ObsidianBg
                        )
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(
                modifier = Modifier
                    .widthIn(max = 720.dp)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Top Header Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "APEX RIDER",
                            style = MaterialTheme.typography.displayMedium,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = stringResource(R.string.tagline_highway_rider),
                            style = MaterialTheme.typography.labelMedium,
                            color = RacingAmber
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            color = CarbonSurface.copy(alpha = 0.9f),
                            shape = RoundedCornerShape(50),
                            border = BorderStroke(1.dp, EmeraldCash.copy(alpha = 0.5f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AttachMoney,
                                    contentDescription = null,
                                    tint = EmeraldCash,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = "${playerState.cash}",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = EmeraldCash
                                )
                            }
                        }

                        IconButton(
                            onClick = onOpenCareer,
                            modifier = Modifier
                                .size(48.dp)
                                .testTag("career_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.EmojiEvents,
                                contentDescription = stringResource(R.string.action_career),
                                tint = RacingAmber
                            )
                        }
                    }
                }

                // Active Bike Showcase Card
                if (selectedBike != null) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = CarbonSurface.copy(alpha = 0.92f)),
                        border = BorderStroke(1.dp, SteelBorder),
                        shape = MaterialTheme.shapes.large,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(onClick = onOpenGarage)
                            .testTag("equipped_bike_card")
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = selectedBike.spec.category.displayName.uppercase(),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = CyanTelemetry
                                    )
                                    Text(
                                        text = selectedBike.spec.name,
                                        style = MaterialTheme.typography.headlineMedium,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                Surface(
                                    color = AsphaltSurfaceVariant,
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = "${selectedBike.currentTopSpeedKmh} KM/H",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = RacingAmber,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            BikeIllustrationCanvas(
                                spec = selectedBike.spec,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(126.dp)
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                StatPill("TOP SPEED", "${selectedBike.currentTopSpeedKmh} km/h", RacingAmber)
                                StatPill("HANDLING", "${selectedBike.currentHandling}/100", CyanTelemetry)
                                StatPill("BRAKING", "${selectedBike.currentBraking}/100", EmeraldCash)
                            }
                        }
                    }
                }

                // Highway Environment & Weather Selector
                Text(
                    text = stringResource(R.string.label_select_environment),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(end = 8.dp)
                ) {
                    items(HighwayMap.entries) { map ->
                        val isSelected = playerState.selectedMap == map
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) AsphaltSurfaceVariant else CarbonSurface
                            ),
                            border = BorderStroke(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = if (isSelected) RacingAmber else SteelBorder
                            ),
                            shape = MaterialTheme.shapes.medium,
                            modifier = Modifier
                                .width(185.dp)
                                .clickable { onSelectMap(map) }
                                .testTag("map_card_${map.id}")
                        ) {
                            Column {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(84.dp)
                                        .clip(RoundedCornerShape(topStart = 14.dp, topEnd = 14.dp))
                                ) {
                                    MapPreviewCanvas(
                                        map = map,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                    Surface(
                                        color = ObsidianBg.copy(alpha = 0.8f),
                                        shape = RoundedCornerShape(6.dp),
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .padding(6.dp)
                                    ) {
                                        Text(
                                            text = "${map.cashMultiplier}x CASH",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = EmeraldCash,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                        )
                                    }
                                }
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text(
                                        text = map.title,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = map.weather.label,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = CyanTelemetry
                                    )
                                }
                            }
                        }
                    }
                }

                // Traffic Flow & Steering Input Options
                Card(
                    colors = CardDefaults.cardColors(containerColor = CarbonSurface),
                    border = BorderStroke(1.dp, SteelBorder),
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.label_traffic_mode),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            TrafficMode.entries.forEach { mode ->
                                FilterChip(
                                    selected = playerState.trafficMode == mode,
                                    onClick = { onToggleTraffic(mode) },
                                    label = { Text(mode.label, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.CompareArrows,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = RacingAmber.copy(alpha = 0.2f),
                                        selectedLabelColor = RacingAmber,
                                        selectedLeadingIconColor = RacingAmber
                                    ),
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("traffic_chip_${mode.name}")
                                )
                            }
                        }

                        Text(
                            text = stringResource(R.string.label_controls_mode),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            SteeringMode.entries.forEach { mode ->
                                FilterChip(
                                    selected = playerState.steeringMode == mode,
                                    onClick = { onToggleSteering(mode) },
                                    label = { Text(mode.label, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = if (mode == SteeringMode.GYROSCOPE_TILT)
                                                Icons.Default.ScreenRotation else Icons.Default.TouchApp,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = CyanTelemetry.copy(alpha = 0.18f),
                                        selectedLabelColor = CyanTelemetry,
                                        selectedLeadingIconColor = CyanTelemetry
                                    ),
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("steering_chip_${mode.name}")
                                )
                            }
                        }
                    }
                }

                // Primary Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onOpenGarage,
                        border = BorderStroke(1.5.dp, CyanTelemetry),
                        shape = MaterialTheme.shapes.medium,
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp)
                            .testTag("garage_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.TwoWheeler,
                            contentDescription = null,
                            tint = CyanTelemetry
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = stringResource(R.string.action_garage),
                            color = CyanTelemetry,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Button(
                        onClick = onStartRide,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = RacingAmber,
                            contentColor = ObsidianBg
                        ),
                        shape = MaterialTheme.shapes.medium,
                        modifier = Modifier
                            .weight(1.15f)
                            .height(56.dp)
                            .testTag("ride_now_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = stringResource(R.string.action_ride_now),
                            style = MaterialTheme.typography.titleLarge,
                            maxLines = 1
                        )
                    }
                }

                // Quick Best Stats Strip
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp, bottom = 12.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    MiniRecord(Icons.Default.EmojiEvents, "BEST SCORE", "${playerState.bestScore}")
                    MiniRecord(Icons.Default.Speed, "BEST DIST", String.format("%.2f km", playerState.bestDistanceKm))
                    MiniRecord(Icons.Default.DirectionsBike, "OVERTAKES", "${playerState.totalOvertakes}")
                }
            }
        }
    }
}

@Composable
private fun StatPill(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.Start) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.labelMedium,
            color = color
        )
    }
}

@Composable
private fun MiniRecord(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = RacingAmber, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Column {
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}
