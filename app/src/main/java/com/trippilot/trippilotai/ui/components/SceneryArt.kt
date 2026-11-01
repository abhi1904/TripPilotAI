package com.trippilot.trippilotai.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.trippilot.trippilotai.domain.model.Scene

/**
 * Illustrated destination scenery drawn entirely with Compose Canvas, so the app needs
 * no bundled photos or network images in Phase 1. Swap for real photography later.
 */
@Composable
fun SceneryArt(scene: Scene, modifier: Modifier = Modifier) {
    val palette = paletteFor(scene)
    Canvas(modifier) {
        drawRect(Brush.verticalGradient(listOf(palette.skyTop, palette.skyBottom)))
        drawCircle(palette.sun.copy(alpha = 0.25f), radius = size.minDimension * 0.22f, center = sunCenter(scene))
        drawCircle(palette.sun, radius = size.minDimension * 0.12f, center = sunCenter(scene))
        when (scene) {
            Scene.BEACH, Scene.ISLAND -> drawBeach(palette, scene == Scene.ISLAND)
            Scene.MOUNTAIN -> drawMountains(palette)
            Scene.DESERT -> drawDesert(palette)
            Scene.BACKWATERS -> drawBackwaters(palette)
            Scene.TEMPLE_CITY -> drawTempleCity(palette)
        }
    }
}

private data class ScenePalette(
    val skyTop: Color,
    val skyBottom: Color,
    val sun: Color,
    val far: Color,
    val near: Color,
    val ground: Color,
    val accent: Color,
)

private fun paletteFor(scene: Scene) = when (scene) {
    Scene.BEACH -> ScenePalette(Color(0xFF4FC3D9), Color(0xFFBDEFF0), Color(0xFFFFF3C4), Color(0xFF2B9FB3), Color(0xFF138A87), Color(0xFFF6DDA8), Color(0xFF0B5E4F))
    Scene.ISLAND -> ScenePalette(Color(0xFFFF9E7A), Color(0xFFFFD9A8), Color(0xFFFFF0C9), Color(0xFF3A8F8A), Color(0xFF1F6E6A), Color(0xFFEFC98F), Color(0xFF1B4D45))
    Scene.MOUNTAIN -> ScenePalette(Color(0xFF6DB7D6), Color(0xFFDDF3F6), Color(0xFFFFFBE6), Color(0xFF8FB8C6), Color(0xFF2F6F73), Color(0xFF1F5156), Color(0xFF0F3B3E))
    Scene.DESERT -> ScenePalette(Color(0xFFF7A26B), Color(0xFFFFE1B0), Color(0xFFFFF4D6), Color(0xFFE59A5C), Color(0xFFD07A3E), Color(0xFFB8612E), Color(0xFF8A3F24))
    Scene.BACKWATERS -> ScenePalette(Color(0xFF7FCFC4), Color(0xFFE2F6EE), Color(0xFFFFF6D0), Color(0xFF5BAE8D), Color(0xFF2F8A64), Color(0xFF3FA3A0), Color(0xFF155C43))
    Scene.TEMPLE_CITY -> ScenePalette(Color(0xFFF2A7B4), Color(0xFFFFE6DC), Color(0xFFFFF2E8), Color(0xFF9DB7B4), Color(0xFF4F7C78), Color(0xFF365F5B), Color(0xFFB8322E))
}

private fun DrawScope.sunCenter(scene: Scene) = when (scene) {
    Scene.ISLAND, Scene.DESERT -> Offset(size.width * 0.5f, size.height * 0.55f)
    else -> Offset(size.width * 0.78f, size.height * 0.24f)
}

private fun DrawScope.hills(color: Color, baseline: Float, amplitude: Float, waves: Int, phase: Float = 0f) {
    val path = Path().apply {
        moveTo(0f, size.height)
        lineTo(0f, size.height * baseline)
        val step = size.width / (waves * 2)
        var x = 0f
        var up = phase > 0.5f
        while (x < size.width) {
            val peak = size.height * (baseline - if (up) amplitude else -amplitude / 3)
            quadraticTo(x + step / 2, peak, x + step, size.height * baseline)
            x += step
            up = !up
        }
        lineTo(size.width, size.height)
        close()
    }
    drawPath(path, color)
}

