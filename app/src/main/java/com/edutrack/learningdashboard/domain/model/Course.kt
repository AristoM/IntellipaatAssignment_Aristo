package com.edutrack.learningdashboard.domain.model

data class Course(
    val id: Int,
    val title: String,
    val instructor: String,
    val progress: Int,
    val lessons: Int
)

data class Lesson(
    val id: String,
    val courseId: Int,
    val title: String,
    val isCompleted: Boolean,
    val order: Int
)

data class User(
    val id: String,
    val email: String,
    val name: String,
    val token: String
)

object ProgressCalculator {
    /**
     * Calculates the course completion percentage accurately.
     * Edge cases:
     * - Empty lesson list -> returns 0
     * - All completed -> returns 100
     * - Partial completed -> Math.round((completed / total) * 100)
     */
    fun calculate(lessons: List<Lesson>): Int {
        if (lessons.isEmpty()) return 0
        val completed = lessons.count { it.isCompleted }
        return Math.round((completed.toFloat() / lessons.size.toFloat()) * 100f)
    }
}
