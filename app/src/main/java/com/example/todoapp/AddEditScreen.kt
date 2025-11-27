package com.example.todoapp

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectTransformGestures
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.Transparent
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import coil.compose.rememberAsyncImagePainter
import com.example.todoapp.model.MediaBlock
import androidx.compose.ui.text.TextStyle as textstyle
import java.io.File
import java.time.format.TextStyle
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.Calendar
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.ui.window.Dialog
import android.Manifest
import android.content.pm.PackageManager
import android.provider.Settings
import androidx.core.content.ContextCompat


fun combineDateWithTime(dateTimestamp: Long, hour: Int, minute: Int): Long {
    val date = Date(dateTimestamp)
    val calendar = Calendar.getInstance().apply {
        time = date
        set(Calendar.HOUR_OF_DAY, hour)
        set(Calendar.MINUTE, minute)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
    return calendar.timeInMillis
}
// AddEditScreen.kt

@Composable
fun formatTimestamp(timestamp: Long?): String {
    return if (timestamp != null && timestamp > 0) {
        val date = Date(timestamp)
        val calendar = Calendar.getInstance().apply { time = date }
        val isMidnight = calendar.get(Calendar.HOUR_OF_DAY) == 0 &&
                calendar.get(Calendar.MINUTE) == 0 &&
                calendar.get(Calendar.SECOND) == 0

        val dateFormat = if (isMidnight) {
            SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) // Sin hora
        } else {
            SimpleDateFormat("dd MMM yyyy HH:mm", Locale.getDefault()) // Con hora
        }

        dateFormat.format(date)
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
            Text(stringResource(id = R.string.add_image), style = MaterialTheme.typography.titleMedium)

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
                Text(stringResource(id = R.string.take_photo))
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
                Text(stringResource(id = R.string.choose_from_gallery))
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
    var showTimePicker by remember { mutableStateOf(false) }
    var showReminderDatePicker by remember { mutableStateOf(false) }
    var reminderBaseDateMillis by remember { mutableStateOf<Long?>(null) }
    var showPermissionDeniedDialog by remember { mutableStateOf(false) }
    var blockToDelete by remember { mutableStateOf<MediaBlock?>(null) }
    var showFullImage by remember { mutableStateOf(false) }
    var fullImageUri by remember { mutableStateOf<String?>(null) }
    val focusManager = LocalFocusManager.current


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
            .padding(top = 10.dp)
            .let {
                if (showFullImage) it.clickable(enabled = false) {}
                else it
            },
        topBar = {
            TextField(
                value = uiState.title,
                onValueChange = { viewModel.onTitleChange(it) },
                maxLines = 3,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    disabledContainerColor = Color.Transparent,
                    errorContainerColor = Color.Transparent,

                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent,
                    errorIndicatorColor = Color.Transparent
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp)
                    .padding(top = 30.dp)
                    .background(Color.Transparent),

                placeholder = {
                    Text(text = stringResource(id = R.string.write_a_title_placeholder))
                },
                textStyle = textstyle(
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )
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
                    Text(stringResource(id = R.string.add_image), style = MaterialTheme.typography.titleMedium)
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
                        Text(stringResource(id = R.string.take_photo))
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
                        Text(stringResource(id = R.string.choose_from_gallery))
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

                // Text("Contenido:") // Removed hardcoded text
                uiState.mediaBlocks.forEachIndexed { index, block ->
                    key(block.id) {
                        val     description = block.description ?: ""
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
                                                    contentDescription = stringResource(id = R.string.add_image_desc),
                                                    modifier = Modifier
                                                        .width(250.dp)
                                                        .heightIn(min = 200.dp)
                                                        .clickable {
                                                            focusManager.clearFocus()
                                                            fullImageUri = uri
                                                            showFullImage = true
                                                        },
                                                    contentScale = ContentScale.Fit,
                                                    alignment = Alignment.TopStart
                                                )
                                            }
                                        } ?: Text(stringResource(id = R.string.select_an_image))
                                    }

                                    MediaType.AUDIO -> {
                                        Text(stringResource(id = R.string.add_audio) + ": ${block.content ?: stringResource(id = R.string.add_audio)}")
                                        // Luego aquí agregamos exoplayer de audio si quieres
                                    }

                                    MediaType.VIDEO -> {
                                        Text(stringResource(id = R.string.add_video) + ": ${block.content ?: stringResource(id = R.string.add_video)}")
                                        // Luego metemos ExoPlayer
                                    }
                                }

                                val descriptionModifier =
                                    if (block.type == MediaType.TEXT) {
                                        Modifier.fillMaxWidth()
                                    } else {
                                        Modifier
                                            .fillMaxWidth()
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
                    Text(stringResource(id = R.string.is_a_taskq))
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
                                stringResource(id = R.string.select_due_date)
                            else
                                stringResource(id = R.string.date_label) + " ${
                                    formatTimestamp(
                                        uiState.dueDateTimestamp
                                    )
                                }"
                        )
                    }
                }

                Button(onClick = {
                    //  Verificacion de la version de Android
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                        // Verificacion del permiso (Lo tiene o no9
                        val hasPermission = ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.POST_NOTIFICATIONS
                        ) == PackageManager.PERMISSION_GRANTED

                        if (hasPermission) {
                            // Si tiene el permiso
                            showReminderDatePicker = true
                            reminderBaseDateMillis = uiState.dueDateTimestamp
                        } else {
                            // Si no tiene el permiso mostramos el mensaje
                            showPermissionDeniedDialog = true
                        }
                    } else {
                        // Android < V13: No necesita permiso en tiempo de ejecución
                        showReminderDatePicker = true
                        reminderBaseDateMillis = uiState.dueDateTimestamp
                    }
                }) {
                    Text(stringResource(id = R.string.add_reminder))
                }

                uiState.reminders.sortedBy { it.reminderTime }.forEach { reminder ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Recordatorio: ${formatTimestamp(reminder.reminderTime)}",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = { viewModel.removeReminder(reminder) }) {
                            Icon(Icons.Default.Close, contentDescription = stringResource(id = R.string.delete_reminder))
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Button(onClick = { viewModel.addMediaBlock(MediaType.TEXT, "Nuevo texto") }) {
                        Icon(Icons.Filled.Description, contentDescription = stringResource(id = R.string.add_text))
                    }
                    Button(onClick = { showImageSheet = true }) {
                        Icon(Icons.Filled.Image, contentDescription = stringResource(id = R.string.add_image_desc))
                    }
                    Button(onClick = { viewModel.addMediaBlock(MediaType.AUDIO, "uri_de_audio") }) {
                        Icon(Icons.Filled.Audiotrack, contentDescription = stringResource(id = R.string.add_audio))
                    }
                    Button(onClick = { viewModel.addMediaBlock(MediaType.VIDEO, "uri_de_video") }) {
                        Icon(Icons.Filled.Videocam, contentDescription = stringResource(id = R.string.add_video))
                    }
                }
            }


            if (showBlockMediaDeleteDialog) {
                AlertDialog(
                    onDismissRequest = { showBlockMediaDeleteDialog = false },
                    title = { Text(stringResource(id = R.string.delete_item)) },
                    text = { Text(stringResource(id = R.string.confirm_delete_content)) },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                viewModel.removeMediaBlock(blockToDelete!!.id)
                                showBlockMediaDeleteDialog = false
                            }
                        ) {
                            Text(stringResource(id = R.string.delete), color = MaterialTheme.colorScheme.error)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showBlockMediaDeleteDialog = false }) {
                            Text(stringResource(id = R.string.cancel))
                        }
                    }
                )
            }

            if (showPermissionDeniedDialog) {
                AlertDialog(
                    onDismissRequest = { showPermissionDeniedDialog = false },
                    title = { Text("Permiso requerido") },
                    text = {
                        Text("Necesitas permitir las notificaciones para crear recordatorios. Por favor, habilítalas en la configuración.")
                    },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                showPermissionDeniedDialog = false
                                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                    data = Uri.fromParts("package", context.packageName, null)
                                }
                                context.startActivity(intent)
                            }
                        ) {
                            Text("Ir a Configuración")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showPermissionDeniedDialog = false }) {
                            Text(stringResource(id = R.string.cancel))
                        }
                    }
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .imePadding()
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = stringResource(id = R.string.exit),
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
                        title = { Text(stringResource(id = R.string.delete_note)) },
                        text = { Text(stringResource(id = R.string.confirm_delete_note)) },
                        confirmButton = {
                            TextButton(
                                onClick = {
                                    viewModel.deleteNoteWithDetails(noteId)
                                    showDeleteDialog = false
                                    navController.popBackStack()
                                }
                            ) {
                                Text(stringResource(id = R.string.delete), color = MaterialTheme.colorScheme.error)
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showDeleteDialog = false }) {
                                Text(stringResource(id = R.string.cancel))
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
                    Icon(Icons.Default.Save, contentDescription = stringResource(id = R.string.save_note))
                }
            }

        }

    }

    if (uiState.showDatePicker) {
        val datePickerState = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { viewModel.setShowDatePicker(false) },
            confirmButton = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    TextButton(onClick = { viewModel.setShowDatePicker(false) }) {
                        Text(stringResource(id = R.string.cancel))
                    }
                    TextButton(onClick = {
                        datePickerState.selectedDateMillis?.let { dateMillis ->
                            val calendar = Calendar.getInstance().apply {
                                timeInMillis = dateMillis

                                set(Calendar.HOUR_OF_DAY, 0)
                                set(Calendar.MINUTE, 0)
                                set(Calendar.SECOND, 0)
                                set(Calendar.MILLISECOND, 0)
                            }

                            viewModel.onDueDateChange(calendar.timeInMillis)
                            viewModel.setShowDatePicker(false)
                            showTimePicker = true
                        }
                    }) {
                        Text(stringResource(id = R.string.next))
                    }
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
    @OptIn(ExperimentalMaterial3Api::class)
    if (showReminderDatePicker) {
        val datePickerState = rememberDatePickerState(
            // Opcional: Si quieres que el picker se abra en la fecha de vencimiento de la tarea.
            initialSelectedDateMillis = uiState.dueDateTimestamp
        )
        DatePickerDialog(
            onDismissRequest = { showReminderDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { dateMillis ->
                        // Guardar solo la fecha a medianoche (00:00:00) como base para el TimePicker.
                        val calendar = Calendar.getInstance().apply {
                            timeInMillis = dateMillis
                            set(Calendar.HOUR_OF_DAY, 0)
                            set(Calendar.MINUTE, 0)
                            set(Calendar.SECOND, 0)
                            set(Calendar.MILLISECOND, 0)
                        }
                        reminderBaseDateMillis = calendar.timeInMillis // Almacenar la fecha seleccionada
                        showReminderDatePicker = false
                        showTimePicker = true
                    }
                }) { Text(stringResource(id = R.string.next)) }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
    @OptIn(ExperimentalMaterial3Api::class)
    if (showTimePicker) {
        val timePickerState = rememberTimePickerState()

        // 1. Obtener la fecha base: Siempre debe haber sido establecida al abrir showTimePicker.
        val baseDateTimestamp = reminderBaseDateMillis ?: Calendar.getInstance().timeInMillis

        TimePickerDialog(
            onDismissRequest = {
                showTimePicker = false
                reminderBaseDateMillis = null // Limpiar el estado al cerrar
            },
            title = { Text("Selecciona la hora del recordatorio") },
            confirmButton = {
                TextButton(
                    onClick = {
                        val combinedTimestamp = combineDateWithTime(
                            baseDateTimestamp,
                            timePickerState.hour,
                            timePickerState.minute
                        )

                        if (uiState.isTask && uiState.dueDateTimestamp == baseDateTimestamp) {
                            viewModel.onDueDateChange(combinedTimestamp)
                        }

                        viewModel.addReminder(combinedTimestamp)
                        showTimePicker = false
                        reminderBaseDateMillis = null // Limpiar el estado
                    }
                ) { Text(stringResource(id = R.string.accept)) }
            }
        ) {
            TimePicker(state = timePickerState)
        }
    }
    @Composable
    fun TimePickerDialog(
        onDismissRequest: () -> Unit,
        confirmButton: @Composable (() -> Unit),
        content: @Composable () -> Unit,
    ) {
        Dialog(onDismissRequest = onDismissRequest) {
            Surface(
                shape = MaterialTheme.shapes.extraLarge,
                tonalElevation = 6.dp,
                modifier = Modifier
                    .width(IntrinsicSize.Min)
                    .background(
                        shape = MaterialTheme.shapes.extraLarge,
                        color = MaterialTheme.colorScheme.surface
                    ),
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    content()
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp),
                        horizontalArrangement = Arrangement.End
                    ) {
                        confirmButton()
                    }
                }
            }
        }
    }

    if (showFullImage && fullImageUri != null) {
        FullScreenImageViewer(
            imageUri = fullImageUri!!,
            onClose = { showFullImage = false },
        )
    }


}


@Composable
fun FullScreenImageViewer(
    imageUri: String,
    onClose: () -> Unit
) {
    // Estados para zoom y desplazamiento
    var scale by remember { mutableStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, gestureZoom, _ ->
                    scale = (scale * gestureZoom).coerceIn(1f, 5f)
                    offset += pan
                }
            }
            // cierre con un toque
            .clickable { onClose() }
    ) {
        Image(
            painter = rememberAsyncImagePainter(imageUri),
            contentDescription = null,
            modifier = Modifier
                .align(Alignment.Center)
                .graphicsLayer(
                    scaleX = scale,
                    scaleY = scale,
                    translationX = offset.x,
                    translationY = offset.y
                ),
            contentScale = ContentScale.Fit
        )

        Icon(
            imageVector = Icons.Default.Close,
            contentDescription = stringResource(id = R.string.close),
            tint = Color.White,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(vertical = 50.dp)
                .padding(horizontal = 20.dp)
                .size(35.dp)
                .clickable { onClose() }
        )
    }
}