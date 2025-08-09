package com.example.damonditrichs_weight_tracking_app

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.damonditrichs_weight_tracking_app.ui.theme.DamonDitrichs_Weight_Tracking_AppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Check login and SMS permission states
        val preferences: SharedPreferences = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val isLoggedIn = preferences.getBoolean("is_logged_in", false)
        val isSmsPermissionGranted = preferences.getBoolean("is_sms_permission_granted", false)

        when {
            !isLoggedIn -> {
                // Redirect to login activity
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
                return
            }
            !isSmsPermissionGranted -> {
                // Redirect to SMS permission activity
                startActivity(Intent(this, SmsPermissionActivity::class.java))
                finish()
                return
            }
            else -> {
                // Redirect to data display activity
                startActivity(Intent(this, DataDisplayActivity::class.java))
                finish()
                return
            }
        }

        enableEdgeToEdge()
        setContent {
            DamonDitrichs_Weight_Tracking_AppTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Greeting(
                        name = "Android",
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    DamonDitrichs_Weight_Tracking_AppTheme {
        Greeting("Android")
    }
}
