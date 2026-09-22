package br.com.fiap.aguiainova.ui.common

import androidx.compose.runtime.Composable
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect

/**
 * Executa [acao] sempre que a tela volta a ficar ativa (na abertura e ao retornar de outra tela), para que listas e
 * indicadores reflitam o que foi criado, editado ou aprovado no meio do caminho.
 */
@Composable
fun AoRetomar(acao: () -> Unit) {
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { acao() }
}
