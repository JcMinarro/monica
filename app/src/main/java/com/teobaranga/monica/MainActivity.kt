package com.teobaranga.monica

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import com.ramcosta.composedestinations.DestinationsNavHost
import com.ramcosta.composedestinations.generated.NavGraphs
import com.teobaranga.monica.ui.datetime.LocalMonthDayFormatter
import com.teobaranga.monica.ui.datetime.getMonthDayFormatter
import com.teobaranga.monica.ui.theme.MonicaTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            MonicaTheme(
                dynamicColor = false,
            ) {
                CompositionLocalProvider(
                    LocalMonthDayFormatter provides getMonthDayFormatter(),
                ) {
                    MonicaBackground {
                        DestinationsNavHost(
                            modifier = Modifier
                                .fillMaxSize(),
                            navGraph = NavGraphs.root,
                        )
                    }
                }
            }
        }
    }
}
