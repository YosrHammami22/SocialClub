package com.yosrhammami.socialclub.ui
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun HomeScreen(
    onGetFromFirestoreClick: () -> Unit,
    onGetFromApiClick: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Button(onClick = onGetFromFirestoreClick) {
            Text("Get from Firestore")
        }
        Button(onClick = onGetFromApiClick) {
            Text("Get from RandomUser API")
        }
    }
}