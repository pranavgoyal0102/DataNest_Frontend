package com.example.myapplication.ui.theme.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val Question: ImageVector
    get() {
        if (_Question != null) return _Question!!
        
        _Question = ImageVector.Builder(
            name = "Question",
            defaultWidth = 16.dp,
            defaultHeight = 16.dp,
            viewportWidth = 16f,
            viewportHeight = 16f
        ).apply {
            path(
                fill = SolidColor(Color.Black)
            ) {
                moveTo(7.5f, 1f)
                arcToRelative(6.5f, 6.5f, 0f, true, false, 0f, 13f)
                arcToRelative(6.5f, 6.5f, 0f, false, false, 0f, -13f)
                close()
                moveToRelative(0f, 12f)
                arcToRelative(5.5f, 5.5f, 0f, true, true, 0f, -11f)
                arcToRelative(5.5f, 5.5f, 0f, false, true, 0f, 11f)
                close()
                moveToRelative(1.55f, -8.42f)
                arcToRelative(1.84f, 1.84f, 0f, false, false, -0.61f, -0.42f)
                arcTo(2.25f, 2.25f, 0f, false, false, 7.53f, 4f)
                arcToRelative(2.16f, 2.16f, 0f, false, false, -0.88f, 0.17f)
                curveToRelative(-0.239f, 0.1f, -0.45f, 0.254f, -0.62f, 0.45f)
                arcToRelative(1.89f, 1.89f, 0f, false, false, -0.38f, 0.62f)
                arcToRelative(3f, 3f, 0f, false, false, -0.15f, 0.72f)
                horizontalLineToRelative(1.23f)
                arcToRelative(0.84f, 0.84f, 0f, false, true, 0.506f, -0.741f)
                arcToRelative(0.72f, 0.72f, 0f, false, true, 0.304f, -0.049f)
                arcToRelative(0.86f, 0.86f, 0f, false, true, 0.27f, 0f)
                arcToRelative(0.64f, 0.64f, 0f, false, true, 0.22f, 0.14f)
                arcToRelative(0.6f, 0.6f, 0f, false, true, 0.16f, 0.22f)
                arcToRelative(0.73f, 0.73f, 0f, false, true, 0.06f, 0.3f)
                curveToRelative(0f, 0.173f, -0.037f, 0.343f, -0.11f, 0.5f)
                arcToRelative(2.4f, 2.4f, 0f, false, true, -0.27f, 0.46f)
                lineToRelative(-0.35f, 0.42f)
                curveToRelative(-0.12f, 0.13f, -0.24f, 0.27f, -0.35f, 0.41f)
                arcToRelative(2.33f, 2.33f, 0f, false, false, -0.27f, 0.45f)
                arcToRelative(1.18f, 1.18f, 0f, false, false, -0.1f, 0.5f)
                verticalLineToRelative(0.66f)
                horizontalLineTo(8f)
                verticalLineToRelative(-0.49f)
                arcToRelative(0.94f, 0.94f, 0f, false, true, 0.11f, -0.42f)
                arcToRelative(3.09f, 3.09f, 0f, false, true, 0.28f, -0.41f)
                lineToRelative(0.36f, -0.44f)
                arcToRelative(4.29f, 4.29f, 0f, false, false, 0.36f, -0.48f)
                arcToRelative(2.59f, 2.59f, 0f, false, false, 0.28f, -0.55f)
                arcToRelative(1.91f, 1.91f, 0f, false, false, 0.11f, -0.64f)
                arcToRelative(2.18f, 2.18f, 0f, false, false, -0.1f, -0.67f)
                arcToRelative(1.52f, 1.52f, 0f, false, false, -0.35f, -0.55f)
                close()
                moveTo(6.8f, 9.83f)
                horizontalLineToRelative(1.17f)
                verticalLineTo(11f)
                horizontalLineTo(6.8f)
                verticalLineTo(9.83f)
                close()
            }
        }.build()
        
        return _Question!!
    }

private var _Question: ImageVector? = null

