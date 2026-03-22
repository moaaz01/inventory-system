package com.inventory.system.util

import android.util.Log
import com.inventory.system.BuildConfig

/**
 * Debug Logger - Debug agent for the inventory system
 * 
 * This utility provides consistent logging across the app and helps
 * trace issues in the data flow between UI, ViewModels, and Repositories.
 * 
 * Usage:
 *   DebugLogger.d("Tag", "Message")  - Debug
 *   DebugLogger.e("Tag", "Error", exception)  - Error
 *   DebugLogger.i("Tag", "Info")  - Info
 *   DebugLogger.w("Tag", "Warning")  - Warning
 * 
 * Enable/Disable logging:
 *   DebugLogger.enabled = true/false
 */
object DebugLogger {
    
    var enabled: Boolean = BuildConfig.DEBUG
    
    private const val TAG_PREFIX = "InventoryApp"
    
    fun d(tag: String, message: String) {
        if (enabled) {
            Log.d("$TAG_PREFIX/$tag", message)
        }
    }
    
    fun e(tag: String, message: String, throwable: Throwable? = null) {
        if (enabled) {
            Log.e("$TAG_PREFIX/$tag", message, throwable)
        }
    }
    
    fun i(tag: String, message: String) {
        if (enabled) {
            Log.i("$TAG_PREFIX/$tag", message)
        }
    }
    
    fun w(tag: String, message: String) {
        if (enabled) {
            Log.w("$TAG_PREFIX/$tag", message)
        }
    }
    
    fun v(tag: String, message: String) {
        if (enabled) {
            Log.v("$TAG_PREFIX/$tag", message)
        }
    }
    
    // Log API calls
    fun logApiCall(tag: String, endpoint: String, method: String = "GET") {
        d(tag, "📡 API Call: $method $endpoint")
    }
    
    // Log API response
    fun logApiResponse(tag: String, endpoint: String, success: Boolean, dataSize: Int = 0) {
        val status = if (success) "✅" else "❌"
        val sizeInfo = if (dataSize > 0) " ($dataSize items)" else ""
        d(tag, "$status API Response: $endpoint$sizeInfo")
    }
    
    // Log state changes
    fun logStateChange(tag: String, stateName: String, oldValue: Any?, newValue: Any?) {
        d(tag, "🔄 State[$stateName]: $oldValue → $newValue")
    }
    
    // Log user actions
    fun logUserAction(tag: String, action: String, params: Map<String, Any> = emptyMap()) {
        val paramsStr = if (params.isNotEmpty()) {
            params.entries.joinToString(", ", "{", "}") { "${it.key}=${it.value}" }
        } else ""
        d(tag, "👤 User Action: $action $paramsStr")
    }
    
    // Log database operations
    fun logDbOperation(tag: String, operation: String, table: String, count: Int = 0) {
        val countStr = if (count > 0) " ($count rows)" else ""
        d(tag, "💾 DB: $operation on $table$countStr")
    }
    
    // Log errors with full stack trace
    fun logError(tag: String, message: String, error: Throwable) {
        e(tag, message, error)
        if (enabled) {
            Log.e("$TAG_PREFIX/$tag", "Stack trace:", error)
        }
    }
    
    // Verification helper - logs code path execution
    fun verifyExecution(tag: String, methodName: String) {
        d(tag, "✅ Executed: $methodName")
    }
    
    // Check interconnection between components
    fun checkConnection(tag: String, from: String, to: String, success: Boolean) {
        val status = if (success) "✅" else "❌"
        d(tag, "$status Connection: $from → $to")
    }
}
