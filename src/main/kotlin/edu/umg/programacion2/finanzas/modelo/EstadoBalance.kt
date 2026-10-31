package edu.umg.programacion2.finanzas.modelo

// Cómo voy en el mes. Con esto decido el color en pantalla:
// NEGATIVO = rojo, CUIDADO = naranja, POSITIVO = verde.
// El mensaje ya viene listo para mostrarlo tal cual.
enum class EstadoBalance(val mensaje: String) {
    SIN_MOVIMIENTOS("Mes nuevo: todavía no hay movimientos"),
    POSITIVO("Vamos bien"),
    CUIDADO("Cuidado: ya se gastó el 80 % o más de los ingresos"),
    NEGATIVO("¡Vamos mal! Los gastos superan a los ingresos")
}