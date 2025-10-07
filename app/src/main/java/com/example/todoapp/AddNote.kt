package com.example.todoapp

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage

@Composable
fun AddNoteScreen(
    onAddNote: (title: String, description: String, imageUri: String?) -> Unit,
    onCancel: () -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var imageUri by remember { mutableStateOf<String?>(null) }

    // Launcher para seleccionar imagen desde la galería
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        imageUri = uri.toString()
    }

    NoteDetail(
        title = title,
        description = description,
        imageUri = imageUri,
        onTitleChange = { title = it },
        onDescriptionChange = { description = it },
        onAddClick = {
            onAddNote(title, description, imageUri)
            title = ""
            description = ""
            imageUri = null
        },
        onCancelClick = {
            onCancel()
            title = ""
            description = ""
            imageUri = null
        },
        onAddMediaClick = {
            launcher.launch("image/*") // Permite seleccionar imágenes
        }
    )
}

@Composable
fun NoteDetail(
    title: String,
    description: String,
    imageUri: String?,
    onTitleChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onAddClick: () -> Unit,
    onCancelClick: () -> Unit,
    onAddMediaClick: () -> Unit
) {
    Column(Modifier.padding(16.dp)) {
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

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            OutlinedButton(onClick = onCancelClick) {
                Text("Cancelar")
            }
            Button(onClick = onAddMediaClick) {
                Text("Agregar archivos")
            }
            Button(onClick = onAddClick) {
                Text("Agregar")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AddNotePreview() {
    AddNoteScreen(
        onAddNote = { title, description, imageUri ->
            println("Nota agregada: $title - $description - $imageUri")
        },
        onCancel = { println("Agregar nota cancelado") }
    )
}
