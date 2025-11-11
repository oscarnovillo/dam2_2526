package com.example.navigationhiltroom.data.local.di

import android.content.Context
import androidx.room.Room
import com.example.navigationhiltroom.data.local.AppDatabase
import com.example.navigationhiltroom.data.local.dao.AlumnoSDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "app_database"
        )
            .build()
    }

    @Provides
    fun provideMEhaceisputocasoDao(database: AppDatabase) = database.alumnoDao()


}