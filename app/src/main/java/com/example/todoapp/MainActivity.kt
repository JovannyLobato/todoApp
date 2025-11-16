package com.example.todoapp

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.*
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
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
    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("TodoAppDebug", "onCreate started")
        try {
            val app = application as TodoApplication
            Log.d("TodoAppDebug", "TodoApplication OK")

            val viewModel = NoteViewModel(app.repository)
            Log.d("TodoAppDebug", "NoteViewModel OK")

            setContent {
                TodoappTheme {
                    val windowSize = calculateWindowSizeClass(this)
                    Surface {
                        MyApp(
                            windowSizeClass = windowSize.widthSizeClass,
                            viewModel = viewModel
                        )
                    }
                }
            }

        } catch (e: Exception) {
            Log.e("TodoAppDebug", "Error en MainActivity", e)
        }
    }
}



@Composable
fun MyApp(windowSizeClass: WindowWidthSizeClass, viewModel: NoteViewModel) {
    val navController = rememberNavController()

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
    // Estado compartido entre ambas pantallas
    var selectedNote by remember { mutableStateOf<Note?>(null) }

    Row(modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier.weight(1f)) {
            // Pasamos un callback que actualiza la nota seleccionada
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
    // val context = LocalContext.current.applicationContext as TodoApplication
    // val viewModel = remember { NoteViewModel(context.repository) }
    val allString = stringResource(id = R.string.all)
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf( allString) }

    val notesWithDetails by viewModel.allNotes.collectAsState()


    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(18.dp)
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
                        // En pantallas compact navega a la pantalla de edición
                        // val defaultRoute = "edit/0/${Uri.encode("")}/${Uri.encode("")}/null/false/null"
                        val defaultRoute = "edit?noteId=-1"
                        navController.navigate(defaultRoute)
                    } else {
                        // En pantallas medianas/grandes, solo limpia el formulario

                        onAddNote?.invoke()
                    }
                },
                modifier = Modifier
                    .size(60.dp) // controla el diametro del círculo
                    .padding(8.dp),
                shape = CircleShape, // hace el boton redondo
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = Color.White
                ),
                contentPadding = PaddingValues(0.dp) // evita espacio interno extra
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = "Agregar",
                    modifier = Modifier.size(28.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
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
        Spacer(modifier = Modifier.height(16.dp))
        val all =  stringResource(id = R.string.all)
        val notesString = stringResource(id = R.string.notes)
        val tasks = stringResource(id = R.string.tasks)
        LazyColumn {
            items(
                notesWithDetails.filter { noteWithDetails ->
                    val note = noteWithDetails.note
                    (selectedFilter == all ||
                            (selectedFilter == notesString && !note.isTask) ||
                            (selectedFilter == tasks && note.isTask)) &&
                            note.title.contains(searchQuery, ignoreCase = true)
                }
            ) { noteWithDetails ->
                val note = noteWithDetails.note

                // Obtiene el primer bloque de texto e imagen si existen (para mostrar algo en la lista)
                val firstText = noteWithDetails.mediaBlocks.firstOrNull { it.type == MediaType.TEXT }?.content ?: ""
                val firstImageUri = noteWithDetails.mediaBlocks.firstOrNull { it.type == MediaType.IMAGE }?.content

                NoteItem(
                    title = note.title,
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


