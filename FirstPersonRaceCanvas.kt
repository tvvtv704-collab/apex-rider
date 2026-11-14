package com.example.ui.game

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.sp
import com.example.domain.model.BikeUiModel
import com.example.domain.model.HandlebarStyle
import com.example.domain.model.HighwayMap
import com.example.domain.model.Snowflake
import com.example.domain.model.TrafficMode
import com.example.domain.model.TrafficVehicle
import com.example.domain.model.TrafficVehicleType
import com.example.domain.model.WeatherCondition
import com.example.ui.theme.CrimsonRedline
import com.example.ui.theme.CyanTelemetry
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.RacingAmber
import com.example.ui.theme.RajdhaniFontFamily
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

@Composable
fun FirstPersonRaceCanvas(
    bike: BikeUiModel,
    map: HighwayMap,
    trafficMode: TrafficMode,
    playerX: Float,
    leanAngleDeg: Float,
    suspensionBob: Float,
    roadScroll: Float,
    speedKmh: Float,
    rpmRatio: Float,
    gear: Int,
    isBraking: Boolean,
    isThrottle: Boolean,
    crashShake: Float,
    vehicles: List<TrafficVehicle>,
    snowflakes: List<Snowflake>,
    modifier: Modifier = Modifier
) {
    val textMeasurer = rememberTextMeasurer()
    val shakeJitter = remember(crashShake) {
        if (crashShake > 0.01f) {
            Offset(
                (Random.nextFloat() - 0.5f) * 28f * crashShake,
                (Random.nextFloat() - 0.5f) * 28f * crashShake
            )
        } else {
            Offset.Zero
        }
    }

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val horizonY = h * 0.37f
        val vanishingX = w * 0.50f - playerX * w * 0.04f
        val roadHalfWBottom = w * 0.68f

        withTransform({
            translate(left = shakeJitter.x, top = shakeJitter.y)
        }) {
            // 1. Sky / Tunnel Ceiling
            drawEnvironmentSky(map, w, h, horizonY, vanishingX, roadScroll)

            // 2. Shoulder Ground
            drawRect(
                color = map.shoulderColor,
                topLeft = Offset(0f, horizonY),
                size = Size(w, h - horizonY)
            )

            // 3. Perspective 4-Lane Asphalt Road
            drawPerspectiveRoad(
                map = map,
                trafficMode = trafficMode,
                w = w,
                h = h,
                horizonY = horizonY,
                vanishingX = vanishingX,
                playerX = playerX,
                roadHalfWBottom = roadHalfWBottom,
                roadScroll = roadScroll
            )

            // 4. Roadside Props (Trees, Streetlamps, Tunnel Ribs)
            drawRoadsideProps(
                map = map,
                w = w,
                h = h,
                horizonY = horizonY,
                vanishingX = vanishingX,
                playerX = playerX,
                roadHalfWBottom = roadHalfWBottom,
                roadScroll = roadScroll
            )

            // 5. Headlight Cone in Night / Tunnel
            if (map.weather == WeatherCondition.NIGHT || map.weather == WeatherCondition.TUNNEL) {
                val beamPath = Path().apply {
                    moveTo(w * 0.44f, h * 0.85f)
                    lineTo(w * 0.30f, horizonY + (h - horizonY) * 0.08f)
                    lineTo(w * 0.70f, horizonY + (h - horizonY) * 0.08f)
                    lineTo(w * 0.56f, h * 0.85f)
                    close()
                }
                drawPath(
                    path = beamPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color(0xFFFFF8E1).copy(alpha = 0.16f)
                        ),
                        startY = horizonY,
                        endY = h * 0.85f
                    )
                )
            }

            // 6. Traffic Vehicles (sorted far to near)
            val sortedVehicles = vehicles.sortedByDescending { it.zPos }
            for (v in sortedVehicles) {
                if (v.zPos in 0.0f..1.0f) {
                    drawTrafficVehicle(
                        v = v,
                        map = map,
                        w = w,
                        h = h,
                        horizonY = horizonY,
                        vanishingX = vanishingX,
                        playerX = playerX,
                        roadHalfWBottom = roadHalfWBottom
                    )
                }
            }

            // 7. Snowfall Weather Particles
            if (map.weather == WeatherCondition.SNOW) {
                for (flake in snowflakes) {
                    drawCircle(
                        color = Color.White.copy(alpha = 0.82f),
                        radius = flake.sizeDp * density * 0.5f,
                        center = Offset(flake.x * w, flake.y * h)
                    )
                }
            }

            // 8. Speed Wind Lines above 120 km/h
            if (speedKmh > 120f) {
                val intensity = ((speedKmh - 120f) / 180f).coerceIn(0f, 1f)
                for (i in 0 until 8) {
                    val angle = (i * 45f + (roadScroll * 360f)) % 360f
                    val rad = Math.toRadians(angle.toDouble())
                    val rStart = w * 0.36f
                    val rEnd = w * (0.48f + intensity * 0.10f)
                    val cx = w * 0.5f
                    val cy = horizonY * 0.95f
                    drawLine(
                        color = Color.White.copy(alpha = 0.14f * intensity),
                        start = Offset(cx + cos(rad).toFloat() * rStart, cy + sin(rad).toFloat() * rStart * 0.6f),
                        end = Offset(cx + cos(rad).toFloat() * rEnd, cy + sin(rad).toFloat() * rEnd * 0.6f),
                        strokeWidth = 2.2f
                    )
                }
            }

            // 9. First-Person Motorcycle Cockpit (Handlebars, Gloved Hands, Mirrors, Dash)
            drawFirstPersonCockpit(
                bike = bike,
                map = map,
                leanAngleDeg = leanAngleDeg,
                suspensionBob = suspensionBob,
                speedKmh = speedKmh,
                rpmRatio = rpmRatio,
                gear = gear,
                isBraking = isBraking,
                isThrottle = isThrottle,
                roadScroll = roadScroll,
                vehicles = vehicles,
                textMeasurer = textMeasurer,
                w = w,
                h = h
            )
        }
    }
}

