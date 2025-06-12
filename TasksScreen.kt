package com.example.taskmanagerapp

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.material.icons.filled.BrightnessHigh

import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api ::class)
@Composable
fun TasksScreen (viewModel : TasksViewModel = TasksViewModel( LocalContext.current)) {
    val tasks by viewModel .tasks.collectAsState ()
    val progress by viewModel .progress .collectAsState ()
    val isDarkTheme by viewModel .isDarkTheme .collectAsState ()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val scope = rememberCoroutineScope ()
    MaterialTheme (
        colorScheme = if (isDarkTheme ) darkColorScheme() else lightColorScheme()
    ) {
        Scaffold (
            snackbarHost = { SnackbarHost (snackbarHostState ) },
            topBar = {
                TopAppBar (
                    title = { Text("Gerenciador de Tarefas" ) },
                    actions = {
                        IconButton (onClick = { viewModel .toggleTheme( context) }) {
                            Icon(Icons.Default.BrightnessHigh, contentDescription = "Alternar Tema" )
                        }
                    },
                    colors = TopAppBarDefaults.smallTopAppBarColors(
                        containerColor = Color(0xFF6200EE)
                    )                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    . padding(paddingValues )
                    . padding(16.dp)
            ) {
// Progress Indicator
                Text("Progresso das Tarefas" )
                LinearProgressIndicator (
                    progress = progress ,
                    modifier = Modifier
                        .fillMaxWidth()
                        . height(8.dp)
                        . padding(bottom = 8.dp)
                )
                // Lista de tarefas
                LazyColumn (modifier = Modifier .weight(1f)) {
                    items(tasks) { task ->
                        TaskItem (
                            task = task,

                            onToggleCompletion = { viewModel .toggleTaskCompletion( task) },

                            onDelete = {
                                viewModel .removeTask( task)

                                scope.launch {

                                    val result = snackbarHostState .showSnackbar(
                                        message = "Tarefa removida" ,
                                        actionLabel = "Desfazer" ,
                                        duration = SnackbarDuration .Short

                                    )

                                    if (result == SnackbarResult .ActionPerformed) {

                                        viewModel .undoDelete()
                                    }
                                }
                            }
                        )
                    }
                }
// Adicionar nova tarefa
                AddTaskSection(onAddTask = { name, category, priority ->
                    viewModel.addTask(Task(name, false, category, priority))
                })
            }
        }
    }
}
@Composable
fun TaskItem (task: Task, onToggleCompletion : () -> Unit, onDelete : () -> Unit) {
    val scale by animateFloatAsState (if (task.isCompleted ) 1.05f else 1f)
    val backgroundColor = when (task.priority ) {
        TaskPriority .BAIXA -> Color(0xFFC8E6C9 )
        TaskPriority .MEDIA -> Color(0xFFFFF59D )
        TaskPriority .ALTA -> Color(0xFFFFCDD2 )
    }
    AnimatedVisibility (visible = true) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                . padding(vertical = 4.dp)
                . background(backgroundColor , RoundedCornerShape(8.dp))
                . pointerInput(Unit) {
                    detectHorizontalDragGestures { _, _ ->
                        onDelete ()
                    }
                }
                .padding(16.dp),
            verticalAlignment = Alignment .CenterVertically
        ) {
            Checkbox (checked = task.isCompleted , onCheckedChange = { onToggleCompletion () })
            Text(
                text = task.name,
                modifier = Modifier .weight(1f),
                color = if (task.isCompleted ) Color .Gray else Color .Black
            )
        }
    }
}
@Composable
fun AddTaskSection(onAddTask: (String, TaskCategory, TaskPriority) -> Unit) {
    var taskName by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(TaskCategory.CASA) }
    var selectedPriority by remember { mutableStateOf(TaskPriority.MEDIA) }
    Column {
        OutlinedTextField(
            value = taskName,
            onValueChange = { taskName = it },
            label = { Text("Nova tarefa") },
            modifier = Modifier.fillMaxWidth()
        )
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            DropdownMenuBox("Categoria", TaskCategory.values().map { it.name }) {
                selectedCategory = TaskCategory.valueOf(it)
            }
            DropdownMenuBox("Prioridade", TaskPriority.values().map { it.name }) {
                selectedPriority = TaskPriority.valueOf(it)
            }
        }
        Button(
            onClick = {
                if (taskName.isNotBlank()) {
                    onAddTask(taskName, selectedCategory, selectedPriority)
                    taskName = ""
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
        ) {
            Text("Adicionar Tarefa")
        }
    }
}
@Composable
fun DropdownMenuBox(label: String, options: List<String>, onSelection: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    var selectedOption by remember { mutableStateOf(options.first()) }
    Box(modifier = Modifier.padding(4.dp)) {
        OutlinedButton(onClick = { expanded = true }) {
            Text("$label: $selectedOption")
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        selectedOption = option
                        onSelection(option)
                        expanded = false
                    }
                )
            }
        }
    }
}
