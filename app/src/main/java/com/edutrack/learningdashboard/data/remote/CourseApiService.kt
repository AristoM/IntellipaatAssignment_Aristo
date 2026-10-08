package com.edutrack.learningdashboard.data.remote

import com.edutrack.learningdashboard.domain.model.Course
import com.edutrack.learningdashboard.domain.model.Lesson
import com.edutrack.learningdashboard.domain.model.User
import kotlinx.coroutines.delay
import java.io.IOException

interface CourseApiService {
    suspend fun login(email: String, password: String): User
    suspend fun fetchCourses(): List<Course>
    suspend fun fetchLessons(courseId: Int): List<Lesson>
}

class MockCourseApiService(
    var isSimulatedOffline: Boolean = false,
    var shouldSimulateApiFailure: Boolean = false
) : CourseApiService {

    override suspend fun login(email: String, password: String): User {
        delay(600)

        if (isSimulatedOffline) {
            throw IOException("Unable to resolve host 'api.edutrack.com': No address associated with hostname")
        }

        if (shouldSimulateApiFailure) {
            throw IOException("HTTP 500: Authentication service internal error")
        }

        if (password == "wrongpassword") {
            throw IllegalArgumentException("HTTP 401: Invalid email or password")
        }

        return User(
            id = "usr_101",
            email = email,
            name = email.substringBefore("@").replace(".", " ").capitalizeWords(),
            token = "jwt_bearer_token_production_grade"
        )
    }

    override suspend fun fetchCourses(): List<Course> {
        delay(700)

        if (isSimulatedOffline) {
            throw IOException("ConnectException: Failed to connect to api.edutrack.com (Network offline)")
        }

        if (shouldSimulateApiFailure) {
            throw IOException("HTTP 503: Service catalog temporarily unavailable")
        }

        return listOf(
            Course(
                id = 1,
                title = "Python Programming",
                instructor = "John Smith",
                progress = 50,
                lessons = 4
            ),
            Course(
                id = 2,
                title = "Generative AI",
                instructor = "Sarah Williams",
                progress = 40,
                lessons = 5
            ),
            Course(
                id = 3,
                title = "Full Stack Development",
                instructor = "David Brown",
                progress = 25,
                lessons = 4
            ),
            Course(
                id = 4,
                title = "Android Architecture with Compose",
                instructor = "Alex Rivera",
                progress = 75,
                lessons = 4
            )
        )
    }

    override suspend fun fetchLessons(courseId: Int): List<Lesson> {
        delay(400)
        return when (courseId) {
            1 -> listOf(
                Lesson("1-1", 1, "Introduction", true, 1),
                Lesson("1-2", 1, "Variables & Data Types", true, 2),
                Lesson("1-3", 1, "Functions", false, 3),
                Lesson("1-4", 1, "OOP", false, 4)
            )
            2 -> listOf(
                Lesson("2-1", 2, "Foundations of Neural Networks", true, 1),
                Lesson("2-2", 2, "Transformer Self-Attention", true, 2),
                Lesson("2-3", 2, "Prompt Engineering & Few-Shot", false, 3),
                Lesson("2-4", 2, "Retrieval-Augmented Generation (RAG)", false, 4),
                Lesson("2-5", 2, "Multimodal Models & Agents", false, 5)
            )
            3 -> listOf(
                Lesson("3-1", 3, "Client-Server Architecture & HTTP", true, 1),
                Lesson("3-2", 3, "State Management in Modern UI", false, 2),
                Lesson("3-3", 3, "RESTful API Design & Auth", false, 3),
                Lesson("3-4", 3, "Database Indexing & Persistence", false, 4)
            )
            else -> listOf(
                Lesson("4-1", 4, "Declarative UI with Jetpack Compose", true, 1),
                Lesson("4-2", 4, "Unidirectional Data Flow & StateFlow", true, 2),
                Lesson("4-3", 4, "Room Database & Offline-First Single Source of Truth", true, 3),
                Lesson("4-4", 4, "Unit Testing ViewModels & Repositories", false, 4)
            )
        }
    }

    private fun String.capitalizeWords(): String = split(" ")
        .joinToString(" ") { it.replaceFirstChar { char -> char.uppercase() } }
}
