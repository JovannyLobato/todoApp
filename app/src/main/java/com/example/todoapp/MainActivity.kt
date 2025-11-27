package com.example.todoapp

import android.Manifest
import android.app.AlertDialog
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.* // Importa todos los layout
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.*
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.todoapp.model.MediaType
import com.example.todoapp.model.Note
import com.example.todoapp.ui.theme.TodoappTheme
import com.example.todoapp.viewmodel.NoteViewModel

class MainActivity : ComponentActivity() {

    private var hasShownRationale = false

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            Toast.makeText(this, "Notificaciones activadas", Toast.LENGTH_SHORT).show()
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.POST_NOTIFICATIONS)) {
                    showPermissionRationaleDialog()
                } else {
                    Toast.makeText(this, "Habilita las notificaciones en Ajustes para ver las alarmas", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        checkNotificationPermission()

        val noteIdFromNotification = intent.getIntExtra("NOTE_ID", -1)

        val app = application as TodoApplication
        val viewModelFactory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(NoteViewModel::class.java)) {
                    @Suppress("UNCHECKED_CAST")
                    return NoteViewModel(app.repository) as T
                }
                throw IllegalArgumentException("Unknown ViewModel class")
            }
        }

        setContent {
            TodoappTheme {
                val windowSize = calculateWindowSizeClass(this)
                Surface {
                    MyApp(
                        windowSizeClass = windowSize.widthSizeClass,
                        viewModel = viewModel(factory = viewModelFactory),
                        startNoteId = noteIdFromNotification
                    )
                }
            }
        }
    }

    private fun checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
                // Ya tenemos permiso
            } else {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    private fun showPermissionRationaleDialog() {
        if (hasShownRationale) return
        hasShownRationale = true

        AlertDialog.Builder(this)
            .setTitle("Permiso necesario")
            .setMessage("Para que la app funcione correctamente y te avise de tus tareas, necesitas aceptar el permiso de notificaciones.")
            .setPositiveButton("Intentar de nuevo") { _, _ ->
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
            .setNegativeButton("Cancelar") { dialog, _ ->
                dialog.dismiss()
                Toast.makeText(this, "Las alarmas no sonarán visualmente", Toast.LENGTH_SHORT).show()
            }
            .setCancelable(false)
            .show()
    }
}

@Composable
fun MyApp(
    windowSizeClass: WindowWidthSizeClass,
    viewModel: NoteViewModel,
    startNoteId: Int = -1
) {
    val navController = rememberNavController()
    LaunchedEffect(startNoteId) {
        if (startNoteId != -1) {
            // Navegar a la pantalla de edición con el ID de la nota
            navController.navigate("edit?noteId=$startNoteId")
        }
    }

    when (windowSizeClass) {
        WindowWidthSizeClass.Compact -> TodoAppCompact(navController, viewModel)
        WindowWidthSizeClass.Medium -> TodoAppMedium(navController, viewModel)
        WindowWidthSizeClass.Expanded -> TodoAppExpanded(navController, viewModel)
    }
}

@Composable
fun TodoAppCompact(navController: NavHostController, viewModel: NoteViewModel) {
    NavHost(
        navController = navController,
        startDestination = "main"
    ) {
        composable("main") {
            MainScreen(navController, viewModel, isCompact = true)
        }

        composable(
            route = "edit?noteId={noteId}",
            arguments = listOf(navArgument("noteId") {
                type = NavType.IntType
                defaultValue = -1
            })
        ) { backStackEntry ->
            val noteId = backStackEntry.arguments?.getInt("noteId") ?: -1
            AddEditScreen(
                navController = navController,
                viewModel = viewModel,
                noteId = noteId
            )
        }
    }
}


@Composable
fun TodoAppMedium(navController: NavHostController, viewModel: NoteViewModel) {
    var selectedNote by remember { mutableStateOf<Note?>(null) }

    Row(modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier.weight(1f)) {
            MainScreen(
                navController = navController,
                viewModel = viewModel,
                isCompact = false,
                onNoteSelected = { note ->
                    selectedNote = note
                },

                onAddNote = {
                    viewModel.resetUiState()
                    selectedNote = Note(id = -1, title = "", isTask = false, dueDateTimestamp = null)
                }
            )
        }

        Box(modifier = Modifier.weight(1f)) {
            if (selectedNote != null) {
                AddEditScreen(
                    navController = navController,
                    viewModel = viewModel,
                    noteId = selectedNote!!.id
                )
            } else {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(stringResource(id = R.string.select_a_note_for_edit))
                }
            }
        }
    }
}

