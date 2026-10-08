package com.edutrack.learningdashboard.data.repository

import com.edutrack.learningdashboard.data.local.CourseDao
import com.edutrack.learningdashboard.data.local.CourseEntity
import com.edutrack.learningdashboard.data.local.LessonEntity
import com.edutrack.learningdashboard.data.remote.CourseApiService
import com.edutrack.learningdashboard.domain.model.Course
import com.edutrack.learningdashboard.domain.model.Lesson
import com.edutrack.learningdashboard.domain.model.User
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
            val entities = remoteCourses.map { CourseEntity.fromDomain(it) }
            
            // Upsert into Room Database
            courseDao.insertCourses(entities)
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