private fun DrawScope.palm(baseX: Float, baseY: Float, height: Float, color: Color) {
    val topX = baseX + height * 0.15f
    val topY = baseY - height
    drawLine(color, Offset(baseX, baseY), Offset(topX, topY), strokeWidth = height * 0.06f)
    listOf(-1f to -0.25f, -0.9f to 0.15f, 1f to -0.2f, 0.85f to 0.2f, 0.1f to -0.45f).forEach { (dx, dy) ->
        val leaf = Path().apply {
            moveTo(topX, topY)
            quadraticTo(topX + dx * height * 0.25f, topY + dy * height * 0.6f - height * 0.15f, topX + dx * height * 0.42f, topY + dy * height * 0.5f + height * 0.12f)
            quadraticTo(topX + dx * height * 0.2f, topY + dy * height * 0.2f, topX, topY)
        }
        drawPath(leaf, color)
    }
}

private fun DrawScope.drawBeach(p: ScenePalette, island: Boolean) {
    hills(p.far, 0.62f, 0.08f, 2)
    drawRect(Brush.verticalGradient(listOf(p.near, p.far)), topLeft = Offset(0f, size.height * 0.64f), size = Size(size.width, size.height * 0.36f))
    repeat(3) { i ->
        val y = size.height * (0.7f + i * 0.06f)
        drawLine(Color.White.copy(alpha = 0.35f), Offset(size.width * (0.1f + i * 0.2f), y), Offset(size.width * (0.35f + i * 0.2f), y), strokeWidth = 3f)
    }
    val sand = Path().apply {
        moveTo(0f, size.height)
        lineTo(0f, size.height * 0.82f)
        quadraticTo(size.width * 0.35f, size.height * 0.74f, size.width * 0.7f, size.height * 0.9f)
        lineTo(size.width * 0.75f, size.height)
        close()
    }
    drawPath(sand, p.ground)
    palm(size.width * 0.12f, size.height * 0.84f, size.height * 0.42f, p.accent)
    palm(size.width * 0.27f, size.height * 0.82f, size.height * 0.3f, p.accent)
    if (island) palm(size.width * 0.88f, size.height * 0.68f, size.height * 0.25f, p.accent)
}

private fun DrawScope.drawMountains(p: ScenePalette) {
    fun peak(left: Float, top: Float, right: Float, color: Color, snow: Boolean) {
        val apex = Offset(size.width * (left + right) / 2, size.height * top)
        drawPath(Path().apply {
            moveTo(size.width * left, size.height)
            lineTo(apex.x, apex.y)
            lineTo(size.width * right, size.height)
            close()
        }, color)
        if (snow) {
            val w = size.width * (right - left) * 0.14f
            drawPath(Path().apply {
                moveTo(apex.x, apex.y)
                lineTo(apex.x - w, apex.y + w * 1.1f)
                lineTo(apex.x - w * 0.3f, apex.y + w * 0.8f)
                lineTo(apex.x + w * 0.2f, apex.y + w * 1.2f)
                lineTo(apex.x + w, apex.y + w * 1.1f)
                close()
            }, Color.White.copy(alpha = 0.9f))
        }
    }
    peak(-0.2f, 0.25f, 0.6f, p.far, snow = true)
    peak(0.3f, 0.18f, 1.2f, p.far, snow = true)
    hills(p.near, 0.72f, 0.08f, 3)
    repeat(9) { i ->
        val x = size.width * (0.04f + i * 0.12f)
        val h = size.height * (0.16f + (i % 3) * 0.04f)
        val base = size.height * 0.92f
        drawPath(Path().apply {
            moveTo(x, base - h)
            lineTo(x - h * 0.28f, base)
            lineTo(x + h * 0.28f, base)
            close()
        }, p.accent)
    }
    drawRect(p.ground, topLeft = Offset(0f, size.height * 0.92f), size = Size(size.width, size.height * 0.08f))
}

private fun DrawScope.drawDesert(p: ScenePalette) {
    hills(p.far, 0.7f, 0.06f, 2)
    // Fort silhouette on the ridge
    val fortBase = size.height * 0.66f
    val fortLeft = size.width * 0.18f
    val unit = size.width * 0.05f
    drawRect(p.accent, topLeft = Offset(fortLeft, fortBase - unit * 1.6f), size = Size(unit * 6f, unit * 1.6f))
    listOf(0f, 2.5f, 5f).forEach { offset ->
        drawRect(p.accent, topLeft = Offset(fortLeft + unit * offset, fortBase - unit * 2.6f), size = Size(unit, unit * 2.6f))
        drawCircle(p.accent, radius = unit * 0.6f, center = Offset(fortLeft + unit * (offset + 0.5f), fortBase - unit * 2.6f))
    }
    hills(p.near, 0.8f, 0.07f, 2, phase = 1f)
    hills(p.ground, 0.9f, 0.05f, 3)
}