private fun projectDepth(z: Float): Float = (0.042f / (z + 0.042f)).coerceIn(0f, 1.25f)

private fun DrawScope.drawEnvironmentSky(
    map: HighwayMap,
    w: Float,
    h: Float,
    horizonY: Float,
    vanishingX: Float,
    roadScroll: Float
) {
    drawRect(
        brush = Brush.verticalGradient(
            colors = listOf(map.skyTop, map.skyBottom),
            startY = 0f,
            endY = horizonY
        ),
        size = Size(w, horizonY)
    )

    when (map) {
        HighwayMap.DAYLIGHT_HIGHWAY -> {
            // Sun + distant hills
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFFFFF59D), Color(0xFFFFCA28).copy(alpha = 0.1f), Color.Transparent),
                    center = Offset(w * 0.78f, h * 0.16f),
                    radius = h * 0.14f
                ),
                radius = h * 0.14f,
                center = Offset(w * 0.78f, h * 0.16f)
            )
            drawCircle(
                color = Color(0xFFFFFDE7),
                radius = h * 0.045f,
                center = Offset(w * 0.78f, h * 0.16f)
            )
            drawPath(
                path = Path().apply {
                    moveTo(0f, horizonY)
                    lineTo(w * 0.18f, horizonY - h * 0.09f)
                    lineTo(w * 0.42f, horizonY - h * 0.03f)
                    lineTo(w * 0.68f, horizonY - h * 0.11f)
                    lineTo(w * 0.90f, horizonY - h * 0.05f)
                    lineTo(w, horizonY)
                    close()
                },
                color = Color(0xFF285943).copy(alpha = 0.7f)
            )
        }

        HighwayMap.SNOWY_MOUNTAIN -> {
            drawPath(
                path = Path().apply {
                    moveTo(0f, horizonY)
                    lineTo(w * 0.15f, horizonY - h * 0.15f)
                    lineTo(w * 0.34f, horizonY - h * 0.05f)
                    lineTo(w * 0.56f, horizonY - h * 0.18f)
                    lineTo(w * 0.80f, horizonY - h * 0.08f)
                    lineTo(w, horizonY - h * 0.14f)
                    lineTo(w, horizonY)
                    close()
                },
                color = Color(0xFF546E7A)
            )
            // Snow caps
            drawPath(
                path = Path().apply {
                    moveTo(w * 0.49f, horizonY - h * 0.12f)
                    lineTo(w * 0.56f, horizonY - h * 0.18f)
                    lineTo(w * 0.64f, horizonY - h * 0.11f)
                    close()
                },
                color = Color(0xFFECEFF1)
            )
        }

        HighwayMap.NIGHT_CITY -> {
            // Stars
            val stars = listOf(0.12f to 0.08f, 0.28f to 0.14f, 0.44f to 0.06f, 0.66f to 0.11f, 0.85f to 0.07f)
            stars.forEach { (sx, sy) ->
                drawCircle(Color.White.copy(alpha = 0.65f), radius = 2f, center = Offset(w * sx, h * sy))
            }
            // City skyline towers
            val towers = listOf(
                0.04f to 0.16f, 0.13f to 0.10f, 0.23f to 0.19f, 0.33f to 0.13f,
                0.62f to 0.12f, 0.72f to 0.18f, 0.82f to 0.09f, 0.91f to 0.15f
            )
            towers.forEachIndexed { idx, (tx, th) ->
                val topY = horizonY - h * th
                drawRect(
                    color = Color(0xFF0A0F1D),
                    topLeft = Offset(w * tx, topY),
                    size = Size(w * 0.085f, h * th)
                )
                val winColor = if (idx % 2 == 0) Color(0xFFFFB300) else Color(0xFF00E5FF)
                for (row in 0..3) {
                    drawRect(
                        color = winColor.copy(alpha = 0.55f),
                        topLeft = Offset(w * tx + w * 0.015f, topY + row * h * 0.025f + 6f),
                        size = Size(w * 0.02f, 4f)
                    )
                }
            }
        }

        HighwayMap.TUNNEL_ROAD -> {
            // Dark tunnel ceiling + perspective ribs + sodium strip lights
            for (k in 0 until 8) {
                val z = ((k - roadScroll * 2f) / 8f).let { if (it < 0f) it + 1f else it }
                val d = projectDepth(z)
                val archHalfW = w * (0.14f + d * 0.65f)
                val archTopY = horizonY - h * (0.04f + d * 0.42f)
                val archBaseY = horizonY + (h - horizonY) * d
                drawArc(
                    color = Color(0xFF2B3445),
                    startAngle = 180f,
                    sweepAngle = 180f,
                    useCenter = false,
                    topLeft = Offset(vanishingX - archHalfW, archTopY),
                    size = Size(archHalfW * 2f, (archBaseY - archTopY) * 1.15f),
                    style = Stroke(width = (2f + d * 10f))
                )
                // Overhead glowing strip
                drawLine(
                    color = CyanTelemetry.copy(alpha = (0.35f + d * 0.65f).coerceAtMost(1f)),
                    start = Offset(vanishingX - archHalfW * 0.22f, archTopY + 3f),
                    end = Offset(vanishingX + archHalfW * 0.22f, archTopY + 3f),
                    strokeWidth = (2f + d * 5f),
                    cap = StrokeCap.Round
                )
            }
        }
    }
}

