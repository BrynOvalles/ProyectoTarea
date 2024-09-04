package edu.ucne.proyectotarea

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.room.Room
import edu.ucne.proyectotarea.data.local.database.PrioridadDb
import edu.ucne.proyectotarea.data.local.entities.PrioridadEntity
import edu.ucne.proyectotarea.ui.theme.ProyectoTareaTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private lateinit var prioridadDb: PrioridadDb

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        prioridadDb = Room.databaseBuilder(
            applicationContext,
            PrioridadDb::class.java,
            "prioridadDb"
        ).fallbackToDestructiveMigration()
            .build()

        setContent {
            ProyectoTareaTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        PrioridadScreen()
                    }
                }
            }
        }
    }

    @Composable
    fun PrioridadScreen() {
        var descripcion by remember { mutableStateOf("") }
        var diasCompromiso by remember { mutableStateOf("") }
        var errorMessage: String? by remember { mutableStateOf(null) }

        Scaffold { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(8.dp)
            ) {
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                    ) {
                        OutlinedTextField(
                            label = { Text(text = "Descripción") },
                            value = descripcion,
                            onValueChange = { descripcion = it },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            label = { Text(text = "Días Compromiso") },
                            value = diasCompromiso,
                            onValueChange = { input ->
                                if (input.all { it.isDigit() }) {
                                    diasCompromiso = input
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                        Spacer(modifier = Modifier.padding(2.dp))
                        errorMessage?.let {
                            Text(text = it, color = Color.Red)
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            OutlinedButton(
                                onClick = {
                                    descripcion = ""
                                    diasCompromiso = ""
                                    errorMessage = null
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Nuevo"
                                )
                                Text(text = "Nuevo")
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            val scope = rememberCoroutineScope()
                            OutlinedButton(
                                onClick = {
                                    if (descripcion.isBlank()) {
                                        errorMessage = "Falta la Descripción"
                                    } else if (diasCompromiso.isBlank()) {
                                        errorMessage = "Faltan los Días de Compromiso"
                                    } else {
                                        errorMessage = null
                                        scope.launch {
                                            savePrioridad(
                                                PrioridadEntity(
                                                    descripcion = descripcion,
                                                    diasCompromiso = diasCompromiso.toIntOrNull() ?: 0
                                                )
                                            )
                                            descripcion = ""
                                            diasCompromiso = ""
                                        }
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Guardar"
                                )
                                Text(text = "Guardar")
                            }
                        }
                    }
                }

                val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
                val prioridadList by prioridadDb.prioridadDao().getAll()
                    .collectAsStateWithLifecycle(
                        initialValue = emptyList(),
                        lifecycleOwner = lifecycleOwner,
                        minActiveState = Lifecycle.State.STARTED
                    )
                PrioridadListScreen(prioridadList)
            }
        }
    }

    private suspend fun savePrioridad(prioridad: PrioridadEntity) {
        prioridadDb.prioridadDao().save(prioridad)
    }


    @Composable
    fun PrioridadListScreen(prioridadList: List<PrioridadEntity>) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Spacer(modifier = Modifier.height(32.dp))
            Text("Lista de Prioridades")

            LazyColumn(
                modifier = Modifier.fillMaxSize()
            ) {
                items(prioridadList) {
                    PrioridadRow(it)
                }
            }
        }
    }

    @Composable
    private fun PrioridadRow(it: PrioridadEntity) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Text(modifier = Modifier.weight(1f), text = it.priordadId.toString())
            Text(
                modifier = Modifier.weight(2f),
                text = it.descripcion,
                style = MaterialTheme.typography.bodyLarge
            )
            Text(modifier = Modifier.weight(2f), text = it.diasCompromiso.toString())
        }
        HorizontalDivider()
    }

    // Preview para la lista de prioridades
    @Preview(showBackground = true, showSystemUi = true)
    @Composable
    fun PrioridadListPreview() {
        ProyectoTareaTheme {
            val prioridadList = listOf(
                PrioridadEntity(priordadId = 1, descripcion = "Alta", diasCompromiso = 3),
                PrioridadEntity(priordadId = 2, descripcion = "Media", diasCompromiso = 5)
            )
            PrioridadListScreen(prioridadList)
        }
    }

    @Preview(showBackground = true, showSystemUi = true)
    @Composable
    fun PrioridadScreenPreview() {
        PrioridadScreenPreviewContent()
    }

    @Composable
    fun PrioridadScreenPreviewContent() {
        ProyectoTareaTheme {
            var descripcion by remember { mutableStateOf("Prioridad Ejemplo") }
            var diasCompromiso by remember { mutableStateOf("5") }
            var errorMessage: String? by remember { mutableStateOf(null) }
            val prioridadList = listOf(
                PrioridadEntity(priordadId = 1, descripcion = "Alta", diasCompromiso = 3),
                PrioridadEntity(priordadId = 2, descripcion = "Media", diasCompromiso = 5),
                PrioridadEntity(priordadId = 3, descripcion = "Baja", diasCompromiso = 7)
            )

            Scaffold { innerPadding ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(8.dp)
                ) {
                    ElevatedCard(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp)
                        ) {
                            OutlinedTextField(
                                label = { Text(text = "Descripción") },
                                value = descripcion,
                                onValueChange = { descripcion = it },
                                modifier = Modifier.fillMaxWidth()
                            )
                            OutlinedTextField(
                                label = { Text(text = "Días Compromiso") },
                                value = diasCompromiso,
                                onValueChange = { diasCompromiso = it },
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.padding(2.dp))
                            errorMessage?.let {
                                Text(text = it, color = Color.Red)
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                OutlinedButton(
                                    onClick = {
                                        descripcion = ""
                                        diasCompromiso = ""
                                        errorMessage = null                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = "Nuevo"
                                    )
                                    Text(text = "Nuevo")
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                val scope = rememberCoroutineScope()
                                OutlinedButton(
                                    onClick = {
                                        if (descripcion.isBlank()) {
                                            errorMessage = "Falta la Descripción"
                                        } else if (diasCompromiso.isBlank()) {
                                            errorMessage = "Faltan los Días de Compromiso"
                                        } else {
                                            errorMessage = null
                                            scope.launch {
                                                savePrioridad(
                                                    PrioridadEntity(
                                                        descripcion = descripcion,
                                                        diasCompromiso = diasCompromiso.toIntOrNull() ?: 0
                                                    )
                                                )
                                                descripcion = ""
                                                diasCompromiso = ""
                                            }
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Done,
                                        contentDescription = "Guardar"
                                    )
                                    Text(text = "Guardar")
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    PrioridadListScreen(prioridadList)
                }
            }
        }
    }
}
