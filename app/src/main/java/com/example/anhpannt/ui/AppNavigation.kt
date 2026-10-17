package com.example.anhpannt.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.anhpannt.ui.screens.AboutScreen
import com.example.anhpannt.ui.screens.NoteEditScreen
import com.example.anhpannt.ui.screens.NoteListScreen
import com.example.anhpannt.ui.screens.OnboardingScreen
import com.example.anhpannt.ui.screens.SettingsScreen

sealed class Screen(val route: String) {
    data object Onboarding : Screen("onboarding")
    data object NoteList : Screen("note_list")
    data object NoteEdit : Screen("note_edit?noteId={noteId}") {
        fun createRoute(noteId: String? = null): String {
            return if (noteId != null) "note_edit?noteId=$noteId" else "note_edit"
        }
    }
    data object Settings : Screen("settings")
    data object About : Screen("about")
}




@Composable
fun AppNavHost(
    navController: NavHostController,
    viewModel: NoteViewModel,
    modifier: Modifier = Modifier
) {
    val isOnboardingCompleted by viewModel.isOnboardingCompleted.collectAsState()

    if (isOnboardingCompleted == null) {
        Box(modifier = modifier.fillMaxSize())
        return
    }

    val startDestination = if (isOnboardingCompleted == true) {
        Screen.NoteList.route
    } else {
        Screen.Onboarding.route
    }

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        composable(route = Screen.Onboarding.route) {
            OnboardingScreen(
                onFinishOnboarding = {
                    viewModel.setOnboardingCompleted(true)
                    navController.navigate(Screen.NoteList.route) {
                        popUpTo(Screen.Onboarding.route) { inclusive = true }
                    }
                }
            )
        }

        composable(route = Screen.NoteList.route) {
            NoteListScreen(
                viewModel = viewModel,
                onNavigateToEdit = { noteId ->
                    navController.navigate(Screen.NoteEdit.createRoute(noteId))
                },
                onNavigateToSettings = {
                    navController.navigate(Screen.Settings.route)
                }
            )
        }

        composable(
            route = Screen.NoteEdit.route,
            arguments = listOf(
                navArgument("noteId") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            val noteId = backStackEntry.arguments?.getString("noteId")
            NoteEditScreen(
                noteId = noteId,
                viewModel = viewModel,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(route = Screen.Settings.route) {
            SettingsScreen(
                viewModel = viewModel,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToAbout = {
                    navController.navigate(Screen.About.route)
                }
            )
        }

        composable(route = Screen.About.route) {
            AboutScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
