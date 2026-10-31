package edu.umg.programacion2.finanzas.datos

import edu.umg.programacion2.finanzas.modelo.Ingreso

/** Guarda los ingresos en una lista. Misma estructura que GastoRepositoryEnMemoria. */
class IngresoRepositoryEnMemoria : IngresoRepository {

    private val ingresos = mutableListOf<Ingreso>()
    private var siguienteId = 1

    override suspend fun agregar(ingreso: Ingreso): Ingreso {
        val guardado = ingreso.copy(id = siguienteId)
        siguienteId++
        ingresos.add(guardado)
        return guardado
    }

    override suspend fun listar(): List<Ingreso> = ingresos.toList()

    override suspend fun buscarPorId(id: Int): Ingreso? {
        for (ingreso in ingresos) {
            if (ingreso.id == id) {
                return ingreso
            }
        }
        return null
    }

    override suspend fun actualizar(ingreso: Ingreso): Boolean {
        val posicion = buscarPosicion(ingreso.id)
        if (posicion == -1) {
            return false
        }
        ingresos[posicion] = ingreso
        return true
    }

    override suspend fun eliminar(id: Int): Boolean {
        val posicion = buscarPosicion(id)
        if (posicion == -1) {
            return false
        }
        ingresos.removeAt(posicion)
        return true
    }

    private fun buscarPosicion(id: Int): Int {
        for (posicion in ingresos.indices) {
            if (ingresos[posicion].id == id) {
                return posicion
            }
        }
        return -1
    }
}