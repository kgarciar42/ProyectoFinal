package edu.umg.programacion2.appgastos.ui.ajustes

import android.content.DialogInterface
import android.text.InputType
import android.view.Gravity
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import edu.umg.programacion2.appgastos.datos.Preferencias
import edu.umg.programacion2.appgastos.seguridad.Sesion
import edu.umg.programacion2.appgastos.ui.comun.Pantallas

// crear el PIN: lo pide dos veces para no equivocarse
// y una pregunta secreta para recuperarlo si se olvida.
class DialogoCrearPin(
    private val activity: AppCompatActivity,
    private val prefs: Preferencias,
    private val alGuardar: () -> Unit,
    private val alCancelar: () -> Unit
) {

    private val etPin = campoPin("PIN de 4 a 6 números")
    private val etRepetir = campoPin("Repite el PIN")
    private val etPregunta = campoTexto("Pregunta secreta (ej. Nombre de mi mascota)", conMayuscula = true)
    private val etRespuesta = campoTexto("Respuesta", conMayuscula = false)

    fun mostrar() {
        var guardado = false
        val dialogo = AlertDialog.Builder(activity)
            .setTitle("Crear PIN")
            .setView(armarFormulario())
            .setNegativeButton("Cancelar", null)
            .setPositiveButton("Guardar", null) // el clic lo manejo abajo para validar
            .create()
        dialogo.setOnShowListener {
            dialogo.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener {
                if (validar()) {
                    prefs.guardarPin(etPin.text.toString())
                    prefs.guardarRecuperacion(etPregunta.text.toString(), etRespuesta.text.toString())
                    Sesion.desbloqueado = true // ya lo puse yo, que no me lo pida ahorita
                    guardado = true
                    alGuardar()
                    Toast.makeText(activity, "PIN activado", Toast.LENGTH_SHORT).show()
                    dialogo.dismiss()
                }
            }
        }
        dialogo.setOnDismissListener { if (!guardado) alCancelar() }
        dialogo.show()
    }

    private fun validar(): Boolean {
        val pin = etPin.text.toString()
        when {
            pin.length !in 4..6 -> etPin.error = "Debe tener de 4 a 6 números"
            pin != etRepetir.text.toString() -> etRepetir.error = "No coincide"
            etPregunta.text.toString().trim().length < 3 -> etPregunta.error = "Escribe una pregunta"
            etRespuesta.text.toString().trim().length < 2 -> etRespuesta.error = "Escribe una respuesta"
            else -> return true
        }
        return false
    }

    private fun armarFormulario() = LinearLayout(activity).apply {
        orientation = LinearLayout.VERTICAL
        val p = Pantallas.dp(activity, 20)
        setPadding(p, Pantallas.dp(activity, 8), p, 0)
        addView(etPin)
        addView(etRepetir)
        addView(TextView(activity).apply {
            text = "Por si olvidas tu PIN:"
            setPadding(0, Pantallas.dp(activity, 14), 0, 0)
        })
        addView(etPregunta)
        addView(etRespuesta)
    }

    private fun campoPin(pista: String) = EditText(activity).apply {
        hint = pista
        inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_VARIATION_PASSWORD
        gravity = Gravity.CENTER
    }

    private fun campoTexto(pista: String, conMayuscula: Boolean) = EditText(activity).apply {
        hint = pista
        inputType = if (conMayuscula) InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_CAP_SENTENCES
        else InputType.TYPE_CLASS_TEXT
    }
}