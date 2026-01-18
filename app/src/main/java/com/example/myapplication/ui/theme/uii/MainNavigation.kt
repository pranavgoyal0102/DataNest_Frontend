package com.example.myapplication.ui.theme.uii


import android.app.Activity
import coil.compose.AsyncImage
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.composables.Video
import com.example.myapplication.ui.theme.icons.Account_circle
import com.example.myapplication.ui.theme.icons.Camera
import com.example.myapplication.ui.theme.icons.CancelCircle
import com.example.myapplication.ui.theme.icons.Feedback
import com.example.myapplication.ui.theme.icons.FilePdf
import com.example.myapplication.ui.theme.icons.Folder
import com.example.myapplication.ui.theme.icons.ImageIcon
import com.example.myapplication.ui.theme.icons.Question
import com.example.myapplication.ui.theme.icons.Solid_Folder
import com.example.myapplication.ui.theme.icons.Upload
import com.example.myapplication.ui.theme.icons.sideBar
import com.example.myapplication.ui.theme.mod.FolderEntity
import com.example.myapplication.ui.theme.models.FileStored
import com.example.myapplication.ui.theme.viewModel.RoomViewModel
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.rememberAnimatedNavController
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import org.jetbrains.annotations.Async
import java.io.File
import java.util.UUID
import kotlin.contracts.contract

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun MainNavigation(roomViewModel: RoomViewModel, sharedUri: Uri? = null,
                   sharedUris: List<Uri>? = null) {
    val navController = rememberAnimatedNavController()
    var search by remember { mutableStateOf("") }
    var rightDrawer by remember { mutableStateOf(false) }
    var showCancel by remember { mutableStateOf(false) }
    var showCreateFolder by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableStateOf("Everything") }
    var isStarred by remember { mutableStateOf(false) }
    var createFolderName by remember { mutableStateOf("New folder") }
    var showBottomSheet by remember { mutableStateOf(false)}
    var showCameraOptions by remember { mutableStateOf(false) }
    var mediaUri by remember { mutableStateOf<Uri?>(null) }
    var folderId by remember { mutableLongStateOf(1) }
    var currentParentId by remember { mutableLongStateOf(-1) }
    var selectedFiles by remember { mutableStateOf<List<Triple<String, String, Uri>>>(emptyList()) }
    var showReviewScreen by remember { mutableStateOf(false) }
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp
    val halfWidth = screenWidth / 2
    val underlineOffsetFraction by animateFloatAsState(
        targetValue = if (selectedTab == "Everything") 0f else 1f,
        label = "underline_offset_fraction"
    )
    val auth = FirebaseAuth.getInstance()
    val user = remember { mutableStateOf(auth.currentUser) }
    LaunchedEffect(sharedUri, sharedUris) {
        folderId = 1
        when {
            sharedUri != null -> {
                navController.navigate("reviewscreen?uri=${Uri.encode(sharedUri.toString())}")
            }
            !sharedUris.isNullOrEmpty() -> {
                val encodedList = sharedUris.joinToString(",") { Uri.encode(it.toString()) }
                navController.navigate("review_multiple_screen?uris=$encodedList")
            }
        }
    }
    val context  = LocalContext.current
    var captureMode by remember { mutableStateOf("") }
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            val photoIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            val videoIntent = Intent(MediaStore.ACTION_VIDEO_CAPTURE)

            val chooser = Intent.createChooser(photoIntent, "Capture Media")
            chooser.putExtra(Intent.EXTRA_INITIAL_INTENTS, arrayOf(videoIntent))
        } else {
            Toast.makeText(context, "Camera permission denied", Toast.LENGTH_SHORT).show()
        }
    }
    val takePicture = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success && mediaUri != null) {
            selectedFiles = listOf(
                Triple(
                    File(mediaUri!!.path ?: "captured_image").name,
                    "image/jpeg",
                    mediaUri!!
                )
            )
            showCameraOptions = false
            navController.navigate("reviewscreen")

        }
    }
    val takeVideo = rememberLauncherForActivityResult(ActivityResultContracts.CaptureVideo()) { success ->
        if (success && mediaUri != null) {
            selectedFiles = listOf(
                Triple(
                    File(mediaUri!!.path ?: "captured_video").name,
                    "video/mp4",
                    mediaUri!!
                )
            )
            showCameraOptions = false
            navController.navigate("reviewscreen")
        }
    }
    val tabs = listOf("Everything", "Starred")
    var folderName by remember { mutableStateOf("") }
    LaunchedEffect(folderId) {
        val folder = roomViewModel.getFolderById(folderId)
        folderName = folder?.folderName ?: "Unknown Folder"
    }
    LaunchedEffect(selectedTab) {
        isStarred = selectedTab == "Starred"
    }
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenMultipleDocuments()
    ) { uris: List<Uri> ->
        val files = mutableListOf<Triple<String, String, Uri>>()
        uris.forEach { uri ->
            try {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (_: SecurityException) { }

            var fileName = "Unknown"
            val fileType = context.contentResolver.getType(uri) ?: "Unknown"

            val cursor = context.contentResolver.query(uri, null, null, null, null)
            cursor?.use { c ->
                val nameIndex = c.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (c.moveToFirst() && nameIndex >= 0) {
                    fileName = c.getString(nameIndex)
                }
            }

            files.add(Triple(fileName, fileType, uri))
        }
        if (files.isNotEmpty()) {
            selectedFiles = files
            showReviewScreen = true
        }
    }
    ModalNavigationDrawer(drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Column(
                    modifier = Modifier
                ) {
                    Text(text = "DataNest",
                        fontSize = 36.sp,
                        modifier = Modifier.padding(12.dp)
                    )
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 6.dp))
                    NavigationDrawerItem(
                        icon = { Icon(imageVector = Icons.Default.Settings, contentDescription = "setting") },
                        label = { Text("Settings", fontSize = 18.sp) },
                        selected = false,
                        onClick = {
                            scope.launch { drawerState.close() }
                        }
                    )
                    NavigationDrawerItem(
                        icon = { Icon(imageVector = Icons.Default.Delete, contentDescription = "") },
                        label = { Text("Bin",fontSize = 18.sp) },
                        selected = false,
                        onClick = {
                            scope.launch { drawerState.close() }
                            folderId = 2
                            currentParentId = -1
                        }
                    )
                    NavigationDrawerItem(
                        icon = { Icon(imageVector = Feedback,
                            contentDescription = "",
                            ) },
                        label = { Text("Feedback",fontSize = 18.sp) },
                        selected = false,
                        onClick = {
                            scope.launch { drawerState.close() }
                        }
                    )
                    NavigationDrawerItem(
                        icon = { Icon(imageVector = Question,
                            contentDescription = "",
                            modifier = Modifier.size(25.dp)) },
                        label = { Text("About",fontSize = 18.sp) },
                        selected = false,
                        onClick = {
                            scope.launch { drawerState.close() }
                        }
                    )
                }
            }
    }) {
        Scaffold(
            modifier = Modifier.fillMaxSize()
                .background(Color(0Xff18191B))
            , topBar = {
                if(currentRoute?.startsWith("profile") == true){
                    AnimatedVisibility(currentRoute.startsWith("profile") == true)  {
                        Box{
                            Column(
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Spacer(modifier = Modifier.height(60.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 5.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("PROFILE", fontSize = 30.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                    Icon(imageVector = Icons.Rounded.Close,
                                        contentDescription = "",
                                        modifier = Modifier.size(30.dp).clip(RoundedCornerShape(100)).clickable {
                                            navController.navigate("home")
                                            showReviewScreen = false
                                        })
                                }
                            }
                        }
                    }
                }else{
                    if (currentRoute?.startsWith("reviewscreen") == false) {
                        Box (
                            modifier = Modifier.background(Color(0Xff242527))
                        ){
                            Column(
                            ){
                                Spacer(modifier = Modifier.height(60.dp))
                                Row {
                                    OutlinedTextField(value = search,
                                        onValueChange = {search = it
                                            roomViewModel.search(search,folderId)
                                        },
                                        placeholder = {
                                            Text(text = "Search in Nest")
                                        },
                                        modifier = Modifier.fillMaxWidth().padding(6.dp),
                                        singleLine = true,
                                        shape = RoundedCornerShape(30),
                                        leadingIcon = {
                                            Icon(imageVector = sideBar,
                                                contentDescription = "side drawer button",
                                                modifier = Modifier.size(18.dp).clickable {
                                                    scope.launch { drawerState.open() }
                                                },
                                                tint = Color.White)
                                        },
                                        trailingIcon = {
                                            if(search == "") showCancel = true else showCancel = false
                                            if(user.value == null){
                                                AnimatedContent(
                                                    targetState = showCancel
                                                ) {
                                                        target ->
                                                    when(target){
                                                        false -> Icon(imageVector = Icons.Default.Close,
                                                            contentDescription = "Cancel",
                                                            tint = Color.White,
                                                            modifier= Modifier.size(26.dp).clickable {
                                                                search = ""
                                                                roomViewModel.search("", folderId)
                                                            }
                                                        )
                                                        true -> Icon(imageVector = Account_circle,
                                                            contentDescription = "Profile",
                                                            tint = Color.Gray,
                                                            modifier= Modifier.size(30.dp).clip(RoundedCornerShape(100)).clickable {
                                                                navController.navigate("profile")
                                                            }
                                                        )
                                                    }
                                                }
                                            }
                                            else{
                                                AnimatedContent(
                                                    targetState = showCancel
                                                ) {
                                                        target ->
                                                    when(target){
                                                        false -> Icon(imageVector = Icons.Default.Close,
                                                            contentDescription = "Cancel",
                                                            tint = Color.White,
                                                            modifier= Modifier.size(26.dp).clickable {
                                                                search = ""
                                                                roomViewModel.search("", folderId)
                                                            }
                                                        )
                                                        true -> AsyncImage(
                                                            model = user.value!!.photoUrl,
                                                            contentDescription = "Profile Image",
                                                            modifier = Modifier.size(30.dp).clip(RoundedCornerShape(100)).clickable {
                                                                navController.navigate("profile")
                                                            }
                                                        )

                                                    }
                                                }
                                            }
                                        }
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Column {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceAround
                                    ) {
                                        tabs.forEach { tab ->
                                            Column(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .clickable { selectedTab = tab }
                                                    .padding(vertical = 12.dp),
                                                horizontalAlignment = Alignment.CenterHorizontally
                                            ) {
                                                Text(text = tab, fontSize = 15.sp,
                                                    color =  if(selectedTab == tab) Color(0xff1E90FF) else Color.White)
                                            }
                                        }
                                    }
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(4.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .width(halfWidth).height(5.dp)
                                                .align(Alignment.BottomStart)
                                                .offset(x = halfWidth * underlineOffsetFraction)
                                                .background(Color(0xff1E90FF))
                                        )
                                    }
                                    Divider()
                                }
                            }
                        }
                    }else{
                        Box(
                            modifier = Modifier.background(Color(0Xff242527))
                        ){
                            Column(
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Spacer(modifier = Modifier.height(60.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 5.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Icon(imageVector = Icons.Rounded.Close,
                                        contentDescription = "",
                                        modifier = Modifier.clickable {
                                            navController.navigateUp()
                                            showReviewScreen = false
                                        })
                                    Text("Review Selected Files", fontSize = 20.sp, color = Color.White)
                                    TextButton(onClick = {
                                        if(sharedUri != null){
                                            val new : FileInfo =
                                                getFileInfo(context, sharedUri)
                                            val newfile = Triple(new.name, new.mimeType, new.uri)
                                            val fileToStore = FileStored(
                                                title = newfile.first,
                                                fileType = newfile.second,
                                                path = newfile.third.toString(),
                                                folderId = 1,
                                                isSynced = false,
                                                syncId = UUID.randomUUID().toString()
                                            )
                                            roomViewModel.saveFile(fileToStore)
                                        }else{
                                            for (file in selectedFiles) {
                                                val fileToStore = FileStored(
                                                    title = file.first,
                                                    fileType = file.second,
                                                    path = file.third.toString(),
                                                    folderId = folderId,
                                                    isSynced = false,
                                                    syncId = UUID.randomUUID().toString()
                                                )
                                                roomViewModel.saveFile(fileToStore)
                                            }
                                        }
                                        showReviewScreen = false
                                        navController.navigateUp()
                                    }) {
                                        Text(text = "Save", fontSize = 22.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            },
            floatingActionButton = {
                if(currentRoute != "reviewscreen" && currentRoute != "profile"){
                    FloatingActionButton(onClick = {
                        showBottomSheet = true
                    },
                        modifier = Modifier.size(70.dp).offset(x=-15.dp, y=-15.dp),
                        contentColor = Color.White,
                        containerColor = Color(0Xff242527)) {
                        Icon(imageVector = Icons.Default.Add,
                            contentDescription = "Settings",
                            tint = Color.White,
                            modifier = Modifier.size(45.dp)
                        )
                    }
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier.fillMaxSize().background(Color(0Xff18191B))
            ){
                if(showBottomSheet){
                    ModalBottomSheet(onDismissRequest = { showBottomSheet = false}) {
                        Box(
                            modifier = Modifier.height(200.dp)
                        ){
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Card(
                                    colors = CardDefaults.cardColors(Color.Transparent),
                                    modifier = Modifier.weight(1f).wrapContentSize().clickable {
                                        showCreateFolder = true
                                    }){
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                    ) {
                                        Icon(imageVector = Folder,
                                            contentDescription = "",
                                            modifier = Modifier.size(40.dp).
                                            border(width = 1.dp,
                                                color = Color.Gray,
                                                shape = RoundedCornerShape(100)).padding(8.dp))
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(text= "Folder", fontSize = 20.sp)
                                    }
                                }
                                Card(
                                    colors = CardDefaults.cardColors(Color.Transparent),
                                    modifier = Modifier.weight(1f).wrapContentSize().clickable {
                                        launcher.launch(arrayOf("*/*"))
                                        showReviewScreen = true
                                        navController.navigate("reviewscreen")
                                        showBottomSheet = false
                                    }
                                ){
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Icon(imageVector = Upload,
                                            contentDescription = "",
                                            modifier = Modifier.size(40.dp).
                                            border(width = 1.dp,
                                                color = Color.Gray,
                                                shape = RoundedCornerShape(100)).padding(8.dp))
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(text= "Upload", fontSize = 20.sp)
                                    }
                                }
                                Card(
                                    colors = CardDefaults.cardColors(Color.Transparent),
                                    modifier = Modifier
                                        .weight(1f)
                                        .wrapContentSize()
                                        .clickable {
                                            cameraPermissionLauncher.launch(android.Manifest.permission.CAMERA)
                                            showCameraOptions = true
                                        }
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(
                                            imageVector = Camera,
                                            contentDescription = "",
                                            modifier = Modifier
                                                .size(40.dp)
                                                .border(1.dp, Color.Gray, RoundedCornerShape(100))
                                                .padding(8.dp)
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(text = "Camera", fontSize = 20.sp)
                                    }
                                }

                            }
                        }
                    }
                }
                if(showCameraOptions){
                    AlertDialog(
                        onDismissRequest = {showCameraOptions  = false},
                        title = { Text("Capture Media") },
                        text = { Text("Do you want to capture Photo or Video?") },
                        confirmButton = {
                            TextButton(onClick = {
                                captureMode = "photo"
                                mediaUri = createMediaUri(context, "jpg")
                                takePicture.launch(mediaUri!!)
                                showBottomSheet = false
                            }) {
                                Text("Photo")
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = {
                                captureMode = "video"
                                mediaUri = createMediaUri(context, "mp4")
                                takeVideo.launch(mediaUri!!)
                                showBottomSheet = false
                            }) {
                                Text("Video")
                            }
                        }
                    )
                }
                if(showCreateFolder){
                    BasicAlertDialog(
                        onDismissRequest = { showCreateFolder = false },
                        modifier = Modifier.
                        width(400.dp).
                        height(180.dp).
                        clip(RoundedCornerShape(20)).
                        background(Color(0Xff18191B))
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Column(
                                modifier = Modifier.padding(15.dp)
                            ) {
                                Text(text = "Create Folder")
                                OutlinedTextField(
                                    onValueChange = { createFolderName = it },
                                    value = createFolderName,
                                    placeholder = {Text(text = "New folder")}
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    TextButton(onClick = {
                                        createFolderName = "New folder"
                                        showCreateFolder = false
                                    }) {
                                        Text(text = "Cancel")
                                    }
                                    TextButton(onClick = {
                                        val newFolder = FolderEntity(
                                            folderName = createFolderName,
                                            parentId = folderId
                                        )
                                        roomViewModel.saveFolder(newFolder) { success, message ->
                                            if (!success) {
                                                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                                                createFolderName = "New folder"
                                            } else {
                                                showCreateFolder = false
                                                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                                                showBottomSheet = false
                                                createFolderName = "New folder"
                                            }
                                        }
                                    }) {
                                        Text(text = "Create")
                                    }
                                }
                            }
                        }
                    }
                }
                AnimatedNavHost(
                    modifier = Modifier.padding(innerPadding),
                    navController = navController,
                    startDestination = "home",
                    enterTransition = {
                        slideInHorizontally(
                            initialOffsetX = { 1000 },
                            animationSpec = tween(700)
                        ) + fadeIn(animationSpec = tween(700))
                    },
                    exitTransition = {
                        slideOutHorizontally(
                            targetOffsetX = { -1000 },
                            animationSpec = tween(700)
                        ) + fadeOut(animationSpec = tween(700))
                    },
                    popEnterTransition = {
                        slideInHorizontally(
                            initialOffsetX = { -1000 },
                            animationSpec = tween(700)
                        ) + fadeIn(animationSpec = tween(700))
                    },
                    popExitTransition = {
                        slideOutHorizontally(
                            targetOffsetX = { 1000 },
                            animationSpec = tween(700)
                        ) + fadeOut(animationSpec = tween(700))
                    }
                ) {
                    composable("profile"){
                        ProfileScreen(navController)
                    }
                    composable("home") {
                        Home(
                            isStarred,
                            folderId,
                            roomViewModel,
                            navController,
                            onClick = { fId, pId ->
                                folderId = fId
                                currentParentId = pId
                                navController.navigate("home")
                                search = ""
                                roomViewModel.search(search, folderId)
                            }
                        )
                    }
                    composable("reviewscreen?uri={uri}",
                        arguments = listOf(navArgument("uri") {nullable = true})
                    ) {
                        entry ->
                        val uri = entry.arguments?.getString("uri")?.let { Uri.parse(it) }
                        val new : FileInfo? = uri?.let { getFileInfo(context, it) }
                        val newfile = new?.let { Triple(it.name, new.mimeType, new.uri) }
                        ReviewFilesScreen(
                            folderName,
                            navController,
                            files = selectedFiles,
                            onRemove = { file -> selectedFiles = selectedFiles - file },
                            newFile = newfile
                        )
                    }
                }
            }
        }
    }
}
fun createMediaUri(context: Context, extension: String): Uri {
    val dir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
    val file = File(dir, "media_${System.currentTimeMillis()}.$extension")
    return FileProvider.getUriForFile(
        context,
        "${context.packageName}.provider",
        file
    )
}


data class FileInfo(
    val name: String,
    val mimeType: String,
    val uri : Uri
)

fun getFileInfo(context: Context, uri: Uri): FileInfo {
    val contentResolver = context.contentResolver

    val mimeType = contentResolver.getType(uri) ?: "application/octet-stream"

    var fileName: String? = null
    val cursor = contentResolver.query(uri, null, null, null, null)
    cursor?.use {
        if (it.moveToFirst()) {
            val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (nameIndex != -1) {
                fileName = it.getString(nameIndex)
            }
        }
    }
    if (fileName == null) {
        fileName = uri.lastPathSegment ?: "shared_file"
    }

    return FileInfo(
        name = fileName ?: "shared_file",
        mimeType = mimeType,
        uri = uri
    )
}







