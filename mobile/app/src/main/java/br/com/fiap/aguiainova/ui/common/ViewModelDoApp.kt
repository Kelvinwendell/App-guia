package br.com.fiap.aguiainova.ui.common

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import br.com.fiap.aguiainova.AguiaInovaApp
import br.com.fiap.aguiainova.core.di.AppContainer

/** Cria (ou recupera) um ViewModel entregando a ele o [AppContainer]; [chave] separa instancias por parametro. */
@Composable
inline fun <reified VM : ViewModel> viewModelDoApp(
    chave: String? = null,
    crossinline criar: (AppContainer) -> VM
): VM {
    val container = (LocalContext.current.applicationContext as AguiaInovaApp).container
    return viewModel(key = chave, factory = viewModelFactory { initializer { criar(container) } })
}

@Composable
fun containerDoApp(): AppContainer =
    (LocalContext.current.applicationContext as AguiaInovaApp).container
