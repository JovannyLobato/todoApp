package com.example.todoapp

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.todoapp.model.MediaType
import com.example.todoapp.repository.NoteRepository
import com.example.todoapp.viewmodel.NoteViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.Videocam

import androidx.compose.foundation.layout.size
import androidx.compose.material3.ButtonDefaults
import androidx.compose.ui.unit.dp

@Composable
fun formatTimestamp(timestamp: Long?): String {
    return if (timestamp != null && timestamp > 0) {
        val date = Date(timestamp)
        SimpleDateFormat("dd MMM yyyy HH:mm", Locale.getDefault()).format(date)
    } else {
        stringResource(id = R.string.select_a_date_and_time_for_the_task )
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditScreen(
    navController: NavController,
    viewModel: NoteViewModel,
    noteId: Int = -1
) {
    val uiState by viewModel.uiState.collectAsState()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    // carga la nota seleccionada
    LaunchedEffect(noteId) {
        if (noteId != -1) {
            val noteWithDetails = viewModel.getNoteWithDetails(noteId)
            viewModel.loadNoteDetails(noteWithDetails)
        } else {
            viewModel.resetUiState()
        }
    }


    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (noteId != -1) "Editar Nota/Tarea" else "Nueva Nota/Tarea") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    viewModel.saveNote(isEditing = noteId != -1, noteId = noteId)
                    navController.popBackStack()
                },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Save, contentDescription = "Guardar")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            OutlinedTextField(
                value = uiState.title,
                onValueChange = { viewModel.onTitleChange(it) },
                label = { Text("Título") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(8.dp))




            Text("Contenido:")
            uiState.mediaBlocks.forEachIndexed { index, block ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Column(Modifier.padding(8.dp)) {
                        Text("Tipo: ${block.type}")
                        Text("Contenido: ${block.content ?: "Sin contenido"}")
                        block.description?.let { Text("Descripción: $it") }
                        Spacer(Modifier.height(4.dp))
                        TextButton(onClick = { viewModel.removeMediaBlock(index) }) {
                            Text("Eliminar", color = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
            Spacer(Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("¿Es una tarea?")
                Switch(
                    checked = uiState.isTask,
                    onCheckedChange = { viewModel.onIsTaskChange(it) }
                )
            }

            if (uiState.isTask) {
                Spacer(Modifier.height(8.dp))
                Button(onClick = { viewModel.setShowDatePicker(true) }) {
                    Text(
                        if (uiState.dueDateTimestamp == null)
                            "Seleccionar fecha límite"
                        else
                            "Fecha: ${formatTimestamp(uiState.dueDateTimestamp)}"
                    )
                }
            }

            Spacer(Modifier.height(8.dp))
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(onClick = { viewModel.addMediaBlock(MediaType.TEXT, "Nuevo texto") }) {
                    Icon(Icons.Filled.Description, contentDescription = "Agregar texto")
                }
                Button(onClick = { viewModel.addMediaBlock(MediaType.IMAGE, "uri_de_imagen") }) {
                    Icon(Icons.Filled.Image, contentDescription = "Agregar imagen")
                }
                Button(onClick = { viewModel.addMediaBlock(MediaType.AUDIO, "uri_de_audio") }) {
                    Icon(Icons.Filled.Audiotrack, contentDescription = "Agregar audio")
                }
                Button(onClick = { viewModel.addMediaBlock(MediaType.VIDEO, "uri_de_video") }) {
                    Icon(Icons.Filled.Videocam, contentDescription = "Agregar video")
                }
            }
        }
    }

    if (uiState.showDatePicker) {
        val datePickerState = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { viewModel.setShowDatePicker(false) },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let {
                        viewModel.onDueDateChange(it)
                    }
                    viewModel.setShowDatePicker(false)
                }) { Text("Aceptar") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }


}


