package edu.ucne.proyectotarea.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "Prioridades")
data class PrioridadEntity(
    @PrimaryKey
    val priordadId: Int? = null,
    val descripcion: String = "",
    val diasCompromiso: Int = 0
)
