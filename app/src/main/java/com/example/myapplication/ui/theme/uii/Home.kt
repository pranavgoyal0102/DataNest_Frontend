package com.example.myapplication.ui.theme.uii

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
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.example.myapplication.ui.theme.viewModel.RoomViewModel
import androidx.compose.runtime.livedata.observeAsState
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
import androidx.navigation.NavController
import com.composables.Video
import com.example.myapplication.ui.theme.icons.Download
import com.example.myapplication.ui.theme.icons.FilePdf
import com.example.myapplication.ui.theme.icons.Grid
import com.example.myapplication.ui.theme.icons.ImageIcon
import com.example.myapplication.ui.theme.icons.List_Show
import com.example.myapplication.ui.theme.icons.Question
import com.example.myapplication.ui.theme.icons.Solid_Folder
import com.example.myapplication.ui.theme.icons.Star
import com.example.myapplication.ui.theme.icons.StarSolid
import com.example.myapplication.ui.theme.mod.FolderEntity
import com.example.myapplication.ui.theme.models.FileStored


@Composable
fun Home(isStarred : Boolean,folderId : Long, roomViewModel: RoomViewModel,navController: NavController, onClick : (Long, Long) -> Unit) {
    var showInGrid by remember { mutableStateOf(true) }
    var navigateUp by remember { mutableStateOf(true) }
    var folder by remember { mutableStateOf<FolderEntity?>(null) }
    var ppid by remember { mutableStateOf<Long?>(null) }

    LaunchedEffect(folderId) {
        folder = roomViewModel.getFolderById(folderId)
        roomViewModel.getFolders(folderId)
        roomViewModel.getFiles(folderId)
        ppid = roomViewModel.getFolderById(folderId)?.parentId
    }
    val orgfolderList by roomViewModel.folderList.observeAsState(emptyList())
    val orgfileList by roomViewModel.fileList.observeAsState(emptyList())

    var folderList : List<FolderEntity> by remember { mutableStateOf(emptyList()) }
    var fileList : List<FileStored> by remember { mutableStateOf(emptyList()) }

    LaunchedEffect(orgfolderList) {
        folderList = orgfolderList
    }
    LaunchedEffect(orgfileList) {
        fileList = orgfileList as List<FileStored>
    }

    val searchFolderList by roomViewModel.searchFolderList.observeAsState(emptyList())
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
            if(folderId == 1L && folderId != 2L){
                Text(text = "Files", color = Color.White, fontSize = 18.sp)
            }else{
                Row {
                    Icon(imageVector = Icons.Rounded.ArrowBack,
                        contentDescription = "",
                        tint = Color.LightGray,
                        modifier = Modifier
                            .size(22.dp)
                            .clickable {
                                if (folderId == 2L) {
                                    onClick(1, -1)
                                } else {
                                    folder?.parentId?.let { parentId ->
                                        onClick(parentId, ppid ?: -1)
                                    }
                                }
                            })
                    Spacer(modifier = Modifier.width(12.dp))
                    folder?.let { Text(text = it.folderName, color = Color.White, fontSize = 18.sp) }
                }
            }
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
            val hasSearchResults = searchFileList.isNotEmpty() || searchFolderList.isNotEmpty()
            if(hasSearchResults){
                if(!isStarred){
                    items(searchFolderList, key = { "folder_${it.id}" }) { folder ->
                        if(!folder.isDeleted){
                            FolderList(folder, roomViewModel, onClick = { onClick(folder.id, folder.parentId ?: -1) })
                        }
                    }
                    items(searchFileList, key = { "file_${it?.id}" }) { file ->
                        if (file != null && !file.isDeleted) {
                            FileList(file, roomViewModel)
                        }
                    }
                }else{
                    items(searchFolderList){folder ->
                        if(folder.isStarred && !folder.isDeleted){
                            FolderList(folder, roomViewModel, onClick = { onClick(folder.id, folder.parentId ?: -1) })
                        }
                    }
                    items(searchFileList){file ->
                        if (file != null&& !file.isDeleted)  {
                            if(file.isStarred){
                                FileList(file, roomViewModel)
                            }
                        }
                    }
                }
            }else{
                if(!isStarred){
                    items(folderList, key = { "folder_${it.id}" }) { folder ->
                        if(!folder.isDeleted){
                            FolderList(folder, roomViewModel, onClick = { onClick(folder.id, folder.parentId ?: -1);roomViewModel.search("",folderId) })
                        }
                    }
                    items(fileList, key = { "file_${it?.id}" }) { file ->
                        if (file != null && !file.isDeleted) {
                            FileList(file, roomViewModel)
                        }
                    }
                }else{
                    items(folderList){folder ->
                        if(folder.isStarred && !folder.isDeleted){
                            FolderList(folder, roomViewModel, onClick = { onClick(folder.id, folder.parentId ?: -1) })
                        }
                    }
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
fun FolderList(folder : FolderEntity,roomViewModel: RoomViewModel,onClick : () -> Unit) {
    val context = LocalContext.current
    var isStar by remember { mutableStateOf(folder.isStarred) }
    var showDelete by remember { mutableStateOf(false) }
    var showRename by remember { mutableStateOf(false) }
    var rename by remember { mutableStateOf(folder.folderName) }
    var showDropDownMenu by remember { mutableStateOf(false) }
    val formattedTime = try {
        val timeInMillis = folder.createdAt
        DateFormat.format("hh:mm a", timeInMillis).toString()
    } catch (e: Exception) {
        "--:--"
    }
    Spacer(modifier = Modifier.height(16.dp))
    Box(){
        AnimatedVisibility(visible = true) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20))
                    .clickable {
                        onClick()
                    },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ){
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Solid_Folder,
                        contentDescription = "",
                        tint = Color.Gray,
                        modifier = Modifier.size(40.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(text = folder.folderName)
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if(folder.isStarred){
                                Icon(imageVector = StarSolid, tint = Color.Yellow,
                                    contentDescription = "",
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Text(text = "Created at   • $formattedTime", fontSize = 12.sp,color = Color.LightGray)
                        }
                    }
                }
                Box{
                    Icon(imageVector = Icons.Rounded.MoreVert,
                        contentDescription = "",
                        tint = Color.LightGray,
                        modifier = Modifier.clickable {
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
                        Text(text = "Rename Folder")
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
                                val newFolder = folder.copy(folderName = rename)
                                roomViewModel.updateFolder(newFolder) { success, message ->
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
                            Text(text = folder.folderName,
                                fontSize = 24.sp,
                                modifier = Modifier.padding(10.dp),
                                lineHeight = 30.sp,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis)
                            HorizontalDivider()
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
                                            roomViewModel.deleteFolder(folder)
                                            Toast.makeText(
                                                context,
                                                "Folder Deleted Permanently",
                                                Toast.LENGTH_SHORT
                                            ).show()
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
                                        .clickable {
                                            isStar = !isStar
                                            val newFolder = folder.copy(isStarred = isStar)
                                            roomViewModel.updateFolder(
                                                newFolder,
                                                onResult = {success, message ->}
                                            )
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FileList(file : FileStored,roomViewModel: RoomViewModel) {
    var isStar by remember { mutableStateOf(file.isStarred) }
    var rename by remember { mutableStateOf(file.title) }
    var showRename by remember { mutableStateOf(false) }
    val fileType by remember { mutableStateOf(file.fileType) }
    var showDropDownMenu by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val formattedTime = try {
        val timeInMillis = file.date
        DateFormat.format("hh:mm a", timeInMillis).toString()
    } catch (e: Exception) {
        "--:--"
    }
    Spacer(modifier = Modifier.height(16.dp))
    Box(){
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
                            roomViewModel.search("", 0)
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
                        }
                    }
                }
                if(!file.isSynced){
                    Icon(imageVector = MaterialIconsSync_disabled,
                        contentDescription = "", tint = Color.Red,
                        modifier = Modifier.size(20.dp))
                }
                Box(
                    modifier = Modifier
                ){
                    Icon(imageVector = Icons.Rounded.MoreVert,
                        contentDescription = "",
                        tint = Color.LightGray,
                        modifier = Modifier.clickable {
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
                                val newFile = FileStored(
                                    id = file.id,
                                    title = rename,
                                    folderId = file.folderId,
                                    fileType = file.fileType,
                                    path = file.path,
                                    isStarred = file.isStarred
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
                                            roomViewModel.deleteFile(file)
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
                                        .clickable { shareFile(context, file.path, file.fileType) },
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
                                            file.isStarred = !file.isStarred
                                            roomViewModel.updateFile(
                                                file,
                                                onResult = { success, message -> }
                                            )
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
