package com.example.myapplication.ui.theme.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val Download: ImageVector
    get() {
        if (_Download != null) return _Download!!
        _Download = ImageVector.Builder(
            name = "Download",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            // Vertical bar (stem of the arrow)
            path(
                fill = SolidColor(Color(0xFF000000)),
                pathFillType = PathFillType.NonZero
            ) {
                moveTo(11f, 2f)
                lineTo(13f, 2f)
                lineTo(13f, 14f)
                lineTo(11f, 14f)
                close()
            }

            // Downward arrowhead
            path(
                fill = SolidColor(Color(0xFF000000)),
                pathFillType = PathFillType.NonZero
            ) {
                moveTo(8f, 11f)
                lineTo(6.5f, 12.5f)
                lineTo(12f, 18f)
                lineTo(17.5f, 12.5f)
                lineTo(16f, 11f)
                lineTo(12f, 15f)
                close()
            }

            // Bottom horizontal tray line
            path(
                fill = SolidColor(Color(0xFF000000)),
                pathFillType = PathFillType.NonZero
            ) {
                moveTo(5f, 20f)
                lineTo(19f, 20f)
                lineTo(19f, 22f)
                lineTo(5f, 22f)
                close()
            }
        }.build()
        return _Download!!
    }

private var _Download: ImageVector? = null
