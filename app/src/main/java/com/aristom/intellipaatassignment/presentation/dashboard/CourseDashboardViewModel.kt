package com.aristom.intellipaatassignment.presentation.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aristom.intellipaatassignment.data.repository.CourseRepository
import com.aristom.intellipaatassignment.domain.model.Course
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

sealed interface DashboardUiState {
    object Loading : DashboardUiState
    data class Success(val courses: List<Course>, val isOfflineCache: Boolean = false) : DashboardUiState
    object Empty : DashboardUiState
    data class Error(val message: String) : DashboardUiState
}

class CourseDashboardViewModel(
    private val repository: CourseRepository
) : ViewModel() {

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val _networkError = MutableStateFlow<String?>(null)
    val networkError: StateFlow<String?> = _networkError.asStateFlow()

    // Combining Room Flow (SSOT) with refreshing and error states
    val uiState: StateFlow<DashboardUiState> = repository.coursesFlow
        .combine(_networkError) { courses, error ->
            when {
                courses.isNotEmpty() -> DashboardUiState.Success(courses, isOfflineCache = error != null)
                error != null -> DashboardUiState.Error(error)
                else -> DashboardUiState.Loading
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = DashboardUiState.Loading
        )

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _isRefreshing.value = true
            _networkError.value = null
            val result = repository.refreshCourses()
            result.onFailure { error ->
                _networkError.value = error.localizedMessage ?: "Failed to refresh courses"
            }
            _isRefreshing.value = false
        }
    }
}
