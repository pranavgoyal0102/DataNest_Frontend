package com.example.myapplication.ui.theme.uii

import android.util.Log
import android.widget.Space
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.myapplication.ui.theme.icons.Account_circle
import com.example.myapplication.ui.theme.viewModel.GoogleAuthUiClient
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

@Composable
fun ProfileScreen(navController: NavController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val googleClient = remember { GoogleAuthUiClient(context) }
    val user = remember { mutableStateOf(googleClient.getCurrentUser()) }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ){
        Spacer(modifier = Modifier.height(50.dp))
        if(user.value != null){
            AsyncImage(model = user.value!!.photoUrl, contentDescription = "")
        }else{
            Icon(imageVector = Icons.Default.AccountCircle,
                contentDescription = "",
                modifier = Modifier.size(250.dp),
                tint = Color.Gray
            )
        }
        if(user.value == null){
            TextButton(onClick = {
                scope.launch {
                    val result = googleClient.signIn()
                    result?.credential?.data?.getString("id_token")?.let { token ->
                        googleClient.firebaseAuthWithGoogle(token)
                        user.value = googleClient.getCurrentUser()
                        Log.d("Auth", "Signed in as: ${user.value?.displayName}")
                        Toast.makeText(context,"Successful Login", Toast.LENGTH_SHORT).show()
                    }
                }
            }) {
                Text(text = "Login", fontSize = 36.sp, color = Color.White)
            }
        }
    }
}