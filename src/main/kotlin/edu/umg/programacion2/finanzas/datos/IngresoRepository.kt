package edu.umg.programacion2.finanzas.datos

import edu.umg.programacion2.finanzas.modelo.Ingreso

/** Contrato de donde se guardan los ingresos. Mismo patron que GastoRepository. */
interface IngresoRepository {

    suspend fun agregar(ingreso: Ingreso): Ingreso

    suspend fun listar(): List<Ingreso>

    suspend fun buscarPorId(id: Int): Ingreso?

    suspend fun actualizar(ingreso: Ingreso): Boolean

    suspend fun eliminar(id: Int): Boolean
}
