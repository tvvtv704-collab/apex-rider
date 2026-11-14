package com.example.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddCircleOutline
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.local.UpgradeStatType
import com.example.domain.model.BikeCategory
import com.example.domain.model.BikeSpec
import com.example.domain.model.BikeUiModel
import com.example.domain.model.PlayerState
import com.example.ui.components.BikeIllustrationCanvas
import com.example.ui.theme.AsphaltSurfaceVariant
import com.example.ui.theme.CarbonSurface
import com.example.ui.theme.CyanTelemetry
import com.example.ui.theme.EmeraldCash
import com.example.ui.theme.ObsidianBg
import com.example.ui.theme.RacingAmber
import com.example.ui.theme.SteelBorder

@Composable
fun GarageScreen(
    playerState: PlayerState,
    onBack: () -> Unit,
    onBuyBike: (BikeSpec) -> Unit,
    onSelectBike: (String) -> Unit,
    onUpgradeStat: (BikeSpec, UpgradeStatType) -> Unit,
    onRideWithSelected: () -> Unit
) {
    var selectedCategory by remember { mutableStateOf<BikeCategory?>(null) }
    var inspectedBikeId by remember(playerState.selectedBikeId) {
        mutableStateOf(playerState.selectedBikeId)
    }

    val filteredBikes = remember(playerState.bikes, selectedCategory) {
        if (selectedCategory == null) playerState.bikes
        else playerState.bikes.filter { it.spec.category == selectedCategory }
    }

    val inspectedBike = remember(playerState.bikes, inspectedBikeId, filteredBikes) {
        playerState.bikes.firstOrNull { it.spec.id == inspectedBikeId }
            ?: filteredBikes.firstOrNull()
            ?: playerState.selectedBike
    }

    val unlockedCount = remember(playerState.bikes) {
        playerState.bikes.count { it.isUnlocked }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ObsidianBg)
            .statusBarsPadding()
            .navigationBarsPadding(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 760.dp)
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            // Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("garage_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.cd_back)
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Column {
                        Text(
                            text = stringResource(R.string.label_garage_title),
                            style = MaterialTheme.typography.titleLarge
                        )
                        Text(
                            text = "$unlockedCount / ${playerState.bikes.size} UNLOCKED",
                            style = MaterialTheme.typography.labelSmall,
                            color = CyanTelemetry
                        )
                    }
                }

                Surface(
                    color = CarbonSurface,
                    shape = RoundedCornerShape(50),
                    border = BorderStroke(1.dp, EmeraldCash.copy(alpha = 0.6f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.AttachMoney, contentDescription = null, tint = EmeraldCash, modifier = Modifier.size(18.dp))
                        Text(
                            text = "${playerState.cash}",
                            style = MaterialTheme.typography.labelLarge,
                            color = EmeraldCash
                        )
                    }
                }
            }

            // Category Filter Chips
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 4.dp)
            ) {
                item {
                    FilterChip(
                        selected = selectedCategory == null,
                        onClick = { selectedCategory = null },
                        label = { Text("All (${playerState.bikes.size})") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = RacingAmber.copy(alpha = 0.22f),
                            selectedLabelColor = RacingAmber
                        ),
                        modifier = Modifier.testTag("cat_all")
                    )
                }
                items(BikeCategory.entries) { cat ->
                    val count = playerState.bikes.count { it.spec.category == cat }
                    FilterChip(
                        selected = selectedCategory == cat,
                        onClick = {
                            selectedCategory = cat
                            val firstInCat = playerState.bikes.firstOrNull { it.spec.category == cat }
                            if (firstInCat != null) inspectedBikeId = firstInCat.spec.id
                        },
                        label = { Text("${cat.displayName} ($count)") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = RacingAmber.copy(alpha = 0.22f),
                            selectedLabelColor = RacingAmber
                        ),
                        modifier = Modifier.testTag("cat_${cat.name}")
                    )
                }
            }

            // Inspected Bike Stage + Upgrade Panel
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (inspectedBike != null) {
                    BikeShowcasePanel(
                        bike = inspectedBike,
                        cash = playerState.cash,
                        onUpgradeStat = { stat -> onUpgradeStat(inspectedBike.spec, stat) }
                    )
                }

                Text(
                    text = "SELECT MOTORCYCLE (${filteredBikes.size})",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 8.dp)
                ) {
                    items(filteredBikes, key = { it.spec.id }) { bike ->
                        val isInspected = inspectedBike?.spec?.id == bike.spec.id
                        BikeThumbCard(
                            bike = bike,
                            isInspected = isInspected,
                            onClick = { inspectedBikeId = bike.spec.id }
                        )
                    }
                }
            }

            // Bottom Buy / Equip / Ride Action Bar
            if (inspectedBike != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (!inspectedBike.isUnlocked) {
                        val canAfford = playerState.cash >= inspectedBike.spec.price
                        Button(
                            onClick = { onBuyBike(inspectedBike.spec) },
                            enabled = canAfford,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = EmeraldCash,
                                contentColor = ObsidianBg,
                                disabledContainerColor = AsphaltSurfaceVariant,
                                disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            shape = MaterialTheme.shapes.medium,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(54.dp)
                                .testTag("buy_bike_button")
                        ) {
                            Icon(Icons.Default.Lock, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (canAfford) {
                                    "UNLOCK FOR $${inspectedBike.spec.price}"
                                } else {
                                    "NEED $${inspectedBike.spec.price} TO UNLOCK"
                                },
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    } else if (!inspectedBike.isSelected) {
                        OutlinedButton(
                            onClick = { onSelectBike(inspectedBike.spec.id) },
                            border = BorderStroke(1.5.dp, CyanTelemetry),
                            shape = MaterialTheme.shapes.medium,
                            modifier = Modifier
                                .weight(1f)
                                .height(54.dp)
                                .testTag("equip_bike_button")
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = CyanTelemetry)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("EQUIP BIKE", color = CyanTelemetry)
                        }
                        Button(
                            onClick = {
                                onSelectBike(inspectedBike.spec.id)
                                onRideWithSelected()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = RacingAmber, contentColor = ObsidianBg),
                            shape = MaterialTheme.shapes.medium,
                            modifier = Modifier
                                .weight(1f)
                                .height(54.dp)
                                .testTag("equip_and_ride_button")
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("RIDE NOW", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }
                    } else {
                        Button(
                            onClick = onRideWithSelected,
                            colors = ButtonDefaults.buttonColors(containerColor = RacingAmber, contentColor = ObsidianBg),
                            shape = MaterialTheme.shapes.medium,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(54.dp)
                                .testTag("garage_ride_now_button")
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("EQUIPPED — RIDE HIGHWAY", style = MaterialTheme.typography.titleLarge)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BikeShowcasePanel(
    bike: BikeUiModel,
    cash: Int,
    onUpgradeStat: (UpgradeStatType) -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = CarbonSurface),
        border = BorderStroke(1.dp, SteelBorder),
        shape = MaterialTheme.shapes.large,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "${bike.spec.category.displayName.uppercase()} • ${bike.spec.engineSpec}",
                        style = MaterialTheme.typography.labelSmall,
                        color = CyanTelemetry
                    )
                    Text(
                        text = bike.spec.name,
                        style = MaterialTheme.typography.headlineMedium
                    )
                    Text(
                        text = bike.spec.subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (bike.isSelected) {
                    Surface(color = RacingAmber.copy(alpha = 0.2f), shape = RoundedCornerShape(8.dp)) {
                        Text(
                            text = "EQUIPPED",
                            style = MaterialTheme.typography.labelSmall,
                            color = RacingAmber,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                } else if (!bike.isUnlocked) {
                    Surface(color = AsphaltSurfaceVariant, shape = RoundedCornerShape(8.dp)) {
                        Text(
                            text = "$${bike.spec.price}",
                            style = MaterialTheme.typography.labelMedium,
                            color = EmeraldCash,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            BikeIllustrationCanvas(
                spec = bike.spec,
                isLocked = !bike.isUnlocked,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(145.dp)
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Stat Rows with Upgrade buttons
            UpgradeStatRow(
                label = "SPEED",
                valueLabel = "${bike.currentTopSpeedKmh} km/h",
                fraction = bike.currentTopSpeedKmh / 400f,
                level = bike.speedLevel,
                cost = bike.spec.upgradeCost(bike.speedLevel),
                isUnlocked = bike.isUnlocked,
                canAfford = cash >= bike.spec.upgradeCost(bike.speedLevel),
                barColor = RacingAmber,
                tag = "upgrade_speed_button",
                onUpgrade = { onUpgradeStat(UpgradeStatType.SPEED) }
            )
            Spacer(modifier = Modifier.height(8.dp))
            UpgradeStatRow(
                label = "HANDLING",
                valueLabel = "${bike.currentHandling}/100",
                fraction = bike.currentHandling / 100f,
                level = bike.handlingLevel,
                cost = bike.spec.upgradeCost(bike.handlingLevel),
                isUnlocked = bike.isUnlocked,
                canAfford = cash >= bike.spec.upgradeCost(bike.handlingLevel),
                barColor = CyanTelemetry,
                tag = "upgrade_handling_button",
                onUpgrade = { onUpgradeStat(UpgradeStatType.HANDLING) }
            )
            Spacer(modifier = Modifier.height(8.dp))
            UpgradeStatRow(
                label = "BRAKING",
                valueLabel = "${bike.currentBraking}/100",
                fraction = bike.currentBraking / 100f,
                level = bike.brakingLevel,
                cost = bike.spec.upgradeCost(bike.brakingLevel),
                isUnlocked = bike.isUnlocked,
                canAfford = cash >= bike.spec.upgradeCost(bike.brakingLevel),
                barColor = EmeraldCash,
                tag = "upgrade_braking_button",
                onUpgrade = { onUpgradeStat(UpgradeStatType.BRAKING) }
            )
        }
    }
}

@Composable
private fun UpgradeStatRow(
    label: String,
    valueLabel: String,
    fraction: Float,
    level: Int,
    cost: Int,
    isUnlocked: Boolean,
    canAfford: Boolean,
    barColor: Color,
    tag: String,
    onUpgrade: () -> Unit
) {
    val animatedFraction by animateFloatAsState(targetValue = fraction.coerceIn(0.05f, 1f), label = label)
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "$label  •  LV $level/${BikeSpec.MAX_UPGRADE_LEVEL}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = valueLabel,
                    style = MaterialTheme.typography.labelMedium,
                    color = barColor
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            LinearProgressIndicator(
                progress = { animatedFraction },
                color = barColor,
                trackColor = AsphaltSurfaceVariant,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
            )
        }

        if (isUnlocked) {
            val isMax = level >= BikeSpec.MAX_UPGRADE_LEVEL
            OutlinedButton(
                onClick = onUpgrade,
                enabled = !isMax && canAfford,
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                border = BorderStroke(
                    1.dp,
                    if (!isMax && canAfford) barColor else SteelBorder
                ),
                modifier = Modifier
                    .height(48.dp)
                    .testTag(tag)
            ) {
                if (isMax) {
                    Text("MAX", style = MaterialTheme.typography.labelSmall, color = barColor)
                } else {
                    Icon(Icons.Default.AddCircleOutline, contentDescription = null, modifier = Modifier.size(15.dp), tint = if (canAfford) barColor else SteelBorder)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("$$cost", style = MaterialTheme.typography.labelSmall, color = if (canAfford) barColor else MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
private fun BikeThumbCard(
    bike: BikeUiModel,
    isInspected: Boolean,
    onClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (isInspected) AsphaltSurfaceVariant else CarbonSurface
        ),
        border = BorderStroke(
            width = if (isInspected) 2.dp else 1.dp,
            color = if (isInspected) RacingAmber else SteelBorder
        ),
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier
            .width(156.dp)
            .clickable(onClick = onClick)
            .testTag("bike_card_${bike.spec.id}")
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${bike.currentTopSpeedKmh} km/h",
                    style = MaterialTheme.typography.labelSmall,
                    color = RacingAmber
                )
                Icon(
                    imageVector = when {
                        bike.isSelected -> Icons.Default.CheckCircle
                        bike.isUnlocked -> Icons.Default.CheckCircle
                        else -> Icons.Default.Lock
                    },
                    contentDescription = null,
                    tint = when {
                        bike.isSelected -> RacingAmber
                        bike.isUnlocked -> EmeraldCash
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    modifier = Modifier.size(15.dp)
                )
            }
            BikeIllustrationCanvas(
                spec = bike.spec,
                isLocked = !bike.isUnlocked,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
            )
            Text(
                text = bike.spec.name,
                style = MaterialTheme.typography.labelLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = if (bike.isUnlocked) "OWNED" else "$${bike.spec.price}",
                style = MaterialTheme.typography.labelSmall,
                color = if (bike.isUnlocked) EmeraldCash else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
