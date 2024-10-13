package meea.licence.greeny

import android.app.Application
import android.content.Context

class MyApp : Application() {

    init {
        instance = this
    }

    companion object {
        private var instance: MyApp? = null

        fun getContext(): Context {
            return instance!!.applicationContext
        }
    }
}
