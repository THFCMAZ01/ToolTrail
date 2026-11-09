package com.zict.tooltrail

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.zict.tooltrail.ui.ToolTrailApp
import com.zict.tooltrail.ui.theme.ToolTrailTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ToolTrailTheme {
                ToolTrailApp()
            }
        }
    }
}
