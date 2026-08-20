package com.pranavgoyal.datanest.ui.theme.uii

import MaterialIconsSync_disabled
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.text.format.DateFormat
import android.util.Log
import android.widget.Toast
import androidx.compose.animation.AnimatedContent
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.pranavgoyal.datanest.ui.theme.viewModel.RoomViewModel
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.composables.Video
import com.pranavgoyal.datanest.ui.theme.icons.Download
import com.pranavgoyal.datanest.ui.theme.icons.FilePdf
import com.pranavgoyal.datanest.ui.theme.icons.Grid
import com.pranavgoyal.datanest.ui.theme.icons.ImageIcon
import com.pranavgoyal.datanest.ui.theme.icons.List_Show
import com.pranavgoyal.datanest.ui.theme.icons.Question
import com.pranavgoyal.datanest.ui.theme.icons.Star
import com.pranavgoyal.datanest.ui.theme.icons.StarSolid
import com.pranavgoyal.datanest.ui.theme.models.FileStored
import com.pranavgoyal.datanest.ui.theme.models.SyncStatus
import java.io.File


@Composable
fun Home(isStarred : Boolean,roomViewModel: RoomViewModel) {
    var showInGrid by remember { mutableStateOf(true) }
    val fileList by roomViewModel.fileList.collectAsState()
    val searchFileList by roomViewModel.searchFileList.collectAsState()



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
            Text(text = "Files", color = Color.White, fontSize = 18.sp)
            AnimatedContent(
                targetState = showInGrid
            ) { target ->
                when(target){
                    false -> Icon(imageVector = Grid,
                        contentDescription = "",
                        modifier = Modifier.clickable { showInGrid = !showInGrid },
                        tint = Color.LightGray)

                    true -> Icon(imageVector = List_Show,
                        contentDescription = "",
                        modifier = Modifier.clickable { showInGrid = !showInGrid },
                        tint = Color.LightGray
                    )

                }

            }
        }
        LazyColumn {
            val hasSearchResults = searchFileList.isNotEmpty()
            if(hasSearchResults){
                if(!isStarred){
                    items(searchFileList, key = { "file_${it?.id}" }) { file ->
                        if (file != null && !file.isDeleted) {
                            FileList(file, roomViewModel)
                        }
                    }
                }else{
                    items(searchFileList){file ->
                        if (file != null && !file.isDeleted)  {
                            if(file.isStarred){
                                FileList(file, roomViewModel)
                            }
                        }
                    }
                }
            }else{
                if(!isStarred){
                    items(fileList, key = { "file_${it?.id}" }) { file ->
                        if (file != null && !file.isDeleted) {
                            FileList(file, roomViewModel)
                        }
                    }
                }else{
                    items(fileList){file ->
                        if (file != null && !file.isDeleted) {
                            if(file.isStarred){
                                FileList(file, roomViewModel)
                            }
                        }
                    }
                }
            }
        }

    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FileList(file : FileStored,roomViewModel: RoomViewModel) {
    var isStar by remember { mutableStateOf(file.isStarred) }
    var rename by remember { mutableStateOf(file.title) }
    var showRename by remember { mutableStateOf(false) }
    val fileType by remember { mutableStateOf(file.mimeType) }
    var showDropDownMenu by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val formattedTime = try {
        val timeInMillis = file.createdAt
        DateFormat.format("hh:mm a", timeInMillis).toString()
    } catch (e: Exception) {
        "--:--"
    }
    Log.d("OPEN", "URI = ${file.uri}")
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
                            val localFile = File(file.uri)

                            Log.d(
                                "OPEN",
                                "exists=${localFile.exists()} path=${localFile.absolutePath}"
                            )

                            val fileUri = FileProvider.getUriForFile(
                                context,
                                "${context.packageName}.provider",
                                localFile
                            )

                            val intent = Intent(Intent.ACTION_VIEW).apply {
                                setDataAndType(fileUri, file.mimeType)
                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            }

                            context.startActivity(intent)
                            roomViewModel.search("")
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
                                modifier = Modifier
                                    .size(30.dp)
                                    .background(Color(0Xff242527), shape = RoundedCornerShape(30))
                                    .padding(3.dp))
                        }
                        fileType == "application/pdf" -> {
                            Icon(imageVector = FilePdf,
                                contentDescription = "",
                                tint = Color(0xffF07B79),
                                modifier = Modifier
                                    .size(30.dp)
                                    .background(Color(0Xff242527), shape = RoundedCornerShape(30))
                                    .padding(3.dp))
                        }
                        fileType.startsWith("video") -> {
                            Icon(imageVector = Video,
                                contentDescription = "",
                                tint = Color(0Xff03c04a),
                                modifier = Modifier
                                    .size(30.dp)
                                    .background(Color(0Xff242527), shape = RoundedCornerShape(30))
                                    .padding(3.dp))
                        }
                        else -> {
                            Icon(imageVector = Question,
                                contentDescription = "",
                                tint = Color(0xffF07B79),
                                modifier = Modifier
                                    .size(30.dp)
                                    .background(Color(0Xff242527), shape = RoundedCornerShape(30))
                                    .padding(3.dp))
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
                            Spacer(modifier = Modifier.width(6.dp))
                            if (file.syncStatus != SyncStatus.SYNCED) {
                                Icon(
                                    imageVector = MaterialIconsSync_disabled,
                                    contentDescription = null,
                                    tint = Color.Red,
                                    modifier = Modifier.size(12.dp)
                                )
                            }
                        }
                    }
                }
                Box(
                    modifier = Modifier
                ){
                    Icon(imageVector = Icons.Rounded.MoreVert,
                        contentDescription = "",
                        tint = Color.LightGray,
                        modifier = Modifier.clip(RoundedCornerShape(100)).clickable {
                            showDropDownMenu = true
                        }
                    )
                }
            }
        }
        if(showRename){
            BasicAlertDialog(
                onDismissRequest = { showRename = false },
                modifier = Modifier
                    .width(400.dp)
                    .height(180.dp)
                    .clip(RoundedCornerShape(20))
                    .background(Color(0Xff18191B))
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Column(
                        modifier = Modifier.padding(15.dp)
                    ) {
                        Text(text = "Rename File")
                        OutlinedTextField(
                            onValueChange = { rename = it },
                            value = rename,
                            placeholder = {Text(text = "New title")}
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(onClick = {
                                showRename = false
                            }) {
                                Text(text = "Cancel")
                            }
                            TextButton(onClick = {
                                val newFile = file.copy(
                                    title = rename,
                                    updatedAt = System.currentTimeMillis()
                                )
                                roomViewModel.updateFile(newFile) { success, message ->
                                    if (!success) {
                                        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                                    } else {
                                        showRename = false
                                        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }) {
                                Text(text = "Rename")
                            }
                        }
                    }
                }
            }
        }
        if(showDropDownMenu){
            ModalBottomSheet(onDismissRequest = {showDropDownMenu = false}, modifier = Modifier.heightIn(min = 250.dp,max = 500.dp)) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize()
                ) {
                    item{
                        Column(
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Text(text = file.title,
                                fontSize = 24.sp,
                                modifier = Modifier.padding(10.dp),
                                lineHeight = 30.sp,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis)
                            Divider()
                            Spacer(modifier = Modifier.height(10.dp))
                            Column(
                                modifier = Modifier.padding(horizontal = 25.dp),
                                verticalArrangement = Arrangement.spacedBy(18.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { showRename = true },
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(imageVector = Icons.Rounded.Edit, contentDescription = "",modifier = Modifier.size(30.dp))
                                    Spacer(modifier = Modifier.width(18.dp))
                                    Text(text = "Rename", fontSize = 26.sp)
                                }
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            roomViewModel.moveToTrash(file.id)
                                        },
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(imageVector = Icons.Rounded.Delete, contentDescription = "",modifier = Modifier.size(30.dp))
                                    Spacer(modifier = Modifier.width(18.dp))
                                    Text(text = "Delete",fontSize = 26.sp)
                                }
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { shareFile(context, file.uri, file.mimeType) },
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(imageVector = Icons.Rounded.Share, contentDescription = "",modifier = Modifier.size(30.dp))
                                    Spacer(modifier = Modifier.width(18.dp))
                                    Text(text = "Share",fontSize = 26.sp)
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(imageVector = Download, contentDescription = "",modifier = Modifier.size(30.dp))
                                    Spacer(modifier = Modifier.width(18.dp))
                                    Text(text = "Download",fontSize = 26.sp)
                                }
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            isStar = !isStar
                                            val updatedFile = file.copy(
                                                isStarred = !file.isStarred,
                                                updatedAt = System.currentTimeMillis()
                                            )
                                            roomViewModel.updateFile(updatedFile) { _, _ -> }
                                        },
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    AnimatedContent(isStar) {
                                            target ->
                                        when(target){
                                            false -> Row{
                                                Icon(imageVector = Star, contentDescription = "",modifier = Modifier.size(30.dp))
                                                Spacer(modifier = Modifier.width(18.dp))
                                                Text(text = "Starred",fontSize = 26.sp)
                                            }
                                            true -> Row{
                                                Icon(imageVector = StarSolid, contentDescription = "", tint = Color.Yellow,modifier = Modifier.size(30.dp))
                                                Spacer(modifier = Modifier.width(18.dp))
                                                Text(text = "Starred",fontSize = 26.sp)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

fun shareFile(context: Context, uriString: String, mimeType: String) {
    val uri = Uri.parse(uriString)

    val shareIntent = Intent(Intent.ACTION_SEND).apply {
        type = mimeType
        putExtra(Intent.EXTRA_STREAM, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }

    context.startActivity(Intent.createChooser(shareIntent, "Share File"))
}
