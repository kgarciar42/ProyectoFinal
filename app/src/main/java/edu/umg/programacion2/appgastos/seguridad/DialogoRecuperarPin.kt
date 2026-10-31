package edu.umg.programacion2.appgastos.seguridad

import android.content.DialogInterface
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import edu.umg.programacion2.appgastos.datos.Preferencias
import edu.umg.programacion2.appgastos.ui.comun.Pantallas

// Olvidaste tu PIN, primero la pregunta secreta y como último recurso, empezar de cero
class DialogoRecuperarPin(
    private val activity: AppCompatActivity,
    private val prefs: Preferencias,
    private val alEntrar: () -> Unit,
    private val alBorrarTodo: () -> Unit
) {

    fun mostrar() {
        if (prefs.tieneRecuperacion) pedirRespuesta() else ofrecerEmpezarDeCero()
    }

    // Si acierta la respuesta entra y se le quita el PIN para que cree uno nuevo
    private fun pedirRespuesta() {
        val (contenedor, campo) = Pantallas.campoParaDialogo(activity, "Tu respuesta")
        val dialogo = AlertDialog.Builder(activity)
            .setTitle(prefs.preguntaRecuperacion)
            .setView(contenedor)
            .setNegativeButton("Cancelar", null)
            .setNeutralButton("Empezar de cero", null)
            .setPositiveButton("Entrar", null)
            .create()
        dialogo.setOnShowListener {
            dialogo.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener {
                if (prefs.respuestaCorrecta(campo.text.toString())) {
                    prefs.quitarPin()
                    dialogo.dismiss()
                    alEntrar()
                    Toast.makeText(activity, "Entraste. Tu PIN se quitó: crea uno nuevo en Ajustes.", Toast.LENGTH_LONG).show()
                } else {
                    campo.error = "Respuesta incorrecta"
                }
            }
            dialogo.getButton(DialogInterface.BUTTON_NEUTRAL).setOnClickListener {
                dialogo.dismiss()
                ofrecerEmpezarDeCero()
            }
        }
        dialogo.show()
    }

    // Último recurso, borra movimientos y ajustes, y entra sin PIN
    private fun ofrecerEmpezarDeCero() {
        AlertDialog.Builder(activity)
            .setTitle("Empezar de cero")
            .setMessage("Se borrarán TODOS tus movimientos y ajustes, y se quitará el PIN. Esto no se puede deshacer.")
            .setNegativeButton("Cancelar", null)
            .setPositiveButton("Borrar todo") { _, _ ->
                alBorrarTodo()
                alEntrar()
                Toast.makeText(activity, "Listo, la app quedó como nueva", Toast.LENGTH_LONG).show()
            }
            .show()
    }
}