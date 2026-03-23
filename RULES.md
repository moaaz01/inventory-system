# قواعد تطوير مشاريع Full-Stack (Android + Backend + Admin)

## ⚠️ القواعد الذهبية — لا تكرر هذه الأخطاء!

### 1. Backend → Android: تطابق الـ DTOs (الأهم!)
**الخطأ:** Android DTO field names لا تتطابق مع backend response
**الأمثلة اللي صارت:**
- `movement_type` vs `type` في MovementDto
- `stock_info` vs `stock` في ProductDto
- `retail_price` / `wholesale_price` / `currency` ناقصة في Android

**الوقاية:** قبل ما تنشئ أي DTO في Android:
1. اقرأ schema الملف في `backend/app/schemas/`
2. اقرأ الـ API endpoint في `backend/app/api/`
3. أي اختلاف → Document it + Fix immediately

### 2. Android: الـ State في ViewModel لازم يُحدَّث
**الخطأ:** `loadData()` تنادي API لكن ما تحدّث `_uiState.value`
```kotlin
// ❌ غلط
fun loadData() {
    viewModelScope.launch {
        repository.getData() // result never assigned to state
    }
}

// ✅ صح
fun loadData() {
    viewModelScope.launch {
        _uiState.update { it.copy(isLoading = true) }
        try {
            val data = repository.getData()
            _uiState.update { it.copy(isLoading = false, data = data) }
        } catch (e) {
            _uiState.update { it.copy(isLoading = false, error = e.message) }
        }
    }
}
```
**الوقاية:** أي دالة `loadXxx()` لازم تنتهي بـ `_uiState.update{}`

### 3. Android: الـ Name Conflict مع kotlin.Unit
**الخطأ:** `Unit` في domain model يتعارض مع `kotlin.Unit`
**الحل:** استخدم alias
```kotlin
typealias InventoryUnit = com.inventory.system.domain.model.Unit
```
**الوقاية:** أي domain model باسم عام (Unit, Result, Error) → استخدم prefix

### 4. Android: cleartext traffic للـ IP المحلي
**الخطأ:** جهاز حقيقي على WiFi لا يستطيع الاتصال بـ HTTP backend
**الحل:** لازم يكون في `network_security_config.xml`:
```xml
<domain-config cleartextTrafficPermitted="true">
    <domain includeSubdomains="true">192.168.1.105</domain>
</domain-config>
```
**الوقاية:** أي مشروع Android يتصل بـ backend → تحقق من cleartext config أولاً

### 5. Backend: الـ Business Logic لازم يكون مكتمل
**الخطأ:** `create_invoice()` أنشأ الفاتورة بدون ما يخصم المخزون
**الحل:** أي عملية تغير حالة → تحقق من side effects:
- Invoice created → خصم stock + سجل movement
- Invoice cancelled → أعد stock + سجل movement
- Stock transferred → خصم من مصدر + أضف لهدف
**الوقاية:** اكتب test case لكل business rule قبل commit

### 6. Android: الـ Navigation routes لازم تكون متناسقة
**الخطأ:** Cashier موجود في BottomNav AND في NavHost كـ redirect → دوخة
**الحل:** كل feature في مكان واحد فقط:
- Cashier button → Dashboard (مفتوح)
- Cashier redirect → احذفه
**الوقاية:** قبل ما تضيف route جديدة → تأكد إنها مش مكررة

### 7. Backend: Admin-only endpoints لازم تتأكد من الصلاحيات
**الخطأ:** `/api/movements` كان يقبل user role للـ receipt/issue/transfer
**الحل:** استخدم `require_admin` decorator لكل endpoint يغيّر بيانات
**الوقاية:** أي endpoint يكتب أو يعدل → تحقق من `deps.py` للـ authorization

### 8. Android: الـ API Base URL للجهاز الحقيقي vs Emulator
| البيئة | Base URL |
|--------|----------|
| Emulator | `http://10.0.2.2:8000` |
| جهاز حقيقي (WiFi) | `http://192.168.1.x:8000` |

**الوقاية:** الـ NetworkModule لازم يدعم الاثنين، القيمة الافتراضية للجهاز الحقيقي

### 9. Android: Translation of API Errors
**الخطأ:** الأخطاء بتطلع بالإنجليزي للمستخدم العربي
**الحل:** لازم يكون `translateApiError()` يحوّل كل error لـ Arabic
```kotlin
fun translateApiError(e: Exception): String {
    return when {
        e.message?.contains("401") == true -> "اسم المستخدم أو كلمة المرور غير صحيحة"
        e.message?.contains("403") == true -> "ليس لديك صلاحية لهذا الإجراء"
        e.message?.contains("404") == true -> "المورد غير موجود"
        else -> "حدث خطأ: ${e.message}"
    }
}
```

### 10. Sub-Agents: السياق لازم يكون كامل
**الخطأ:** sub-agent ما يعرف الـ routes + schemas + BASE_URL
**الحل:** عند استدعاء sub-agent، أرسل دائماً:
```
- مسار المشروع: ~/projects/[slug]/
- BASE_URL: http://192.168.1.x:8000/
- الـ routes اللي موجودة: [list]
- الـ DTOs اللي محتاجة: [list]
- Commits الأخيرة: [git log --oneline -5]
```

---

## 📁 هيكل المشروع المثالي

```
~/projects/[slug]/
├── backend/           # FastAPI
│   ├── app/
│   │   ├── models/     # SQLAlchemy models
│   │   ├── schemas/    # Pydantic schemas (must match models!)
│   │   ├── api/        # API endpoints
│   │   ├── core/       # Config, auth
│   │   └── database.py
│   ├── requirements.txt
│   └── Dockerfile
├── android/           # Kotlin Android
│   ├── app/src/main/java/
│   │   └── com/inventory/[package]/
│   │       ├── data/          # DTOs, API service, Repository impl
│   │       ├── domain/        # Models, Repository interfaces
│   │       ├── presentation/  # Screens, ViewModels
│   │       └── di/            # Hilt modules
│   ├── network_security_config.xml
│   └── build.gradle.kts
├── admin/             # Next.js
│   ├── app/
│   │   └── (dashboard)/
│   └── lib/
│       └── api.ts     # All API calls (must match backend!)
├── docker-compose.yml
└── releases/          # APK + built artifacts
```

## 🔍 فحص ما قبل Commit

قبل كل commit، تأكد:
- [ ] `python3 -c "from app.api.invoices import router; print('OK')"` في backend
- [ ] `./gradlew assembleDebug` ينجح في android
- [ ] `npm run build` ينجح في admin
- [ ] لا TODOs في الكود الجديد
- [ ] كل الـ API endpoints مترابطة بين الـ 3 أجزاء

## 📋 Checklist عند بدء مشروع جديد

- [ ] نسخ هذا الملف إلى المشروع الجديد
- [ ] إعداد port forwarding WSL2 → Windows
- [ ] إعداد network_security_config.xml
- [ ] تحديد BASE_URL للجهاز الحقيقي
- [ ] التحقق من تطابق DTOs بين backend و android و admin
- [ ] إعداد translateApiError في Android
