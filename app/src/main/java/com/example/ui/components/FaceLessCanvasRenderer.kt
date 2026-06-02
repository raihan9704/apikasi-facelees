package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.withTransform
import com.example.api.FacelessCanvasConfig
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun FaceLessCanvasRenderer(
    config: FacelessCanvasConfig,
    modifier: Modifier = Modifier,
    isColoringPageMode: Boolean = false,
    upscaleFactor: Float = 1.0f // Simulated upscaler modifier
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val cx = w / 2f
        val cy = h / 2f

        // 1. Background Fill and Ambient Glow (skip background color in coloring page mode to allow coloring!)
        if (!isColoringPageMode) {
            val bgMain = try { Color(android.graphics.Color.parseColor(config.backgroundColor)) } catch (e: Exception) { Color(0xFF0F172A) }
            val bgGrad = Brush.radialGradient(
                colors = listOf(bgMain.copy(alpha = 0.9f), bgMain),
                center = Offset(cx, cy),
                radius = w * 0.7f
            )
            drawRect(brush = bgGrad)
            
            // Draw sleek subtle concentric space circles for "Cyber/Futuristic Startup" depth
            drawCircle(
                color = Color.White.copy(alpha = 0.04f),
                radius = w * 0.42f,
                center = Offset(cx, cy),
                style = Stroke(width = 2f)
            )
            drawCircle(
                color = Color.White.copy(alpha = 0.02f),
                radius = w * 0.55f,
                center = Offset(cx, cy),
                style = Stroke(width = 1f)
            )
        } else {
            // Coloring mode shows clean paper back
            drawRect(color = Color.White)
        }

        // Draw Ambient Sparkles/Dots/Leaves
        if (!isColoringPageMode) {
            drawAmbientDecorations(config.ambientElements, w, h)
        }

        // Apply scale based on upscaler setting
        scale(scaleX = upscaleFactor, scaleY = upscaleFactor, pivot = Offset(cx, cy)) {
            // Draw Main Character/Subject
            when (config.entity) {
                "human" -> drawHumanCharacter(config, cx, cy, w, h, isColoringPageMode)
                "animal" -> drawAnimalCharacter(config, cx, cy, w, h, isColoringPageMode)
                else -> drawInanimateScene(config, cx, cy, w, h, isColoringPageMode)
            }
        }

        // If Sticker Mode is active (has custom outline in styling config), draw an elegant white sticker die-cut stroke around the entire card canvas
        if (config.isLivingCreature && config.styleName.contains("Sticker", ignoreCase = true)) {
            val stickerWidth = 12f
            drawRoundRect(
                color = Color.White,
                topLeft = Offset(16f, 16f),
                size = Size(w - 32f, h - 32f),
                cornerRadius = CornerRadius(24f),
                style = Stroke(width = stickerWidth)
            )
        }
    }
}

