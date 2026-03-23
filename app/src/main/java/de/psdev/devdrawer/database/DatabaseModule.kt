package de.psdev.devdrawer.database

import android.app.Application
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class DatabaseModule {

    @Provides
    @Singleton
    fun database(application: Application): DevDrawerDatabase = Room.databaseBuilder(
        application,
        DevDrawerDatabase::class.java,
        DevDrawerDatabase.NAME
    ).apply {
        addMigrations(MigrationFrom1To2(application))
        addMigrations(MigrationFrom2To3)
    }.build()

}