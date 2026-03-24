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

## 📁 هيكل المشروع

```
inventory-system/
├── backend/          ← FastAPI (Python)
├── admin/            ← Next.js Dashboard (Node.js)
├── android/          ← تطبيق Android (Kotlin)
├── docker-compose.yml
└── SETUP.md          ← دليل مفصّل
```

> **ملاحظة:** `node_modules/` و `venv/` و `build/` غير مضمنة — تُحمّل تلقائياً عند التشغيل.

---

## 🚀 التثبيت

### 🔹 الطريقة 1: Docker (الأسهل — أنصح بها)

تعمل على أي نظام (Windows / Ubuntu / macOS / VPS):

```bash
# 1. استنساخ
git clone https://github.com/moaaz01/inventory-system.git
cd inventory-system

# 2. تشغيل PostgreSQL
docker-compose up -d postgres

# 3. Backend
cd backend
python3 -m venv venv && source venv/bin/activate
pip install -r requirements.txt
uvicorn app.main:app --host 0.0.0.0 --port 8000

# 4. Admin (terminal جديد)
cd ../admin
npm install
cp .env.example .env.local
npm run dev
```

> ✅ Backend: http://localhost:8000 | Docs: http://localhost:8000/docs | Admin: http://localhost:3000

---

### 🔹 الطريقة 2: Windows + WSL2

#### إعداد WSL2 (مرة واحدة)

```powershell
# PowerShell كمسؤول
wsl --install -d Ubuntu-24.04
# أعد التشغيل
```

#### داخل WSL2

```bash
# تثبيت المتطلبات
sudo apt update && sudo apt install -y python3 python3-pip python3-venv nodejs npm git

# تثبيت Docker (اختياري - لقاعدة البيانات)
curl -fsSL https://get.docker.com | sudo sh
sudo usermod -aG docker $USER && exit
# أعد تسجيل الدخول

# استنساخ وتشغيل
git clone https://github.com/moaaz01/inventory-system.git
cd inventory-system

# قاعدة البيانات
docker-compose up -d postgres

# Backend
cd backend
python3 -m venv venv && source venv/bin/activate
pip install -r requirements.txt
uvicorn app.main:app --host 0.0.0.0 --port 8000

# Admin (terminal جديد)
cd ../admin && npm install && cp .env.example .env.local && npm run dev
```

#### الوصول من Windows

```powershell
# PowerShell كمسؤول — مرة واحدة فقط
$wslIp = (wsl hostname -I).Trim()
netsh interface portproxy add v4tov4 listenport=8000 connectaddress=$wslIp
netsh interface portproxy add v4tov4 listenport=3000 connectaddress=$wslIp
netsh advfirewall firewall add rule name="Inventory" dir=in action=allow protocol=TCP localport=8000,3000
```

> الآن تقدر تفتح `http://localhost:8000` و `http://localhost:3000` من المتصفح في Windows

---

### 🔹 الطريقة 3: Ubuntu / VPS (إنتاج)

#### متطلبات أولية

```bash
sudo apt update && sudo apt install -y python3 python3-pip python3-venv nodejs npm postgresql nginx ufw
```

#### قاعدة البيانات

```bash
sudo -u postgres psql <<EOF
CREATE DATABASE inventory_db;
CREATE USER inv_user WITH PASSWORD 'غيّر_هذا_الرقم';
GRANT ALL PRIVILEGES ON DATABASE inventory_db TO inv_user;
\q
EOF
```

#### المشروع

```bash
sudo git clone https://github.com/moaaz01/inventory-system.git /opt/inventory
sudo chown -R $USER:$USER /opt/inventory
cd /opt/inventory

# Backend
cd backend
python3 -m venv venv && source venv/bin/activate
pip install -r requirements.txt
cp .env.example .env
# عدّل DATABASE_URL و SECRET_KEY في .env
uvicorn app.main:app --host 127.0.0.1 --port 8000

# Admin (terminal جديد)
cd /opt/inventory/admin
npm install && npm run build
cp .env.example .env.local
# عدّل NEXT_PUBLIC_API_URL=http://localhost:8000
npm start
```

#### تشغيل دائم مع PM2

```bash
sudo npm install -g pm2

# Backend
pm2 start "/opt/inventory/backend/venv/bin/uvicorn app.main:app --host 127.0.0.1 --port 8000" \
  --name backend --cwd /opt/inventory/backend

# Admin
pm2 start "npm start" --name admin --cwd /opt/inventory/admin

pm2 save && pm2 startup
```

#### Nginx (Reverse Proxy)

```bash
sudo nano /etc/nginx/sites-available/inventory
```

```nginx
server {
    listen 80;
    server_name your-domain.com;    # ← غيّر هذا

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

#### SSL (HTTPS مجاني)

```bash
sudo apt install certbot python3-certbot-nginx -y
sudo certbot --nginx -d your-domain.com
```

#### جدار الحماية

```bash
sudo ufw allow 22,80,443
sudo ufw enable
```

---

## 🔐 بيانات الدخول الافتراضية

```
المستخدم:  admin
كلمة المرور: admin123
```

> ⚠️ غيّرها فوراً في بيئة الإنتاج!

---

## 📡 الـ APIs الرئيسية

| الطريقة | الرابط | الوصف |
|---------|--------|-------|
| `POST` | `/api/auth/login` | تسجيل الدخول |
| `GET/POST` | `/api/products` | المنتجات |
| `GET/POST` | `/api/invoices` | الفواتير (خصم تلقائي للمخزون) |
| `POST` | `/api/invoices/{id}/cancel` | إلغاء فاتورة (استرجاع المخزون) |
| `GET` | `/api/statistics/overview` | إحصائيات عامة |
| `GET` | `/api/statistics/sales/daily` | مبيعات يومية بالعملات |
| `GET` | `/api/statistics/sales/monthly` | مبيعات شهرية |

📄 التوثيق الكامل: `http://localhost:8000/docs`

---

## 🔧 استكشاف الأخطاء

```bash
# قاعدة البيانات لا تتصل؟
sudo systemctl status postgresql
sudo systemctl restart postgresql

# المنفذ مستخدم؟
lsof -i :8000    # Backend
lsof -i :3000    # Admin

# إعادة بناء Admin
cd admin && rm -rf node_modules .next && npm install && npm run build

# سجلات PM2
pm2 logs --lines 50
pm2 restart all
```

---

## 🛡️ أمان الإنتاج

- [ ] غيّر كلمة المرور الافتراضية
- [ ] استخدم `SECRET_KEY` عشوائي وقوي
- [ ] فعّل HTTPS (Certbot)
- [ ] فعّل جدار الحماية (UFW)
- [ ] شغّل PostgreSQL على localhost فقط

---

## 📜 الترخيص

MIT — استخدمه بحرية.

---

<div align="center">

**Built with ❤️ by [moaaz01](https://github.com/moaaz01)**

</div>
