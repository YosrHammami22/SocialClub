package com.yosrhammami.socialclub.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import com.yosrhammami.socialclub.core.util.Logger
import com.yosrhammami.socialclub.ui.navigation.AppNavHost
import com.yosrhammami.socialclub.ui.theme.SocialClubTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity: ComponentActivity() {
    @Inject lateinit var logger: Logger
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SocialClubTheme(dynamicColor = false) {
                Surface(color = MaterialTheme.colorScheme.background) {
                    AppNavHost(logger = logger)
                }
            }
        }
    }
}

