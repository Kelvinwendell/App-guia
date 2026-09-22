package br.com.fiap.aguiainova

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import br.com.fiap.aguiainova.navigation.NavGraph
import br.com.fiap.aguiainova.ui.theme.AguiaTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val container = (application as AguiaInovaApp).container
        setContent {
            AguiaTheme {
                NavGraph(container)
            }
        }
    }
}