private fun DrawScope.drawAmbientDecorations(elements: List<String>, w: Float, h: Float) {
    val elementSet = elements.toSet()
    val glowColor = Color(0xFFA855F7).copy(alpha = 0.4f) // Neon purple spark

    if (elementSet.contains("stars")) {
        // Draw cyber cross stars
        drawStarCross(Offset(w * 0.15f, h * 0.22f), size = 12f, color = glowColor)
        drawStarCross(Offset(w * 0.85f, h * 0.18f), size = 16f, color = Color(0xFFFF6D00).copy(alpha = 0.5f))
        drawStarCross(Offset(w * 0.10f, h * 0.75f), size = 10f, color = Color(0xFF06B6D4).copy(alpha = 0.5f))
        drawStarCross(Offset(w * 0.82f, h * 0.70f), size = 14f, color = glowColor)
    }

    if (elementSet.contains("clouds")) {
        // Aesthetic modern flat clouds
        drawOval(
            color = Color.White.copy(alpha = 0.07f),
            topLeft = Offset(w * 0.05f, h * 0.1f),
            size = Size(w * 0.3f, h * 0.08f)
        )
        drawOval(
            color = Color.White.copy(alpha = 0.05f),
            topLeft = Offset(w * 0.65f, h * 0.25f),
            size = Size(w * 0.25f, h * 0.07f)
        )
    }

    if (elementSet.contains("leaves")) {
        // Sleek abstract organic shapes
        val leafColor = Color(0xFF4CAF50).copy(alpha = 0.12f)
        val path = Path().apply {
            moveTo(w * 0.8f, h * 0.4f)
            quadraticTo(w * 0.9f, h * 0.35f, w * 0.95f, h * 0.45f)
            quadraticTo(w * 0.85f, h * 0.5f, w * 0.8f, h * 0.4f)
        }
        drawPath(path = path, color = leafColor)
    }

    // Default techno dots for "premium tech AI" vibes
    drawCircle(color = Color.White.copy(alpha = 0.15f), radius = 3f, center = Offset(w * 0.3f, h * 0.15f))
    drawCircle(color = Color.White.copy(alpha = 0.10f), radius = 2f, center = Offset(w * 0.72f, h * 0.45f))
    drawCircle(color = Color.White.copy(alpha = 0.20f), radius = 4f, center = Offset(w * 0.25f, h * 0.6f))
}

private fun DrawScope.drawStarCross(center: Offset, size: Float, color: Color) {
    val path = Path().apply {
        moveTo(center.x, center.y - size)
        lineTo(center.x, center.y + size)
        moveTo(center.x - size, center.y)
        lineTo(center.x + size, center.y)
    }
    drawPath(path = path, color = color, style = Stroke(width = size * 0.25f, cap = StrokeCap.Round))
}

