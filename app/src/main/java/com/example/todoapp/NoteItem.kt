package com.example.todoapp

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun NoteItem(
    title: String,
    description: String,
    imageUri: String?,
    isTask: Boolean,
    dueDateTimestamp: Long?,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        onClick = onClick
    ) {
        Column(Modifier.padding(16.dp)) {
            if (isTask && dueDateTimestamp != null) {
                Spacer(modifier = Modifier.height(8.dp))
                val date = Date(dueDateTimestamp)
                val formatter = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())
                val formattedDateTime = formatter.format(date)


                Text(
                    text = "Fecha: $formattedDateTime",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.error
                )
            }

            Text(title, style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Text(description, style = MaterialTheme.typography.bodyMedium)

            if (imageUri != null) {
                AsyncImage(
                    model = imageUri,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .padding(vertical = 4.dp)
                )
            }
        }
    }
}