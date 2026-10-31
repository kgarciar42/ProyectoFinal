package edu.umg.programacion2.appgastos.logica

import edu.umg.programacion2.finanzas.modelo.Ingreso

// Regla: un ingreso de Q 5,000 o más queda protegido, o sea que no se puede editar ni borrar.
// No usa vistas ni Android igual que Periodo o ConversionDolares; las pantallas solo le preguntan.
object ReglasMovimientos {

    const val LIMITE_PROTEGIDO = 5000.0

    const val MENSAJE_PROTEGIDO = "Los ingresos de Q 5,000 o más están protegidos: no se pueden modificar ni eliminar"

    // original es el Gasto o Ingreso que tocó el usuario. Solo los ingresos se protegen.
    fun estaProtegido(original: Any): Boolean =
        original is Ingreso && original.monto >= LIMITE_PROTEGIDO
}