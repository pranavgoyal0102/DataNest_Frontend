package com.example.myapplication.ui.theme.uii

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.composables.Video
import com.example.myapplication.ui.theme.icons.CancelCircle
import com.example.myapplication.ui.theme.icons.FilePdf
import com.example.myapplication.ui.theme.icons.ImageIcon
import com.example.myapplication.ui.theme.icons.Solid_Folder


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReviewFilesScreen(
    folderName : String,
    navController: NavController,
    files: List<Triple<String, String, Uri>>,
    onRemove: (Triple<String, String, Uri>) -> Unit,
    newFile : Triple<String, String, Uri>? = null
) {
    var location by remember { mutableStateOf("  $folderName") }
    if(newFile != null){
        location = "Home"
    }
    TopAppBar(title = {
    },
        modifier = Modifier.height(100.dp))
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0Xff18191B))
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ){
                if(newFile != null){
                    Column(
                        modifier = Modifier.
                        border(1.dp, Color.Gray, RoundedCornerShape(32.dp)).
                        heightIn(min = 100.dp, max = 300.dp)
                    ) {
                        LazyColumn(
                            modifier = Modifier.padding(10.dp)
                        ) {
                            item{
                                val fileType = newFile.second
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(8.dp)
                                        .border(1.dp, Color.Gray, RoundedCornerShape(35))
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        modifier = Modifier.weight(1f),
                                        verticalAlignment = Alignment.CenterVertically
                                    ){
                                        when {
                                            fileType == "image/jpeg" -> {
                                                Icon(imageVector = ImageIcon, contentDescription = "")
                                            }
                                            fileType == "application/pdf" -> {
                                                Icon(imageVector = FilePdf, contentDescription = "")
                                            }
                                            fileType.startsWith("video") -> {
                                                Icon(imageVector = Video, contentDescription = "")
                                            }
                                            else -> {
                                                Icon(imageVector = Icons.Default.Warning, contentDescription = "")
                                            }
                                        }
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column {
                                            Text(newFile.first,
                                                color = Color.White,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis)
                                            Text(newFile.second,
                                                color = Color.Gray,
                                                fontSize = 12.sp,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }else{
                    if(files.isNotEmpty()){
                        Column(
                            modifier = Modifier.
                            border(1.dp, Color.Gray, RoundedCornerShape(32.dp)).
                            heightIn(min = 100.dp, max = 300.dp)
                        ) {
                            LazyColumn(
                                modifier = Modifier.padding(10.dp)
                            ) {
                                items(files){file ->
                                    val fileType = file.second
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(8.dp)
                                            .border(1.dp, Color.Gray, RoundedCornerShape(35))
                                            .padding(12.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            modifier = Modifier.weight(1f),
                                            verticalAlignment = Alignment.CenterVertically
                                        ){
                                            when {
                                                fileType == "image/jpeg" -> {
                                                    Icon(imageVector = ImageIcon, contentDescription = "")
                                                }
                                                fileType == "application/pdf" -> {
                                                    Icon(imageVector = FilePdf, contentDescription = "")
                                                }
                                                fileType.startsWith("video") -> {
                                                    Icon(imageVector = Video, contentDescription = "")
                                                }
                                                else -> {
                                                    Icon(imageVector = Icons.Default.Warning, contentDescription = "")
                                                }
                                            }
                                            Spacer(modifier = Modifier.width(12.dp))
                                            Column {
                                                Text(file.first,
                                                    color = Color.White,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis)
                                                Text(file.second,
                                                    color = Color.Gray,
                                                    fontSize = 12.sp,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }
                                        }
                                        Icon(
                                            imageVector = CancelCircle,
                                            contentDescription = "Remove",
                                            tint = Color.White,
                                            modifier = Modifier
                                                .size(24.dp)
                                                .clickable { onRemove(file)
                                                }
                                        )
                                    }
                                }
                            }

                        }
                    }else{
                        navController.navigateUp()
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(value = location,
                    onValueChange = {location = it},
                    enabled = false,
                    textStyle = TextStyle(color = Color.White, fontSize = 16.sp),
                    maxLines = 1,
                    leadingIcon = {
                        Icon(imageVector = Solid_Folder,
                            contentDescription = "",
                            tint = Color.White)
                    },
                    shape = RoundedCornerShape(20),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor = Color.White,
                        unfocusedBorderColor = Color.White,
                        disabledBorderColor = Color.White,
                        errorBorderColor = Color.White
                    ),
                    label = { Text(text = "Location", color = Color.White) },)
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}