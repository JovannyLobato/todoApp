package com.example.todoapp

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.Transparent
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.todoapp.model.MediaBlock
import com.example.todoapp.model.MediaType
import com.example.todoapp.viewmodel.NoteViewModel
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import androidx.compose.ui.text.TextStyle as textstyle
import android.media.MediaPlayer
import android.media.MediaRecorder

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

@Composable
fun formatTimestamp(timestamp: Long?): String {
    return if (timestamp != null && timestamp > 0) {
        val date = Date(timestamp)
        val calendar = Calendar.getInstance().apply { time = date }
        val isMidnight = calendar.get(Calendar.HOUR_OF_DAY) == 0 &&
                calendar.get(Calendar.MINUTE) == 0 &&
                calendar.get(Calendar.SECOND) == 0

        val dateFormat = if (isMidnight) {
            SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        } else {
            SimpleDateFormat("dd MMM yyyy HH:mm", Locale.getDefault())
        }

        dateFormat.format(date)
    } else {
        stringResource(id = R.string.select_a_date_and_time_for_the_task )
    }
}


@androidx.annotation.OptIn(UnstableApi::class)
@RequiresApi(Build.VERSION_CODES.Q)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditScreen(
    navController: NavController,
    viewModel: NoteViewModel,
    noteId: Int = -1
) {
    // Solo observamos el estado único del ViewModel
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current


    val takeVideoLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CaptureVideo()) { success ->
        viewModel.onVideoCaptured(success)
    }

    val permissionsLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { results ->
        val granted = results.values.all { it }
        if (granted) {
            val uri = viewModel.prepareVideoUri(context)
            if (uri != null) {
                viewModel.setPendingVideoUri(uri)
                takeVideoLauncher.launch(uri)
            }
        } else {
            viewModel.setShowVideoPermissionDeniedDialog(true)
        }
    }

    val audioPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.setShowAudioSheet(true) // Usamos ViewModel
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            try {
                context.contentResolver.takePersistableUriPermission(it, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            } catch(e: Exception){ }
            viewModel.addMediaBlock(MediaType.IMAGE, it.toString())
        }
    }

    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success: Boolean ->
        if (success) {
            uiState.tempPhotoUri?.let { uri -> // Leemos del uiState
                viewModel.addMediaBlock(MediaType.IMAGE, uri.toString())
            }
        }
        viewModel.setTempPhotoUri(null) // Limpiamos en ViewModel
    }




    // Función auxiliar que ahora usa el ViewModel
    fun launchCamera() {
        val uri = viewModel.prepareImageUri(context) // Nueva función en VM
        viewModel.setTempPhotoUri(uri)
        if (uri != null) {
            takePictureLauncher.launch(uri)
        }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            // Si el usuario dice SÍ, lanzamos la cámara inmediatamente
            launchCamera()
        } else {
            // Si dice NO, podrías mostrar un mensaje o simplemente no hacer nada
            // (Opcional) viewModel.setShowCameraPermissionDeniedDialog(true)
        }
    }

    // Carga inicial
    LaunchedEffect(noteId) {
        if (noteId != -1) {
            val noteWithDetails = viewModel.getNoteWithDetails(noteId)
            viewModel.loadNoteDetails(noteWithDetails)
        } else {
            viewModel.resetUiState()
            viewModel.addMediaBlock(MediaType.TEXT, "Nuevo texto")
        }
    }

    // --- UI ---
    Scaffold(
        contentWindowInsets = WindowInsets(0),
        containerColor = Color.Transparent,
        modifier = Modifier
            .padding(top = 10.dp)
            .let {
                // Leemos estado desde uiState
                if (uiState.showFullImage) it.clickable(enabled = false) {} else it
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
                placeholder = { Text(text = stringResource(id = R.string.write_a_title_placeholder)) },
                textStyle = textstyle(fontSize = 28.sp, fontWeight = FontWeight.Bold)
            )
        }
    ) { padding ->

        // Sheet de Imagen (Estado en VM)
        if (uiState.showImageSheet) {
            ModalBottomSheet(
                onDismissRequest = { viewModel.setShowImageSheet(false) }
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(16.dp)
                ) {
                    Text(stringResource(id = R.string.add_image), style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = {
                            val permission = Manifest.permission.CAMERA

                            if (ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED) {
                                launchCamera()
                            } else {
                                cameraPermissionLauncher.launch(permission)
                            }
                            viewModel.setShowImageSheet(false)
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
                            viewModel.setShowImageSheet(false)
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

        Box(modifier = Modifier.fillMaxSize().padding(horizontal = 0.dp)) {
            Column(
                modifier = Modifier.padding(padding).fillMaxSize().verticalScroll(rememberScrollState())
            ) {
                uiState.mediaBlocks.forEachIndexed { index, block ->
                    key(block.id) {
                        Card(
                            modifier = Modifier
                                .padding(horizontal = 0.dp)
                                .combinedClickable(
                                    onClick = {},
                                    onLongClick = {
                                        viewModel.setBlockToDelete(block) // Guardamos en VM
                                        viewModel.setShowBlockMediaDeleteDialog(true) // Mostramos dialogo desde VM
                                    }
                                )
                                .fillMaxWidth(),
                            shape = RectangleShape,
                            elevation = CardDefaults.cardElevation(0.dp),
                            colors = CardDefaults.cardColors(containerColor = Transparent)
                        ) {
                            Column(Modifier.fillMaxWidth().padding(horizontal = 10.dp).padding(bottom = 10.dp)) {
                                when (block.type) {
                                    MediaType.TEXT -> { }
                                    MediaType.IMAGE -> {
                                        block.content?.let { uri ->
                                            Image(
                                                painter = rememberAsyncImagePainter(uri),
                                                contentDescription = null,
                                                modifier = Modifier
                                                    .width(250.dp)
                                                    .heightIn(min = 200.dp)
                                                    .clickable {
                                                        focusManager.clearFocus()
                                                        viewModel.setShowFullImage(true, uri) // VM
                                                    },
                                                contentScale = ContentScale.Fit
                                            )
                                        } ?: Text(stringResource(id = R.string.select_an_image))
                                    }
                                    MediaType.AUDIO -> {
                                        block.content?.let { uriString ->
                                            AudioPlayerBlock(uriString = uriString)
                                        } ?: Text("Error al cargar audio")
                                    }
                                    MediaType.VIDEO -> {
                                        block.content?.let { uriString ->
                                            val uri = Uri.parse(uriString)
                                            val exoPlayer = remember {
                                                ExoPlayer.Builder(context).build().apply {
                                                    setMediaItem(MediaItem.fromUri(uri))
                                                    prepare()
                                                    playWhenReady = false
                                                }
                                            }
                                            DisposableEffect(Unit) { onDispose { exoPlayer.release() } }
                                            AndroidView(
                                                modifier = Modifier.fillMaxWidth().height(200.dp).clip(RoundedCornerShape(12.dp)),
                                                factory = { ctx ->
                                                    PlayerView(ctx).apply {
                                                        player = exoPlayer
                                                        useController = true
                                                        setShowNextButton(false) // etc...
                                                    }
                                                }
                                            )
                                        } ?: Text("Sin video")
                                    }
                                }

                                TextField(
                                    value = block.description ?: "",
                                    onValueChange = { viewModel.updateBlockDescription(block.id, it) },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = TextFieldDefaults.colors(
                                        focusedContainerColor = Color.Transparent,
                                        unfocusedContainerColor = Color.Transparent,
                                        disabledContainerColor = Color.Transparent,
                                        focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                                        unfocusedIndicatorColor = Color.Gray
                                    )
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                // Switch Es Tarea
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
                    Button(onClick = { viewModel.setShowDatePicker(true) }) { // VM
                        Text(
                            if (uiState.dueDateTimestamp == null) stringResource(id = R.string.select_due_date)
                            else "${stringResource(id = R.string.date_label)} ${formatTimestamp(uiState.dueDateTimestamp)}"
                        )
                    }
                }

                // Botón Recordatorio
                Button(onClick = {
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                        val hasPermission = ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
                        if (hasPermission) {
                            viewModel.setShowReminderDatePicker(true)
                            viewModel.setReminderBaseDateMillis(uiState.dueDateTimestamp)
                        } else {
                            viewModel.setShowNotificationPermissionDeniedDialog(true)
                        }
                    } else {
                        viewModel.setShowReminderDatePicker(true)
                        viewModel.setReminderBaseDateMillis(uiState.dueDateTimestamp)
                    }
                }) {
                    Text(stringResource(id = R.string.add_reminder))
                }

                uiState.reminders.sortedBy { it.reminderTime }.forEach { reminder ->
                    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "Recordatorio: ${formatTimestamp(reminder.reminderTime)}", modifier = Modifier.weight(1f))
                        IconButton(onClick = { viewModel.removeReminder(reminder) }) {
                            Icon(Icons.Default.Close, contentDescription = null)
                        }
                    }
                }

                Spacer(Modifier.height(8.dp))

                // Botonera Inferior
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    Button(onClick = { viewModel.addMediaBlock(MediaType.TEXT, "Nuevo texto") }) {
                        Icon(Icons.Filled.Description, contentDescription = null)
                    }
                    Button(onClick = { viewModel.setShowImageSheet(true) }) { // VM
                        Icon(Icons.Filled.Image, contentDescription = null)
                    }
                    Button(onClick = {
                        val permissionCheck = ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO)
                        if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
                            viewModel.setShowAudioSheet(true) // VM
                        } else {
                            audioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                        }
                    }) {
                        Icon(Icons.Filled.Audiotrack, contentDescription = null)
                    }
                    Button(onClick = { viewModel.setShowVideoSheet(true) }) {
                        Icon(Icons.Filled.Videocam, contentDescription = null)
                    }
                }
                Spacer(Modifier.height(70.dp))
            }

            // --- DIÁLOGOS (Ahora controlados por uiState) ---

            if (uiState.showNotificationPermissionDeniedDialog) {
                AlertDialog(
                    onDismissRequest = { viewModel.setShowNotificationPermissionDeniedDialog(false) },
                    title = { Text("Permiso requerido") },
                    text = { Text("Necesitas permitir las notificaciones para crear recordatorios.") },
                    confirmButton = {
                        TextButton(onClick = {
                            viewModel.setShowNotificationPermissionDeniedDialog(false)
                            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                data = Uri.fromParts("package", context.packageName, null)
                            }
                            context.startActivity(intent)
                        }) { Text("Ir a Configuración") }
                    },
                    dismissButton = {
                        TextButton(onClick = { viewModel.setShowNotificationPermissionDeniedDialog(false) }) { Text("Cancelar") }
                    }
                )
            }

            if (uiState.showBlockMediaDeleteDialog) {
                AlertDialog(
                    onDismissRequest = { viewModel.setShowBlockMediaDeleteDialog(false) },
                    title = { Text(stringResource(id = R.string.delete_item)) },
                    text = { Text(stringResource(id = R.string.confirm_delete_content)) },
                    confirmButton = {
                        TextButton(onClick = {
                            uiState.blockToDelete?.let { viewModel.removeMediaBlock(it.id) }
                            viewModel.setShowBlockMediaDeleteDialog(false)
                        }) { Text(stringResource(id = R.string.delete), color = MaterialTheme.colorScheme.error) }
                    },
                    dismissButton = {
                        TextButton(onClick = { viewModel.setShowBlockMediaDeleteDialog(false) }) { Text("Cancelar") }
                    }
                )
            }

            if (uiState.showVideoPermissionDeniedDialog) {
                AlertDialog(
                    onDismissRequest = { viewModel.setShowVideoPermissionDeniedDialog(false) },
                    title = { Text("Permiso requerido") },
                    text = { Text("Necesitas permitir cámara y micrófono.") },
                    confirmButton = {
                        TextButton(onClick = {
                            viewModel.setShowVideoPermissionDeniedDialog(false)
                            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", context.packageName, null))
                            context.startActivity(intent)
                        }) { Text("Ir a Configuración") }
                    },
                    dismissButton = {
                        TextButton(onClick = { viewModel.setShowVideoPermissionDeniedDialog(false) }) { Text("Cancelar") }
                    }
                )
            }

            Box(modifier = Modifier.fillMaxSize().padding(padding).imePadding()) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = null,
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(16.dp)
                        .size(48.dp)
                        .combinedClickable(
                            onClick = { navController.popBackStack() },
                            onLongClick = { viewModel.setShowDeleteDialog(true) } // VM
                        )
                        .background(Color.Transparent, shape = CircleShape)
                        .padding(8.dp)
                )

                if (uiState.showDeleteDialog) {
                    AlertDialog(
                        onDismissRequest = { viewModel.setShowDeleteDialog(false) },
                        title = { Text(stringResource(id = R.string.delete_note)) },
                        text = { Text(stringResource(id = R.string.confirm_delete_note)) },
                        confirmButton = {
                            TextButton(onClick = {
                                viewModel.deleteNoteWithDetails(noteId)
                                viewModel.setShowDeleteDialog(false)
                                navController.popBackStack()
                            }) { Text(stringResource(id = R.string.delete), color = MaterialTheme.colorScheme.error) }
                        },
                        dismissButton = {
                            TextButton(onClick = { viewModel.setShowDeleteDialog(false) }) { Text("Cancelar") }
                        }
                    )
                }

                IconButton(
                    onClick = {
                        viewModel.saveNote(isEditing = noteId != -1, noteId = noteId)
                        navController.popBackStack()
                    },
                    modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp)
                ) {
                    Icon(Icons.Default.Save, contentDescription = null)
                }
            }
        }
    }

    // --- PICKERS Y SHEETS EXTERNOS ---

    if (uiState.showDatePicker) {
        val datePickerState = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { viewModel.setShowDatePicker(false) },
            confirmButton = {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    TextButton(onClick = { viewModel.setShowDatePicker(false) }) { Text("Cancelar") }
                    TextButton(onClick = {
                        datePickerState.selectedDateMillis?.let { dateMillis ->
                            val utcCalendar = Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC")).apply { timeInMillis = dateMillis }
                            val localCalendar = Calendar.getInstance().apply {
                                set(utcCalendar.get(Calendar.YEAR), utcCalendar.get(Calendar.MONTH), utcCalendar.get(Calendar.DAY_OF_MONTH), 0, 0, 0)
                                set(Calendar.MILLISECOND, 0)
                            }
                            viewModel.onDueDateChange(localCalendar.timeInMillis)
                            viewModel.setShowDatePicker(false)
                            viewModel.setShowTimePicker(true) // VM
                        }
                    }) { Text("Siguiente") }
                }
            }
        ) { DatePicker(state = datePickerState) }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    if (uiState.showReminderDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = uiState.dueDateTimestamp)
        DatePickerDialog(
            onDismissRequest = { viewModel.setShowReminderDatePicker(false) },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { dateMillis ->
                        val utcCalendar = Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC")).apply { timeInMillis = dateMillis }
                        val localCalendar = Calendar.getInstance().apply {
                            set(utcCalendar.get(Calendar.YEAR), utcCalendar.get(Calendar.MONTH), utcCalendar.get(Calendar.DAY_OF_MONTH), 0, 0, 0)
                            set(Calendar.MILLISECOND, 0)
                        }
                        viewModel.setReminderBaseDateMillis(localCalendar.timeInMillis)
                        viewModel.setShowReminderDatePicker(false)
                        viewModel.setShowTimePicker(true)
                    }
                }) { Text("Siguiente") }
            }
        ) { DatePicker(state = datePickerState) }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    if (uiState.showTimePicker) {
        val timePickerState = rememberTimePickerState()
        val baseDateTimestamp = uiState.reminderBaseDateMillis ?: uiState.dueDateTimestamp ?: Calendar.getInstance().timeInMillis

        TimePickerDialog(
            onDismissRequest = {
                viewModel.setShowTimePicker(false)
                viewModel.setReminderBaseDateMillis(null)
            },
            title = { Text("Selecciona la hora") },
            confirmButton = {
                TextButton(onClick = {
                    val combinedTimestamp = combineDateWithTime(baseDateTimestamp, timePickerState.hour, timePickerState.minute)
                    if (uiState.reminderBaseDateMillis != null) {
                        viewModel.addReminder(combinedTimestamp)
                    } else {
                        viewModel.onDueDateChange(combinedTimestamp)
                    }
                    viewModel.setShowTimePicker(false)
                    viewModel.setReminderBaseDateMillis(null)
                }) { Text("Aceptar") }
            }
        ) { TimePicker(state = timePickerState) }
    }

    if (uiState.showFullImage && uiState.fullImageUri != null) {
        FullScreenImageViewer(
            imageUri = uiState.fullImageUri!!,
            onClose = { viewModel.setShowFullImage(false) },
        )
    }

    val pickVideoLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let {
            try { context.contentResolver.takePersistableUriPermission(it, Intent.FLAG_GRANT_READ_URI_PERMISSION) } catch (e: Exception) {}
            viewModel.onVideoSelected(context, it)
        }
    }

    if (uiState.showVideoSheet) {
        VideoPickerSheet(
            onTakeVideo = {
                val perms = requiredVideoPermissions()
                val granted = perms.all { ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED }
                if (!granted) {
                    permissionsLauncher.launch(perms)
                } else {
                    val uri = viewModel.prepareVideoUri(context)
                    if (uri != null) {
                        viewModel.setPendingVideoUri(uri)
                        takeVideoLauncher.launch(uri)
                    }
                }
            },
            onPickGallery = { pickVideoLauncher.launch(arrayOf("video/*")) },
            onDismiss = { viewModel.setShowVideoSheet(false) }
        )
    }

    if (uiState.showAudioSheet) {
        AudioRecorderSheet(
            onDismiss = { viewModel.setShowAudioSheet(false) },
            onFileReady = { uri ->
                viewModel.addMediaBlock(MediaType.AUDIO, uri.toString())
                viewModel.setShowAudioSheet(false)
            }
        )
    }

    LaunchedEffect(uiState.requestVideoPermission) {
        if (uiState.requestVideoPermission) {
            permissionsLauncher.launch(requiredVideoPermissions())
        }
    }
}

