package com.example.myapplication.ui.theme.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val List_Show: ImageVector
    get() {
        if (_Format_list_bulleted != null) return _Format_list_bulleted!!
        
        _Format_list_bulleted = ImageVector.Builder(
            name = "Format_list_bulleted",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 960f,
            viewportHeight = 960f
        ).apply {
            path(
                fill = SolidColor(Color(0xFF000000))
            ) {
                moveTo(360f, 760f)
                verticalLineToRelative(-80f)
                horizontalLineToRelative(480f)
                verticalLineToRelative(80f)
                close()
                moveToRelative(0f, -240f)
                verticalLineToRelative(-80f)
                horizontalLineToRelative(480f)
                verticalLineToRelative(80f)
                close()
                moveToRelative(0f, -240f)
                verticalLineToRelative(-80f)
                horizontalLineToRelative(480f)
                verticalLineToRelative(80f)
                close()
                moveTo(200f, 800f)
                quadToRelative(-33f, 0f, -56.5f, -23.5f)
                reflectiveQuadTo(120f, 720f)
                reflectiveQuadToRelative(23.5f, -56.5f)
                reflectiveQuadTo(200f, 640f)
                reflectiveQuadToRelative(56.5f, 23.5f)
                reflectiveQuadTo(280f, 720f)
                reflectiveQuadToRelative(-23.5f, 56.5f)
                reflectiveQuadTo(200f, 800f)
                moveToRelative(0f, -240f)
                quadToRelative(-33f, 0f, -56.5f, -23.5f)
                reflectiveQuadTo(120f, 480f)
                reflectiveQuadToRelative(23.5f, -56.5f)
                reflectiveQuadTo(200f, 400f)
                reflectiveQuadToRelative(56.5f, 23.5f)
                reflectiveQuadTo(280f, 480f)
                reflectiveQuadToRelative(-23.5f, 56.5f)
                reflectiveQuadTo(200f, 560f)
                moveToRelative(0f, -240f)
                quadToRelative(-33f, 0f, -56.5f, -23.5f)
                reflectiveQuadTo(120f, 240f)
                reflectiveQuadToRelative(23.5f, -56.5f)
                reflectiveQuadTo(200f, 160f)
                reflectiveQuadToRelative(56.5f, 23.5f)
                reflectiveQuadTo(280f, 240f)
                reflectiveQuadToRelative(-23.5f, 56.5f)
                reflectiveQuadTo(200f, 320f)
            }
        }.build()
        
        return _Format_list_bulleted!!
    }

private var _Format_list_bulleted: ImageVector? = null

