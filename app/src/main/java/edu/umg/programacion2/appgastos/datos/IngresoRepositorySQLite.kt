package edu.umg.programacion2.appgastos.datos

import android.content.ContentValues
import android.database.Cursor
import edu.umg.programacion2.finanzas.datos.IngresoRepository
import edu.umg.programacion2.finanzas.modelo.Ingreso
import edu.umg.programacion2.finanzas.modelo.TipoIngreso
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

// Igual que GastoRepositorySQLite, pero para la tabla "ingreso"
class IngresoRepositorySQLite(private val bd: BaseDatos) : IngresoRepository {

    override suspend fun agregar(ingreso: Ingreso): Ingreso = withContext(Dispatchers.IO) {
        val id = bd.writableDatabase.insert("ingreso", null, valores(ingreso))
        ingreso.copy(id = id.toInt())
    }

    override suspend fun listar(): List<Ingreso> = withContext(Dispatchers.IO) {
        val lista = mutableListOf<Ingreso>()
        bd.readableDatabase.rawQuery(
            "SELECT id, descripcion, monto, tipo, fecha FROM ingreso ORDER BY fecha, id", null
        ).use { cursor ->
            while (cursor.moveToNext()) lista.add(leer(cursor))
        }
        lista
    }

    override suspend fun buscarPorId(id: Int): Ingreso? = withContext(Dispatchers.IO) {
        bd.readableDatabase.rawQuery(
            "SELECT id, descripcion, monto, tipo, fecha FROM ingreso WHERE id = ?", arrayOf(id.toString())
        ).use { cursor ->
            if (cursor.moveToFirst()) leer(cursor) else null
        }
    }

    override suspend fun actualizar(ingreso: Ingreso): Boolean = withContext(Dispatchers.IO) {
        bd.writableDatabase.update("ingreso", valores(ingreso), "id = ?", arrayOf(ingreso.id.toString())) > 0
    }

    override suspend fun eliminar(id: Int): Boolean = withContext(Dispatchers.IO) {
        bd.writableDatabase.delete("ingreso", "id = ?", arrayOf(id.toString())) > 0
    }

    // Paso un Ingreso a columnas. El tipo lo guardo con su nombre, ejemplo: SALARIO
    private fun valores(ingreso: Ingreso) = ContentValues().apply {
        put("descripcion", ingreso.descripcion)
        put("monto", ingreso.monto)
        put("tipo", ingreso.tipo.name)
        put("fecha", ingreso.fecha)
    }

    // Paso una fila del SELECT a un Ingreso. Si el tipo no existe, cae en OTROS.
    private fun leer(cursor: Cursor) = Ingreso(
        id = cursor.getInt(0),
        descripcion = cursor.getString(1),
        monto = cursor.getDouble(2),
        tipo = TipoIngreso.entries.firstOrNull { it.name == cursor.getString(3) } ?: TipoIngreso.OTROS,
        fecha = cursor.getString(4)
    )
}