package com.edutrack.learningdashboard.data.local

import androidx.room.*
import com.edutrack.learningdashboard.domain.model.Course
import com.edutrack.learningdashboard.domain.model.Lesson

@Entity(tableName = "courses")
data class CourseEntity(
    @PrimaryKey val id: Int,
    val title: String,
    val instructor: String,
    val progress: Int,
    val lessons: Int
) {
    fun toDomain() = Course(
        id = id,
        title = title,
        instructor = instructor,
        progress = progress,
        lessons = lessons
    )

    companion object {
        fun fromDomain(domain: Course) = CourseEntity(
            id = domain.id,
            title = domain.title,
            instructor = domain.instructor,
            progress = domain.progress,
            lessons = domain.lessons
        )
    }
}

@Entity(
    tableName = "lessons",
    foreignKeys = [
        ForeignKey(
            entity = CourseEntity::class,
            parentColumns = ["id"],
            childColumns = ["courseId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["courseId"])]
)
data class LessonEntity(
    @PrimaryKey val id: String,
    val courseId: Int,
    val title: String,
    val isCompleted: Boolean,
    val order: Int
) {
    fun toDomain() = Lesson(
        id = id,
        courseId = courseId,
        title = title,
        isCompleted = isCompleted,
        order = order
    )

    companion object {
        fun fromDomain(domain: Lesson) = LessonEntity(
            id = domain.id,
            courseId = domain.courseId,
            title = domain.title,
            isCompleted = domain.isCompleted,
            order = domain.order
        )
    }
}