private fun DrawScope.drawPerspectiveRoad(
    map: HighwayMap,
    trafficMode: TrafficMode,
    w: Float,
    h: Float,
    horizonY: Float,
    vanishingX: Float,
    playerX: Float,
    roadHalfWBottom: Float,
    roadScroll: Float
) {
    val segCount = 16
    for (k in 0 until segCount) {
        val rawZ0 = ((k - roadScroll) / segCount).let { if (it < 0f) it + 1f else it }
        val rawZ1 = rawZ0 + (1f / segCount)
        val z0 = rawZ0.coerceIn(0f, 1f)
        val z1 = rawZ1.coerceIn(0f, 1f)

        val d0 = projectDepth(z0)
        val d1 = projectDepth(z1)
        val y0 = horizonY + (h - horizonY) * d0
        val y1 = horizonY + (h - horizonY) * d1
        if (y0 <= y1) continue

        val cx0 = vanishingX - playerX * (roadHalfWBottom / 1.8f) * d0
        val cx1 = vanishingX - playerX * (roadHalfWBottom / 1.8f) * d1
        val hw0 = roadHalfWBottom * d0
        val hw1 = roadHalfWBottom * d1

        val isAltBand = k % 2 == 0
        val asphaltColor = if (isAltBand) map.roadBase else map.roadStripe

        // Road quad
        drawPath(
            path = Path().apply {
                moveTo(cx1 - hw1, y1)
                lineTo(cx1 + hw1, y1)
                lineTo(cx0 + hw0, y0)
                lineTo(cx0 - hw0, y0)
                close()
            },
            color = asphaltColor
        )

        // Shoulder edge lines
        val edgeStroke = (1.5f + d0 * 5f)
        drawLine(Color(0xFFE0E6EE).copy(alpha = 0.85f), Offset(cx1 - hw1 * 0.96f, y1), Offset(cx0 - hw0 * 0.96f, y0), strokeWidth = edgeStroke)
        drawLine(Color(0xFFE0E6EE).copy(alpha = 0.85f), Offset(cx1 + hw1 * 0.96f, y1), Offset(cx0 + hw0 * 0.96f, y0), strokeWidth = edgeStroke)

        // Center divider (solid double amber for Two-Way, dashed white for One-Way)
        if (trafficMode == TrafficMode.TWO_WAY) {
            val gap0 = hw0 * 0.025f
            val gap1 = hw1 * 0.025f
            drawLine(RacingAmber, Offset(cx1 - gap1, y1), Offset(cx0 - gap0, y0), strokeWidth = edgeStroke)
            drawLine(RacingAmber, Offset(cx1 + gap1, y1), Offset(cx0 + gap0, y0), strokeWidth = edgeStroke)
        } else if (isAltBand) {
            drawLine(Color.White.copy(alpha = 0.72f), Offset(cx1, y1), Offset(cx0, y0), strokeWidth = edgeStroke * 0.9f)
        }

        // Left & right lane dividers
        if (isAltBand) {
            drawLine(Color.White.copy(alpha = 0.65f), Offset(cx1 - hw1 * 0.5f, y1), Offset(cx0 - hw0 * 0.5f, y0), strokeWidth = edgeStroke * 0.85f)
            drawLine(Color.White.copy(alpha = 0.65f), Offset(cx1 + hw1 * 0.5f, y1), Offset(cx0 + hw0 * 0.5f, y0), strokeWidth = edgeStroke * 0.85f)
        }
    }
}

