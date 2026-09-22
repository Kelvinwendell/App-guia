package br.com.fiap.aguiainova

import android.app.Application
import br.com.fiap.aguiainova.core.di.AppContainer

class AguiaInovaApp : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
