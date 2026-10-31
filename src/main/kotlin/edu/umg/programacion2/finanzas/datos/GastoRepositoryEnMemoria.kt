package edu.umg.programacion2.finanzas.datos

import edu.umg.programacion2.finanzas.modelo.Gasto

/**
 * Guarda los gastos en una lista.
 * Cuidado: al cerrar el programa los datos se pierden (despues se cambia por Room).
 */
class GastoRepositoryEnMemoria : GastoRepository {

    // 1. Encapsulamiento: la lista es private; nadie de afuera la modifica directo.
    private val gastos = mutableListOf<Gasto>()

    // 2. Contador para generar ids, como el AUTO_INCREMENT de MySQL.
    private var siguienteId = 1

    override suspend fun agregar(gasto: Gasto): Gasto {
        // 3. copy() crea un Gasto igual pero con el id asignado.
        val guardado = gasto.copy(id = siguienteId)
        siguienteId++
        gastos.add(guardado)
        return guardado
    }

    // IMPORTANTE: toList() devuelve una COPIA para no romper el encapsulamiento.
    override suspend fun listar(): List<Gasto> = gastos.toList()

    override suspend fun buscarPorId(id: Int): Gasto? {
        for (gasto in gastos) {
            if (gasto.id == id) {
                return gasto
            }
        }
        return null
    }

    override suspend fun actualizar(gasto: Gasto): Boolean {
        val posicion = buscarPosicion(gasto.id)
        if (posicion == -1) {
            return false
        }
        gastos[posicion] = gasto
        return true
    }

    override suspend fun eliminar(id: Int): Boolean {
        val posicion = buscarPosicion(id)
        if (posicion == -1) {
            return false
        }
        gastos.removeAt(posicion)
        return true
    }

    // 4. Devuelve la posicion en la lista o -1 si no esta.
    private fun buscarPosicion(id: Int): Int {
        for (posicion in gastos.indices) {
            if (gastos[posicion].id == id) {
                return posicion
            }
        }
        return -1
    }
}