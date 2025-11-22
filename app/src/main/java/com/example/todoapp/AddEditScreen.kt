package com.example.todoapp

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
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


import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.Close
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.Transparent
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.core.content.FileProvider
import coil.compose.rememberAsyncImagePainter
import com.example.todoapp.model.MediaBlock

import java.io.File

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
fun ImagePickerSheet(
    onTakePhoto: () -> Unit,
    onPickGallery: () -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text("Agregar imagen", style = MaterialTheme.typography.titleMedium)

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = {
                    onTakePhoto()
                    onDismiss()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.PhotoCamera, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Tomar foto")
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    onPickGallery()
                    onDismiss()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.PhotoLibrary, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Elegir desde galería")
            }
        }
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
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showBlockMediaDeleteDialog by remember { mutableStateOf(false)}
    var blockToDelete by remember { mutableStateOf<MediaBlock?>(null) }

    // Estado para mostrar el bottom sheet / cámara
    var showImageSheet by remember { mutableStateOf(false) }
    var tempPhotoUri by remember { mutableStateOf<Uri?>(null) } // URI temporal para TakePicture
    // GetContent
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            try {
                context.contentResolver.takePersistableUriPermission(
                    it,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch(e: Exception){

            }
            viewModel.addMediaBlock(MediaType.IMAGE, it.toString())
        }
    }
    // TakePicture - necesita un URI FileProvider
    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success: Boolean ->
        if (success) {
            tempPhotoUri?.let { uri ->
                viewModel.addMediaBlock(MediaType.IMAGE, uri.toString())
            }
        }
        // limpia el estado
        tempPhotoUri = null
    }
    // funcionn para crear archivo temporal y lanzar la camara
    fun launchCamera() {
        val file = File(context.cacheDir, "photo_${System.currentTimeMillis()}.jpg")
        file.createNewFile()
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            file
        )
        tempPhotoUri = uri
        takePictureLauncher.launch(uri)
    }



    // carga la nota seleccionada
    LaunchedEffect(noteId) {
        if (noteId != -1) {
            val noteWithDetails = viewModel.getNoteWithDetails(noteId)
            viewModel.loadNoteDetails(noteWithDetails)
        } else {
            viewModel.resetUiState()
            viewModel.addMediaBlock(MediaType.TEXT, "Nuevo texto")
        }
    }

    val color = Color(0xFF120524)
    Scaffold(
        contentWindowInsets = WindowInsets(0),
        containerColor = Color.Transparent,
        modifier = Modifier
            .padding(top = 10.dp),
        topBar = {
            TextField(
                value = uiState.title,
                onValueChange = { viewModel.onTitleChange(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp)
                    .padding(top = 30.dp)
                    .background(Color.Transparent),
                maxLines = 2,
            )
        }
    ) { padding ->
        if (showImageSheet) {
            ModalBottomSheet(
                onDismissRequest = { showImageSheet = false }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text("Agregar imagen", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            launchCamera()
                            showImageSheet = false
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.PhotoCamera, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Tomar foto")
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = {
                            galleryLauncher.launch("image/*")
                            showImageSheet = false
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.PhotoLibrary, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Elegir desde galería")
                    }
                }
            }
        }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 0.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(padding)
                    .padding(horizontal = 0.dp)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                // Spacer(Modifier.height(12.dp))

                // Text("Contenido:")
                uiState.mediaBlocks.forEachIndexed { index, block ->
                    key(block.id) {
                        val description = block.description ?: ""
                        Card(
                            modifier = Modifier
                                .padding(
                                    horizontal = 0.dp
                                )
                                .combinedClickable(
                                    onClick = {},
                                    onLongClick = {
                                        blockToDelete = block
                                        showBlockMediaDeleteDialog = true }
                                )
                                .fillMaxWidth(),

                            shape = RectangleShape,
                            elevation = CardDefaults.cardElevation(0.dp),
                            //hay que cambiar  esto cuando esten los estilos listos
                            colors = CardDefaults.cardColors(
                                containerColor = Transparent
                            )
                        ) {
                            Column(
                                Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 10.dp)
                                    .padding(bottom = 10.dp),
                                horizontalAlignment = Alignment.Start
                            ) {
                                when (block.type) {

                                    MediaType.TEXT -> {
                                    }

                                    MediaType.IMAGE -> {
                                        block.content?.let { uri ->
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.Start
                                            ) {
                                                Image(
                                                    painter = rememberAsyncImagePainter(uri),
                                                    contentDescription = "Imagen",
                                                    modifier = Modifier
                                                        .width(250.dp)
                                                        .heightIn(min = 200.dp),

                                                    contentScale = ContentScale.Fit,
                                                    alignment = Alignment.TopStart
                                                )
                                            }
                                        } ?: Text("Sin imagen")
                                    }

                                    MediaType.AUDIO -> {
                                        Text("Audio: ${block.content ?: "Sin audio"}")
                                        // Luego aquí agregamos exoplayer de audio si quieres
                                    }

                                    MediaType.VIDEO -> {
                                        Text("Video: ${block.content ?: "Sin video"}")
                                        // Luego metemos ExoPlayer
                                    }
                                }

                                val descriptionModifier =
                                    if (block.type == MediaType.TEXT) {
                                        Modifier.fillMaxWidth()
                                    } else {
                                        Modifier
                                            .fillMaxWidth(0.9f)
                                            .padding(start = 30.dp)
                                    }

                                TextField(
                                    value = block.description ?: "",
                                    onValueChange = { newValue ->
                                        var description = newValue
                                        viewModel.updateBlockDescription(block.id, newValue)
                                    },
                                    modifier = descriptionModifier,
                                    placeholder = { Text("") },
                                    colors = TextFieldDefaults.colors(
                                        focusedContainerColor = Color.Transparent,
                                        unfocusedContainerColor = Color.Transparent,
                                        disabledContainerColor = Color.Transparent,
                                        focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                                        unfocusedIndicatorColor = Color.Gray
                                    )
                                )
                                TextButton(onClick = { /*viewModel.removeMediaBlock(index) */},
                                    modifier = Modifier.height(1.dp)) {
                                    Text("", color = MaterialTheme.colorScheme.error)
                                }
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
                    Button(onClick = { showImageSheet = true }) {
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
            if (showBlockMediaDeleteDialog) {
                AlertDialog(
                    onDismissRequest = { showBlockMediaDeleteDialog = false },
                    title = { Text("Eliminar Item") },
                    text = { Text("¿Seguro que quieres eliminar este contenido?") },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                viewModel.removeMediaBlock(blockToDelete!!.id)
                                showBlockMediaDeleteDialog = false
                            }
                        ) {
                            Text("Eliminar", color = MaterialTheme.colorScheme.error)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showBlockMediaDeleteDialog = false }) {
                            Text("Cancelar")
                        }
                    }
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .imePadding()     // hace que el contenido suba cuando aparece el teclado
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Salir",
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(16.dp)
                        .size(48.dp)
                        .combinedClickable(
                            onClick = { navController.popBackStack() },
                            onLongClick = { showDeleteDialog = true }
                        )
                        .background(Color.Transparent, shape = CircleShape)
                        .padding(8.dp)
                )

                if (showDeleteDialog) {
                    AlertDialog(
                        onDismissRequest = { showDeleteDialog = false },
                        title = { Text("Eliminar nota") },
                        text = { Text("¿Seguro que quieres eliminar esta nota y todos sus contenidos?") },
                        confirmButton = {
                            TextButton(
                                onClick = {
                                    viewModel.deleteNoteWithDetails(noteId)
                                    showDeleteDialog = false
                                    navController.popBackStack()
                                }
                            ) {
                                Text("Eliminar", color = MaterialTheme.colorScheme.error)
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showDeleteDialog = false }) {
                                Text("Cancelar")
                            }
                        }
                    )
                }


                IconButton(
                    onClick = {
                        viewModel.saveNote(isEditing = noteId != -1, noteId = noteId)
                        navController.popBackStack()
                    },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(16.dp)
                ) {
                    Icon(Icons.Default.Save, contentDescription = "Guardar")
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


