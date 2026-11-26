package iq.alaa.generatormanager

import android.app.Application
import iq.alaa.generatormanager.data.AppDatabase
import iq.alaa.generatormanager.data.Repository

class GeneratorApp : Application() {
    val repository: Repository by lazy { Repository(AppDatabase.get(this)) }
}
