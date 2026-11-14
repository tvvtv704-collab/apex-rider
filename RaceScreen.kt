package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CenterFocusStrong
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.ScreenRotation
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.TwoWheeler
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.domain.model.BikeUiModel
import com.example.domain.model.OvertakePopup
import com.example.domain.model.PlayerState
import com.example.domain.model.RideResult
import com.example.domain.model.Snowflake
import com.example.domain.model.SteeringMode
import com.example.domain.model.TrafficVehicle
import com.example.game.RaceEngine
import com.example.game.TiltSensorManager
import com.example.ui.game.FirstPersonRaceCanvas
import com.example.ui.theme.AsphaltSurfaceVariant
import com.example.ui.theme.CarbonSurface
import com.example.ui.theme.CrimsonRedline
import com.example.ui.theme.CyanTelemetry
import com.example.ui.theme.EmeraldCash
import com.example.ui.theme.ObsidianBg
import com.example.ui.theme.RacingAmber
import com.example.ui.theme.SteelBorder

private data class RaceHudSnapshot(
    val speedKmh: Float = 45f,
    val playerX: Float = 0.45f,
    val leanAngleDeg: Float = 0f,
    val suspensionBob: Float = 0f,
    val roadScroll: Float = 0f,
    val rpmRatio: Float = 0.35f,
    val gear: Int = 2,
    val distanceKm: Float = 0f,
    val score: Int = 0,
    val cashEarned: Int = 0,
    val overtakes: Int = 0,
    val crashShake: Float = 0f,
    val isBraking: Boolean = false,
    val isThrottle: Boolean = false,
    val vehicles: List<TrafficVehicle> = emptyList(),
    val popups: List<OvertakePopup> = emptyList(),
    val snowflakes: List<Snowflake> = emptyList()
)

