package com.inventory.system.data.repository

import com.inventory.system.data.remote.InventoryApiService
import com.inventory.system.data.remote.dto.CreateUserRequest
import com.inventory.system.data.remote.dto.UpdateUserRequest
import com.inventory.system.domain.model.Result
import com.inventory.system.domain.model.User
import com.inventory.system.domain.repository.UserRepository
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    private val api: InventoryApiService
) : UserRepository {

    private suspend fun <T> safeApiCall(apiCall: suspend () -> T): Result<T> {
        return try {
            Result.Success(apiCall())
        } catch (e: Exception) {
            Result.Error(e.message ?: "Unknown error")
        }
    }


    override suspend fun getUsers(): Result<List<User>> = safeApiCall {
        api.getUsers().map { it.toDomain() }
    }

    override suspend fun createUser(username: String, email: String, password: String, role: String): Result<User> = safeApiCall {
        api.createUser(CreateUserRequest(username, email, password, role)).toDomain()
    }

    override suspend fun updateUser(id: Int, role: String?, isActive: Boolean?): Result<User> = safeApiCall {
        api.updateUser(id, UpdateUserRequest(role, isActive)).toDomain()
    }

    override suspend fun deleteUser(id: Int): Result<Unit> = safeApiCall {
        api.deleteUser(id)
    }
}
