package edu.umg.programacion2.appgastos.ui.comun

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.provider.Settings
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import edu.umg.programacion2.appgastos.R

// Cosas que se repetía en varias pantallas, ahora en un solo lugar
object Pantallas {

    // Tema oscuro y que nada quede debajo de las barras del teléfono.
    // El layout tiene que tener su vista principal con id "main".
    fun prepararPantallaOscura(activity: ComponentActivity, layout: Int) {
        activity.enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(Color.TRANSPARENT)
        )
        activity.setContentView(layout)
        ViewCompat.setOnApplyWindowInsetsListener(activity.findViewById(R.id.main)) { vista, insets ->
            val barras = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            vista.setPadding(barras.left, barras.top, barras.right, barras.bottom)
            insets
        }
    }

    // Hoja que sube desde abajo
    // Devuelve la hoja y su vista para buscar los campos.
    fun crearHoja(activity: ComponentActivity, layout: Int): Pair<BottomSheetDialog, View> {
        val hoja = BottomSheetDialog(activity)
        val vista = activity.layoutInflater.inflate(layout, null)
        hoja.setContentView(vista)
        (vista.parent as? View)?.setBackgroundColor(Color.TRANSPARENT) // quito el fondo blanco
        hoja.behavior.state = BottomSheetBehavior.STATE_EXPANDED
        hoja.behavior.skipCollapsed = true
        return hoja to vista
    }

    // Un campo de texto con márgenes
    fun campoParaDialogo(context: Context, pista: String): Pair<LinearLayout, EditText> {
        val campo = EditText(context).apply { hint = pista }
        val contenedor = LinearLayout(context).apply {
            val p = dp(context, 20)
            setPadding(p, 0, p, 0)
            addView(campo, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT))
        }
        return contenedor to campo
    }

    // Convierte dp medida de diseño a pixeles del teléfono
    fun dp(context: Context, valor: Number): Int =
        (valor.toFloat() * context.resources.displayMetrics.density).toInt()

    // Abre una pantalla del sistema, si ese teléfono no la tiene, abre los ajustes generales
    fun abrirSeguro(context: Context, intent: Intent) {
        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            context.startActivity(Intent(Settings.ACTION_SETTINGS))
        }
    }
}