@Composable
fun RaceScreen(
    playerState: PlayerState,
    onToggleSteering: (SteeringMode) -> Unit,
    onRideFinished: (RideResult) -> Unit,
    onExitToMenu: () -> Unit,
    onExitToGarage: () -> Unit
) {
    val bike: BikeUiModel = playerState.selectedBike ?: return
    val context = LocalContext.current
    val haptics = LocalHapticFeedback.current

    var runCounter by remember { mutableIntStateOf(0) }
    var isPaused by remember { mutableStateOf(false) }
    var rideResult by remember(runCounter) { mutableStateOf<RideResult?>(null) }

    val engine = remember(runCounter, bike.spec.id, playerState.selectedMap, playerState.trafficMode) {
        RaceEngine(
            bike = bike,
            map = playerState.selectedMap,
            trafficMode = playerState.trafficMode
        )
    }

    var throttleHeld by remember { mutableStateOf(false) }
    var brakeHeld by remember { mutableStateOf(false) }
    var leftHeld by remember { mutableStateOf(false) }
    var rightHeld by remember { mutableStateOf(false) }
    var lastOvertakeCount by remember(runCounter) { mutableIntStateOf(0) }

    val tiltManager = remember(context) { TiltSensorManager(context) }
    val gyroSteer by tiltManager.steerInput.collectAsStateWithLifecycle()

    DisposableEffect(playerState.steeringMode, isPaused, rideResult) {
        if (playerState.steeringMode == SteeringMode.GYROSCOPE_TILT && !isPaused && rideResult == null) {
            tiltManager.start()
        } else {
            tiltManager.stop()
        }
        onDispose { tiltManager.stop() }
    }

    var snapshot by remember(runCounter) {
        mutableStateOf(
            RaceHudSnapshot(
                vehicles = engine.vehicles.toList(),
                snowflakes = engine.snowflakes.toList()
            )
        )
    }

    BackHandler(enabled = rideResult == null) {
        isPaused = !isPaused
    }

    LaunchedEffect(runCounter, isPaused, rideResult) {
        if (isPaused) return@LaunchedEffect
        var lastFrameNanos = 0L
        while (true) {
            withFrameNanos { frameNanos ->
                if (lastFrameNanos == 0L) {
                    lastFrameNanos = frameNanos
                    return@withFrameNanos
                }
                val dt = (frameNanos - lastFrameNanos) / 1_000_000_000f
                lastFrameNanos = frameNanos

                val touchSteer = when {
                    leftHeld && !rightHeld -> -1f
                    rightHeld && !leftHeld -> 1f
                    else -> 0f
                }
                val steer = if (playerState.steeringMode == SteeringMode.GYROSCOPE_TILT) {
                    (gyroSteer + touchSteer).coerceIn(-1f, 1f)
                } else {
                    touchSteer
                }

                val crashedNow = engine.step(
                    dtSec = dt,
                    throttlePressed = throttleHeld,
                    brakePressed = brakeHeld,
                    steerInput = steer
                )

                if (engine.overtakes > lastOvertakeCount) {
                    lastOvertakeCount = engine.overtakes
                    haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                }

                if (crashedNow && rideResult == null) {
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    val result = engine.buildResult()
                    rideResult = result
                    onRideFinished(result)
                }

                snapshot = RaceHudSnapshot(
                    speedKmh = engine.speedKmh,
                    playerX = engine.playerX,
                    leanAngleDeg = engine.leanAngleDeg,
                    suspensionBob = engine.suspensionBob,
                    roadScroll = engine.roadScroll,
                    rpmRatio = engine.rpmRatio,
                    gear = engine.gear,
                    distanceKm = engine.distanceMeters / 1000f,
                    score = engine.score.toInt(),
                    cashEarned = engine.sessionCash.toInt(),
                    overtakes = engine.overtakes,
                    crashShake = engine.crashShake,
                    isBraking = engine.isBrakingVisual,
                    isThrottle = engine.isThrottleVisual,
                    vehicles = engine.vehicles.toList(),
                    popups = engine.popups.toList(),
                    snowflakes = engine.snowflakes.toList()
                )
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // 60FPS First-Person 3D Road & Cockpit Renderer
        FirstPersonRaceCanvas(
            bike = bike,
            map = playerState.selectedMap,
            trafficMode = playerState.trafficMode,
            playerX = snapshot.playerX,
            leanAngleDeg = snapshot.leanAngleDeg,
            suspensionBob = snapshot.suspensionBob,
            roadScroll = snapshot.roadScroll,
            speedKmh = snapshot.speedKmh,
            rpmRatio = snapshot.rpmRatio,
            gear = snapshot.gear,
            isBraking = snapshot.isBraking,
            isThrottle = snapshot.isThrottle,
            crashShake = snapshot.crashShake,
            vehicles = snapshot.vehicles,
            snowflakes = snapshot.snowflakes,
            modifier = Modifier.fillMaxSize()
        )

        // Top Telemetry HUD
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Speed & 100+ Bonus Card
                Surface(
                    color = ObsidianBg.copy(alpha = 0.78f),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(
                        1.5.dp,
                        if (snapshot.speedKmh >= 100f) RacingAmber else SteelBorder
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${snapshot.speedKmh.toInt()}",
                            style = MaterialTheme.typography.headlineMedium,
                            color = if (snapshot.speedKmh >= 100f) RacingAmber else Color.White
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Column {
                            Text("KM/H", style = MaterialTheme.typography.labelSmall, color = CyanTelemetry)
                            if (snapshot.speedKmh >= 100f) {
                                Text("100+ BONUS", style = MaterialTheme.typography.labelSmall, color = RacingAmber)
                            }
                        }
                    }
                }

                // Distance, Score, Cash
                Surface(
                    color = ObsidianBg.copy(alpha = 0.78f),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, SteelBorder)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        HudMetric("DIST", String.format("%.2f km", snapshot.distanceKm), Color.White)
                        HudMetric("SCORE", "${snapshot.score}", CyanTelemetry)
                        HudMetric("CASH", "+$${snapshot.cashEarned}", EmeraldCash)
                    }
                }

                // Pause Button
                Surface(
                    color = ObsidianBg.copy(alpha = 0.8f),
                    shape = CircleShape,
                    border = BorderStroke(1.dp, SteelBorder)
                ) {
                    IconButton(
                        onClick = { if (rideResult == null) isPaused = true },
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("pause_race_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Pause,
                            contentDescription = stringResource(R.string.cd_pause_race),
                            tint = Color.White
                        )
                    }
                }
            }

            // Close Overtake / Combo Popup Banner
            val latestPopup = snapshot.popups.lastOrNull()
            AnimatedVisibility(
                visible = latestPopup != null,
                enter = fadeIn() + scaleIn(),
                exit = fadeOut(),
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                if (latestPopup != null) {
                    Surface(
                        color = RacingAmber,
                        shape = RoundedCornerShape(50),
                        shadowElevation = 6.dp
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Bolt, contentDescription = null, tint = ObsidianBg, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${latestPopup.title} +$${latestPopup.cashBonus}" +
                                    if (latestPopup.comboCount > 1) "  x${latestPopup.comboCount}" else "",
                                style = MaterialTheme.typography.labelLarge,
                                color = ObsidianBg
                            )
                        }
                    }
                }
            }
        }

        // Bottom Multi-Touch Riding Controls
        if (rideResult == null && !isPaused) {
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                // Left Side: Steering (Touch Left/Right OR Gyroscope Calibrate) + Brake
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.Bottom
                ) {
                    HoldPedalButton(
                        label = "BRAKE",
                        isActive = brakeHeld,
                        activeColor = CrimsonRedline,
                        onPressChange = { brakeHeld = it },
                        modifier = Modifier
                            .size(width = 76.dp, height = 84.dp)
                            .testTag("brake_pedal")
                    )

                    if (playerState.steeringMode == SteeringMode.TOUCH_BUTTONS) {
                        HoldSteerButton(
                            icon = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                            contentDescription = stringResource(R.string.cd_steer_left),
                            isActive = leftHeld,
                            onPressChange = { leftHeld = it },
                            modifier = Modifier
                                .size(68.dp)
                                .testTag("steer_left_button")
                        )
                        HoldSteerButton(
                            icon = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = stringResource(R.string.cd_steer_right),
                            isActive = rightHeld,
                            onPressChange = { rightHeld = it },
                            modifier = Modifier
                                .size(68.dp)
                                .testTag("steer_right_button")
                        )
                    } else {
                        Surface(
                            color = ObsidianBg.copy(alpha = 0.78f),
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(1.dp, CyanTelemetry.copy(alpha = 0.6f))
                        ) {
                            IconButton(
                                onClick = { tiltManager.calibrateZero() },
                                modifier = Modifier
                                    .size(56.dp)
                                    .testTag("calibrate_gyro_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CenterFocusStrong,
                                    contentDescription = stringResource(R.string.cd_calibrate_gyro),
                                    tint = CyanTelemetry
                                )
                            }
                        }
                    }
                }

                // Right Side: Throttle Pedal
                HoldPedalButton(
                    label = "GAS",
                    isActive = throttleHeld,
                    activeColor = EmeraldCash,
                    onPressChange = { throttleHeld = it },
                    modifier = Modifier
                        .size(width = 86.dp, height = 104.dp)
                        .testTag("throttle_pedal")
                )
            }
        }

        // Pause Overlay
        if (isPaused && rideResult == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(ObsidianBg.copy(alpha = 0.82f)),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = CarbonSurface),
                    border = BorderStroke(1.5.dp, SteelBorder),
                    shape = MaterialTheme.shapes.large,
                    modifier = Modifier
                        .widthIn(max = 380.dp)
                        .fillMaxWidth()
                        .padding(24.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(22.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text("RIDE PAUSED", style = MaterialTheme.typography.headlineMedium, color = RacingAmber)
                        Text(
                            text = "${bike.spec.name} • ${playerState.selectedMap.title}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        // Quick Control Mode Toggle inside Pause
                        OutlinedButton(
                            onClick = {
                                val next = if (playerState.steeringMode == SteeringMode.TOUCH_BUTTONS) {
                                    SteeringMode.GYROSCOPE_TILT
                                } else {
                                    SteeringMode.TOUCH_BUTTONS
                                }
                                onToggleSteering(next)
                            },
                            border = BorderStroke(1.dp, CyanTelemetry),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                        ) {
                            Icon(
                                imageVector = if (playerState.steeringMode == SteeringMode.GYROSCOPE_TILT)
                                    Icons.Default.ScreenRotation else Icons.Default.TouchApp,
                                contentDescription = null,
                                tint = CyanTelemetry
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Steering: ${playerState.steeringMode.label}", color = CyanTelemetry)
                        }

                        Button(
                            onClick = { isPaused = false },
                            colors = ButtonDefaults.buttonColors(containerColor = RacingAmber, contentColor = ObsidianBg),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("resume_race_button")
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("RESUME RIDE", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    isPaused = false
                                    runCounter++
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                            ) {
                                Text("RESTART")
                            }
                            OutlinedButton(
                                onClick = onExitToMenu,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                            ) {
                                Text("MENU")
                            }
                        }
                    }
                }
            }
        }

        // Crash / Run Summary Modal
        rideResult?.let { result ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(ObsidianBg.copy(alpha = 0.88f)),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = CarbonSurface),
                    border = BorderStroke(1.5.dp, CrimsonRedline.copy(alpha = 0.7f)),
                    shape = MaterialTheme.shapes.large,
                    modifier = Modifier
                        .widthIn(max = 420.dp)
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = CrimsonRedline, modifier = Modifier.size(26.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("WIPEOUT!", style = MaterialTheme.typography.displayMedium, color = CrimsonRedline)
                        }
                        Text(
                            text = result.crashReason,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Surface(
                            color = AsphaltSurfaceVariant,
                            shape = MaterialTheme.shapes.medium,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                SummaryStatRow("Distance Covered", String.format("%.2f km", result.distanceKm), Color.White)
                                SummaryStatRow("Top Speed", "${result.topSpeedKmh} km/h", RacingAmber)
                                SummaryStatRow("Close Overtakes", "${result.overtakes}", CyanTelemetry)
                                SummaryStatRow("100+ km/h Time", String.format("%.1f sec", result.highSpeedSeconds), RacingAmber)
                                SummaryStatRow("Total Score", "${result.score}", CyanTelemetry)
                                SummaryStatRow("Cash Earned", "+$${result.cashEarned}", EmeraldCash)
                            }
                        }

                        Button(
                            onClick = {
                                rideResult = null
                                runCounter++
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = RacingAmber, contentColor = ObsidianBg),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .testTag("ride_again_button")
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("RIDE AGAIN", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedButton(
                                onClick = onExitToGarage,
                                border = BorderStroke(1.dp, CyanTelemetry),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                            ) {
                                Icon(Icons.Default.TwoWheeler, contentDescription = null, tint = CyanTelemetry, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("GARAGE", color = CyanTelemetry)
                            }
                            OutlinedButton(
                                onClick = onExitToMenu,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                            ) {
                                Text("MENU")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HudMetric(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.labelMedium, color = color)
    }
}

@Composable
private fun SummaryStatRow(label: String, value: String, valueColor: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.labelLarge, color = valueColor)
    }
}

