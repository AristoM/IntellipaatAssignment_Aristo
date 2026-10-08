package com.aristom.intellipaatassignment

import android.app.Application
import com.aristom.intellipaatassignment.data.local.AppDatabase
import com.aristom.intellipaatassignment.data.remote.MockCourseApiService
import com.aristom.intellipaatassignment.data.repository.CourseRepository
import com.aristom.intellipaatassignment.data.repository.CourseRepositoryImpl

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
