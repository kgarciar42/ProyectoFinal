package edu.umg.programacion2.finanzas.datos

import edu.umg.programacion2.finanzas.modelo.Gasto

/**
 * El "contrato" de donde se guardan los gastos patron DAO, como con MySQL.
 * Quien lo usa NO sabe si los datos estan en una lista, en MySQL o en el telefono.
 */
interface GastoRepository {

    /** Guarda el gasto y lo devuelve con su id asignado. */
    suspend fun agregar(gasto: Gasto): Gasto

    /** Devuelve una copia de todos los gastos guardados. */
    suspend fun listar(): List<Gasto>

    /** Devuelve el gasto o null si no existe. */
    suspend fun buscarPorId(id: Int): Gasto?

    /** Reemplaza el gasto con el mismo id. Devuelve false si ese id no existe. */
    suspend fun actualizar(gasto: Gasto): Boolean

    /** Elimina el gasto. Devuelve false si ese id no existe. */
    suspend fun eliminar(id: Int): Boolean
}