@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.todoapp
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage


@Composable
fun NotesApp() {
    NotesTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            NotesListScreen()
        }
    }
}

@Composable
fun NotesListScreen() {
    var openEditor by remember { mutableStateOf(false) }

    if (openEditor) {
        NoteEditorScreen(onSave = { openEditor = false }, onCancel = { openEditor = false })
    } else {
        Scaffold(
            floatingActionButton = {
                FloatingActionButton(onClick = { openEditor = true }) {
                    Text("+")
                }
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .padding(padding)
                    .padding(16.dp)
            ) {
                Text(
                    text = "Mis Notas",
                    style = MaterialTheme.typography.headlineLarge
                )
                Spacer(Modifier.height(8.dp))
                NoteItem(title = "Título", description = "Descripción breve de la nota")
                NoteItem(title = "Otra nota", description = "Texto más largo de ejemplo...")
            }
        }
    }
}

@Composable
fun NoteItem(
    title: String,
    description: String,
    images: List<Uri> = emptyList(),
    videos: List<Uri> = emptyList(),
    audios: List<Uri> = emptyList()
    ) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(4.dp))
            Text(description, style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.height(8.dp))

            images.forEach { uri ->
                AsyncImage(
                    model = uri,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .padding(vertical = 4.dp)
                )
            }

            videos.forEach { uri ->
                Text("Video adjunto: ${uri.lastPathSegment}")
            }

            audios.forEach { uri ->
                Text("Audio adjunto: ${uri.lastPathSegment}")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteEditorScreen(onSave: () -> Unit, onCancel: () -> Unit) {
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Nueva Nota") },
                actions = {
                    TextButton(onClick = onSave) { Text("Guardar", color = MaterialTheme.colorScheme.primary) }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Título") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = content,
                onValueChange = { content = it },
                label = { Text("Contenido") },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                textStyle = TextStyle(fontSize = 16.sp)
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onCancel) { Text("Cancelar") }
                Spacer(Modifier.width(8.dp))
                Button(onClick = onSave) { Text("Guardar") }
            }
        }
    }
}

@Composable
fun NotesTheme(content: @Composable () -> Unit) {
    val darkColorScheme = darkColorScheme(
        primary = MaterialTheme.colorScheme.primary,
        secondary = MaterialTheme.colorScheme.secondary,
        background = MaterialTheme.colorScheme.background,
        surface = MaterialTheme.colorScheme.surface
    )

    val shapes = Shapes(
        small = RoundedCornerShape(4.dp),
        medium = RoundedCornerShape(8.dp),
        large = RoundedCornerShape(16.dp)
    )

    val typography = Typography(
        bodyMedium = TextStyle(fontSize = 16.sp, fontFamily = FontFamily.SansSerif),
        titleMedium = TextStyle(fontSize = 20.sp, fontFamily = FontFamily.SansSerif),
        headlineMedium = TextStyle(fontSize = 24.sp, fontFamily = FontFamily.SansSerif)
    )

    MaterialTheme(
        colorScheme = darkColorScheme,
        shapes = shapes,
        typography = typography,
        content = content
    )
}