@Composable
fun TodoAppExpanded(navController: NavHostController, viewModel: NoteViewModel) {
    Row(modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier.weight(0.4f)) {
            MainScreen(navController, viewModel, isCompact = false)
        }
        Box(modifier = Modifier.weight(0.6f)) {
            AddEditScreen(navController, viewModel, noteId = 0)
        }
    }
}

@Composable
fun MainScreen(
    navController: NavController,
    viewModel: NoteViewModel,
    isCompact: Boolean,
    onNoteSelected: (Note) -> Unit = {},
    onAddNote: (() -> Unit)? = null
) {
    val allString = stringResource(id = R.string.all)
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf( allString) }

    val notesWithDetails by viewModel.allNotes.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 15.dp)
            .padding(top = 10.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text(text = stringResource(id = R.string.search)) },
                shape = RoundedCornerShape(25.dp),
                modifier = Modifier
                    .weight(2f)
                    .fillMaxHeight()
                    .heightIn(min = 50.dp),
                singleLine = true
            )
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = {
                    if (isCompact) {
                        val defaultRoute = "edit?noteId=-1"
                        navController.navigate(defaultRoute)
                    } else {
                        onAddNote?.invoke()
                    }
                },
                modifier = Modifier
                    .size(60.dp)
                    .padding(8.dp),
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = Color.White
                ),
                contentPadding = PaddingValues(0.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = "Agregar",
                    modifier = Modifier.size(28.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        val all = stringResource(id = R.string.all)
        val notesString = stringResource(id = R.string.notes)
        val tasks = stringResource(id = R.string.tasks)
        Row {
            listOf(stringResource(id = R.string.all), stringResource(id = R.string.notes),
                stringResource(id = R.string.tasks)).forEach { option ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(end = 16.dp)
                ) {
                    RadioButton(
                        selected = selectedFilter == option,
                        onClick = { selectedFilter = option }
                    )
                    Text(option)
                }
            }
        }
        Spacer(modifier = Modifier.height(5.dp))
        LazyColumn (modifier = Modifier.padding(bottom = 10.dp)){
            items(
                notesWithDetails.filter { noteWithDetails ->
                    val note = noteWithDetails.note
                    (selectedFilter == all ||
                            (selectedFilter == notesString && !note.isTask) ||
                            (selectedFilter == tasks && note.isTask)) &&
                            note.title.contains(searchQuery, ignoreCase = true)
                }
            ) { noteWithDetails ->
                val orderedBlocks = noteWithDetails.mediaBlocks.sortedBy { it.order }
                val note = noteWithDetails.note
                val nChars=50
                val firstText = orderedBlocks.firstOrNull { it.type == MediaType.TEXT }
                    ?.description?.take(nChars) ?: ""
                val firstImageUri = orderedBlocks.firstOrNull { it.type == MediaType.IMAGE }?.content

                NoteItem(
                    title = if (note.title != "") note.title else stringResource(R.string.no_tittle),
                    description = firstText,
                    imageUri = firstImageUri,
                    isTask = note.isTask,
                    dueDateTimestamp = note.dueDateTimestamp,
                    onClick = {
                        if (isCompact) {
                            navController.navigate("edit?noteId=${note.id}")
                        } else {
                            onNoteSelected(note)
                        }
                    }
                )
            }
        }
    }
}