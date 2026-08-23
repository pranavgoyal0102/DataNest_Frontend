package com.pranavgoyal.datanest.ui.screens

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
import androidx.compose.runtime.LaunchedEffect
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
import com.pranavgoyal.datanest.ui.icons.Video
import com.pranavgoyal.datanest.ui.icons.CancelCircle
import com.pranavgoyal.datanest.ui.icons.FilePdf
import com.pranavgoyal.datanest.ui.icons.ImageIcon
import com.pranavgoyal.datanest.ui.icons.Solid_Folder


/**
 * [isResolving] is true while a shared URI is still being read off the
 * ContentResolver. It has no default on purpose: without it this screen
 * cannot tell "nothing to review" from "the file has not arrived yet",
 * and guessing wrong means leaving before the user sees anything.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReviewFilesScreen(
    navController: NavController,
    files: List<Triple<String, String, Uri>>,
    onRemove: (Triple<String, String, Uri>) -> Unit,
    newFile : Triple<String, String, Uri>? = null,
    isResolving: Boolean
) {

    // Leaving is a side effect, so it belongs in an effect rather than in
    // the composition — run inline it fired again on every recomposition.
    // The isResolving guard is what keeps it from firing on the first
    // frame, when newFile is still null because getFileInfo is in flight.
    LaunchedEffect(isResolving, newFile, files) {
        if (!isResolving && newFile == null && files.isEmpty()) {
            navController.navigateUp()
        }
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
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}