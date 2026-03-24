# 📦 Inventory Management System

> نظام إدارة مخزون متكامل — FastAPI + Kotlin Android + Next.js Admin

![FastAPI](https://img.shields.io/badge/Backend-FastAPI-009688?style=flat-square&logo=fastapi)
![Kotlin](https://img.shields.io/badge/Android-Kotlin-7F52FF?style=flat-square&logo=kotlin)
![Next.js](https://img.shields.io/badge/Admin-Next.js-000000?style=flat-square&logo=next.js)
![PostgreSQL](https://img.shields.io/badge/Database-PostgreSQL-4169E1?style=flat-square&logo=postgresql)
![Docker](https://img.shields.io/badge/Docker-Ready-2496ED?style=flat-square&logo=docker)

---

## ✨ المميزات

- 🏪 إدارة المنتجات، الفئات، المستودعات، الوحدات
- 📦 تتبع المخزون والحركات (وارد، صادر، نقل)
- 🧾 نظام نقاط بيع (POS) مع باركود وفواتير PDF
- 📊 إحصائيات مبيعات يومية وشهرية بمختلف العملات
- 👥 إدارة مستخدمين بصلاحيات (admin / user)
- 📱 تطبيق Android + لوحة تحكم ويب

---

## 🚀 التثبيت

### الخطوة 1: استنساخ المشروع

```bash
git clone https://github.com/moaaz01/inventory-system.git
cd inventory-system
```

### الخطوة 2: قاعدة البيانات

**اختر طريقة واحدة:**

| الطريقة | الأمر |
|---------|-------|
| Docker (الأفضل) | `docker-compose up -d postgres` |
| Ubuntu/Debian | `sudo apt install postgresql -y` ثم أنشئ DB يدوياً |
| macOS | `brew install postgresql@16 && brew services start postgresql@16` |

أنشئ قاعدة البيانات:
```sql
sudo -u postgres psql
CREATE DATABASE inventory_db;
CREATE USER inv_user WITH PASSWORD 'your_password';
GRANT ALL PRIVILEGES ON DATABASE inventory_db TO inv_user;
\q
```

> مع Docker لا تحتاج هذا — القاعدة تُنشأ تلقائياً.

### الخطوة 3: Backend

```bash
cd backend
python3 -m venv venv && source venv/bin/activate
pip install -r requirements.txt
cp .env.example .env
# عدّل DATABASE_URL و SECRET_KEY في ملف .env
uvicorn app.main:app --host 0.0.0.0 --port 8000
```

### الخطوة 4: Admin Dashboard

```bash
cd admin
npm install
cp .env.example .env.local
# عدّل NEXT_PUBLIC_API_URL=http://localhost:8000
npm run dev
```

> ✅ Backend: `http://localhost:8000` \| Docs: `http://localhost:8000/docs` \| Admin: `http://localhost:3000`

### الخطوة 5: Android (اختياري)

```bash
cd android
chmod +x gradlew
./gradlew assembleDebug
# APK → android/app/build/outputs/apk/debug/app-debug.apk
```

---

## 💻 Windows + WSL2

إذا تبي تشغّل المشروع على Windows، تحتاج WSL2:

```powershell
# PowerShell كمسؤول (مرة واحدة)
wsl --install -d Ubuntu-24.04
# أعد التشغيل
```

بعدها شغّل كل خطوات التثبيت أعلاه داخل WSL2.

**للوصول من متصفح Windows:** (مرة واحدة فقط)

```powershell
# PowerShell كمسؤول
$wslIp = (wsl hostname -I).Trim()
netsh interface portproxy add v4tov4 listenport=8000 connectaddress=$wslIp
netsh interface portproxy add v4tov4 listenport=3000 connectaddress=$wslIp
netsh advfirewall firewall add rule name="Inventory" dir=in action=allow protocol=TCP localport=8000,3000
```

---

## 🖥️ نشر على VPS / Ubuntu (إنتاج)

### تثبيت المتطلبات

```bash
sudo apt update && sudo apt install -y python3 python3-pip python3-venv nodejs npm postgresql nginx certbot python3-certbot-nginx pm2 ufw
```

### تشغيل دائم مع PM2

```bash
# Backend
pm2 start "/path/to/backend/venv/bin/uvicorn app.main:app --host 127.0.0.1 --port 8000" \
  --name backend --cwd /path/to/backend

# Admin
pm2 start "npm start" --name admin --cwd /path/to/admin

pm2 save && pm2 startup
```

### Nginx Reverse Proxy

```nginx
server {
    listen 80;
    server_name your-domain.com;
    client_max_body_size 50M;

    location /api {
        proxy_pass http://127.0.0.1:8000;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }

    location / {
        proxy_pass http://127.0.0.1:3000;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }
}
```

```bash
sudo ln -sf /etc/nginx/sites-available/inventory /etc/nginx/sites-enabled/
sudo rm -f /etc/nginx/sites-enabled/default
sudo nginx -t && sudo systemctl reload nginx
```

### SSL + جدار الحماية

```bash
sudo certbot --nginx -d your-domain.com
sudo ufw allow 22,80,443 && sudo ufw enable
```

---

## 🔐 بيانات الدخول

```
admin / admin123
```
> ⚠️ غيّرها في الإنتاج!

---

## 📡 APIs رئيسية

| الطريقة | الرابط | الوصف |
|---------|--------|-------|
| `POST` | `/api/auth/login` | تسجيل الدخول |
| `GET/POST` | `/api/products` | المنتجات |
| `POST` | `/api/invoices` | فاتورة جديدة (يخصم المخزون) |
| `POST` | `/api/invoices/{id}/cancel` | إلغاء فاتورة (يسترجع المخزون) |
| `GET` | `/api/statistics/overview` | إحصائيات عامة |
| `GET` | `/api/statistics/sales/daily` | مبيعات يومية |
| `GET` | `/api/statistics/sales/monthly` | مبيعات شهرية |

📄 التوثيق الكامل: `http://localhost:8000/docs`

---

## 🔧 استكشاف الأخطاء

| المشكلة | الحل |
|---------|------|
| قاعدة البيانات لا تتصل | `sudo systemctl restart postgresql` |
| المنفذ مستخدم | `lsof -i :8000` أو `lsof -i :3000` |
| Admin لا يعمل | `cd admin && rm -rf node_modules .next && npm install` |
| PM2 لا يشتغل | `pm2 logs --lines 50 && pm2 restart all` |

---

<div align="center">

**Built with ❤️ by [moaaz01](https://github.com/moaaz01)**

</div>
