package io.stepuplabs.pvba

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val payViaBankAppResolver = SpaydPayViaBankAppResolver(application, getString(R.string.file_provider_authority))
        setContent {
            Screen(payViaBankAppResolver)
        }
    }
}