private fun DrawScope.drawRoadsideProps(
    map: HighwayMap,
    w: Float,
    h: Float,
    horizonY: Float,
    vanishingX: Float,
    playerX: Float,
    roadHalfWBottom: Float,
    roadScroll: Float
) {
    if (map == HighwayMap.TUNNEL_ROAD) return
    val propCount = 8
    for (k in 0 until propCount) {
        val z = ((k - roadScroll) / propCount).let { if (it < 0f) it + 1f else it }
        val d = projectDepth(z)
        val yBase = horizonY + (h - horizonY) * d
        val cx = vanishingX - playerX * (roadHalfWBottom / 1.8f) * d
        val hw = roadHalfWBottom * d
        val leftX = cx - hw * 1.15f
        val rightX = cx + hw * 1.15f

        when (map) {
            HighwayMap.DAYLIGHT_HIGHWAY, HighwayMap.SNOWY_MOUNTAIN -> {
                val treeH = h * 0.24f * d
                val treeW = w * 0.08f * d
                val foliageColor = if (map == HighwayMap.SNOWY_MOUNTAIN) Color(0xFF90A4AE) else Color(0xFF1B5E20)
                listOf(leftX, rightX).forEach { tx ->
                    if (tx in -w * 0.2f..w * 1.2f) {
                        drawRect(
                            color = Color(0xFF4E342E),
                            topLeft = Offset(tx - treeW * 0.14f, yBase - treeH * 0.25f),
                            size = Size(treeW * 0.28f, treeH * 0.25f)
                        )
                        drawPath(
                            path = Path().apply {
                                moveTo(tx, yBase - treeH)
                                lineTo(tx - treeW, yBase - treeH * 0.22f)
                                lineTo(tx + treeW, yBase - treeH * 0.22f)
                                close()
                            },
                            color = foliageColor
                        )
                    }
                }
            }

            HighwayMap.NIGHT_CITY -> {
                val poleH = h * 0.34f * d
                listOf(leftX to 1f, rightX to -1f).forEach { (px, dir) ->
                    if (px in -w * 0.2f..w * 1.2f) {
                        drawLine(
                            color = Color(0xFF455A64),
                            start = Offset(px, yBase),
                            end = Offset(px, yBase - poleH),
                            strokeWidth = (2f + d * 6f)
                        )
                        val lampHead = Offset(px + dir * w * 0.05f * d, yBase - poleH)
                        drawLine(
                            color = Color(0xFF455A64),
                            start = Offset(px, yBase - poleH),
                            end = lampHead,
                            strokeWidth = (2f + d * 4f)
                        )
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(Color(0xFFFFE082), Color(0xFFFF8F00).copy(alpha = 0.25f), Color.Transparent),
                                center = lampHead,
                                radius = (18f + d * 50f)
                            ),
                            radius = (18f + d * 50f),
                            center = lampHead
                        )
                    }
                }
            }

            HighwayMap.TUNNEL_ROAD -> Unit
        }
    }
}

