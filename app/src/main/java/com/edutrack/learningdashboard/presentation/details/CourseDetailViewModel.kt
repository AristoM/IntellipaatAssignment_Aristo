package com.edutrack.learningdashboard.presentation.details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.edutrack.learningdashboard.data.repository.CourseRepository
import com.edutrack.learningdashboard.domain.model.Course
import com.edutrack.learningdashboard.domain.model.Lesson
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class CourseDetailUiState(
    val course: Course? = null,
    val lessons: List<Lesson> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class CourseDetailViewModel(
    private val courseId: Int,
    private val repository: CourseRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CourseDetailUiState(isLoading = true))
    val uiState: StateFlow<CourseDetailUiState> = _uiState.asStateFlow()

    init {
        // Observe lessons from Room database (reactive flow)
        viewModelScope.launch {
            repository.loadLessonsIfEmpty(courseId)
        }

        repository.observeLessons(courseId)
            .onEach { lessonList ->
                _uiState.update { current ->
                    current.copy(
                        lessons = lessonList,
                        isLoading = false
                    )
                }
            }
            .launchIn(viewModelScope)

        // Observe course
        repository.coursesFlow
            .map { list -> list.find { it.id == courseId } }
            .onEach { course ->
                _uiState.update { current ->
                    current.copy(course = course)
                }
            }
            .launchIn(viewModelScope)
    }

    fun toggleLesson(lessonId: String, currentCompleted: Boolean) {
        viewModelScope.launch {
            repository.toggleLessonCompletion(courseId, lessonId, !currentCompleted)
        }
    }
}
