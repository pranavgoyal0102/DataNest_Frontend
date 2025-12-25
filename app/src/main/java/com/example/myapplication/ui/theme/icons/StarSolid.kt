package com.example.myapplication.ui.theme.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val StarSolid: ImageVector
    get() {
        if (_StarSolid != null) return _StarSolid!!
        _StarSolid = ImageVector.Builder(
            name = "StarSolid",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            // Solid star fill
            path(
                fill = SolidColor(Color(0xFF000000)), // Default black, tintable via Icon()
                pathFillType = PathFillType.NonZero
            ) {
                moveTo(12f, 2.5f)
                lineTo(14.9f, 8.2f)
                lineTo(21.2f, 9.1f)
                lineTo(16.7f, 13.5f)
                lineTo(17.8f, 20.0f)
                lineTo(12f, 17.1f)
                lineTo(6.2f, 20.0f)
                lineTo(7.3f, 13.5f)
                lineTo(2.8f, 9.1f)
                lineTo(9.1f, 8.2f)
                close()
            }
        }.build()
        return _StarSolid!!
    }

private var _StarSolid: ImageVector? = null