private fun DrawScope.drawTrafficVehicle(
    v: TrafficVehicle,
    map: HighwayMap,
    w: Float,
    h: Float,
    horizonY: Float,
    vanishingX: Float,
    playerX: Float,
    roadHalfWBottom: Float
) {
    val d = projectDepth(v.zPos)
    val groundY = horizonY + (h - horizonY) * d
    val cx = vanishingX + (v.laneX - playerX) * (roadHalfWBottom / 1.8f) * d
    val laneWidthAtD = (roadHalfWBottom * 0.5f) * d
    val vehW = laneWidthAtD * v.type.widthRatio * 1.95f
    val vehH = vehW * (v.type.heightRatio / v.type.widthRatio) * 1.05f

    val bodyColor = TrafficVehicleType.bodyColors[v.colorIndex % TrafficVehicleType.bodyColors.size]
    val darkBody = bodyColor.copy(
        red = bodyColor.red * 0.65f,
        green = bodyColor.green * 0.65f,
        blue = bodyColor.blue * 0.65f
    )

    val left = cx - vehW / 2f
    val top = groundY - vehH

    // Ground Shadow
    drawOval(
        color = Color.Black.copy(alpha = 0.55f),
        topLeft = Offset(left - vehW * 0.06f, groundY - vehH * 0.08f),
        size = Size(vehW * 1.12f, vehH * 0.16f)
    )

    // Tires
    val wheelW = vehW * 0.16f
    val wheelH = vehH * 0.15f
    drawRoundRect(Color(0xFF0C0E12), Offset(left + vehW * 0.06f, groundY - wheelH), Size(wheelW, wheelH), CornerRadius(4f, 4f))
    drawRoundRect(Color(0xFF0C0E12), Offset(left + vehW - vehW * 0.06f - wheelW, groundY - wheelH), Size(wheelW, wheelH), CornerRadius(4f, 4f))

    // Main Chassis
    drawRoundRect(
        brush = Brush.verticalGradient(listOf(bodyColor, darkBody), startY = top, endY = groundY),
        topLeft = Offset(left, top + vehH * 0.22f),
        size = Size(vehW, vehH * 0.68f),
        cornerRadius = CornerRadius(vehW * 0.08f, vehW * 0.08f)
    )

    // Cabin / Roof
    val cabinInset = if (v.type == TrafficVehicleType.CITY_BUS || v.type == TrafficVehicleType.SEMI_TRUCK) 0.05f else 0.14f
    drawRoundRect(
        color = if (v.type == TrafficVehicleType.SEMI_TRUCK) Color(0xFFCFD8DC) else darkBody,
        topLeft = Offset(left + vehW * cabinInset, top),
        size = Size(vehW * (1f - cabinInset * 2f), vehH * 0.36f),
        cornerRadius = CornerRadius(vehW * 0.07f, vehW * 0.07f)
    )

    // Rear Window or Front Windshield
    drawRoundRect(
        color = if (v.isOncoming) Color(0xFF37474F) else Color(0xFF151E2B),
        topLeft = Offset(left + vehW * (cabinInset + 0.06f), top + vehH * 0.06f),
        size = Size(vehW * (1f - (cabinInset + 0.06f) * 2f), vehH * 0.22f),
        cornerRadius = CornerRadius(vehW * 0.04f, vehW * 0.04f)
    )

    // Lights: Bright White Headlights if oncoming, Red Taillights if same-way
    val lightW = vehW * 0.17f
    val lightH = vehH * 0.11f
    val lightY = top + vehH * 0.60f
    if (v.isOncoming) {
        val headColor = Color(0xFFFFFDE7)
        drawRoundRect(headColor, Offset(left + vehW * 0.09f, lightY), Size(lightW, lightH), CornerRadius(4f, 4f))
        drawRoundRect(headColor, Offset(left + vehW - vehW * 0.09f - lightW, lightY), Size(lightW, lightH), CornerRadius(4f, 4f))
        if (map.weather == WeatherCondition.NIGHT || map.weather == WeatherCondition.TUNNEL) {
            drawCircle(headColor.copy(alpha = 0.28f), radius = vehW * 0.28f, center = Offset(left + vehW * 0.17f, lightY + lightH * 0.5f))
            drawCircle(headColor.copy(alpha = 0.28f), radius = vehW * 0.28f, center = Offset(left + vehW * 0.83f, lightY + lightH * 0.5f))
        }
    } else {
        val tailColor = CrimsonRedline
        drawRoundRect(tailColor, Offset(left + vehW * 0.09f, lightY), Size(lightW, lightH), CornerRadius(4f, 4f))
        drawRoundRect(tailColor, Offset(left + vehW - vehW * 0.09f - lightW, lightY), Size(lightW, lightH), CornerRadius(4f, 4f))
        if (map.weather == WeatherCondition.NIGHT || map.weather == WeatherCondition.TUNNEL) {
            drawCircle(tailColor.copy(alpha = 0.26f), radius = vehW * 0.24f, center = Offset(left + vehW * 0.17f, lightY + lightH * 0.5f))
            drawCircle(tailColor.copy(alpha = 0.26f), radius = vehW * 0.24f, center = Offset(left + vehW * 0.83f, lightY + lightH * 0.5f))
        }
    }
}

