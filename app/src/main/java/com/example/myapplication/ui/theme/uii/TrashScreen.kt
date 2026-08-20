package com.example.myapplication.ui.theme.uii

import android.text.format.DateFormat
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material3.HorizontalDivider
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import com.composables.Video
import com.example.myapplication.ui.theme.icons.FilePdf
import com.example.myapplication.ui.theme.icons.ImageIcon
import com.example.myapplication.ui.theme.icons.Question
import com.example.myapplication.ui.theme.models.FileStored
import com.example.myapplication.ui.theme.viewModel.RoomViewModel


@Composable
fun TrashScreen(
    roomViewModel: RoomViewModel
) {

    val deletedFiles by roomViewModel.deletedFiles.collectAsState()

    Scaffold(
    ) { padding ->
        if (deletedFiles.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {

                Text(
                    text = "Trash is empty",
                    fontSize = 18.sp
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {

                items(
                    items = deletedFiles,
                    key = { it.id }
                ) { file ->

                    TrashFileItem(
                        file = file,
                        roomViewModel = roomViewModel
                    )
                }
            }
        }
    }
}
@Composable
private fun TrashFileItem(
    file: FileStored,
    roomViewModel: RoomViewModel
) {

    val fileType = file.mimeType

    var showMenu by remember {
        mutableStateOf(false)
    }

    val formattedTime = try {
        DateFormat.format(
            "hh:mm a",
            file.createdAt
        ).toString()
    } catch (e: Exception) {
        "--:--"
    }

    Spacer(
        modifier = Modifier.height(16.dp)
    )

    Box {

        AnimatedVisibility(
            visible = true,
            enter = fadeIn() + slideInVertically()
        ) {

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp)),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {

                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    when {

                        fileType.startsWith("image/") -> {

                            Icon(
                                imageVector = ImageIcon,
                                contentDescription = null,
                                tint = Color(0xff73c2fb),
                                modifier = Modifier
                                    .size(30.dp)
                                    .background(
                                        Color(0Xff242527),
                                        RoundedCornerShape(30.dp)
                                    )
                                    .padding(3.dp)
                            )
                        }

                        fileType == "application/pdf" -> {

                            Icon(
                                imageVector = FilePdf,
                                contentDescription = null,
                                tint = Color(0xffF07B79),
                                modifier = Modifier
                                    .size(30.dp)
                                    .background(
                                        Color(0Xff242527),
                                        RoundedCornerShape(30.dp)
                                    )
                                    .padding(3.dp)
                            )
                        }

                        fileType.startsWith("video/") -> {

                            Icon(
                                imageVector = Video,
                                contentDescription = null,
                                tint = Color(0Xff03c04a),
                                modifier = Modifier
                                    .size(30.dp)
                                    .background(
                                        Color(0Xff242527),
                                        RoundedCornerShape(30.dp)
                                    )
                                    .padding(3.dp)
                            )
                        }

                        else -> {

                            Icon(
                                imageVector = Question,
                                contentDescription = null,
                                tint = Color(0xffF07B79),
                                modifier = Modifier
                                    .size(30.dp)
                                    .background(
                                        Color(0Xff242527),
                                        RoundedCornerShape(30.dp)
                                    )
                                    .padding(3.dp)
                            )
                        }
                    }

                    Spacer(
                        modifier = Modifier.width(12.dp)
                    )

                    Column {

                        Text(
                            text = file.title,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            fontSize = 14.sp
                        )

                        Text(
                            text = "Deleted • $formattedTime",
                            fontSize = 12.sp,
                            color = Color.LightGray
                        )
                    }
                }

                Box {

                    Icon(
                        imageVector = Icons.Rounded.MoreVert,
                        contentDescription = null,
                        tint = Color.LightGray,
                        modifier = Modifier
                            .clip(RoundedCornerShape(100.dp))
                            .clickable {
                                showMenu = true
                            }
                    )

                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = {
                            showMenu = false
                        }
                    ) {

                        DropdownMenuItem(
                            text = {
                                Text("Restore")
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Rounded.Check,
                                    contentDescription = null
                                )
                            },
                            onClick = {

                                showMenu = false

                                roomViewModel.restoreFromTrash(
                                    file.id
                                )
                            }
                        )

                        DropdownMenuItem(
                            text = {
                                Text("Delete Permanently")
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Rounded.Delete,
                                    contentDescription = null
                                )
                            },
                            onClick = {

                                showMenu = false

                                roomViewModel.purgeFile(
                                    file
                                )
                            }
                        )
                    }
                }
            }
        }
    }
}