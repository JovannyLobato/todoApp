package com.example.todoapp

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Alignment
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.rememberDatePickerState
import android.app.TimePickerDialog
import android.net.Uri
import android.content.Intent
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import com.example.todoapp.viewmodel.NoteViewModel
import kotlinx.coroutines.launch


@Composable
fun AddEditScreen(
    navController: NavController,
    viewModel: NoteViewModel,
    noteId: Int, // 0 si es nueva, >0 si es edición
    initialTitle: String,
    initialDescription: String,
    initialImageUri: String?,
    initialIsTask: Boolean,
    initialDueDateTimestamp: Long?
) {
    AddEditContent(
        isEditing = noteId > 0, // Determinar si es edición
        viewModel = viewModel,
        navController = navController,
        noteId = noteId,
        initialTitle = initialTitle,
        initialDescription = initialDescription,
        initialImageUri = initialImageUri,
        initialIsTask = initialIsTask,
        initialDueDateTimestamp = initialDueDateTimestamp
    )
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditContent(
    isEditing: Boolean, // ID > 0
    viewModel: NoteViewModel,
    navController: NavController,
    noteId: Int, // ID de la nota (0 si es nueva)
    initialTitle: String,
    initialDescription: String,
    initialImageUri: String?,
    initialIsTask: Boolean,
    initialDueDateTimestamp: Long?
) {
    // 1. ESTADOS
    var title by remember { mutableStateOf(initialTitle) }
    var description by remember { mutableStateOf(initialDescription) }
    var imageUri by remember { mutableStateOf(initialImageUri) }
    var isTask by remember { mutableStateOf(initialIsTask) }
    var dueDateTimestamp by remember { mutableStateOf(initialDueDateTimestamp) }

    // Lógica de Fecha/Hora
    val scope = rememberCoroutineScope()
    val contextAndroid = LocalContext.current
    var showDatePicker by remember { mutableStateOf(false) }

    // Usamos un Calendar mutable para la lógica de selección.
    val tempDateCalendar = remember {
        Calendar.getInstance().apply {
            initialDueDateTimestamp?.let { timeInMillis = it }
        }
    }

    // 2. LAUNCHER DE IMAGEN (Con persistencia de permisos)
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            imageUri = it.toString()
            val flag = Intent.FLAG_GRANT_READ_URI_PERMISSION
            try {
                // Persistir el permiso de lectura
                contextAndroid.contentResolver.takePersistableUriPermission(it, flag)
            } catch (e: SecurityException) {
                e.printStackTrace()
            }
        }
    }

    // 3. DIÁLOGOS DE HORA
    val timePickerDialog = TimePickerDialog(
        contextAndroid,
        { _, hour: Int, minute: Int ->
            // Actualiza la hora en el Calendar temporal y en el Timestamp
            tempDateCalendar.set(Calendar.HOUR_OF_DAY, hour)
            tempDateCalendar.set(Calendar.MINUTE, minute)
            tempDateCalendar.set(Calendar.SECOND, 0)
            tempDateCalendar.set(Calendar.MILLISECOND, 0)
            dueDateTimestamp = tempDateCalendar.timeInMillis
        },
        tempDateCalendar.get(Calendar.HOUR_OF_DAY),
        tempDateCalendar.get(Calendar.MINUTE),
        true // 24-hour format
    )

    // 4. LÓGICA DE GUARDADO/ACTUALIZACIÓN
    val onSave: () -> Unit = {
        if (title.isNotBlank()) {
            scope.launch {
                val finalTimestamp = if (isTask) dueDateTimestamp else null

                // Determinar si llamar a addNote (creación) o updateNote (edición)
                if (isEditing) {
                    viewModel.updateNote(noteId, title, description, imageUri, isTask, finalTimestamp)
                } else {
                    // Si es nueva, Room inserta automáticamente un nuevo ID (ID=0)
                    viewModel.addNote(title, description, imageUri, isTask, finalTimestamp)
                }
                navController.popBackStack() // Navegar de vuelta
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEditing) "Editar Elemento" else "Añadir Elemento") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // CAMPO DE TÍTULO
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Título") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(8.dp))

            // CAMPO DE DESCRIPCIÓN
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Descripción") },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 100.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // INTERRUPTOR DE TAREA
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("¿Es una Tarea?", style = MaterialTheme.typography.titleMedium)
                Switch(
                    checked = isTask,
                    onCheckedChange = {
                        isTask = it
                        // Limpiar el timestamp si se desactiva Tarea
                        if (!it) dueDateTimestamp = null
                    }
                )
            }

            // SELECTOR DE FECHA Y HORA (solo visible si es tarea)
            if (isTask) {
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedButton(
                    onClick = { showDatePicker = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.DateRange, contentDescription = "Fecha de Vencimiento")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = formatTimestamp(dueDateTimestamp),
                            color = if (dueDateTimestamp == null) MaterialTheme.colorScheme.error else LocalContentColor.current
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // BOTÓN PARA SELECCIONAR IMAGEN
            OutlinedButton(
                onClick = { imagePickerLauncher.launch("image/*") },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (imageUri != null) "Cambiar Imagen" else "Seleccionar Imagen")
            }

            if (imageUri != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Botón para eliminar la imagen
                    TextButton(onClick = { imageUri = null }) {
                        Text("Eliminar Imagen", color = MaterialTheme.colorScheme.error)
                    }
                }

                // VISUALIZACIÓN DE IMAGEN
                AsyncImage(
                    model = Uri.parse(imageUri),
                    contentDescription = "Imagen seleccionada",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .padding(vertical = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // BOTÓN GUARDAR (Llama a onSave)
            Button(
                onClick = onSave,
                // Debe tener título y, si es tarea, debe tener fecha
                enabled = title.isNotBlank() && (!isTask || dueDateTimestamp != null),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (noteId == 0) "Crear ${if (isTask) "Tarea" else "Nota"}" else "Guardar Cambios")
            }
        }
    }

    // 5. DatePickerDialog
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = dueDateTimestamp ?: System.currentTimeMillis()
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDatePicker = false
                        datePickerState.selectedDateMillis?.let { millis ->

                            val tempCal = Calendar.getInstance().apply { timeInMillis = millis }
                            tempDateCalendar.set(tempCal.get(Calendar.YEAR), tempCal.get(Calendar.MONTH), tempCal.get(Calendar.DAY_OF_MONTH))


                            timePickerDialog.show()
                        }
                    }
                ) { Text("Siguiente (Hora)") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancelar") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

fun formatTimestamp(timestamp: Long?): String {
    return if (timestamp != null && timestamp > 0) {
        val date = Date(timestamp)
        SimpleDateFormat("dd MMM yyyy HH:mm", Locale.getDefault()).format(date)
    } else {
        "Seleccionar Fecha y Hora de Vencimiento"
    }
}