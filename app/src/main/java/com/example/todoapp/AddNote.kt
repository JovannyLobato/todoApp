package com.example.todoapp

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.example.todoapp.viewmodel.NoteViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


@Composable
fun AddNote(navController: NavController) {
    val context = LocalContext.current.applicationContext as TodoApplication
    val viewModel = remember { NoteViewModel(context.repository) }

    AddNoteScreen(
        onAddNote = { title, description, imageUri ->
            viewModel.addNote(title, description, imageUri)
            navController.popBackStack()
        },
        onCancel = {
            navController.popBackStack()
        }
    )
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddNoteScreen(
    onAddNote: (title: String, description: String, imageUri: String?) -> Unit,
    onCancel: () -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var imageUri by remember { mutableStateOf<String?>(null) }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        imageUri = uri?.toString()
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Agregar nota") }
            )
        },
        bottomBar = {
            BottomAppBar {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    OutlinedButton(onClick = onCancel) {
                        Text("Cancelar")
                    }
                    Button(onClick = { launcher.launch("image/*") }) {
                        Text("Agregar archivos")
                    }
                    Button(onClick = {
                        onAddNote(title, description, imageUri)
                        title = ""
                        description = ""
                        imageUri = null
                    }) {
                        Text("Agregar")
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
                .imePadding()
        ) {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Título") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text("Descripción", style = MaterialTheme.typography.labelLarge)
            Spacer(modifier = Modifier.height(4.dp))

            val scrollState = rememberScrollState()
            val coroutineScope = rememberCoroutineScope()

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 100.dp, max = 300.dp)
                    .border(1.dp, MaterialTheme.colorScheme.outline, shape = MaterialTheme.shapes.medium)
                    .padding(8.dp)
                    .scrollable(state = scrollState, orientation = Orientation.Vertical)
            ) {
                BasicTextField(
                    value = description,
                    onValueChange = { newText ->
                        description = newText
                        // Hacer scroll al final automáticamente al escribir
                        coroutineScope.launch {
                            delay(10) // pequeño retardo para asegurar que el texto se actualizó
                            scrollState.scrollTo(scrollState.maxValue)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(scrollState),
                    textStyle = TextStyle(color = Color.Black),
                    keyboardOptions = KeyboardOptions.Default.copy(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Default
                    ),
                    decorationBox = { innerTextField ->
                        if (description.isEmpty()) {
                            Text(
                                text = "Escribe la descripción aquí...",
                                style = TextStyle(color = Color.Gray)
                            )
                        }
                        innerTextField()
                    }
                )
            }


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
        }
    }

}

@Preview(showBackground = true)
@Composable
fun AddNotePreview() {
    AddNoteScreen(
        onAddNote = { _, _, _ -> },
        onCancel = {}
    )
}
