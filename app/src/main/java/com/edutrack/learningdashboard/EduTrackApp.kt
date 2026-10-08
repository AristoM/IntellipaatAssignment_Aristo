package com.edutrack.learningdashboard

import android.app.Application
import com.edutrack.learningdashboard.data.local.AppDatabase
import com.edutrack.learningdashboard.data.remote.MockCourseApiService
import com.edutrack.learningdashboard.data.repository.CourseRepository
import com.edutrack.learningdashboard.data.repository.CourseRepositoryImpl

class EduTrackApp : Application() {

    lateinit var database: AppDatabase
        private set

    lateinit var courseRepository: CourseRepository
        private set

    override fun onCreate() {
        super.onCreate()
        database = AppDatabase.getInstance(this)
        val apiService = MockCourseApiService()
        courseRepository = CourseRepositoryImpl(
            courseDao = database.courseDao(),
            apiService = apiService
        )
    }
}