private fun DrawScope.drawFirstPersonCockpit(
    bike: BikeUiModel,
    map: HighwayMap,
    leanAngleDeg: Float,
    suspensionBob: Float,
    speedKmh: Float,
    rpmRatio: Float,
    gear: Int,
    isBraking: Boolean,
    isThrottle: Boolean,
    roadScroll: Float,
    vehicles: List<TrafficVehicle>,
    textMeasurer: TextMeasurer,
    w: Float,
    h: Float
) {
    val pivot = Offset(w * 0.50f, h * 1.06f)
    val cockpitLean = -leanAngleDeg * 0.65f
    val yBob = suspensionBob * 3.5f + if (isBraking) 10f else 0f

    withTransform({
        rotate(degrees = cockpitLean, pivot = pivot)
        translate(top = yBob)
    }) {
        val cockpitTopY = h * 0.72f
        val barCenterY = h * 0.83f
        val primary = bike.spec.primaryColor
        val accent = bike.spec.accentColor

        // Fairing / Windscreen Shield
        if (bike.spec.handlebarStyle == HandlebarStyle.SPORT_CLIPON ||
            bike.spec.handlebarStyle == HandlebarStyle.SCOOTER_COWL
        ) {
            val shieldPath = Path().apply {
                moveTo(w * 0.16f, h * 0.96f)
                quadraticTo(w * 0.30f, cockpitTopY - h * 0.08f, w * 0.50f, cockpitTopY - h * 0.10f)
                quadraticTo(w * 0.70f, cockpitTopY - h * 0.08f, w * 0.84f, h * 0.96f)
                close()
            }
            drawPath(
                path = shieldPath,
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFF4FC3F7).copy(alpha = 0.15f), Color(0xFF0C1018).copy(alpha = 0.55f)),
                    startY = cockpitTopY - h * 0.10f,
                    endY = h * 0.96f
                )
            )
            drawPath(
                path = shieldPath,
                color = primary.copy(alpha = 0.75f),
                style = Stroke(width = 5f)
            )
        }

        // Dual Rear-View Mirrors with Live Reflection
        drawRearViewMirror(
            isLeft = true,
            stalkStart = Offset(w * 0.33f, barCenterY - h * 0.03f),
            mirrorCenter = Offset(w * 0.17f, h * 0.64f),
            mirrorSize = Size(w * 0.17f, h * 0.075f),
            map = map,
            roadScroll = roadScroll,
            vehicles = vehicles,
            w = w,
            h = h
        )
        drawRearViewMirror(
            isLeft = false,
            stalkStart = Offset(w * 0.67f, barCenterY - h * 0.03f),
            mirrorCenter = Offset(w * 0.83f, h * 0.64f),
            mirrorSize = Size(w * 0.17f, h * 0.075f),
            map = map,
            roadScroll = roadScroll,
            vehicles = vehicles,
            w = w,
            h = h
        )

        // Handlebar Steel Tube
        val gripYOffset = when (bike.spec.handlebarStyle) {
            HandlebarStyle.CRUISER_APE -> -h * 0.045f
            HandlebarStyle.SPORT_CLIPON -> h * 0.025f
            else -> 0f
        }
        val leftGrip = Offset(w * 0.18f, barCenterY + gripYOffset)
        val rightGrip = Offset(w * 0.82f, barCenterY + gripYOffset)
        val centerStem = Offset(w * 0.50f, barCenterY)

        drawLine(Color(0xFF546E7A), leftGrip, centerStem, strokeWidth = 20f, cap = StrokeCap.Round)
        drawLine(Color(0xFF546E7A), rightGrip, centerStem, strokeWidth = 20f, cap = StrokeCap.Round)

        // Left Brake Lever & Right Throttle Lever
        val brakePullOffset = if (isBraking) 10f else 0f
        drawLine(
            color = Color(0xFFCFD8DC),
            start = Offset(leftGrip.x + w * 0.06f, leftGrip.y - 12f),
            end = Offset(leftGrip.x - w * 0.06f, leftGrip.y - 22f + brakePullOffset),
            strokeWidth = 7f,
            cap = StrokeCap.Round
        )

        // Gloved Hands gripping left & right bars
        drawGlovedHand(leftGrip, isLeft = true, accent = accent, isActive = isBraking)
        drawGlovedHand(rightGrip, isLeft = false, accent = accent, isActive = isThrottle)

        // Center Cockpit Instrument Cluster (Live Digital + Analog Speedo & Tach)
        val dashW = w * 0.34f
        val dashH = h * 0.145f
        val dashLeft = w * 0.50f - dashW / 2f
        val dashTop = barCenterY - dashH * 0.65f

        drawRoundRect(
            color = Color(0xFF0C1018),
            topLeft = Offset(dashLeft, dashTop),
            size = Size(dashW, dashH),
            cornerRadius = CornerRadius(22f, 22f)
        )
        drawRoundRect(
            color = accent.copy(alpha = 0.85f),
            topLeft = Offset(dashLeft, dashTop),
            size = Size(dashW, dashH),
            cornerRadius = CornerRadius(22f, 22f),
            style = Stroke(width = 3.5f)
        )

        // Tachometer RPM Arc inside dash
        val arcTopLeft = Offset(dashLeft + dashW * 0.08f, dashTop + dashH * 0.12f)
        val arcSize = Size(dashW * 0.84f, dashH * 0.85f)
        drawArc(
            color = Color(0xFF263238),
            startAngle = 180f,
            sweepAngle = 180f,
            useCenter = false,
            topLeft = arcTopLeft,
            size = arcSize,
            style = Stroke(width = 9f, cap = StrokeCap.Round)
        )
        val rpmColor = when {
            rpmRatio > 0.86f -> CrimsonRedline
            rpmRatio > 0.65f -> RacingAmber
            else -> CyanTelemetry
        }
        drawArc(
            color = rpmColor,
            startAngle = 180f,
            sweepAngle = 180f * rpmRatio.coerceIn(0.05f, 1f),
            useCenter = false,
            topLeft = arcTopLeft,
            size = arcSize,
            style = Stroke(width = 9f, cap = StrokeCap.Round)
        )

        // Speed text inside cockpit dash
        val speedStr = "${speedKmh.toInt()}"
        val speedLayout = textMeasurer.measure(
            text = speedStr,
            style = TextStyle(
                fontFamily = JetBrainsMonoFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 21.sp,
                color = if (speedKmh >= 100f) RacingAmber else Color.White
            )
        )
        drawText(
            textLayoutResult = speedLayout,
            topLeft = Offset(
                dashLeft + dashW * 0.50f - speedLayout.size.width / 2f,
                dashTop + dashH * 0.34f
            )
        )

        val unitLayout = textMeasurer.measure(
            text = "KM/H  •  G$gear",
            style = TextStyle(
                fontFamily = RajdhaniFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 10.sp,
                color = CyanTelemetry
            )
        )
        drawText(
            textLayoutResult = unitLayout,
            topLeft = Offset(
                dashLeft + dashW * 0.50f - unitLayout.size.width / 2f,
                dashTop + dashH * 0.72f
            )
        )
    }
}

