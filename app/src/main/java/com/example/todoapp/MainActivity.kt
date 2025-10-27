package com.example.todoapp

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.todoapp.*
import com.example.todoapp.viewmodel.NoteViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApp()

        }
    }
}

@Composable
fun MyApp() {
    val navController = rememberNavController()
    val context = LocalContext.current.applicationContext as TodoApplication
    val viewModel = remember { NoteViewModel(context.repository)}
    NavHost(navController = navController, startDestination = "main") {

        // Pantalla principal
        composable("main") {
            MainScreen(navController)
        }

        // Pantalla para agregar una nota
        composable("add") {
            AddNote(navController)
        }

        composable(
            route = "detail/{id}/{title}/{description}/{imageUri}"
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getString("id")?.toIntOrNull()
            val title = Uri.decode(backStackEntry.arguments?.getString("title") ?: "")
            val description = Uri.decode(backStackEntry.arguments?.getString("description") ?: "")

            val imageUriEncoded = backStackEntry.arguments?.getString("imageUri") ?: "null"
            val imageUri = if (imageUriEncoded == "null") null else Uri.decode(imageUriEncoded)
            NoteDetailScreen(
                navController = navController,
                viewModel = viewModel,
                noteId = id,
                initialTitle = title,
                initialDescription = description,
                imageUri = imageUri
            )
        }
    }
}


@Composable
fun MainScreen(navController: NavController) {
    val context = LocalContext.current.applicationContext as TodoApplication
    val viewModel = remember { NoteViewModel(context.repository) }

    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("All") }

    val notes by viewModel.getAllNotes().collectAsState(initial = emptyList())

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text(text = "Buscar") },
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Button(onClick = { navController.navigate("add") }) {
                Text("Agregar")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Row {
            listOf("All", "Notes", "Tasks").forEach { option ->
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
        LazyColumn {
            items(
                notes.filter { note ->
                    (selectedFilter == "All" ||
                            (selectedFilter == "Notes" && !note.isTask) ||
                            (selectedFilter == "Tasks" && note.isTask)) &&
                            note.title.contains(searchQuery, ignoreCase = true)
                }
            ) { note -> NoteItem(
                title = note.title,
                description = note.description,
                imageUri = note.imageUri,
                isTask = note.isTask,
                dueDateTimestamp = note.dueDateTimestamp,
                onClick = {
                    val id = note.id
                    val titleEncoded = Uri.encode(note.title)
                    val descEncoded = Uri.encode(note.description)
                    val imgEncoded = note.imageUri?.let { Uri.encode(it) } ?: "null"
                    navController.navigate("detail/$id/$titleEncoded/$descEncoded/$imgEncoded")
                }
            )
            }
        }
    }
}


@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

