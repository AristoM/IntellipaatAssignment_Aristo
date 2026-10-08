package com.aristom.intellipaatassignment.data.repository

import com.aristom.intellipaatassignment.data.local.CourseDao
import com.aristom.intellipaatassignment.data.local.CourseEntity
import com.aristom.intellipaatassignment.data.local.LessonEntity
import com.aristom.intellipaatassignment.data.remote.CourseApiService
import com.aristom.intellipaatassignment.domain.model.Course
import com.aristom.intellipaatassignment.domain.model.Lesson
import com.aristom.intellipaatassignment.domain.model.User
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.io.IOException

interface CourseRepository {
    val coursesFlow: Flow<List<Course>>
    suspend fun login(email: String, password: String): Result<User>
    suspend fun refreshCourses(): Result<Unit>
    fun observeLessons(courseId: Int): Flow<List<Lesson>>
    suspend fun loadLessonsIfEmpty(courseId: Int): Result<Unit>
    suspend fun toggleLessonCompletion(courseId: Int, lessonId: String, isCompleted: Boolean): Int
}

class CourseRepositoryImpl(
    private val courseDao: CourseDao,
    private val apiService: CourseApiService
) : CourseRepository {

    // Single Source of Truth: UI always observes Room Database
    override val coursesFlow: Flow<List<Course>> = courseDao.observeAllCourses()
        .map { list -> list.map { it.toDomain() } }

    override suspend fun login(email: String, password: String): Result<User> {
        return try {
            val user = apiService.login(email, password)
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun refreshCourses(): Result<Unit> {
        return try {
            val remoteCourses = apiService.fetchCourses()
            val existingCourses = courseDao.getAllCourses()

            val entities = remoteCourses.map { remoteCourse ->
                val existingCourse = existingCourses.find { it.id == remoteCourse.id }

                CourseEntity.fromDomain(remoteCourse).copy(
                    progress = existingCourse?.progress ?: remoteCourse.progress
                )
            }

            courseDao.syncCoursesPreservingProgress(entities)
            Result.success(Unit)
        } catch (e: IOException) {
            // Offline / Network error: gracefully preserve existing Room cache!
            Result.failure(e)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun observeLessons(courseId: Int): Flow<List<Lesson>> {
        return courseDao.observeLessons(courseId).map { list -> list.map { it.toDomain() } }
    }

    override suspend fun loadLessonsIfEmpty(courseId: Int): Result<Unit> {
        val existing = courseDao.getLessons(courseId)
        if (existing.isNotEmpty()) {
            return Result.success(Unit)
        }
        return try {
            val remoteLessons = apiService.fetchLessons(courseId)
            val entities = remoteLessons.map { LessonEntity.fromDomain(it) }
            courseDao.insertLessons(entities)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun toggleLessonCompletion(
        courseId: Int,
        lessonId: String,
        isCompleted: Boolean
    ): Int {
        return courseDao.setLessonCompletedAndRecalculate(courseId, lessonId, isCompleted)
    }
}