private fun DrawScope.drawHumanCharacter(
    config: FacelessCanvasConfig,
    cx: Float,
    cy: Float,
    w: Float,
    h: Float,
    isColoringMode: Boolean
) {
    // Dynamic styling colors or lines
    val outlineStroke = Stroke(width = 4f)
    val skinColor = if (isColoringMode) Color.White else Color(android.graphics.Color.parseColor(config.skinColor))
    val outfitColor = if (isColoringMode) Color.White else Color(android.graphics.Color.parseColor(config.outfitColor))
    val hijabColor = if (isColoringMode) Color.White else Color(android.graphics.Color.parseColor(config.hijabColor))
    val accessoryColor = if (isColoringMode) Color.White else Color(android.graphics.Color.parseColor(config.accessoryColor))

    val shadowColor = Color.Black.copy(alpha = 0.15f)
    val lineBrush = SolidColor(Color(0xFF1E293B)) // dark carbon lines for cute styling

    // 1. Draw elegant shoulders/body
    val shoulderPath = Path().apply {
        moveTo(cx - w * 0.28f, cy + h * 0.35f)
        cubicTo(
            cx - w * 0.22f, cy + h * 0.15f,
            cx + w * 0.22f, cy + h * 0.15f,
            cx + w * 0.28f, cy + h * 0.35f
        )
        lineTo(cx + w * 0.28f, h)
        lineTo(cx - w * 0.28f, h)
        close()
    }
    
    if (isColoringMode) {
        drawPath(path = shoulderPath, color = Color.White)
        drawPath(path = shoulderPath, color = Color.Black, style = outlineStroke)
    } else {
        // Ambient soft shadow under head
        drawCircle(color = shadowColor, radius = w * 0.16f, center = Offset(cx, cy + h * 0.1f))
        
        // Base apparel color
        drawPath(path = shoulderPath, color = outfitColor)
        
        // Cyber lapels / details in attire of startup developers
        if (config.apparelType == "suit") {
            val collarL = Path().apply {
                moveTo(cx - w * 0.15f, cy + h * 0.15f)
                lineTo(cx, cy + h * 0.28f)
                lineTo(cx - w * 0.05f, cy + h * 0.35f)
                close()
            }
            val collarR = Path().apply {
                moveTo(cx + w * 0.15f, cy + h * 0.15f)
                lineTo(cx, cy + h * 0.28f)
                lineTo(cx + w * 0.05f, cy + h * 0.35f)
                close()
            }
            drawPath(path = collarL, color = outfitColor.copy(alpha = 0.8f))
            drawPath(path = collarR, color = outfitColor.copy(alpha = 0.8f))
        }
    }

    // 2. Draw Hijab drape (if female with Hijab active)
    if (config.hijab) {
        val hijabDrapePath = Path().apply {
            moveTo(cx - w * 0.2f, cy + h * 0.12f)
            cubicTo(
                cx - w * 0.22f, cy + h * 0.25f,
                cx + w * 0.22f, cy + h * 0.25f,
                cx + w * 0.2f, cy + h * 0.12f
            )
            cubicTo(
                cx + w * 0.25f, cy + h * 0.28f,
                cx + w * 0.15f, cy + h * 0.42f,
                cx, cy + h * 0.45f
            )
            cubicTo(
                cx - w * 0.15f, cy + h * 0.42f,
                cx - w * 0.25f, cy + h * 0.28f,
                cx - w * 0.2f, cy + h * 0.12f
            )
            close()
        }
        if (isColoringMode) {
            drawPath(path = hijabDrapePath, color = Color.White)
            drawPath(path = hijabDrapePath, color = Color.Black, style = outlineStroke)
        } else {
            drawPath(path = hijabDrapePath, color = hijabColor)
            // Delicate fabric fold shades
            drawPath(path = hijabDrapePath, color = Color.Black.copy(alpha = 0.08f))
        }
    }

    // 3. Draw Main Frame: Hair or Hijab Hood
    if (config.hijab) {
        // Round hijab hood framing face
        val hoodTopLeft = Offset(cx - w * 0.19f, cy - h * 0.15f)
        val hoodSize = Size(w * 0.38f, h * 0.32f)
        if (isColoringMode) {
            drawOval(color = Color.White, topLeft = hoodTopLeft, size = hoodSize)
            drawOval(color = Color.Black, topLeft = hoodTopLeft, size = hoodSize, style = outlineStroke)
        } else {
            drawOval(color = hijabColor, topLeft = hoodTopLeft, size = hoodSize)
        }
    } else {
        // Draw elegant modern aesthetic hair
        val hairTopLeft = Offset(cx - w * 0.18f, cy - h * 0.15f)
        val hairSize = Size(w * 0.36f, h * 0.30f)
        val hairBrush = if (isColoringMode) Color.White else Color(0xFF1E293B) // dark aesthetic hair
        
        drawOval(color = hairBrush, topLeft = hairTopLeft, size = hairSize)

        // Draw side hair locks
        val lockL = Path().apply {
            moveTo(cx - w * 0.15f, cy)
            quadraticTo(cx - w * 0.18f, cy + h * 0.1f, cx - w * 0.14f, cy + h * 0.15f)
            close()
        }
        val lockR = Path().apply {
            moveTo(cx + w * 0.15f, cy)
            quadraticTo(cx + w * 0.18f, cy + h * 0.1f, cx + w * 0.14f, cy + h * 0.15f)
            close()
        }
        if (isColoringMode) {
            drawPath(path = lockL, color = Color.White)
            drawPath(path = lockL, color = Color.Black, style = outlineStroke)
            drawPath(path = lockR, color = Color.White)
            drawPath(path = lockR, color = Color.Black, style = outlineStroke)
        } else {
            drawPath(path = lockL, color = hairBrush)
            drawPath(path = lockR, color = hairBrush)
        }
    }

    // 4. DRAW FACE OVAL (Polos, Smooth, No Features! Aesthetic Highlight)
    val faceTopLeft = Offset(cx - w * 0.125f, cy - h * 0.08f)
    val faceSize = Size(w * 0.25f, h * 0.20f)
    
    if (isColoringMode) {
        drawOval(color = Color.White, topLeft = faceTopLeft, size = faceSize)
        drawOval(color = Color.Black, topLeft = faceTopLeft, size = faceSize, style = outlineStroke)
    } else {
        drawOval(color = skinColor, topLeft = faceTopLeft, size = faceSize)
        
        // Add smooth minimal aesthetic shadow inside face from hair/hijab
        val facialShade = Path().apply {
            arcTo(
                rect = androidx.compose.ui.geometry.Rect(faceTopLeft.x, faceTopLeft.y, faceTopLeft.x + faceSize.width, faceTopLeft.y + faceSize.height),
                startAngleDegrees = 180f,
                sweepAngleDegrees = 180f,
                forceMoveTo = true
            )
            lineTo(cx, cy)
            close()
        }
        drawPath(path = facialShade, color = Color.Black.copy(alpha = 0.05f))
    }

    // 5. Draw Outfit Neck/Collar Lines
    if (!config.hijab) {
        val neckPath = Path().apply {
            moveTo(cx - w * 0.04f, cy + h * 0.1f)
            lineTo(cx + w * 0.04f, cy + h * 0.1f)
            lineTo(cx + w * 0.05f, cy + h * 0.16f)
            lineTo(cx - w * 0.05f, cy + h * 0.16f)
            close()
        }
        if (isColoringMode) {
            drawPath(path = neckPath, color = Color.White)
            drawPath(path = neckPath, color = Color.Black, style = outlineStroke)
        } else {
            drawPath(path = neckPath, color = skinColor)
        }
    }

    // 6. Draw Props/Accessories (book, laptop, coffee, brushes) in front of chest
    drawCharacterAccessory(config, cx, cy, w, h, isColoringMode, accessoryColor)
}

