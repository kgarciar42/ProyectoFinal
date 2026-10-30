package edu.umg.programacion2.appgastos.datos

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

// Mi base de datos SQLite. Se guarda dentro del teléfono en un archivo "finanzas.db".
// SQLiteOpenHelper la crea la primera vez y después solo la abre.
class BaseDatos(context: Context) : SQLiteOpenHelper(context, NOMBRE, null, VERSION) {

    companion object {
        private const val NOMBRE = "finanzas.db"
        private const val VERSION = 2 // 2 = agregué la tabla de categorías
    }

    // Esto corre UNA sola vez: cuando la base todavía no existe
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE gasto (
                id          INTEGER PRIMARY KEY AUTOINCREMENT,
                descripcion TEXT    NOT NULL,
                monto       REAL    NOT NULL,
                categoria   TEXT    NOT NULL,
                fecha       TEXT    NOT NULL
            )
            """.trimIndent()
        )
        db.execSQL(
            """
            CREATE TABLE ingreso (
                id          INTEGER PRIMARY KEY AUTOINCREMENT,
                descripcion TEXT    NOT NULL,
                monto       REAL    NOT NULL,
                tipo        TEXT    NOT NULL,
                fecha       TEXT    NOT NULL
            )
            """.trimIndent()
        )
        // buscar por mes o por año
        db.execSQL("CREATE INDEX idx_gasto_fecha ON gasto(fecha)")
        db.execSQL("CREATE INDEX idx_ingreso_fecha ON ingreso(fecha)")
        crearTablaCategoria(db)
    }

    // Las categorías que crea el usuario.
    private fun crearTablaCategoria(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS categoria (
                id     INTEGER PRIMARY KEY AUTOINCREMENT,
                nombre TEXT    NOT NULL UNIQUE COLLATE NOCASE,
                icono  TEXT    NOT NULL
            )
            """.trimIndent()
        )
    }

    // ya tenía la base de antes, solo le agrego lo nuevo.
    // Así NO se pierden los gastos e ingresos que ya estaban guardados.
    override fun onUpgrade(db: SQLiteDatabase, versionVieja: Int, versionNueva: Int) {
        if (versionVieja < 2) crearTablaCategoria(db)
    }
}