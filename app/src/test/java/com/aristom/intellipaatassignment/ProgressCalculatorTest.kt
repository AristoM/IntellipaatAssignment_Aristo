package com.aristom.intellipaatassignment

import com.aristom.intellipaatassignment.domain.model.Lesson
import com.aristom.intellipaatassignment.domain.model.ProgressCalculator
import org.junit.Assert.assertEquals
import org.junit.Test

class ProgressCalculatorTest {

    @Test
    fun calculateReturns50PercentWhenHalfLessonsAreCompleted() {
        // Given
        val lessons = listOf(
            Lesson(
                id = "1",
                courseId = 1,
                title = "Lesson 1",
                isCompleted = true,
                order = 1
            ),
            Lesson(
                id = "2",
                courseId = 1,
                title = "Lesson 2",
                isCompleted = false,
                order = 2
            ),
            Lesson(
                id = "3",
                courseId = 1,
                title = "Lesson 3",
                isCompleted = true,
                order = 3
            ),
            Lesson(
                id = "4",
                courseId = 1,
                title = "Lesson 4",
                isCompleted = false,
                order = 4
            )
        )

        // When
        val progress = ProgressCalculator.calculate(lessons)

        // Then
        assertEquals(50, progress)
    }
}