private fun DrawScope.drawCharacterAccessory(
    config: FacelessCanvasConfig,
    cx: Float,
    cy: Float,
    w: Float,
    h: Float,
    isColoringMode: Boolean,
    color: Color
) {
    val strokeWidth = if (isColoringMode) 4f else 3f
    val accentBrush = if (isColoringMode) Color.White else color

    when (config.accessory) {
        "book" -> {
            // Draw a cute open book held in the front
            val bookLeft = Offset(cx - w * 0.15f, cy + h * 0.18f)
            val bookWidth = w * 0.3f
            val bookHeight = h * 0.12f
            
            val coverPath = Path().apply {
                addRoundRect(
                    androidx.compose.ui.geometry.RoundRect(
                        left = bookLeft.x,
                        top = bookLeft.y,
                        right = bookLeft.x + bookWidth,
                        bottom = bookLeft.y + bookHeight,
                        cornerRadius = CornerRadius(12f)
                    )
                )
            }
            val pagesPathL = Path().apply {
                addRoundRect(
                    androidx.compose.ui.geometry.RoundRect(
                        left = bookLeft.x + 8f,
                        top = bookLeft.y + 6f,
                        right = cx,
                        bottom = bookLeft.y + bookHeight - 6f,
                        topLeftCornerRadius = CornerRadius(4f),
                        bottomLeftCornerRadius = CornerRadius(4f)
                    )
                )
            }
            val pagesPathR = Path().apply {
                addRoundRect(
                    androidx.compose.ui.geometry.RoundRect(
                        left = cx,
                        top = bookLeft.y + 6f,
                        right = bookLeft.x + bookWidth - 8f,
                        bottom = bookLeft.y + bookHeight - 6f,
                        topRightCornerRadius = CornerRadius(4f),
                        bottomRightCornerRadius = CornerRadius(4f)
                    )
                )
            }

            if (isColoringMode) {
                drawPath(path = coverPath, color = Color.White)
                drawPath(path = coverPath, color = Color.Black, style = Stroke(strokeWidth))
                drawPath(path = pagesPathL, color = Color.White)
                drawPath(path = pagesPathL, color = Color.Black, style = Stroke(strokeWidth))
                drawPath(path = pagesPathR, color = Color.White)
                drawPath(path = pagesPathR, color = Color.Black, style = Stroke(strokeWidth))
            } else {
                drawPath(path = coverPath, color = accentBrush)
                drawPath(path = pagesPathL, color = Color.White)
                drawPath(path = pagesPathR, color = Color.White)
                
                // Book divider shadow
                drawLine(
                    color = Color.Black.copy(alpha = 0.15f),
                    start = Offset(cx, bookLeft.y + 6f),
                    end = Offset(cx, bookLeft.y + bookHeight - 6f),
                    strokeWidth = 3f
                )
            }
        }
        "laptop" -> {
            // Draw sleek cyber high tech laptop screen emitting light!
            val lapTopLeft = Offset(cx - w * 0.16f, cy + h * 0.22f)
            val lapW = w * 0.32f
            val lapH = h * 0.12f
            
            val laptopScreen = Path().apply {
                addRoundRect(
                    androidx.compose.ui.geometry.RoundRect(
                        left = lapTopLeft.x,
                        top = lapTopLeft.y,
                        right = lapTopLeft.x + lapW,
                        bottom = lapTopLeft.y + lapH,
                        cornerRadius = CornerRadius(8f)
                    )
                )
            }
            
            // Screen interior / screen bloom glow effect
            val screenInterior = Path().apply {
                addRoundRect(
                    androidx.compose.ui.geometry.RoundRect(
                        left = lapTopLeft.x + 6f,
                        top = lapTopLeft.y + 6f,
                        right = lapTopLeft.x + lapW - 6f,
                        bottom = lapTopLeft.y + lapH - 12f,
                        cornerRadius = CornerRadius(6f)
                    )
                )
            }
            
            val laptopBase = Path().apply {
                moveTo(lapTopLeft.x - 12f, lapTopLeft.y + lapH - 4f)
                lineTo(lapTopLeft.x + lapW + 12f, lapTopLeft.y + lapH - 4f)
                lineTo(lapTopLeft.x + lapW + 6f, lapTopLeft.y + lapH + 12f)
                lineTo(lapTopLeft.x - 6f, lapTopLeft.y + lapH + 12f)
                close()
            }

            if (isColoringMode) {
                drawPath(path = laptopScreen, color = Color.White)
                drawPath(path = laptopScreen, color = Color.Black, style = Stroke(strokeWidth))
                drawPath(path = laptopBase, color = Color.White)
                drawPath(path = laptopBase, color = Color.Black, style = Stroke(strokeWidth))
            } else {
                drawPath(path = laptopBase, color = Color(0xFF475569)) // Aluminum space gray base
                drawPath(path = laptopScreen, color = Color(0xFF1E293B))
                
                // Emitting neon gradient screen
                val screenBrush = Brush.verticalGradient(
                    colors = listOf(Color(0xFF00E5FF), Color(0xFFA855F7))
                )
                drawPath(path = screenInterior, brush = screenBrush)
                
                // Ambient screen glow mapped onto character chest & base face
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0xFF00E5FF).copy(alpha = 0.15f), Color.Transparent),
                        center = Offset(cx, cy + h * 0.16f),
                        radius = w * 0.2f
                    ),
                    radius = w * 0.2f,
                    center = Offset(cx, cy + h * 0.16f)
                )
            }
        }
        "coffee" -> {
            // Cozy steaming ceramic mug with cute steam waves in front
            val mugLeft = Offset(cx - w * 0.05f, cy + h * 0.22f)
            val mugSize = Size(w * 0.10f, h * 0.09f)
            
            if (isColoringMode) {
                drawRoundRect(color = Color.White, topLeft = mugLeft, size = mugSize, cornerRadius = CornerRadius(10f))
                drawRoundRect(color = Color.Black, topLeft = mugLeft, size = mugSize, cornerRadius = CornerRadius(10f), style = Stroke(strokeWidth))
                
                // Mug handle
                drawArc(
                    color = Color.Black,
                    startAngle = -90f,
                    sweepAngle = 180f,
                    useCenter = false,
                    topLeft = Offset(mugLeft.x + mugSize.width - 2f, mugLeft.y + 10f),
                    size = Size(16f, mugSize.height - 20f),
                    style = Stroke(strokeWidth)
                )
            } else {
                drawRoundRect(color = accentBrush, topLeft = mugLeft, size = mugSize, cornerRadius = CornerRadius(10f))
                
                // Handle
                drawArc(
                    color = accentBrush,
                    startAngle = -90f,
                    sweepAngle = 180f,
                    useCenter = false,
                    topLeft = Offset(mugLeft.x + mugSize.width - 3f, mugLeft.y + 10f),
                    size = Size(18f, mugSize.height - 20f),
                    style = Stroke(strokeWidth)
                )
                
                // Friendly warm coffee steam waves
                val steamPath = Path().apply {
                    moveTo(cx - 10f, mugLeft.y - 4f)
                    quadraticTo(cx - 15f, mugLeft.y - 12f, cx - 8f, mugLeft.y - 18f)
                    moveTo(cx, mugLeft.y - 6f)
                    quadraticTo(cx - 4f, mugLeft.y - 15f, cx + 2f, mugLeft.y - 22f)
                    moveTo(cx + 10f, mugLeft.y - 4f)
                    quadraticTo(cx + 5f, mugLeft.y - 12f, cx + 12f, mugLeft.y - 18f)
                }
                drawPath(path = steamPath, color = Color.White.copy(alpha = 0.4f), style = Stroke(width = 3f, cap = StrokeCap.Round))
            }
        }
        "balloon" -> {
            // Playful glowing string holiday balloon
            val balloonCenter = Offset(cx + w * 0.15f, cy + h * 0.1f)
            val balloonRad = w * 0.08f
            
            if (isColoringMode) {
                drawCircle(color = Color.White, radius = balloonRad, center = balloonCenter)
                drawCircle(color = Color.Black, radius = balloonRad, center = balloonCenter, style = Stroke(strokeWidth))
                // string
                drawLine(color = Color.Black, start = Offset(balloonCenter.x, balloonCenter.y + balloonRad), end = Offset(cx, cy + h * 0.28f), strokeWidth = strokeWidth)
            } else {
                // Glow string balloon
                drawCircle(color = accentBrush, radius = balloonRad, center = balloonCenter)
                drawLine(
                    color = Color.White.copy(alpha = 0.3f),
                    start = Offset(balloonCenter.x, balloonCenter.y + balloonRad),
                    end = Offset(cx, cy + h * 0.28f),
                    strokeWidth = 2f
                )
            }
        }
    }
}

