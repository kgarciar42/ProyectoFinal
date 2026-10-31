package edu.umg.programacion2.finanzas.modelo

// Una categoría de gasto, antes era un enum fijo, ahora es una clase normal
// para poder crear las que yo quiera ejemplo: Mascotas, Gimnasio
class Categoria(nombre: String, val icono: String = "🏷️") {

    val nombre: String = nombre.trim()

    // Dos categorías son la misma si se llaman igual sin importar mayúsculas.
    // Ejemplo: así comida y Comida, cuentan como una sola en los totales.
    override fun equals(other: Any?): Boolean =
        other is Categoria && other.nombre.equals(nombre, ignoreCase = true)

    override fun hashCode(): Int = nombre.lowercase().hashCode()


    override fun toString(): String = nombre


    val esPredeterminada: Boolean
        get() = PREDETERMINADAS.contains(this)

    companion object {
        val DESPENSA = Categoria("Despensa", "🛒")
        val COMIDA = Categoria("Comida", "🍔")
        val UNIVERSIDAD = Categoria("Universidad", "🎓")
        val TRANSPORTE = Categoria("Transporte", "🚌")
        val SALUD = Categoria("Salud", "💊")
        val ENTRETENIMIENTO = Categoria("Entretenimiento", "🎮")
        val OTROS = Categoria("Otros", "📦")

        val PREDETERMINADAS = listOf(DESPENSA, COMIDA, UNIVERSIDAD, TRANSPORTE, SALUD, ENTRETENIMIENTO, OTROS)

        // Para que el código que usaba Categoria.entries cuando era enum siga funcionando
        val entries: List<Categoria>
            get() = PREDETERMINADAS

        //  Acepta el nombre viejo del enum COMIDA o el nuevo Comida
        fun predeterminada(texto: String): Categoria? =
            PREDETERMINADAS.firstOrNull { it.nombre.equals(texto.trim(), ignoreCase = true) }
    }
}