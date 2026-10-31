package edu.umg.programacion2.finanzas.datos

import edu.umg.programacion2.finanzas.modelo.Categoria

// Categorías personalizadas guardadas en una lista para la consola y para probar
class CategoriaRepositoryEnMemoria : CategoriaRepository {

    private val categorias = mutableListOf<Categoria>()

    override suspend fun listar(): List<Categoria> = categorias.toList()

    override suspend fun agregar(categoria: Categoria): Boolean {
        if (categorias.contains(categoria)) return false // compara por nombre
        categorias.add(categoria)
        return true
    }

    override suspend fun eliminar(nombre: String): Boolean =
        categorias.removeAll { it.nombre.equals(nombre, ignoreCase = true) }
}