package pt.ipt.dam2025.nocrastination.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import pt.ipt.dam2025.nocrastination.data.repositories.AuthRepositoryImpl
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Singleton
    @Provides
    fun provideAuthRepository(@ApplicationContext context: Context): AuthRepositoryImpl {
        return AuthRepositoryImpl(context)
    }
}