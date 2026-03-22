package com.inventory.system.di

import com.inventory.system.data.repository.*
import com.inventory.system.domain.repository.*
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds @Singleton
    abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository

    @Binds @Singleton
    abstract fun bindProductRepository(impl: ProductRepositoryImpl): ProductRepository

    @Binds @Singleton
    abstract fun bindCategoryRepository(impl: CategoryRepositoryImpl): CategoryRepository

    @Binds @Singleton
    abstract fun bindWarehouseRepository(impl: WarehouseRepositoryImpl): WarehouseRepository

    @Binds @Singleton
    abstract fun bindStockRepository(impl: StockRepositoryImpl): StockRepository

    @Binds @Singleton
    abstract fun bindMovementRepository(impl: MovementRepositoryImpl): MovementRepository

    @Binds @Singleton
    abstract fun bindReportRepository(impl: ReportRepositoryImpl): ReportRepository

    @Binds @Singleton
    abstract fun bindUnitRepository(impl: UnitRepositoryImpl): UnitRepository

    @Binds @Singleton
    abstract fun bindUserRepository(impl: UserRepositoryImpl): UserRepository

    @Binds @Singleton
    abstract fun bindExportImportRepository(impl: ExportImportRepositoryImpl): ExportImportRepository
}
