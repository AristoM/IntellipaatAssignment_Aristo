package com.aristom.intellipaatassignment.presentation.navigation

import androidx.compose.runtime.*
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.aristom.intellipaatassignment.data.repository.CourseRepository
import com.aristom.intellipaatassignment.presentation.dashboard.CourseDashboardScreen
import com.aristom.intellipaatassignment.presentation.dashboard.CourseDashboardViewModel
import com.aristom.intellipaatassignment.presentation.details.CourseDetailScreen
import com.aristom.intellipaatassignment.presentation.details.CourseDetailViewModel
import com.aristom.intellipaatassignment.presentation.login.LoginScreen
import com.aristom.intellipaatassignment.presentation.login.LoginViewModel

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Dashboard : Screen("dashboard")
    object CourseDetails : Screen("course_details/{courseId}") {
        fun createRoute(courseId: Int) = "course_details/$courseId"
    }
}

@Composable
fun EduTrackNavHost(
    repository: CourseRepository
) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Screen.Login.route
    ) {
        composable(Screen.Login.route) {
            val viewModel = remember { LoginViewModel(repository) }
            val uiState by viewModel.uiState.collectAsState()

            LoginScreen(
                uiState = uiState,
                onLoginClick = { email, pass -> viewModel.login(email, pass) },
                onLoginSuccess = {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Dashboard.route) {
            val viewModel = remember { CourseDashboardViewModel(repository) }
            val uiState by viewModel.uiState.collectAsState()
            val isRefreshing by viewModel.isRefreshing.collectAsState()

            CourseDashboardScreen(
                uiState = uiState,
                isRefreshing = isRefreshing,
                onRefresh = { viewModel.refresh() },
                onCourseClick = { courseId ->
                    navController.navigate(Screen.CourseDetails.createRoute(courseId))
                }
            )
        }

        composable(
            route = Screen.CourseDetails.route,
            arguments = listOf(navArgument("courseId") { type = NavType.IntType })
        ) { backStackEntry ->
            val courseId = backStackEntry.arguments?.getInt("courseId") ?: 1
            val viewModel = remember(courseId) { CourseDetailViewModel(courseId, repository) }
            val uiState by viewModel.uiState.collectAsState()

            CourseDetailScreen(
                uiState = uiState,
                onToggleLesson = { id, completed -> viewModel.toggleLesson(id, completed) },
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}
