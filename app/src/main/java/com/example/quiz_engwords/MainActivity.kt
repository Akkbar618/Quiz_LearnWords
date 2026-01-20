package com.example.quiz_engwords

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.example.quiz_engwords.di.RepositoryModule
import com.example.quiz_engwords.navigation.AppNavigation
import com.example.quiz_engwords.ui.theme.QuizEngWordsTheme

/**
 * MainActivity на Jetpack Compose с навигацией.
 * 
 * Теперь включает множество экранов: Home, Quiz, Dictionary, Settings.
 */
class MainActivity : ComponentActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        setContent {
            QuizEngWordsTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    val repository = RepositoryModule.provideWordRepository(this)
                    
                    AppNavigation(
                        navController = navController,
                        repository = repository,
                        onFinish = { finish() }
                    )
                }
            }
        }
    }
}

