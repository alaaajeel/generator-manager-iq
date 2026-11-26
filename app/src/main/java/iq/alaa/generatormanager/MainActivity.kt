package iq.alaa.generatormanager

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import iq.alaa.generatormanager.ui.AppNav
import iq.alaa.generatormanager.ui.theme.GeneratorTheme

class MainActivity : ComponentActivity() {
    private val vm: AppViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            GeneratorTheme(dark = vm.darkMode) {
                AppNav(vm)
            }
        }
    }
}