private fun DrawScope.drawRearViewMirror(
    isLeft: Boolean,
    stalkStart: Offset,
    mirrorCenter: Offset,
    mirrorSize: Size,
    map: HighwayMap,
    roadScroll: Float,
    vehicles: List<TrafficVehicle>,
    w: Float,
    h: Float
) {
    // Mirror stalk
    drawLine(
        color = Color(0xFF37474F),
        start = stalkStart,
        end = Offset(mirrorCenter.x + (if (isLeft) mirrorSize.width * 0.25f else -mirrorSize.width * 0.25f), mirrorCenter.y + mirrorSize.height * 0.3f),
        strokeWidth = 9f,
        cap = StrokeCap.Round
    )

    val mLeft = mirrorCenter.x - mirrorSize.width / 2f
    val mTop = mirrorCenter.y - mirrorSize.height / 2f

    // Housing
    drawRoundRect(
        color = Color(0xFF1A1F29),
        topLeft = Offset(mLeft - 4f, mTop - 4f),
        size = Size(mirrorSize.width + 8f, mirrorSize.height + 8f),
        cornerRadius = CornerRadius(18f, 18f)
    )

    // Clipped glass reflection showing behind traffic & scrolling lane stripes
    val glassPath = Path().apply {
        addRoundRect(
            androidx.compose.ui.geometry.RoundRect(
                left = mLeft,
                top = mTop,
                right = mLeft + mirrorSize.width,
                bottom = mTop + mirrorSize.height,
                cornerRadius = CornerRadius(14f, 14f)
            )
        )
    }
    clipPath(glassPath) {
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(map.skyBottom.copy(alpha = 0.75f), map.roadBase),
                startY = mTop,
                endY = mTop + mirrorSize.height
            ),
            topLeft = Offset(mLeft, mTop),
            size = mirrorSize
        )
        // Moving behind road lane stripes
        val stripeY = mTop + mirrorSize.height * (0.4f + (roadScroll * 0.6f))
        drawLine(
            color = Color.White.copy(alpha = 0.45f),
            start = Offset(mirrorCenter.x, mTop + mirrorSize.height * 0.35f),
            end = Offset(mirrorCenter.x, stripeY),
            strokeWidth = 2f
        )
        // Tiny traffic dots behind rider (vehicles with small zPos)
        vehicles.filter { it.zPos < 0.16f }.take(2).forEachIndexed { idx, _ ->
            val dotX = mLeft + mirrorSize.width * (0.35f + idx * 0.30f)
            val dotY = mTop + mirrorSize.height * 0.58f
            drawCircle(Color(0xFFFFF59D), radius = 3.2f, center = Offset(dotX, dotY))
        }
    }
}

private fun DrawScope.drawGlovedHand(
    gripCenter: Offset,
    isLeft: Boolean,
    accent: Color,
    isActive: Boolean
) {
    val dir = if (isLeft) -1f else 1f
    val handOffset = if (isActive && !isLeft) 5f else 0f
    // Forearm sleeve
    drawLine(
        color = Color(0xFF1B202B),
        start = Offset(gripCenter.x + dir * 24f, gripCenter.y + 110f),
        end = Offset(gripCenter.x + dir * 6f, gripCenter.y + 18f + handOffset),
        strokeWidth = 34f,
        cap = StrokeCap.Round
    )
    // Knuckle carbon pad & glove body
    drawRoundRect(
        color = Color(0xFF262D3B),
        topLeft = Offset(gripCenter.x - 28f, gripCenter.y - 20f + handOffset),
        size = Size(56f, 44f),
        cornerRadius = CornerRadius(14f, 14f)
    )
    drawRoundRect(
        color = accent,
        topLeft = Offset(gripCenter.x - 20f, gripCenter.y - 12f + handOffset),
        size = Size(40f, 8f),
        cornerRadius = CornerRadius(4f, 4f)
    )
}
