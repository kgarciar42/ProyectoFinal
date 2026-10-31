package edu.umg.programacion2.finanzas.modelo

// El dinero que me entra como salario, ventas.
// Lo necesito para el balance: lo que entra menos lo que gasto.
data class Ingreso(
    val id: Int = 0,
    val descripcion: String,
    val monto: Double,
    val tipo: TipoIngreso,
    val fecha: String
)