private fun DrawScope.drawBackwaters(p: ScenePalette) {
    hills(p.far, 0.6f, 0.05f, 3)
    drawRect(Brush.verticalGradient(listOf(p.ground, p.ground.copy(alpha = 0.7f))), topLeft = Offset(0f, size.height * 0.62f), size = Size(size.width, size.height * 0.38f))
    // Houseboat
    val boatY = size.height * 0.74f
    val boatX = size.width * 0.45f
    val w = size.width * 0.32f
    drawPath(Path().apply {
        moveTo(boatX, boatY)
        lineTo(boatX + w, boatY)
        lineTo(boatX + w * 0.88f, boatY + w * 0.12f)
        lineTo(boatX + w * 0.12f, boatY + w * 0.12f)
        close()
    }, Color(0xFF5A3A22))
    drawPath(Path().apply {
        moveTo(boatX + w * 0.12f, boatY)
        quadraticTo(boatX + w * 0.5f, boatY - w * 0.3f, boatX + w * 0.88f, boatY)
        close()
    }, Color(0xFFC79A5B))
    palm(size.width * 0.12f, size.height * 0.64f, size.height * 0.4f, p.accent)
    palm(size.width * 0.24f, size.height * 0.63f, size.height * 0.3f, p.accent)
    palm(size.width * 0.9f, size.height * 0.62f, size.height * 0.34f, p.accent)
    drawRect(p.near, topLeft = Offset(0f, size.height * 0.6f), size = Size(size.width, size.height * 0.04f))
}

private fun DrawScope.drawTempleCity(p: ScenePalette) {
    hills(p.far, 0.62f, 0.12f, 2)
    hills(p.near, 0.75f, 0.06f, 3)
    // Pagoda
    val cx = size.width * 0.3f
    var y = size.height * 0.8f
    var w = size.width * 0.22f
    repeat(4) {
        drawRect(Color(0xFF4A2A22), topLeft = Offset(cx - w * 0.3f, y - size.height * 0.08f), size = Size(w * 0.6f, size.height * 0.08f))
        drawPath(Path().apply {
            moveTo(cx - w * 0.6f, y - size.height * 0.08f)
            lineTo(cx + w * 0.6f, y - size.height * 0.08f)
            lineTo(cx + w * 0.35f, y - size.height * 0.12f)
            lineTo(cx - w * 0.35f, y - size.height * 0.12f)
            close()
        }, p.accent)
        y -= size.height * 0.12f
        w *= 0.82f
    }
    drawLine(p.accent, Offset(cx, y), Offset(cx, y - size.height * 0.06f), strokeWidth = 4f)
    // Torii gate
    val gx = size.width * 0.72f
    val gy = size.height * 0.9f
    val gw = size.width * 0.16f
    drawRect(p.accent, topLeft = Offset(gx - gw / 2, gy - size.height * 0.22f), size = Size(gw * 0.1f, size.height * 0.22f))
    drawRect(p.accent, topLeft = Offset(gx + gw / 2 - gw * 0.1f, gy - size.height * 0.22f), size = Size(gw * 0.1f, size.height * 0.22f))
    drawRect(p.accent, topLeft = Offset(gx - gw * 0.65f, gy - size.height * 0.24f), size = Size(gw * 1.3f, size.height * 0.03f))
    drawRect(p.accent, topLeft = Offset(gx - gw * 0.55f, gy - size.height * 0.18f), size = Size(gw * 1.1f, size.height * 0.02f))
    drawRect(p.ground, topLeft = Offset(0f, size.height * 0.9f), size = Size(size.width, size.height * 0.1f))
}

@Preview(widthDp = 360)
@Composable
private fun SceneryArtPreview() {
    Column {
        Scene.entries.forEach { scene -> SceneryArt(scene, Modifier.fillMaxWidth().height(120.dp)) }
    }
}
