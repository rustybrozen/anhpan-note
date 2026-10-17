package com.example.anhpannt

import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.example.anhpannt.data.AppDatabase
import com.example.anhpannt.data.NoteRepository
import com.example.anhpannt.data.UserPreferences
import com.example.anhpannt.ui.AppNavHost
import com.example.anhpannt.ui.NoteViewModel
import com.example.anhpannt.ui.NoteViewModelFactory
import com.example.anhpannt.ui.theme.AnhpanNoteTheme
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val context = LocalContext.current
            val database = remember { AppDatabase.getDatabase(context) }
            val repository = remember { NoteRepository(database.noteDao()) }
            val userPreferences = remember { UserPreferences(context) }
            val noteViewModel: NoteViewModel = viewModel(
                factory = NoteViewModelFactory(repository, userPreferences)
            )

            val selectedLanguage by noteViewModel.selectedLanguage.collectAsState()

            val configuration = remember(selectedLanguage) {
                Configuration(context.resources.configuration).apply {
                    val locale = Locale.forLanguageTag(selectedLanguage)
                    setLocale(locale)
                    setLayoutDirection(locale)
                }
            }
            context.resources.updateConfiguration(configuration, context.resources.displayMetrics)
            val localizedContext = remember(configuration) {
                context.createConfigurationContext(configuration)
            }

            CompositionLocalProvider(
                LocalConfiguration provides configuration,
                LocalContext provides localizedContext
            ) {
                AnhpanNoteTheme {
                    Surface(modifier = Modifier.fillMaxSize()) {
                        val navController = rememberNavController()
                        AppNavHost(
                            navController = navController,
                            viewModel = noteViewModel
                        )
                    }
                }
            }
        }
    }
}