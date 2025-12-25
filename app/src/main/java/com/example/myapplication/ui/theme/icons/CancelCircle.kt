package com.example.myapplication.ui.theme.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val CancelCircle: ImageVector
    get() {
        if (_cancelCircle != null) return _cancelCircle!!
        _cancelCircle = ImageVector.Builder(
            name = "CancelCircle",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            // Outer Circle
            path(
                fill = SolidColor(Color.Transparent),
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 2f
            ) {
                moveTo(12f, 2f)
                arcToRelative(10f, 10f, 0f, true, true, -0.01f, 0f) // full circle
                close()
            }

            // Cross Line 1
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 2f
            ) {
                moveTo(8f, 8f)
                lineTo(16f, 16f)
            }

            // Cross Line 2
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 2f
            ) {
                moveTo(16f, 8f)
                lineTo(8f, 16f)
            }
        }.build()
        return _cancelCircle!!
    }

private var _cancelCircle: ImageVector? = null
