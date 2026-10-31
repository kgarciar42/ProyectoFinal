package edu.umg.programacion2.finanzas.modelo

// Un gasto que registro.
// El id empieza en 0 porque todavía no está guardado, el repositorio le pone el id real.
// La fecha la guardo como texto "yyyy-MM-dd" porque así se ordena bien y de ahí saco el mes.
data class Gasto(
    val id: Int = 0,
    val descripcion: String,
    val monto: Double,
    val categoria: Categoria,
    val fecha: String
)