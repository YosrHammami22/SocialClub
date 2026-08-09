package com.yosrhammami.socialclub.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.yosrhammami.socialclub.ui.navigation.AppNavHost
import com.yosrhammami.socialclub.ui.peopleList.PeopleListScreen
import com.yosrhammami.socialclub.ui.theme.SocialClubTheme
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class MainActivity: ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SocialClubTheme {
                AppNavHost()
            }
        }
    }
}

