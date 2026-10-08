package com.aristom.intellipaatassignment.data.local

import androidx.room.*
import com.aristom.intellipaatassignment.domain.model.ProgressCalculator
import kotlinx.coroutines.flow.Flow

@Dao
interface CourseDao {
    @Query("SELECT * FROM courses")
    suspend fun getAllCourses(): List<CourseEntity>
    @Query("SELECT * FROM courses ORDER BY id ASC")
    fun observeAllCourses(): Flow<List<CourseEntity>>

    @Query("SELECT * FROM courses WHERE id = :courseId")
    suspend fun getCourseById(courseId: Int): CourseEntity?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertNewCourses(courses: List<CourseEntity>)

    @Query("""
    UPDATE courses
    SET title = :title,
        instructor = :instructor,
        lessons = :lessons
    WHERE id = :courseId
""")
    suspend fun updateCourseMetadata(
        courseId: Int,
        title: String,
        instructor: String,
        lessons: Int
    )

    @Query("SELECT * FROM lessons WHERE courseId = :courseId ORDER BY `order` ASC")
    fun observeLessons(courseId: Int): Flow<List<LessonEntity>>

    @Query("SELECT * FROM lessons WHERE courseId = :courseId ORDER BY `order` ASC")
    suspend fun getLessons(courseId: Int): List<LessonEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLessons(lessons: List<LessonEntity>)

    @Query("UPDATE lessons SET isCompleted = :isCompleted WHERE id = :lessonId AND courseId = :courseId")
    suspend fun updateLessonStatus(courseId: Int, lessonId: String, isCompleted: Boolean)

    @Query("UPDATE courses SET progress = :newProgress WHERE id = :courseId")
    suspend fun updateCourseProgress(courseId: Int, newProgress: Int)

    /**
     * Atomic transaction: Updates a lesson status, calculates new course completion percentage,
     * and updates the course table.
     */
    @Transaction
    suspend fun setLessonCompletedAndRecalculate(courseId: Int, lessonId: String, isCompleted: Boolean): Int {
        updateLessonStatus(courseId, lessonId, isCompleted)
        val lessonEntities = getLessons(courseId)
        val domainLessons = lessonEntities.map { it.toDomain() }
        val newProgress = ProgressCalculator.calculate(domainLessons)
        updateCourseProgress(courseId, newProgress)
        return newProgress
    }

    @Transaction
    suspend fun syncCoursesPreservingProgress(
        courses: List<CourseEntity>
    ) {
        val existingCourses = getAllCourses()
        val existingIds = existingCourses.map { it.id }.toSet()

        courses.forEach { course ->
            if (course.id in existingIds) {
                updateCourseMetadata(
                    course.id,
                    course.title,
                    course.instructor,
                    course.lessons
                )
            } else {
                insertNewCourses(listOf(course))
            }
        }
    }
}
