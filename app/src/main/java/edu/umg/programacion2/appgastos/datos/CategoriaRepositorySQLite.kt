package edu.umg.programacion2.appgastos.datos

import android.content.ContentValues
import edu.umg.programacion2.finanzas.datos.CategoriaRepository
import edu.umg.programacion2.finanzas.modelo.Categoria
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

// Las categorías que crea el usuario, guardadas en la tabla "categoria" de SQLite.
// Las de fábrica no se guardan aquí: ya vienen en la librería.
class CategoriaRepositorySQLite(private val bd: BaseDatos) : CategoriaRepository {

    override suspend fun listar(): List<Categoria> = withContext(Dispatchers.IO) {
        val lista = mutableListOf<Categoria>()
        bd.readableDatabase.rawQuery("SELECT nombre, icono FROM categoria ORDER BY nombre", null).use { cursor ->
            while (cursor.moveToNext()) {
                lista.add(Categoria(cursor.getString(0), cursor.getString(1)))
            }
        }
        lista
    }

    // insert devuelve -1 si choca con el UNIQUE (nombre repetido)
    override suspend fun agregar(categoria: Categoria): Boolean = withContext(Dispatchers.IO) {
        val valores = ContentValues().apply {
            put("nombre", categoria.nombre)
            put("icono", categoria.icono)
        }
        bd.writableDatabase.insert("categoria", null, valores) != -1L
    }

    // COLLATE NOCASE para que "mascotas" borre también "Mascotas"
    override suspend fun eliminar(nombre: String): Boolean = withContext(Dispatchers.IO) {
        bd.writableDatabase.delete("categoria", "nombre = ? COLLATE NOCASE", arrayOf(nombre)) > 0
    }
}