@Composable
private fun HoldPedalButton(
    label: String,
    isActive: Boolean,
    activeColor: Color,
    onPressChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = if (isActive) activeColor.copy(alpha = 0.85f) else ObsidianBg.copy(alpha = 0.72f),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(2.dp, if (isActive) activeColor else activeColor.copy(alpha = 0.6f)),
        modifier = modifier.pointerInput(Unit) {
            awaitEachGesture {
                awaitFirstDown(requireUnconsumed = false)
                onPressChange(true)
                try {
                    waitForUpOrCancellation()
                } finally {
                    onPressChange(false)
                }
            }
        }
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = if (isActive) ObsidianBg else activeColor
            )
        }
    }
}

@Composable
private fun HoldSteerButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String,
    isActive: Boolean,
    onPressChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = if (isActive) CyanTelemetry.copy(alpha = 0.85f) else ObsidianBg.copy(alpha = 0.72f),
        shape = CircleShape,
        border = BorderStroke(2.dp, if (isActive) CyanTelemetry else CyanTelemetry.copy(alpha = 0.65f)),
        modifier = modifier.pointerInput(Unit) {
            awaitEachGesture {
                awaitFirstDown(requireUnconsumed = false)
                onPressChange(true)
                try {
                    waitForUpOrCancellation()
                } finally {
                    onPressChange(false)
                }
            }
        }
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = if (isActive) ObsidianBg else CyanTelemetry,
                modifier = Modifier.size(34.dp)
            )
        }
    }
}
