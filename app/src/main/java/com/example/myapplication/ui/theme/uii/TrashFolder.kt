package com.example.myapplication.ui.theme.uii

import android.content.Intent
import android.net.Uri
import android.text.format.DateFormat
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.composables.Video
import com.example.myapplication.ui.theme.icons.FilePdf
import com.example.myapplication.ui.theme.icons.ImageIcon
import com.example.myapplication.ui.theme.icons.Question
import com.example.myapplication.ui.theme.icons.Restore
import com.example.myapplication.ui.theme.icons.StarSolid
import com.example.myapplication.ui.theme.models.FileStored
import com.example.myapplication.ui.theme.viewModel.RoomViewModel

@Composable
fun TrashBin(roomViewModel: RoomViewModel, navController: NavController) {

    val fileList by roomViewModel.fileList.observeAsState(emptyList())
    val searchFileList by roomViewModel.searchFileList.observeAsState(emptyList())

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row {
                Icon(imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                    contentDescription = "",
                    tint = Color.LightGray,
                    modifier = Modifier
                        .size(22.dp)
                        .clickable {
                            navController.navigateUp()
                        })
                Spacer(modifier = Modifier.width(12.dp))
                Text(text = "Trash", color = Color.White, fontSize = 18.sp)
            }
        }
        LazyColumn {
            val hasSearchResults = searchFileList.isNotEmpty()
            if(hasSearchResults){
                items(fileList){file ->
                    if (file != null && file.isDeleted) {
                        DeletedFileList(file,roomViewModel)
                    }
                }
            }else{
                items(fileList){file ->
                    if (file != null && file.isDeleted) {
                        DeletedFileList(file,roomViewModel)
                    }
                }
            }
        }
    }
}
@Composable
fun DeletedFileList(file : FileStored,roomViewModel: RoomViewModel) {
    val fileType by remember { mutableStateOf(file.fileType) }
    val context = LocalContext.current
    val formattedTime = try {
        val timeInMillis = file.date
        DateFormat.format("hh:mm a", timeInMillis).toString()
    } catch (e: Exception) {
        "--:--"
    }
    Spacer(modifier = Modifier.height(16.dp))
    Box{
        AnimatedVisibility(visible = true,
            enter = fadeIn() + slideInVertically()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20))
                    .clickable {
                        try {
                            val fileUri = Uri.parse(file.path)

                            val intent = Intent(Intent.ACTION_VIEW).apply {
                                setDataAndType(fileUri, file.fileType)
                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            }
                            roomViewModel.search("",0)
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            Toast.makeText(
                                context,
                                "No app found to open this file",
                                Toast.LENGTH_SHORT
                            ).show()
                            e.printStackTrace()
                        }
                    },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ){
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    when {
                        fileType == "image/jpeg" -> {
                            Icon(imageVector = ImageIcon,
                                contentDescription = "",
                                tint = Color(0xff73c2fb),
                                modifier = Modifier.size(30.dp).background(Color(0Xff242527), shape = RoundedCornerShape(30)).padding(3.dp))
                        }
                        fileType == "application/pdf" -> {
                            Icon(imageVector = FilePdf,
                                contentDescription = "",
                                tint = Color(0xffF07B79),
                                modifier = Modifier.size(30.dp).background(Color(0Xff242527), shape = RoundedCornerShape(30)).padding(3.dp))
                        }
                        fileType.startsWith("video") -> {
                            Icon(imageVector = Video,
                                contentDescription = "",
                                tint = Color(0Xff03c04a),
                                modifier = Modifier.size(30.dp).background(Color(0Xff242527), shape = RoundedCornerShape(30)).padding(3.dp))
                        }
                        else -> {
                            Icon(imageVector = Question,
                                contentDescription = "",
                                tint = Color(0xffF07B79),
                                modifier = Modifier.size(30.dp).background(Color(0Xff242527), shape = RoundedCornerShape(30)).padding(3.dp))
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(
                        modifier = Modifier
                    ) {
                        Text(text = file.title,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            softWrap = true,
                            fontSize = 14.sp)
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if(file.isStarred){
                                Icon(imageVector = StarSolid, tint = Color.Yellow,
                                    contentDescription = "",
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Text(text = "Uploaded at   • $formattedTime", fontSize = 12.sp, color = Color.LightGray)
                        }
                    }
                }
                Icon(imageVector = Restore,
                    contentDescription = "",
                    tint = Color.LightGray,
                    modifier = Modifier.clickable {
                        file.isDeleted = false
                        val newFile = file.copy(
                            isDeleted = false
                        )
                        roomViewModel.updateFile(
                            newFile,
                            onResult = { _, _ ->}
                        )
                    }
                )
                Icon(imageVector = Icons.Rounded.Delete,
                    contentDescription = "",
                    tint = Color.LightGray,
                    modifier = Modifier.clickable {
                        roomViewModel.updateFile(
                            file,
                            onResult = {success, message ->
                                if(success){
                                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                                }else{
                                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                                }
                            }
                        )
                    }
                )
            }
        }
    }
}