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
import android.content.Intent // Necesario para la bandera de permiso
import com.example.todoapp.viewmodel.NoteViewModel // Importación asumida


@Composable
fun AddNote(navController: NavController) {
    val context = LocalContext.current.applicationContext as TodoApplication
    val viewModel = remember { NoteViewModel(context.repository) }

    AddNoteScreen(
        onAddNote = { title, description, imageUri, isTask, dueDateTimestamp ->
            viewModel.addNote(title, description, imageUri, isTask, dueDateTimestamp)
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
    onAddNote: (String, String, String?, Boolean, Long?) -> Unit,
    onCancel: () -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var imageUri by remember { mutableStateOf<String?>(null) }
    var isTask by remember { mutableStateOf(false) }

    // Lógica de Fecha/Hora
    val calendar = remember { Calendar.getInstance() }
    var selectedDate by remember { mutableStateOf<Calendar?>(null) }
    var showDatePicker by remember { mutableStateOf(false) }
    val context = LocalContext.current


    // 💡 CAMBIO APLICADO: Launcher con lógica para persistir permisos de URI
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            imageUri = it.toString() // Guarda la URI como String

            val contentResolver = context.contentResolver
            val takeFlags: Int = Intent.FLAG_GRANT_READ_URI_PERMISSION

            // Persistir el permiso de lectura
            try {
                contentResolver.takePersistableUriPermission(it, takeFlags)
            } catch (e: SecurityException) {
                // En caso de error de seguridad (ej. archivos de otras apps), simplemente continúa
                e.printStackTrace()
            }
        }
    }

    val showTimePicker = {
        val listener = TimePickerDialog.OnTimeSetListener { _, hour, minute ->
            val finalCalendar = selectedDate ?: Calendar.getInstance()
            finalCalendar.set(Calendar.HOUR_OF_DAY, hour)
            finalCalendar.set(Calendar.MINUTE, minute)
            selectedDate = finalCalendar
        }

        TimePickerDialog(
            context,
            listener,
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            false
        ).show()
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Añadir Nota o Tarea") })
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
                .fillMaxHeight()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Título") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Descripción") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Switch para Tarea (isTask)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("¿Es una Tarea?")
                Switch(
                    checked = isTask,
                    onCheckedChange = { isTask = it }
                )
            }

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

            // Lógica de Fecha y Hora (si es tarea)
            if (isTask) {
                Spacer(modifier = Modifier.height(16.dp))
                Divider()
                Text("Fecha y Hora de Tarea", style = MaterialTheme.typography.titleMedium)

                Button(onClick = { showDatePicker = true }, modifier = Modifier.fillMaxWidth()) {
                    Text(
                        selectedDate?.let {
                            SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(it.time)
                        } ?: "Seleccionar Fecha"
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))

                Button(onClick = showTimePicker, modifier = Modifier.fillMaxWidth(), enabled = selectedDate != null) {
                    Text(
                        selectedDate?.let {
                            SimpleDateFormat("hh:mm a", Locale.getDefault()).format(it.time)
                        } ?: "Seleccionar Hora"
                    )
                }

                if (selectedDate != null) {
                    val fullDateTimeFormat = SimpleDateFormat("EEEE, d MMM yyyy, hh:mm a", Locale("es", "ES"))
                    Text("Recordatorio: ${fullDateTimeFormat.format(selectedDate!!.time)}", style = MaterialTheme.typography.bodySmall)
                }
                Divider()
                Spacer(modifier = Modifier.height(16.dp))
            }

            // BOTONES DE ACCIÓN
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                OutlinedButton(onClick = onCancel, modifier = Modifier.weight(1f)) {
                    Text("Cancelar")
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Botón para adjuntar archivos
                Button(onClick = { launcher.launch("image/*") }, modifier = Modifier.weight(1f)) {
                    Text("Adjuntar")
                }

                Spacer(modifier = Modifier.width(8.dp))

                Button(
                    onClick = {
                        val timestamp = if (isTask && selectedDate != null) selectedDate!!.timeInMillis else null

                        onAddNote(title, description, imageUri, isTask, timestamp)
                    },
                    modifier = Modifier.weight(1f),
                    enabled = title.isNotBlank() && description.isNotBlank()
                ) {
                    Text(if (isTask) "Guardar Tarea" else "Guardar Nota")
                }
            }
        }
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = System.currentTimeMillis()
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDatePicker = false
                        datePickerState.selectedDateMillis?.let { millis ->
                            val tempCal = Calendar.getInstance()
                            tempCal.timeInMillis = millis
                            val finalCal = selectedDate ?: Calendar.getInstance()
                            finalCal.set(tempCal.get(Calendar.YEAR), tempCal.get(Calendar.MONTH), tempCal.get(Calendar.DAY_OF_MONTH))
                            selectedDate = finalCal
                        }
                    }
                ) { Text("Aceptar") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancelar") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AddNotePreview() {
    AddNoteScreen(
        onAddNote = { _, _, _, _, _ -> },
        onCancel = {}
    )
}