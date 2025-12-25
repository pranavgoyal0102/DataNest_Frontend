package com.example.myapplication.ui.theme.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val ImageIcon: ImageVector
    get() {
        if (_ImageIcon != null) return _ImageIcon!!

        _ImageIcon = ImageVector.Builder(
            name = "ImageIcon",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                fill = SolidColor(Color(0xFF000000))
            ) {
                moveTo(19f, 3f)
                horizontalLineTo(5f)
                curveTo(3.9f, 3f, 3f, 3.9f, 3f, 5f)
                verticalLineTo(19f)
                curveTo(3f, 20.1f, 3.9f, 21f, 5f, 21f)
                horizontalLineTo(19f)
                curveTo(20.1f, 21f, 21f, 20.1f, 21f, 19f)
                verticalLineTo(5f)
                curveTo(21f, 3.9f, 20.1f, 3f, 19f, 3f)
                close()
                moveTo(8.5f, 11.5f)
                curveTo(7.67f, 11.5f, 7f, 10.83f, 7f, 10f)
                curveTo(7f, 9.17f, 7.67f, 8.5f, 8.5f, 8.5f)
                curveTo(9.33f, 8.5f, 10f, 9.17f, 10f, 10f)
                curveTo(10f, 10.83f, 9.33f, 11.5f, 8.5f, 11.5f)
                close()
                moveTo(19f, 19f)
                horizontalLineTo(5f)
                verticalLineTo(17f)
                lineTo(9f, 13f)
                lineTo(13f, 17f)
                lineTo(17f, 13f)
                lineTo(19f, 15f)
                verticalLineTo(19f)
                close()
            }
        }.build()

        return _ImageIcon!!
    }

private var _ImageIcon: ImageVector? = null
