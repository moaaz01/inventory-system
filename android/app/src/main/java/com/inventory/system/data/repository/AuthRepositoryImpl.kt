package com.inventory.system.data.repository

import com.inventory.system.data.local.TokenDataStore
import com.inventory.system.data.remote.InventoryApiService
import com.inventory.system.domain.model.Result
import com.inventory.system.domain.model.User
import com.inventory.system.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val api: InventoryApiService,
    private val tokenDataStore: TokenDataStore
) : AuthRepository {

    override suspend fun login(username: String, password: String): Result<User> = safeApiCall {
        val response = api.login(com.inventory.system.data.remote.dto.LoginRequest(username, password))
        tokenDataStore.saveToken(response.accessToken, username)
        api.getMe().toDomain()
    }

    override suspend fun register(username: String, email: String, password: String): Result<User> = safeApiCall {
        api.register(com.inventory.system.data.remote.dto.RegisterRequest(username, email, password)).toDomain()
    }

    override suspend fun getMe(): Result<User> = safeApiCall {
        api.getMe().toDomain()
    }

    override suspend fun logout() {
        tokenDataStore.clearToken()
    }

    override fun isLoggedIn(): Flow<Boolean> = tokenDataStore.token.map { !it.isNullOrEmpty() }
}

suspend fun <T> safeApiCall(call: suspend () -> T): Result<T> {
    return try {
        Result.Success(call())
    } catch (e: retrofit2.HttpException) {
        val msg = try {
            e.response()?.errorBody()?.string() ?: e.message()
        } catch (_: Exception) { e.message() }
        Result.Error(msg)
    } catch (e: java.io.IOException) {
        Result.Error("خطأ في الاتصال بالشبكة")
    } catch (e: Exception) {
        Result.Error(e.message ?: "خطأ غير معروف")
    }
}