private fun DrawScope.drawAnimalCharacter(
    config: FacelessCanvasConfig,
    cx: Float,
    cy: Float,
    w: Float,
    h: Float,
    isColoringMode: Boolean
) {
    val outlineStroke = Stroke(width = 4f)
    val mainColor = if (isColoringMode) Color.White else Color(android.graphics.Color.parseColor(config.outfitColor))
    val backdropColor = if (isColoringMode) Color.White else Color(android.graphics.Color.parseColor(config.hijabColor))

    // Renders a wholesome round animal e.g., Kitten or Teddy
    // 1. Chubby torso
    val torsoTopLeft = Offset(cx - w * 0.22f, cy + h * 0.12f)
    val torsoSize = Size(w * 0.44f, h * 0.28f)
    if (isColoringMode) {
        drawOval(color = Color.White, topLeft = torsoTopLeft, size = torsoSize)
        drawOval(color = Color.Black, topLeft = torsoTopLeft, size = torsoSize, style = outlineStroke)
    } else {
        drawOval(color = mainColor.copy(alpha = 0.85f), topLeft = torsoTopLeft, size = torsoSize)
    }

    // 2. Ears (Pointy for cat, round for bear)
    if (config.creatureType.contains("cat", ignoreCase = true) || config.creatureType.isEmpty()) {
        val earL = Path().apply {
            moveTo(cx - w * 0.18f, cy - h * 0.08f)
            lineTo(cx - w * 0.22f, cy - h * 0.22f)
            lineTo(cx - w * 0.06f, cy - h * 0.14f)
            close()
        }
        val earR = Path().apply {
            moveTo(cx + w * 0.18f, cy - h * 0.08f)
            lineTo(cx + w * 0.22f, cy - h * 0.22f)
            lineTo(cx + w * 0.06f, cy - h * 0.14f)
            close()
        }
        if (isColoringMode) {
            drawPath(path = earL, color = Color.White)
            drawPath(path = earL, color = Color.Black, style = outlineStroke)
            drawPath(path = earR, color = Color.White)
            drawPath(path = earR, color = Color.Black, style = outlineStroke)
        } else {
            drawPath(path = earL, color = mainColor)
            drawPath(path = earR, color = mainColor)
        }
    } else {
        // Round teddy bear ears
        val earLTopLeft = Offset(cx - w * 0.21f, cy - h * 0.19f)
        val earRTopLeft = Offset(cx + w * 0.11f, cy - h * 0.19f)
        if (isColoringMode) {
            drawCircle(color = Color.White, radius = 28f, center = Offset(earLTopLeft.x + 14f, earLTopLeft.y + 14f))
            drawCircle(color = Color.Black, radius = 28f, center = Offset(earLTopLeft.x + 14f, earLTopLeft.y + 14f), style = outlineStroke)
            drawCircle(color = Color.White, radius = 28f, center = Offset(earRTopLeft.x + 14f, earRTopLeft.y + 14f))
            drawCircle(color = Color.Black, radius = 28f, center = Offset(earRTopLeft.x + 14f, earRTopLeft.y + 14f), style = outlineStroke)
        } else {
            drawCircle(color = mainColor, radius = 28f, center = Offset(earLTopLeft.x + 14f, earLTopLeft.y + 14f))
            drawCircle(color = mainColor, radius = 28f, center = Offset(earRTopLeft.x + 14f, earRTopLeft.y + 14f))
        }
    }

    // 3. Perfect Chubby Head (Polos skin / smooth fur - FACELESS)
    val headTopLeft = Offset(cx - w * 0.19f, cy - h * 0.12f)
    val headSize = Size(w * 0.38f, h * 0.24f)
    if (isColoringMode) {
        drawOval(color = Color.White, topLeft = headTopLeft, size = headSize)
        drawOval(color = Color.Black, topLeft = headTopLeft, size = headSize, style = outlineStroke)
    } else {
        // Faceless beautiful smooth cute animal face surface! No whiskers, eyes, mouth.
        drawOval(color = mainColor, topLeft = headTopLeft, size = headSize)
        
        // Soft gradient overlay to make it look 3D cozy / tactile clay
        drawOval(
            brush = Brush.linearGradient(
                colors = listOf(Color.White.copy(alpha = 0.12f), Color.Black.copy(alpha = 0.08f)),
                start = Offset(headTopLeft.x, headTopLeft.y),
                end = Offset(headTopLeft.x + headSize.width, headTopLeft.y + headSize.height)
            ),
            topLeft = headTopLeft,
            size = headSize
        )
    }

    // 4. Cozy scarf inside character description
    val scarfPath = Path().apply {
        moveTo(cx - w * 0.15f, cy + h * 0.07f)
        lineTo(cx + w * 0.15f, cy + h * 0.07f)
        lineTo(cx + w * 0.11f, cy + h * 0.13f)
        lineTo(cx - w * 0.11f, cy + h * 0.13f)
        close()
    }
    if (isColoringMode) {
        drawPath(path = scarfPath, color = Color.White)
        drawPath(path = scarfPath, color = Color.Black, style = outlineStroke)
    } else {
        drawPath(path = scarfPath, color = backdropColor)
    }
}

