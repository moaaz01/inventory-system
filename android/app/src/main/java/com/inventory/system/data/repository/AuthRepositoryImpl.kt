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

    private suspend fun <T> safeApiCall(apiCall: suspend () -> T): Result<T> {
        return try {
            Result.Success(apiCall())
        } catch (e: Exception) {
            Result.Error(e.message ?: "Unknown error")
        }
    }


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
    val lower = raw.lowercase()
    return when {
        // Admin / Auth errors
        lower.contains("admin access required") ->
            "ليس لديك صلاحية لهذا الإجراء. يرجى التواصل مع مدير النظام."
        lower.contains("not authenticated") || lower.contains("invalid or expired token") ->
            "انتهت صلاحية الجلسة. يرجى تسجيل الدخول مرة أخرى."
        lower.contains("invalid credentials") ->
            "اسم المستخدم أو كلمة المرور غير صحيحة"
        lower.contains("rate limit") || lower.contains("too many requests") ->
            "محاولات كثيرة. يرجى الانتظار دقيقة قبل المحاولة."

        // User registration errors
        lower.contains("username already taken") || lower.contains("username_already_taken") ->
            "اسم المستخدم مستخدم بالفعل. اختر اسم مستخدم آخر."
        lower.contains("email already taken") || lower.contains("email_already_taken") ->
            "البريد الإلكتروني مستخدم بالفعل. استخدم بريد إلكتروني آخر."
        lower.contains("password must be at least") || lower.contains("password too short") ->
            "كلمة المرور قصيرة جداً. يجب أن تكون 8 أحرف على الأقل."
        lower.contains("not a valid email") || lower.contains("invalid email") ->
            "البريد الإلكتروني غير صحيح. أدخل بريد إلكتروني صحيح."
        lower.contains("password is incorrect") || lower.contains("current password is incorrect") ->
            "كلمة المرور الحالية غير صحيحة."

        // Product/Entity errors
        lower.contains("already exists") || lower.contains("already_taken") ->
            "هذا العنصر موجود بالفعل في النظام."
        lower.contains("foreign key") || lower.contains("constraint") ->
            "لا يمكن تنفيذ هذا الإجراء. هذا العنصر مرتبط ببيانات أخرى."
        lower.contains("not found") && httpCode == 404 ->
            "المورد المطلوب غير موجود."
        lower.contains("bad request") || httpCode == 400 ->
            "بيانات غير صحيحة. يرجى التحقق من المدخلات."

        // Generic
        else -> raw
    }
}
