package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import com.example.domain.model.HighwayMap

@Composable
fun MapPreviewCanvas(
    map: HighwayMap,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val horizonY = h * 0.50f

        // Sky / Ceiling
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(map.skyTop, map.skyBottom),
                startY = 0f,
                endY = horizonY
            ),
            size = Size(w, horizonY)
        )

        // Environment specifics in sky
        when (map) {
            HighwayMap.DAYLIGHT_HIGHWAY -> {
                drawCircle(
                    color = Color(0xFFFFF176),
                    radius = h * 0.11f,
                    center = Offset(w * 0.78f, h * 0.20f)
                )
                drawPath(
                    path = Path().apply {
                        moveTo(0f, horizonY)
                        lineTo(w * 0.25f, h * 0.30f)
                        lineTo(w * 0.52f, horizonY)
                        lineTo(w * 0.75f, h * 0.34f)
                        lineTo(w, horizonY)
                        close()
                    },
                    color = Color(0xFF2E5C48)
                )
            }

            HighwayMap.SNOWY_MOUNTAIN -> {
                drawPath(
                    path = Path().apply {
                        moveTo(0f, horizonY)
                        lineTo(w * 0.22f, h * 0.22f)
                        lineTo(w * 0.45f, horizonY)
                        lineTo(w * 0.72f, h * 0.20f)
                        lineTo(w, horizonY)
                        close()
                    },
                    color = Color(0xFF607D8B)
                )
                drawPath(
                    path = Path().apply {
                        moveTo(w * 0.14f, h * 0.32f)
                        lineTo(w * 0.22f, h * 0.22f)
                        lineTo(w * 0.30f, h * 0.32f)
                        close()
                    },
                    color = Color(0xFFECEFF1)
                )
                // Snowflakes
                val flakes = listOf(
                    Offset(0.15f, 0.18f), Offset(0.35f, 0.30f), Offset(0.58f, 0.16f),
                    Offset(0.82f, 0.28f), Offset(0.45f, 0.45f), Offset(0.70f, 0.60f)
                )
                flakes.forEach { pt ->
                    drawCircle(Color.White.copy(alpha = 0.85f), radius = 2.5f, center = Offset(w * pt.x, h * pt.y))
                }
            }

            HighwayMap.NIGHT_CITY -> {
                // Skyline
                val buildings = listOf(0.08f to 0.28f, 0.20f to 0.18f, 0.34f to 0.32f, 0.66f to 0.24f, 0.80f to 0.16f, 0.90f to 0.29f)
                buildings.forEach { (bx, topRatio) ->
                    drawRect(
                        color = Color(0xFF0A1022),
                        topLeft = Offset(w * bx, h * topRatio),
                        size = Size(w * 0.10f, horizonY - h * topRatio)
                    )
                    drawRect(
                        color = Color(0xFFFFB300).copy(alpha = 0.75f),
                        topLeft = Offset(w * bx + w * 0.02f, h * topRatio + h * 0.04f),
                        size = Size(w * 0.025f, h * 0.025f)
                    )
                }
            }

            HighwayMap.TUNNEL_ROAD -> {
                // Overhead glowing arches
                for (i in 1..3) {
                    val archScale = i / 3.5f
                    val archW = w * (0.25f + archScale * 0.7f)
                    val archTop = horizonY - h * archScale * 0.42f
                    drawArc(
                        color = map.accentGlow.copy(alpha = 0.55f),
                        startAngle = 180f,
                        sweepAngle = 180f,
                        useCenter = false,
                        topLeft = Offset(w * 0.5f - archW / 2f, archTop),
                        size = Size(archW, (horizonY - archTop) * 2f),
                        style = Stroke(width = 3f)
                    )
                }
            }
        }

        // Ground / Shoulders
        drawRect(
            color = map.shoulderColor,
            topLeft = Offset(0f, horizonY),
            size = Size(w, h - horizonY)
        )

        // Perspective Road
        val roadPath = Path().apply {
            moveTo(w * 0.43f, horizonY)
            lineTo(w * 0.57f, horizonY)
            lineTo(w * 0.92f, h)
            lineTo(w * 0.08f, h)
            close()
        }
        drawPath(roadPath, color = map.roadBase)

        // Center dashed line
        drawLine(
            color = map.accentGlow.copy(alpha = 0.85f),
            start = Offset(w * 0.50f, horizonY),
            end = Offset(w * 0.50f, h),
            strokeWidth = 3f
        )
    }
}