fun requiredVideoPermissions(): Array<String> {
    return arrayOf(
        android.Manifest.permission.CAMERA,
        android.Manifest.permission.RECORD_AUDIO
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideoPickerSheet(
    onTakeVideo: () -> Unit,
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
            Text("Añadir video", style = MaterialTheme.typography.titleMedium)

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = {
                    onTakeVideo()
                    onDismiss()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Videocam, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Grabar video")
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    onPickGallery()
                    onDismiss()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.VideoLibrary, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Elegir de la galería")
            }
        }
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AudioRecorderSheet(
    onDismiss: () -> Unit,
    onFileReady: (Uri) -> Unit
) {
    val context = LocalContext.current
    var isRecording by remember { mutableStateOf(false) }
    var recorder by remember { mutableStateOf<MediaRecorder?>(null) }
    var outputFile by remember { mutableStateOf<File?>(null) }

    // Limpiar recorder al cerrar
    DisposableEffect(Unit) {
        onDispose {
            try {
                if (isRecording) {
                    recorder?.stop()
                }
                recorder?.release()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = if (isRecording) "Grabando..." else "Toque el micrófono para grabar",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(24.dp))

            if (isRecording) {
                // Botón de STOP
                Button(
                    onClick = {
                        try {
                            recorder?.stop()
                            recorder?.release()
                            recorder = null
                            isRecording = false
                            outputFile?.let { file ->
                                val uri = FileProvider.getUriForFile(
                                    context,
                                    "${context.packageName}.provider",
                                    file
                                )
                                onFileReady(uri)
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                            isRecording = false
                        }
                    },
                    modifier = Modifier.size(80.dp),
                    shape = CircleShape,
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Icon(Icons.Default.Stop, contentDescription = "Detener", modifier = Modifier.size(40.dp))
                }
            } else {
                // Botón de GRABAR
                Button(
                    onClick = {
                        val file = File(context.cacheDir, "audio_${System.currentTimeMillis()}.mp4")
                        outputFile = file

                        // Configuración del MediaRecorder
                        val newRecorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                            MediaRecorder(context)
                        } else {
                            @Suppress("DEPRECATION")
                            MediaRecorder()
                        }

                        newRecorder.apply {
                            setAudioSource(MediaRecorder.AudioSource.MIC)
                            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                            setOutputFile(file.absolutePath)
                            try {
                                prepare()
                                start()
                                isRecording = true
                                recorder = newRecorder
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    },
                    modifier = Modifier.size(80.dp),
                    shape = CircleShape
                ) {
                    Icon(Icons.Default.Mic, contentDescription = "Grabar", modifier = Modifier.size(40.dp))
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun AudioPlayerBlock(uriString: String) {
    val context = LocalContext.current
    var isPlaying by remember { mutableStateOf(false) }
    var mediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }

    DisposableEffect(uriString) {
        onDispose {
            mediaPlayer?.release()
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = {
                    if (isPlaying) {
                        mediaPlayer?.pause()
                        isPlaying = false
                    } else {
                        if (mediaPlayer == null) {
                            mediaPlayer = MediaPlayer().apply {
                                setDataSource(context, Uri.parse(uriString))
                                prepare()
                                setOnCompletionListener {
                                    isPlaying = false
                                }
                            }
                        }
                        mediaPlayer?.start()
                        isPlaying = true
                    }
                }
            ) {
                Icon(
                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (isPlaying) "Pausar" else "Reproducir"
                )
            }

            Spacer(modifier = Modifier.width(8.dp))
            Text("Nota de voz", style = MaterialTheme.typography.bodyMedium)
        }
    }
}