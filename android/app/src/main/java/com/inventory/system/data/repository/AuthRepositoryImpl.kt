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
        val rawMsg = try {
            e.response()?.errorBody()?.string() ?: e.message()
        } catch (_: Exception) { e.message() }
        val friendlyMsg = translateApiError(rawMsg, e.code())
        Result.Error(friendlyMsg)
    } catch (e: java.io.IOException) {
        Result.Error("خطأ في الاتصال بالشبكة")
    } catch (e: Exception) {
        Result.Error(e.message ?: "خطأ غير معروف")
    }
}

private fun translateApiError(raw: String?, httpCode: Int): String {
    if (raw == null) return "حدث خطأ غير متوقع"
    return when {
        raw.contains("Admin access required", ignoreCase = true) ->
            "ليس لديك صلاحية_manager_access لهذا الإجراء.\nيرجى التواصل مع مدير النظام."
        raw.contains("Not authenticated", ignoreCase = true) ||
        raw.contains("Invalid or expired token", ignoreCase = true) ->
            "انتهت صلاحية الجلسة. يرجى تسجيل الدخول مرة أخرى."
        raw.contains("Not Found", ignoreCase = true) && httpCode == 404 ->
            "المورد المطلوب غير موجود"
        raw.contains("Bad Request", ignoreCase = true) || httpCode == 400 ->
            "بيانات غير صحيحة. يرجى التحقق من المدخلات."
        raw.contains("Rate limit", ignoreCase = true) ->
            "محاولات كثيرة. يرجى الانتظار دقيقة قبل المحاولة."
        raw.contains("duplicate", ignoreCase = true) ||
        raw.contains("already exists", ignoreCase = true) ->
            "هذا العنصر موجود بالفعل"
        raw.contains("foreign key", ignoreCase = true) ||
        raw.contains("constraint", ignoreCase = true) ->
            "لا يمكن تنفيذ هذا الإجراء بسبب ارتباطه ببيانات أخرى"
        else -> raw
    }
}
