@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.todoapp.ui.theme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

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
                    style = MaterialTheme.typography.headlineMedium
                )
                Spacer(Modifier.height(8.dp))
                NoteItem(title = "Título", description = "Descripción breve de la nota")
                NoteItem(title = "Otra nota", description = "Texto más largo de ejemplo...")
            }
        }
    }
}

@Composable
fun NoteItem(title: String, description: String) {
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
