package edu.umg.programacion2.appgastos.logica

import edu.umg.programacion2.finanzas.modelo.Categoria
import edu.umg.programacion2.finanzas.modelo.Gasto
import edu.umg.programacion2.finanzas.modelo.Ingreso
import edu.umg.programacion2.finanzas.modelo.TipoIngreso
import edu.umg.programacion2.finanzas.servicio.ControlGastos

// Decide que hacer al presionar Guardar en la hoja: registrar, actualizar o cambiar de tipo.
class GuardadoMovimiento(private val control: ControlGastos) {

    // editando = null si es nuevo, si no, el Gasto o Ingreso que estoy editando.
    // seleccion = una Categoria gasto o un TipoIngreso ingreso.
    // Devuelve null si salió bien, o el mensaje de error de la librería.
    suspend fun guardar(editando: Any?, descripcion: String, monto: Double, seleccion: Any, fecha: String): String? {
        val esIngreso = seleccion is TipoIngreso
        return when {
            // Cambió de gasto a ingreso o al revés
            editando is Gasto && esIngreso -> cambiarTipo(editando, descripcion, monto, seleccion, fecha)
            editando is Ingreso && !esIngreso -> cambiarTipo(editando, descripcion, monto, seleccion, fecha)
            // Editar sin cambiar de tipo: copy() deja el mismo id con los datos nuevos
            editando is Gasto -> control.actualizar(
                editando.copy(descripcion = descripcion, monto = monto, categoria = seleccion as Categoria, fecha = fecha)
            )
            editando is Ingreso -> control.actualizarIngreso(
                editando.copy(descripcion = descripcion, monto = monto, tipo = seleccion as TipoIngreso, fecha = fecha)
            )
            // Nuevo
            seleccion is TipoIngreso -> control.registrarIngreso(
                Ingreso(descripcion = descripcion, monto = monto, tipo = seleccion, fecha = fecha)
            )
            else -> control.registrar(
                Gasto(descripcion = descripcion, monto = monto, categoria = seleccion as Categoria, fecha = fecha)
            )
        }
    }

    // Primero guardo el nuevo, solo si se guardó bien borro el viejo, así nunca pierdo el movimiento
    private suspend fun cambiarTipo(viejo: Any, descripcion: String, monto: Double, seleccion: Any, fecha: String): String? {
        val error = if (seleccion is TipoIngreso) {
            control.registrarIngreso(Ingreso(descripcion = descripcion, monto = monto, tipo = seleccion, fecha = fecha))
        } else {
            control.registrar(Gasto(descripcion = descripcion, monto = monto, categoria = seleccion as Categoria, fecha = fecha))
        }
        if (error != null) return error
        if (viejo is Gasto) control.eliminar(viejo.id)
        if (viejo is Ingreso) control.eliminarIngreso(viejo.id)
        return null
    }
}