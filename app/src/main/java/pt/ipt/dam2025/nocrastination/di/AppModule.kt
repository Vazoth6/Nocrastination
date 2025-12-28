// di/AppModule.kt
package pt.ipt.dam2025.nocrastination.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import pt.ipt.dam2025.nocrastination.data.mapper.PomodoroMapper
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun providePomodoroMapper(): PomodoroMapper {
        return PomodoroMapper()
    }
}