private fun DrawScope.drawInanimateScene(
    config: FacelessCanvasConfig,
    cx: Float,
    cy: Float,
    w: Float,
    h: Float,
    isColoringMode: Boolean
) {
    // Inanimate objects: Mountains, Trees, Flowers
    val outlineStroke = Stroke(width = 4f)
    val color1 = if (isColoringMode) Color.White else Color(0xFFFF6D00) // Tech orange
    val color2 = if (isColoringMode) Color.White else Color(0xFFA855F7) // Neon purple
    val color3 = if (isColoringMode) Color.White else Color(0xFF06B6D4) // Cyan

    // Renders magnificent flat vector overlapping mountains with custom clouds and shining sun
    if (isColoringMode) {
        // Draw sun
        drawCircle(color = Color.White, radius = w * 0.20f, center = Offset(cx, cy - h * 0.08f))
        drawCircle(color = Color.Black, radius = w * 0.20f, center = Offset(cx, cy - h * 0.08f), style = outlineStroke)

        // Mountain 1 (Back)
        val mountainBack = Path().apply {
            moveTo(cx - w * 0.40f, cy + h * 0.35f)
            lineTo(cx - w * 0.05f, cy - h * 0.05f)
            lineTo(cx + w * 0.30f, cy + h * 0.35f)
            close()
        }
        drawPath(path = mountainBack, color = Color.White)
        drawPath(path = mountainBack, color = Color.Black, style = outlineStroke)

        // Mountain 2 (Front)
        val mountainFront = Path().apply {
            moveTo(cx - w * 0.20f, cy + h * 0.35f)
            lineTo(cx + w * 0.15f, cy + h * 0.04f)
            lineTo(cx + w * 0.45f, cy + h * 0.35f)
            close()
        }
        drawPath(path = mountainFront, color = Color.White)
        drawPath(path = mountainFront, color = Color.Black, style = outlineStroke)
        
    } else {
        // Draw beautiful glowing neon Sun
        drawCircle(
            brush = Brush.verticalGradient(
                colors = listOf(color1, color2)
            ),
            radius = w * 0.22f,
            center = Offset(cx, cy - h * 0.08f)
        )

        // Ambient radial sun ray aura
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(color1.copy(alpha = 0.2f), Color.Transparent),
                center = Offset(cx, cy - h * 0.08f),
                radius = w * 0.38f
            ),
            radius = w * 0.38f,
            center = Offset(cx, cy - h * 0.08f)
        )

        // Mountain 1 (Back)
        val mountainBack = Path().apply {
            moveTo(cx - w * 0.44f, cy + h * 0.35f)
            lineTo(cx - w * 0.08f, cy - h * 0.04f)
            lineTo(cx + w * 0.28f, cy + h * 0.35f)
            close()
        }
        drawPath(path = mountainBack, color = Color(0xFF1E1E38))

        // Mountain 2 (Front)
        val mountainFront = Path().apply {
            moveTo(cx - w * 0.20f, cy + h * 0.35f)
            lineTo(cx + w * 0.16f, cy + h * 0.05f)
            lineTo(cx + w * 0.48f, cy + h * 0.35f)
            close()
        }
        drawPath(
            path = mountainFront,
            brush = Brush.verticalGradient(
                colors = listOf(color2.copy(alpha = 0.9f), Color(0xFF0F172A))
            )
        )

        // Warm mountain mist clouds in center
        drawOval(
            color = color3.copy(alpha = 0.4f),
            topLeft = Offset(cx - w * 0.3f, cy + h * 0.12f),
            size = Size(w * 0.6f, h * 0.08f)
        )
    }
}
