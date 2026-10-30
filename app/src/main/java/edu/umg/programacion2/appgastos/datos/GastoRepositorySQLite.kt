package edu.umg.programacion2.appgastos.datos

import android.content.ContentValues
import android.database.Cursor
import edu.umg.programacion2.finanzas.datos.GastoRepository
import edu.umg.programacion2.finanzas.modelo.Categoria
import edu.umg.programacion2.finanzas.modelo.Gasto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


// Guarda en la tabla gasto de SQLite. ControlGastos ni se entera del cambio.
// La pantalla no se congela mientras lee o escribe.
class GastoRepositorySQLite(private val bd: BaseDatos) : GastoRepository {

    override suspend fun agregar(gasto: Gasto): Gasto = withContext(Dispatchers.IO) {
        // INSERT INTO gasto (...) VALUES (...); SQLite me devuelve el id nuevo
        val id = bd.writableDatabase.insert("gasto", null, valores(gasto))
        gasto.copy(id = id.toInt())
    }

    override suspend fun listar(): List<Gasto> = withContext(Dispatchers.IO) {
        val lista = mutableListOf<Gasto>()
        bd.readableDatabase.rawQuery("$CONSULTA ORDER BY g.fecha, g.id", null).use { cursor ->
            while (cursor.moveToNext()) lista.add(leer(cursor))
        }
        lista
    }

    override suspend fun buscarPorId(id: Int): Gasto? = withContext(Dispatchers.IO) {
        bd.readableDatabase.rawQuery("$CONSULTA WHERE g.id = ?", arrayOf(id.toString())).use { cursor ->
            if (cursor.moveToFirst()) leer(cursor) else null
        }
    }

    override suspend fun actualizar(gasto: Gasto): Boolean = withContext(Dispatchers.IO) {
        // UPDATE gasto SET ... WHERE id = ?   (devuelve cuántas filas cambió)
        bd.writableDatabase.update("gasto", valores(gasto), "id = ?", arrayOf(gasto.id.toString())) > 0
    }

    override suspend fun eliminar(id: Int): Boolean = withContext(Dispatchers.IO) {
        // DELETE FROM gasto WHERE id = ?
        bd.writableDatabase.delete("gasto", "id = ?", arrayOf(id.toString())) > 0
    }

    // LEFT JOIN con la tabla categoria para traer el ícono de las categorías personalizadas.
    private val CONSULTA = """
        SELECT g.id, g.descripcion, g.monto, g.categoria, g.fecha, c.icono
        FROM gasto g
        LEFT JOIN categoria c ON c.nombre = g.categoria COLLATE NOCASE
    """.trimIndent()

    // Paso un Gasto a columnas. La categoría la guardo con su nombre, ejemplo: Comida o Mascotas
    private fun valores(gasto: Gasto) = ContentValues().apply {
        put("descripcion", gasto.descripcion)
        put("monto", gasto.monto)
        put("categoria", gasto.categoria.nombre)
        put("fecha", gasto.fecha)
    }

    // Paso una fila del SELECT a un Gasto en el mismo orden de las columnas
    private fun leer(cursor: Cursor) = Gasto(
        id = cursor.getInt(0),
        descripcion = cursor.getString(1),
        monto = cursor.getDouble(2),
        categoria = categoriaDe(cursor.getString(3), cursor.getString(5)),
        fecha = cursor.getString(4)
    )

    // Si no es personalizadala, la armo con el ícono que vino del JOIN.
    private fun categoriaDe(nombre: String, icono: String?): Categoria =
        Categoria.predeterminada(nombre) ?: Categoria(nombre, icono ?: "🏷️")
}