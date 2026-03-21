package com.inventory.system.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.room.Room
import com.inventory.system.data.local.InventoryDatabase
import com.inventory.system.data.local.TokenDataStore
import com.inventory.system.data.local.dao.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "inventory_prefs")

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): InventoryDatabase {
        return Room.databaseBuilder(context, InventoryDatabase::class.java, "inventory_db")
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides fun provideProductDao(db: InventoryDatabase): ProductDao = db.productDao()
    @Provides fun provideCategoryDao(db: InventoryDatabase): CategoryDao = db.categoryDao()
    @Provides fun provideWarehouseDao(db: InventoryDatabase): WarehouseDao = db.warehouseDao()
    @Provides fun provideUnitDao(db: InventoryDatabase): UnitDao = db.unitDao()

    @Provides
    @Singleton
    fun provideDataStore(@ApplicationContext context: Context): DataStore<Preferences> = context.dataStore

    @Provides
    @Singleton
    fun provideTokenDataStore(dataStore: DataStore<Preferences>) = TokenDataStore(dataStore)
}
