package edu.umg.programacion2.finanzas.datos

import edu.umg.programacion2.finanzas.modelo.Categoria

// Donde se guardan las categorías que crea el usuario
// Las de fábrica no se guardan aquí, ya vienen en Categoria.PREDETERMINADAS.
interface CategoriaRepository {

    // Solo las personalizadas
    suspend fun listar(): List<Categoria>

    suspend fun agregar(categoria: Categoria): Boolean

    suspend fun eliminar(nombre: String): Boolean
}