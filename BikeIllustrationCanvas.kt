package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import com.example.domain.model.BikeCategory
import com.example.domain.model.BikeSpec

@Composable
fun BikeIllustrationCanvas(
    spec: BikeSpec,
    modifier: Modifier = Modifier,
    isLocked: Boolean = false
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val groundY = h * 0.80f
        val primary = if (isLocked) spec.primaryColor.copy(alpha = 0.55f) else spec.primaryColor
        val accent = if (isLocked) spec.accentColor.copy(alpha = 0.55f) else spec.accentColor

        // Ground shadow & spotlight reflection
        drawOval(
            brush = Brush.radialGradient(
                colors = listOf(Color.Black.copy(alpha = 0.65f), Color.Transparent),
                center = Offset(w * 0.50f, groundY + h * 0.04f),
                radius = w * 0.42f
            ),
            topLeft = Offset(w * 0.10f, groundY - h * 0.02f),
            size = Size(w * 0.80f, h * 0.14f)
        )

        val rearWheelX = w * 0.27f
        val frontWheelX = w * 0.74f
        val wheelRadius = when (spec.category) {
            BikeCategory.SCOOTER -> h * 0.14f
            BikeCategory.CRUISER -> h * 0.175f
            else -> h * 0.18f
        }
        val wheelY = groundY - wheelRadius

        // Wheels
        drawWheel(Offset(rearWheelX, wheelY), wheelRadius, accent)
        drawWheel(Offset(frontWheelX, wheelY), wheelRadius, accent)

        // Headlight glow beam
        val headCenter = when (spec.category) {
            BikeCategory.SCOOTER -> Offset(w * 0.75f, h * 0.39f)
            BikeCategory.CRUISER -> Offset(w * 0.74f, h * 0.37f)
            else -> Offset(w * 0.79f, h * 0.40f)
        }
        drawPath(
            path = Path().apply {
                moveTo(headCenter.x, headCenter.y - h * 0.03f)
                lineTo(w * 0.99f, headCenter.y - h * 0.16f)
                lineTo(w * 0.99f, headCenter.y + h * 0.16f)
                lineTo(headCenter.x, headCenter.y + h * 0.03f)
                close()
            },
            brush = Brush.horizontalGradient(
                colors = listOf(Color(0xFFFFF59D).copy(alpha = 0.45f), Color.Transparent),
                startX = headCenter.x,
                endX = w * 0.99f
            )
        )

        // Swingarm & Front Fork
        val swingPivot = Offset(w * 0.43f, h * 0.63f)
        val tripleClamp = when (spec.category) {
            BikeCategory.SCOOTER -> Offset(w * 0.66f, h * 0.33f)
            BikeCategory.CRUISER -> Offset(w * 0.64f, h * 0.33f)
            else -> Offset(w * 0.67f, h * 0.35f)
        }

        // Rear swingarm
        drawLine(
            color = Color(0xFF78859B),
            start = Offset(rearWheelX, wheelY),
            end = swingPivot,
            strokeWidth = h * 0.035f,
            cap = StrokeCap.Round
        )

        // Exhaust Pipe
        val exhaustPath = Path().apply {
            moveTo(w * 0.46f, h * 0.68f)
            lineTo(w * 0.20f, h * 0.66f)
            lineTo(w * 0.16f, h * 0.61f)
            lineTo(w * 0.22f, h * 0.60f)
            lineTo(w * 0.47f, h * 0.64f)
            close()
        }
        drawPath(
            path = exhaustPath,
            brush = Brush.verticalGradient(listOf(Color(0xFFB0BEC5), Color(0xFF455A64)))
        )

        // Front Fork
        drawLine(
            color = if (spec.category == BikeCategory.SUPERBIKE) Color(0xFFFFB300) else Color(0xFFB0BEC5),
            start = Offset(frontWheelX, wheelY),
            end = tripleClamp,
            strokeWidth = h * 0.032f,
            cap = StrokeCap.Round
        )

        // Engine Block
        drawRoundRect(
            color = Color(0xFF263238),
            topLeft = Offset(w * 0.41f, h * 0.50f),
            size = Size(w * 0.19f, h * 0.19f),
            cornerRadius = CornerRadius(h * 0.03f, h * 0.03f)
        )
        drawRoundRect(
            color = Color(0xFF546E7A),
            topLeft = Offset(w * 0.44f, h * 0.53f),
            size = Size(w * 0.12f, h * 0.11f),
            cornerRadius = CornerRadius(h * 0.02f, h * 0.02f)
        )

        // Category-specific chassis, tank & fairing
        when (spec.category) {
            BikeCategory.SCOOTER -> {
                val body = Path().apply {
                    moveTo(w * 0.22f, h * 0.44f)
                    lineTo(w * 0.44f, h * 0.46f)
                    lineTo(w * 0.46f, h * 0.61f)
                    lineTo(w * 0.64f, h * 0.61f)
                    lineTo(w * 0.67f, h * 0.34f)
                    lineTo(w * 0.76f, h * 0.42f)
                    lineTo(w * 0.72f, h * 0.65f)
                    lineTo(w * 0.26f, h * 0.63f)
                    close()
                }
                drawPath(body, color = primary)
                // Seat
                drawRoundRect(
                    color = Color(0xFF1A1E26),
                    topLeft = Offset(w * 0.25f, h * 0.41f),
                    size = Size(w * 0.22f, h * 0.06f),
                    cornerRadius = CornerRadius(8f, 8f)
                )
            }

            BikeCategory.CRUISER -> {
                // Low-slung fuel tank & rear fender
                val fender = Path().apply {
                    moveTo(w * 0.18f, h * 0.50f)
                    quadraticTo(w * 0.30f, h * 0.44f, w * 0.42f, h * 0.52f)
                    lineTo(w * 0.38f, h * 0.58f)
                    lineTo(w * 0.19f, h * 0.56f)
                    close()
                }
                drawPath(fender, color = primary)
                val tank = Path().apply {
                    moveTo(w * 0.46f, h * 0.48f)
                    quadraticTo(w * 0.56f, h * 0.36f, w * 0.67f, h * 0.44f)
                    lineTo(w * 0.62f, h * 0.53f)
                    lineTo(w * 0.46f, h * 0.52f)
                    close()
                }
                drawPath(tank, color = primary)
                // Cruiser ape handlebars
                drawLine(
                    color = Color(0xFFCFD8DC),
                    start = tripleClamp,
                    end = Offset(w * 0.60f, h * 0.21f),
                    strokeWidth = h * 0.024f,
                    cap = StrokeCap.Round
                )
                // Leather saddle
                drawRoundRect(
                    color = Color(0xFF1F1614),
                    topLeft = Offset(w * 0.32f, h * 0.49f),
                    size = Size(w * 0.18f, h * 0.06f),
                    cornerRadius = CornerRadius(10f, 10f)
                )
            }

            BikeCategory.SPORT -> {
                // Tail + Tank + Half Cowl
                val tailTank = Path().apply {
                    moveTo(w * 0.20f, h * 0.38f)
                    lineTo(w * 0.38f, h * 0.43f)
                    lineTo(w * 0.52f, h * 0.37f)
                    lineTo(w * 0.67f, h * 0.40f)
                    lineTo(w * 0.63f, h * 0.53f)
                    lineTo(w * 0.35f, h * 0.52f)
                    close()
                }
                drawPath(tailTank, color = primary)
                // Belly pan
                drawRoundRect(
                    color = accent,
                    topLeft = Offset(w * 0.43f, h * 0.62f),
                    size = Size(w * 0.20f, h * 0.06f),
                    cornerRadius = CornerRadius(8f, 8f)
                )
                // Seat
                drawRoundRect(
                    color = Color(0xFF151820),
                    topLeft = Offset(w * 0.31f, h * 0.42f),
                    size = Size(w * 0.17f, h * 0.05f),
                    cornerRadius = CornerRadius(6f, 6f)
                )
            }

            BikeCategory.SUPERBIKE -> {
                // Full aerodynamic fairing + sharp tail + windscreen
                val fairing = Path().apply {
                    moveTo(w * 0.18f, h * 0.33f)
                    lineTo(w * 0.36f, h * 0.42f)
                    lineTo(w * 0.52f, h * 0.35f)
                    lineTo(w * 0.67f, h * 0.34f)
                    lineTo(w * 0.81f, h * 0.43f)
                    lineTo(w * 0.72f, h * 0.64f)
                    lineTo(w * 0.46f, h * 0.67f)
                    lineTo(w * 0.38f, h * 0.51f)
                    close()
                }
                drawPath(fairing, color = primary)
                // Racing stripe accent
                drawPath(
                    path = Path().apply {
                        moveTo(w * 0.50f, h * 0.42f)
                        lineTo(w * 0.74f, h * 0.44f)
                        lineTo(w * 0.69f, h * 0.52f)
                        lineTo(w * 0.48f, h * 0.50f)
                        close()
                    },
                    color = accent
                )
                // Tinted windscreen
                drawPath(
                    path = Path().apply {
                        moveTo(w * 0.64f, h * 0.34f)
                        lineTo(w * 0.72f, h * 0.25f)
                        lineTo(w * 0.77f, h * 0.37f)
                        close()
                    },
                    color = Color(0xFF80D8FF).copy(alpha = 0.55f)
                )
                // Rider saddle
                drawRoundRect(
                    color = Color(0xFF12151C),
                    topLeft = Offset(w * 0.30f, h * 0.40f),
                    size = Size(w * 0.18f, h * 0.05f),
                    cornerRadius = CornerRadius(6f, 6f)
                )
            }
        }

        // Glowing LED Headlight lens
        drawCircle(
            color = Color(0xFFFFF9C4),
            radius = h * 0.028f,
            center = headCenter
        )
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawWheel(
    center: Offset,
    radius: Float,
    rimAccent: Color
) {
    // Tire
    drawCircle(
        color = Color(0xFF14171E),
        radius = radius,
        center = center
    )
    // Rim ring
    drawCircle(
        color = rimAccent,
        radius = radius * 0.68f,
        center = center,
        style = Stroke(width = radius * 0.11f, join = StrokeJoin.Round)
    )
    // Brake rotor
    drawCircle(
        color = Color(0xFF90A4AE),
        radius = radius * 0.44f,
        center = center,
        style = Stroke(width = radius * 0.07f)
    )
    // Spokes
    for (i in 0 until 5) {
        val angle = (i * 72f) * (Math.PI / 180.0)
        drawLine(
            color = Color(0xFF546E7A),
            start = center,
            end = Offset(
                center.x + (kotlin.math.cos(angle) * radius * 0.64f).toFloat(),
                center.y + (kotlin.math.sin(angle) * radius * 0.64f).toFloat()
            ),
            strokeWidth = radius * 0.08f
        )
    }
    // Axle hub
    drawCircle(
        color = Color(0xFFCFD8DC),
        radius = radius * 0.14f,
        center = center
    )
}
