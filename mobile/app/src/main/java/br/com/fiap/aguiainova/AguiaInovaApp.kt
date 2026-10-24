package br.com.fiap.aguiainova

import android.app.Application
import br.com.fiap.aguiainova.core.di.AppContainer

class AguiaInovaApp : Application() {

    // "by lazy": construido no primeiro acesso (MainActivity.onCreate, praticamente imediato no app real),
    // nao em Application.onCreate(). Isso evita que todo teste Robolectric da suite monte a sessao
    // criptografada de verdade (AndroidKeyStore nao existe no Robolectric) so por existir - testes que nunca
    // tocam o container continuam sem precisar dele.
    val container: AppContainer by lazy { AppContainer(this).also { it.aquecerServidor() } }
}
