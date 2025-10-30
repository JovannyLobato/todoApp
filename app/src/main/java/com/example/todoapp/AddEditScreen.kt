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
import androidx.compose.ui.res.stringResource
import com.example.todoapp.viewmodel.NoteViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.selects.select


@Composable
fun AddEditScreen(
    navController: NavController,
    viewModel: NoteViewModel,
    noteId: Int,
    initialTitle: String,
    initialDescription: String,
    initialImageUri: String?,
    initialIsTask: Boolean,
    initialDueDateTimestamp: Long?
) {
    AddEditContent(
        isEditing = noteId > 0,
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
    isEditing: Boolean,
    viewModel: NoteViewModel,
    navController: NavController,
    noteId: Int,
    initialTitle: String,
    initialDescription: String,
    initialImageUri: String?,
    initialIsTask: Boolean,
    initialDueDateTimestamp: Long?
) {
    val uiState by viewModel.uiState.collectAsState()
    val contextAndroid = LocalContext.current
    val scope = rememberCoroutineScope()

    /*
    var dueDateTimestamp = uiState.dueDateTimestamp
    var title = uiState.title
    var isTask = uiState.isTask
    var description = uiState.description
    var imageUri = uiState.imageUri
    var showDatePicker = uiState.showDatePicker
    */
    LaunchedEffect(noteId) {
        viewModel.loadNoteData(
            initialTitle,
            initialDescription,
            initialImageUri,
            initialIsTask,
            initialDueDateTimestamp
        )
    }

    Column {
        TextField(
            value = uiState.title,
            onValueChange = viewModel::onTitleChange,
            label = { Text(text = stringResource(id = R.string.tittle) ) }
        )

        TextField(
            value = uiState.description,
            onValueChange = viewModel::onDescriptionChange,
            label = { Text(text = stringResource(id = R.string.description)) }
        )

        Switch(
            checked = uiState.isTask,
            onCheckedChange = viewModel::onIsTaskChange
        )

        Button(onClick = { viewModel.setShowDatePicker(true) }) {
            Text(text = stringResource(id = R.string.select_date))
        }

        if (uiState.showDatePicker) {
        }

        Button(onClick = {
            viewModel.saveNote(noteId, isEditing)
            navController.popBackStack()
        }) {
            Text(if (isEditing) stringResource(id = R.string.update)
            else stringResource(id = R.string.save))
        }
    }

    val tempDateCalendar = remember {
        Calendar.getInstance().apply {
            initialDueDateTimestamp?.let { timeInMillis = it }
        }
    }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            uiState.imageUri = it.toString()
            val flag = Intent.FLAG_GRANT_READ_URI_PERMISSION
            try {
                contextAndroid.contentResolver.takePersistableUriPermission(it, flag)
            } catch (e: SecurityException) {
                e.printStackTrace()
            }
        }
    }

    val timePickerDialog = TimePickerDialog(
        contextAndroid,
        { _, hour: Int, minute: Int ->
            tempDateCalendar.set(Calendar.HOUR_OF_DAY, hour)
            tempDateCalendar.set(Calendar.MINUTE, minute)
            tempDateCalendar.set(Calendar.SECOND, 0)
            tempDateCalendar.set(Calendar.MILLISECOND, 0)
            viewModel.onDueDateChange(tempDateCalendar.timeInMillis)
        },
        tempDateCalendar.get(Calendar.HOUR_OF_DAY),
        tempDateCalendar.get(Calendar.MINUTE),
        true
    )

    val onSave: () -> Unit = {
        if (uiState.title.isNotBlank()) {
            scope.launch {
                val finalTimestamp = if (uiState.isTask) uiState.dueDateTimestamp else null

                if (isEditing) {
                    viewModel.updateNote(noteId, uiState.title, uiState.description,
                        uiState.imageUri, uiState.isTask, finalTimestamp)
                } else {
                    viewModel.addNote(uiState.title, uiState.description, uiState.imageUri, uiState.isTask, finalTimestamp)
                }
                navController.popBackStack()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEditing) "Editar Elemento" else "Añadir Elemento") },
                // title = {},
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
            // CAMPO DE titulo
            OutlinedTextField(
                value = uiState.title,
                onValueChange = { viewModel.onTitleChange(it) },
                label = { Text(text = stringResource(id = R.string.tittle)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(8.dp))

            // CAMPO DE DESCRIPCIÓN
            OutlinedTextField(
                value = uiState.description,
                onValueChange = { viewModel.onDescriptionChange(it) },
                label = { Text(text = stringResource(id = R.string.description)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 100.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))


            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = stringResource(id = R.string.is_a_taskq), style = MaterialTheme.typography.titleMedium)
                Switch(
                    checked = uiState.isTask,
                    onCheckedChange = {
                        viewModel.onIsTaskChange(it)
                        // Limpiar el timestamp si se desactiva Tarea
                        if (!it) viewModel.onDueDateChange(null)

                    }
                )
            }

            if (uiState.isTask) {
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedButton(
                    onClick = { viewModel.setShowDatePicker(true) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.DateRange, contentDescription = "Fecha de Vencimiento")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = formatTimestamp(uiState.dueDateTimestamp),
                            color = if (uiState.dueDateTimestamp == null) MaterialTheme.colorScheme.error else LocalContentColor.current
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
                Text (stringResource(
                        if (uiState.imageUri != null)
                            R.string.change_image
                        else
                            R.string.select_an_image
                    )
                )
            }

            if (uiState.imageUri != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Boton para eliminar la imagen
                    TextButton(onClick = { uiState.imageUri = null }) {
                        Text("Delete image", color = MaterialTheme.colorScheme.error)
                    }
                }
                // me quede aqui
                // VISUALIZACIÓN DE IMAGEN
                AsyncImage(
                    model = Uri.parse(uiState.imageUri),
                    contentDescription = "Imagen seleccionada",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .padding(vertical = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onSave,
                enabled = uiState.title.isNotBlank() && (!uiState.isTask || uiState.dueDateTimestamp != null),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (noteId == 0) "Crear ${if (uiState.isTask) "Tarea" else "Nota"}" else "Guardar Cambios")
            }
        }
    }

    if (uiState.showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = uiState.dueDateTimestamp ?: System.currentTimeMillis()
        )
        DatePickerDialog(
            onDismissRequest = { viewModel.setShowDatePicker(false) },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.setShowDatePicker(false)
                        datePickerState.selectedDateMillis?.let { millis ->

                            val tempCal = Calendar.getInstance().apply { timeInMillis = millis }
                            tempDateCalendar.set(tempCal.get(Calendar.YEAR), tempCal.get(Calendar.MONTH), tempCal.get(Calendar.DAY_OF_MONTH))


                            timePickerDialog.show()
                        }
                    }
                ) { Text(text = stringResource(id = R.string.next_hour)) }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.setShowDatePicker(false) }) { Text(
                    text = stringResource(id = R.string.cancel)) }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@Composable
fun formatTimestamp(timestamp: Long?): String {
    return if (timestamp != null && timestamp > 0) {
        val date = Date(timestamp)
        SimpleDateFormat("dd MMM yyyy HH:mm", Locale.getDefault()).format(date)
    } else {
        stringResource(id = R.string.select_a_date_and_time_for_the_task )
    }
}