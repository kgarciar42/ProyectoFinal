package edu.umg.programacion2.appgastos.datos

import android.content.Context
import edu.umg.programacion2.finanzas.servicio.ControlGastos

// Un solo ControlGastos para toda la app, guardando en SQLite.
// Necesita el Context para abrir la base, por eso hay que llamar iniciar() al abrir cada pantalla.
object Datos {

    private var bd: BaseDatos? = null
    private var controlCreado: ControlGastos? = null

    val control: ControlGastos
        get() = controlCreado ?: error("Primero llama a Datos.iniciar(this) en onCreate")

    fun iniciar(context: Context) {
        if (controlCreado != null) return // ya está creado, no lo hago dos veces
        val base = BaseDatos(context.applicationContext)
        bd = base
        controlCreado = ControlGastos(
            GastoRepositorySQLite(base),
            IngresoRepositorySQLite(base),
            CategoriaRepositorySQLite(base) // mis categorías
        )
    }

    // Borra la base completa y la vuelve a crear vacía empezar de cero
    fun borrarTodo(context: Context) {
        bd?.close()
        bd = null
        controlCreado = null
        context.applicationContext.deleteDatabase("finanzas.db")
        iniciar(context)
    }
}