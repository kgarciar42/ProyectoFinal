package edu.umg.programacion2.finanzas.modelo

// De dónde me entra el dinero. Es igual que Categoria pero para los ingresos.
// En Android llena el Spinner cuando registro un ingreso.
enum class TipoIngreso(val nombreVisible: String) {
    SALARIO("Salario"),
    VENTAS("Ventas"),
    OTROS("Otros ingresos");

    override fun toString(): String = nombreVisible
}