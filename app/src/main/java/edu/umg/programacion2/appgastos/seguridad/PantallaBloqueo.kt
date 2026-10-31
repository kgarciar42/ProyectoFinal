package edu.umg.programacion2.appgastos.seguridad

import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import edu.umg.programacion2.appgastos.R
import edu.umg.programacion2.appgastos.datos.Preferencias

// La pantalla que tapa todo hasta poner el PIN o la huella.
// alBorrarTodo se llama si el usuario escoge Empezar de cero.
class PantallaBloqueo(
    private val activity: AppCompatActivity,
    private val prefs: Preferencias,
    private val alBorrarTodo: () -> Unit
) {

    fun mostrar() {
        val raiz = activity.findViewById<FrameLayout>(R.id.main)
        if (raiz.findViewById<View>(R.id.bloqueo) != null) return // ya está puesta
        val vista = activity.layoutInflater.inflate(R.layout.vista_bloqueo, raiz, false)
        raiz.addView(vista)

        val etPin = vista.findViewById<EditText>(R.id.etPinBloqueo)
        val desbloquear = {
            Sesion.desbloqueado = true
            activity.getSystemService(InputMethodManager::class.java)
                ?.hideSoftInputFromWindow(etPin.windowToken, 0)
            raiz.removeView(vista)
        }

        vista.findViewById<View>(R.id.btnDesbloquear).setOnClickListener {
            if (prefs.pinCorrecto(etPin.text.toString())) {
                desbloquear()
            } else {
                etPin.text.clear()
                etPin.error = "PIN incorrecto"
            }
        }
        configurarHuella(vista, desbloquear)
        vista.findViewById<View>(R.id.btnOlvidePin).setOnClickListener {
            DialogoRecuperarPin(activity, prefs, alEntrar = desbloquear, alBorrarTodo = alBorrarTodo).mostrar()
        }
    }

    // El botón de huella solo sale si está activada y el teléfono la tiene
    private fun configurarHuella(vista: View, desbloquear: () -> Unit) {
        val btnHuella = vista.findViewById<View>(R.id.btnUsarHuella)
        val puedeHuella = prefs.usarHuella && Biometria.huellaDisponible(activity)
        btnHuella.visibility = if (puedeHuella) View.VISIBLE else View.GONE
        btnHuella.setOnClickListener { Biometria.pedirHuella(activity, desbloquear) }
        if (puedeHuella) Biometria.pedirHuella(activity, desbloquear) // la pido de una vez
    }
}