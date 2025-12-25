package com.example.myapplication.ui.theme.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val Star: ImageVector
    get() {
        if (_StarRounded != null) return _StarRounded!!
        _StarRounded = ImageVector.Builder(
            name = "StarRounded",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            // 5-point star, drawn as a stroked path with round joins for rounded corners
            path(
                fill = SolidColor(Color(0x00000000)),
                stroke = SolidColor(Color(0xFF000000)),
                strokeLineWidth = 2.0f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round,
                pathFillType = PathFillType.NonZero
            ) {
                moveTo(12f, 2.5f)           // top
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
        return _StarRounded!!
    }

private var _StarRounded: ImageVector? = null
