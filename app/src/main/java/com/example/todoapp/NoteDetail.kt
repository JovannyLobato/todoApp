package com.example.todoapp

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage


@Composable
fun NoteDetailScreen(
    initialTitle: String,
    initialDescription: String,
    imageUri: String?
) {
    // Estados recordados para los campos
    var title by remember { mutableStateOf(initialTitle) }
    var description by remember { mutableStateOf(initialDescription) }

    // Llamamos al composable NoteDetail
    NoteDetail(
        title = title,
        description = description,
        imageUri = imageUri,
        onTitleChange = { title = it },
        onDescriptionChange = { description = it },
        onAddClick = { println("Agregar: $title - $description") },
        onCancelClick = { println("Cancelar") }
    )
}

@Composable
fun NoteDetail(
    title: String,
    description: String,
    imageUri: String?,
    onTitleChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onAddClick: () -> Unit = {},
    onCancelClick: () -> Unit = {}
) {
    Column(
        Modifier
            .padding(16.dp)
            .fillMaxHeight()
    ) {
        OutlinedTextField(
            value = title,
            onValueChange = onTitleChange,
            label = { Text("Título") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = description,
            onValueChange = onDescriptionChange,
            label = { Text("Descripción") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

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

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {

            OutlinedButton(onClick = onCancelClick, modifier = Modifier.weight(1f)) {
                Text("Cancelar")
            }

            Spacer(modifier = Modifier.width(16.dp))

            Button(onClick = onAddClick, modifier = Modifier.weight(1f)) {
                Text("Agregar")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun NoteDetailPreview() {
    NoteDetailScreen(
        initialTitle = "Título de ejemplo",
        initialDescription = "Descripción de ejemplo",
        imageUri = "https://via.placeholder.com/150"
    )
}
