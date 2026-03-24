# 📦 Inventory Management System

> نظام إدارة مخزون متكامل — Full Stack Application

![FastAPI](https://img.shields.io/badge/Backend-FastAPI-009688?style=flat-square&logo=fastapi)
![Kotlin](https://img.shields.io/badge/Android-Kotlin-7F52FF?style=flat-square&logo=kotlin)
![Next.js](https://img.shields.io/badge/Admin-Next.js-000000?style=flat-square&logo=next.js)
![PostgreSQL](https://img.shields.io/badge/Database-PostgreSQL-4169E1?style=flat-square&logo=postgresql)
![Docker](https://img.shields.io/badge/Docker-Ready-2496ED?style=flat-square&logo=docker)
![License](https://img.shields.io/badge/License-MIT-green?style=flat-square)

---

## 📋 جدول المحتويات

- [نظرة عامة](#-نظرة-عامة)
- [المميزات](#-المميزات)
- [هيكل المشروع](#-هيكل-المشروع)
- [المتطلبات](#-المتطلبات)
- [التثبيت والإعداد](#-التثبيت-والإعداد)
  - [الخيار الأول: Docker (الأسرع)](#1-الخيار-الأول-docker-الأسرع)
  - [الخيار الثاني: تشغيل يدوي](#2-الخيار-الثاني-تشغيل-يدوي)
- [الإعداد على السيرفر (Ubuntu)](#-الإعداد-على-السيرفر-ubuntu)
- [التشغيل على Windows + WSL2](#-التشغيل-على-windows--wsl2)
- [التشغيل على macOS](#-التشغيل-على-macos)
- [تهيئة Nginx (إنتاج)](#-تهيئة-nginx-إنتاج)
- [تهيئة SSL (HTTPS)](#-تهيئة-ssl-https)
- [الـ APIs](#-الـ-apis)
- [بيانات الدخول](#-بيانات-الدخول)
- [استكشاف الأخطاء](#-استكشاف-الأخطاء)
- [الترخيص](#-الترخيص)

---

## 🌟 نظرة عامة

نظام إدارة مخزون متكامل مبني بتقنيات حديثة، يدعم:

- 🏪 إدارة المنتجات والفئات والمستودعات
- 📊 إحصائيات مبيعات يومية وشهرية بمختلف العملات
- 🧾 نظام نقاط بيع (POS/Cashier) مع باركود
- 📄 إنشاء وتصدير فواتير PDF
- 📦 تتبع المخزون والحركات (وارد، صادر، نقل)
- 👥 إدارة المستخدمين والصلاحيات
- 📱 تطبيق Android أصلي
- 🖥️ لوحة تحكم إدارية ويب

---

## ✨ المميزات

### Backend (FastAPI)
- ✅ RESTful API كامل مع توثيق Swagger/OpenAPI
- ✅ مصادقة JWT مع أدوار (admin / user)
- ✅ إدارة مخزون مع خصم تلقائي عند البيع
- ✅ استرجاع المخزون عند إلغاء الفاتورة
- ✅ إحصائيات مبيعات يومية وشهرية
- ✅ تقارير بالمبيعات حسب العملات
- ✅ تصدير CSV/PDF
- ✅ باركود generator

### Android App (Kotlin + Jetpack Compose)
- ✅ واجهة عربية حديثة بـ Jetpack Compose
- ✅ ماسح باركود (ZXing)
- ✅ نقاط بيع (Cashier/POS)
- ✅ عرض المنتجات والبحث
- ✅ إدارة المخزون والحركات
- ✅ سلة مشتريات وفواتير
- ✅ مشاركة الفواتير PDF

### Admin Dashboard (Next.js)
- ✅ لوحة تحكم كاملة بـ RTL
- ✅ إدارة المنتجات والفئات والوحدات
- ✅ إدارة المستودعات والمخزون
- ✅ صفحة إحصائيات مع رسوم بيانية
- ✅ صفحة تقارير مع فلترة
- ✅ إدارة المستخدمين
- ✅ نقاط بيع ويب (POS)
- ✅ تصدير Excel/PDF

---

## 📁 هيكل المشروع

> **ملاحظة:** المجلدات التالية تُنشأ تلقائياً عند التشغيل ولا تتضمن في الـ repo:
> - `backend/venv/` — بيئة Python (تنشأ بـ `python3 -m venv venv`)
> - `admin/node_modules/` — حزم npm (تنشأ بـ `npm install`)
> - `android/app/build/` — ملفات بناء Android (تنشأ بـ `./gradlew build`)

```
inventory-system/
├── backend/                    # FastAPI Backend
│   ├── app/
│   │   ├── api/               # API Endpoints
│   │   │   ├── auth.py        # المصادقة
│   │   │   ├── products.py    # المنتجات
│   │   │   ├── categories.py  # الفئات
│   │   │   ├── units.py       # الوحدات
│   │   │   ├── warehouses.py  # المستودعات
│   │   │   ├── stock.py       # المخزون
│   │   │   ├── movements.py   # حركات المخزون
│   │   │   ├── invoices.py    # الفواتير
│   │   │   ├── statistics.py  # الإحصائيات
│   │   │   ├── reports.py     # التقارير
│   │   │   ├── export.py      # التصدير
│   │   │   └── users.py       # المستخدمين
│   │   ├── models/            # SQLAlchemy Models
│   │   ├── schemas/           # Pydantic Schemas
│   │   ├── database.py        # قاعدة البيانات
│   │   └── main.py            # نقطة الدخول
│   ├── requirements.txt
│   └── alembic/               # Database Migrations
│
├── android/                    # Android Application
│   ├── app/src/main/java/
│   │   ├── ui/               # Compose Screens
│   │   ├── data/             # Repositories + API
│   │   ├── di/               # Hilt Modules
│   │   └── viewmodel/        # ViewModels
│   └── build.gradle.kts
│
├── admin/                      # Admin Dashboard (Next.js)
│   ├── app/                   # App Router Pages
│   │   ├── (dashboard)/
│   │   │   ├── page.tsx       # لوحة التحكم
│   │   │   ├── products/      # المنتجات
│   │   │   ├── categories/    # الفئات
│   │   │   ├── warehouses/    # المستودعات
│   │   │   ├── stock/         # المخزون
│   │   │   ├── movements/     # الحركات
│   │   │   ├── statistics/    # الإحصائيات
│   │   │   ├── reports/       # التقارير
│   │   │   └── users/         # المستخدمين
│   │   └── login/             # تسجيل الدخول
│   ├── components/ui/         # shadcn/ui Components
│   ├── lib/api.ts             # API Client
│   └── package.json
│
├── docker-compose.yml          # Docker Configuration
├── RULES.md                   # Golden Rules
└── SETUP.md                   # Setup Guide
```

---

## 📥 التحميل والاستنساخ

```bash
# استنساخ المشروع
git clone https://github.com/moaaz01/inventory-system.git
cd inventory-system
```

> ⚠️ **ملاحظة:** المشروع لا يتضمن بيئات التشغيل (node_modules, .venv, build) لتقليل الحجم.
> يتم تحميلها تلقائياً عند التشغيل عبر الأوامر أدناه.

---

## 📋 المتطلبات

### متطلبات عامة

| الأداة | الإصدار المطلوب | التثبيت |
|--------|----------------|---------|
| **Python** | 3.11+ | `sudo apt install python3 python3-pip python3-venv` |
| **Node.js** | 18+ | `sudo apt install nodejs npm` أو `nvm install 20` |
| **npm** | 9+ | يأتي مع Node.js |
| **PostgreSQL** | 14+ | `sudo apt install postgresql` أو Docker |
| **Docker** | 24+ (اختياري) | `curl -fsSL https://get.docker.com \| sh` |
| **Git** | 2.30+ | `sudo apt install git` |

### لتطوير Android إضافياً

| الأداة | الإصدار | التثبيت |
|--------|---------|---------|
| **Android Studio** | 2024+ | [تحميل](https://developer.android.com/studio) |
| **Kotlin** | 2.0+ | يأتي مع Android Studio |
| **JDK** | 17+ | يأتي مع Android Studio |

---

## 🚀 التثبيت والإعداد

### 1. الخيار الأول: Docker (الأسرع) ⚡

```bash
# 1. استنساخ المشروع
git clone https://github.com/moaaz01/inventory-system.git
cd inventory-system

# 2. تشغيل قاعدة البيانات فقط عبر Docker
docker-compose up -d postgres

# 3. تحميل متطلبات Backend وتشغيله
cd backend
python3 -m venv venv          # إنشاء بيئة افتراضية
source venv/bin/activate      # تفعيل البيئة (Windows: venv\Scripts\activate)
pip install -r requirements.txt   # ⬅️ تحميل الحزم
uvicorn app.main:app --host 0.0.0.0 --port 8000

# 4. تحميل متطلبات Admin وتشغيله (terminal جديد)
cd ../admin
npm install                   # ⬅️ تحميل الحزم (850MB تقريباً)
cp .env.example .env.local    # إعداد متغيرات البيئة
npm run dev
```

> ✅ بعد التشغيل:
> - Backend API: http://localhost:8000
> - API Docs: http://localhost:8000/docs
> - Admin Dashboard: http://localhost:3000

---

### 2. الخيار الثاني: تشغيل يدوي

#### الخطوة 1: قاعدة البيانات (PostgreSQL)

**Ubuntu/Debian:**
```bash
sudo apt update
sudo apt install postgresql postgresql-contrib -y
sudo systemctl enable postgresql
sudo systemctl start postgresql

# إنشاء قاعدة بيانات
sudo -u postgres psql
CREATE DATABASE inventory_db;
CREATE USER inventory_user WITH PASSWORD 'inventory_pass';
GRANT ALL PRIVILEGES ON DATABASE inventory_db TO inventory_user;
\q
```

**macOS (Homebrew):**
```bash
brew install postgresql@16
brew services start postgresql@16

createdb inventory_db
```

**Windows:**
```powershell
# تحميل من https://www.postgresql.org/download/windows/
# أو عبر WSL2 (مفضل)
wsl
sudo apt install postgresql
```

#### الخطوة 2: Backend (FastAPI)

```bash
cd inventory-system/backend

# إنشاء بيئة افتراضية وتفعيلها
python3 -m venv venv
source venv/bin/activate      # Linux/macOS
# venv\Scripts\activate       # Windows

# تحميل الحزم ⬅️
pip install -r requirements.txt

# إعداد متغيرات البيئة
cp .env.example .env
# عدّل .env حسب إعداداتك (DATABASE_URL, SECRET_KEY)

# تشغيل
uvicorn app.main:app --host 0.0.0.0 --port 8000 --reload
```

#### الخطوة 3: Admin Dashboard (Next.js)

```bash
cd inventory-system/admin

# تحميل الحزم ⬅️ (يحتاج ~850MB)
npm install

# إعداد متغيرات البيئة
cp .env.example .env.local
# عدّل NEXT_PUBLIC_API_URL=http://localhost:8000

# تشغيل (تطوير)
npm run dev

# أو للإنتاج
npm run build && npm start
```

#### الخطوة 4: Android (اختياري)

```bash
# افتح المشروع في Android Studio
# File → Open → inventory-system/android/

# أو بناء من سطر الأوامر
cd inventory-system/android
chmod +x gradlew
./gradlew assembleDebug

# APK الناتج:
# android/app/build/outputs/apk/debug/app-debug.apk
```

---

## 🖥️ الإعداد على السيرفر (Ubuntu)

### المتطلبات الأساسية

```bash
# تحديث النظام
sudo apt update && sudo apt upgrade -y

# تثبيت الأدوات الأساسية
sudo apt install -y \
  python3 python3-pip python3-venv \
  nodejs npm \
  postgresql postgresql-contrib \
  nginx certbot python3-certbot-nginx \
  git curl wget \
  build-essential libpq-dev

# تثبيت PM2 (إدارة العمليات)
sudo npm install -g pm2
```

### نشر المشروع

```bash
# 1. استنساخ المشروع
cd /opt
sudo git clone https://github.com/moaaz01/inventory-system.git
sudo chown -R $USER:$USER inventory-system
cd inventory-system

# 2. إعداد قاعدة البيانات
sudo -u postgres psql <<EOF
CREATE DATABASE inventory_db;
CREATE USER inventory_user WITH PASSWORD 'تغيّر_كلمة_المرور';
GRANT ALL PRIVILEGES ON DATABASE inventory_db TO inventory_user;
EOF

# 3. إعداد Backend
cd backend
python3 -m venv venv
source venv/bin/activate
pip install -r requirements.txt

# إنشاء ملف .env
cat > .env <<EOF
DATABASE_URL=postgresql://inventory_user:تغيّر_كلمة_المرور@localhost:5432/inventory_db
SECRET_KEY=$(python3 -c "import secrets; print(secrets.token_hex(32))")
ALGORITHM=HS256
ACCESS_TOKEN_EXPIRE_MINUTES=1440
EOF

# تشغيل Backend مع PM2
pm2 start "venv/bin/uvicorn app.main:app --host 127.0.0.1 --port 8000" \
  --name inventory-backend --cwd /opt/inventory-system/backend
pm2 save
pm2 startup

# 4. إعداد Admin
cd /opt/inventory-system/admin
npm install
npm run build

# إنشاء ملف .env.local
cat > .env.local <<EOF
NEXT_PUBLIC_API_URL=http://localhost:8000
EOF

# تشغيل Admin مع PM2
pm2 start "npm start" \
  --name inventory-admin --cwd /opt/inventory-system/admin
pm2 save
```

### تهيئة Nginx (إنتاج)

```bash
sudo nano /etc/nginx/sites-available/inventory-system
```

```nginx
server {
    listen 80;
    server_name your-domain.com;

    # تحميل الملفات الكبيرة
    client_max_body_size 50M;

    # Backend API
    location /api {
        proxy_pass http://127.0.0.1:8000;
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection 'upgrade';
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        proxy_cache_bypass $http_upgrade;
    }

    # API Documentation
    location /docs {
        proxy_pass http://127.0.0.1:8000/docs;
        proxy_set_header Host $host;
    }

    # Admin Dashboard
    location / {
        proxy_pass http://127.0.0.1:3000;
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection 'upgrade';
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        proxy_cache_bypass $http_upgrade;
    }
}
```

```bash
# تفعيل الإعدادات
sudo ln -sf /etc/nginx/sites-available/inventory-system /etc/nginx/sites-enabled/
sudo rm -f /etc/nginx/sites-enabled/default

# اختبار الإعدادات
sudo nginx -t

# إعادة تشغيل Nginx
sudo systemctl reload nginx
```

### تهيئة SSL (HTTPS)

```bash
# شهادة مجانية من Let's Encrypt
sudo certbot --nginx -d your-domain.com

# تجديد تلقائي (مضاف تلقائياً)
sudo certbot renew --dry-run
```

### جدار الحماية

```bash
sudo ufw allow 22      # SSH
sudo ufw allow 80      # HTTP
sudo ufw allow 443     # HTTPS
sudo ufw enable
```

---

## 💻 التشغيل على Windows + WSL2

### إعداد WSL2

```powershell
# في PowerShell (كمسؤول)
wsl --install
# أعد تشغيل الكمبيوتر

# تثبيت Ubuntu
wsl --install -d Ubuntu-24.04
```

### داخل WSL2

```bash
# تحديث
sudo apt update && sudo apt upgrade -y

# تثبيت المتطلبات
sudo apt install -y python3 python3-pip python3-venv nodejs npm git

# استنساخ المشروع
cd ~
git clone https://github.com/moaaz01/inventory-system.git
cd inventory-system

# تشغيل PostgreSQL عبر Docker
sudo apt install docker.io -y
sudo usermod -aG docker $USER
# أعد تسجيل الدخول
docker-compose up -d postgres

# تشغيل Backend
cd backend
python3 -m venv venv
source venv/bin/activate
pip install -r requirements.txt
uvicorn app.main:app --host 0.0.0.0 --port 8000

# تشغيل Admin (terminal جديد)
cd ~/inventory-system/admin
npm install
npm run dev
```

### Port Forwarding (للوصول من Windows)

```powershell
# في PowerShell (كمسؤول)
netsh interface portproxy add v4tov4 `
  listenport=8000 listenaddress=0.0.0.0 `
  connectport=8000 connectaddress=$(wsl hostname -I)

netsh interface portproxy add v4tov4 `
  listenport=3000 listenaddress=0.0.0.0 `
  connectport=3000 connectport=$(wsl hostname -I)

# فتح Firewall
netsh advfirewall firewall add rule name="Inventory Backend" dir=in action=allow protocol=TCP localport=8000
netsh advfirewall firewall add rule name="Inventory Admin" dir=in action=allow protocol=TCP localport=3000
```

---

## 🍎 التشغيل على macOS

```bash
# تثبيت Homebrew (إذا ما مثبت)
/bin/bash -c "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/HEAD/install.sh)"

# تثبيت المتطلبات
brew install python@3.12 node@22 postgresql@16 git

# تشغيل PostgreSQL
brew services start postgresql@16

# إنشاء قاعدة بيانات
createdb inventory_db

# استنساخ المشروع
git clone https://github.com/moaaz01/inventory-system.git
cd inventory-system

# إعداد Backend
cd backend
python3 -m venv venv
source venv/bin/activate
pip install -r requirements.txt

# إنشاء .env
cat > .env <<EOF
DATABASE_URL=postgresql://$(whoami)@localhost:5432/inventory_db
SECRET_KEY=$(python3 -c "import secrets; print(secrets.token_hex(32))")
EOF

uvicorn app.main:app --host 0.0.0.0 --port 8000

# إعداد Admin (terminal جديد)
cd ../admin
npm install
cat > .env.local <<EOF
NEXT_PUBLIC_API_URL=http://localhost:8000
EOF
npm run dev
```

---

## 📡 الـ APIs

### المصادقة
| الطريقة | الرابط | الوصف |
|---------|--------|-------|
| `POST` | `/api/auth/login` | تسجيل الدخول |
| `POST` | `/api/auth/register` | إنشاء حساب |
| `GET` | `/api/auth/me` | بيانات المستخدم |

### المنتجات
| الطريقة | الرابط | الوصف |
|---------|--------|-------|
| `GET` | `/api/products` | قائمة المنتجات |
| `POST` | `/api/products` | إضافة منتج |
| `GET` | `/api/products/{id}` | تفاصيل منتج |
| `PUT` | `/api/products/{id}` | تحديث منتج |
| `DELETE` | `/api/products/{id}` | حذف منتج |

### الفواتير
| الطريقة | الرابط | الوصف |
|---------|--------|-------|
| `GET` | `/api/invoices` | قائمة الفواتير |
| `POST` | `/api/invoices` | إنشاء فاتورة (يخصم المخزون تلقائياً) |
| `POST` | `/api/invoices/{id}/cancel` | إلغاء فاتورة (يسترجع المخزون) |

### الإحصائيات
| الطريقة | الرابط | الوصف |
|---------|--------|-------|
| `GET` | `/api/statistics/overview` | نظرة عامة (يوم/أسبوع/شهر) |
| `GET` | `/api/statistics/sales/daily?date=YYYY-MM-DD` | مبيعات يومية بالعملات |
| `GET` | `/api/statistics/sales/monthly?year=YYYY&month=M` | مبيعات شهرية |

### التوثيق الكامل
عن تشغيل الـ Backend اذهب إلى:
- **Swagger UI:** `http://localhost:8000/docs`
- **ReDoc:** `http://localhost:8000/redoc`

---

## 🔐 بيانات الدخول

```
المستخدم:  admin
كلمة المرور: admin123
الدور: مسؤول (admin)
```

> ⚠️ **مهم:** غيّر كلمة المرور في بيئة الإنتاج!

---

## 🔧 استكشاف الأخطاء

### قاعدة البيانات لا تتصل

```bash
# تحقق إن PostgreSQL شغال
sudo systemctl status postgresql

# تحقق من الاتصال
psql -U inventory_user -d inventory_db -h localhost

# أعد تشغيل
sudo systemctl restart postgresql
```

### Backend لا يشتغل

```bash
# تحقق من المنافذ
lsof -i :8000
# أو
ss -tulpn | grep 8000

# تحقق من المتطلبات
cd backend && pip list | grep -E "fastapi|uvicorn|sqlalchemy"

# شغل مع debug
uvicorn app.main:app --host 0.0.0.0 --port 8000 --log-level debug
```

### Admin لا يشتغل

```bash
# حذف cache وأعد التثبيت
cd admin
rm -rf node_modules .next
npm install
npm run dev

# تحقق من المنفذ
lsof -i :3000
```

### خطأ CORS

```bash
# تأكد إن Backend يشتغل على 0.0.0.0
uvicorn app.main:app --host 0.0.0.0 --port 8000

# تحقق من .env.local في admin
cat admin/.env.local
# يجب أن يكون: NEXT_PUBLIC_API_URL=http://localhost:8000
```

### خطأ PM2

```bash
pm2 logs inventory-backend --lines 50
pm2 restart inventory-backend
pm2 delete inventory-backend
pm2 start "..." --name inventory-backend
```

---

## 🛡️ الأمان في الإنتاج

- [ ] غيّر كلمة المرور الافتراضية
- [ ] استخدم `SECRET_KEY` قوي وفريد
- [ ] فعّل HTTPS عبر Let's Encrypt
- [ ] فعّل جدار الحماية (UFW)
- [ ] حدّث النظام بانتظام
- [ ] فعّل backups لقاعدة البيانات
- [ ] استخدم `pm2` لإدارة العمليات
- [ ] راجع السجلات بشكل دوري

---

## 📜 الترخيص

MIT License — استخدمه بحرية للمشاريع الشخصية والتجارية.

---

## 🤝 المساهمة

1. Fork المشروع
2. أنشئ branch جديد: `git checkout -b feature/amazing`
3. Commit التغييرات: `git commit -m 'Add amazing feature'`
4. Push: `git push origin feature/amazing`
5. أنشئ Pull Request

---

<div align="center">

**Built with ❤️ by [moaaz01](https://github.com/moaaz01)**

</div>
