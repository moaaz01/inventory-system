# 🚀 دليل التشغيل السريع - نظام إدارة المخزون

## الملفات
- **المشروع المضغوط:** `~/projects/inventory-system.zip` (1.3GB)
- **المسار:** `/home/ali/projects/inventory-system/`

---

## 1. فك الضغط على السيرفر (Ubuntu)

```bash
# الاتصال بالسيرفر
ssh user@your-server-ip

# رفع الملف المضغوط
scp inventory-system.zip user@your-server-ip:/home/user/

# فك الضغط
cd /home/user
unzip inventory-system.zip
cd inventory-system
```

---

## 2. إعداد Docker (PostgreSQL)

```bash
# تثبيت Docker إذا ما مثبت
curl -fsSL https://get.docker.com | sudo sh
sudo usermod -aG docker $USER

# تشغيل PostgreSQL
docker-compose up -d postgres

# انتظر 10 ثواني
sleep 10
```

---

## 3. تشغيل Backend (FastAPI)

```bash
cd inventory-system/backend

# تثبيت المتطلبات
pip install -r requirements.txt

# تشغيل
uvicorn app.main:app --host 0.0.0.0 --port 8000
```

أو مع PM2:
```bash
npm install -g pm2
pm2 start "uvicorn app.main:app --host 0.0.0.0 --port 8000" --name backend
pm2 save
pm2 startup
```

---

## 4. تشغيل Admin Dashboard (Next.js)

```bash
cd inventory-system/admin

# تثبيت المتطلبات
npm install

# تشغيل
npm run dev
```

أو للإنتاج:
```bash
npm run build
npm start
```

---

## 5. إعداد Nginx (إنتاج)

```bash
sudo apt install nginx

sudo nano /etc/nginx/sites-available/inventory-system
```

```nginx
server {
    listen 80;
    server_name your-domain.com;

    # Backend API
    location /api {
        proxy_pass http://127.0.0.1:8000;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }

    # Admin Dashboard
    location / {
        proxy_pass http://127.0.0.1:3000;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }
}
```

```bash
sudo ln -s /etc/nginx/sites-available/inventory-system /etc/nginx/sites-enabled/
sudo nginx -t
sudo systemctl reload nginx
```

---

## 6. SSL Certificate (Let's Encrypt)

```bash
sudo apt install certbot python3-certbot-nginx
sudo certbot --nginx -d your-domain.com
```

---

## 7. بيانات الدخول

```
المستخدم: admin
كلمة المرور: admin123
```

---

## 8. الأوامر المفيدة

```bash
# حالة Docker
docker-compose ps

# سجلات Backend
pm2 logs backend

# إعادة تشغيل
pm2 restart backend
docker-compose restart postgres

# تحديث المشروع
cd inventory-system
git pull
cd admin && npm install && npm run build
```

---

## المنافذ المطلوبة

| الخدمة | المنفذ |
|--------|--------|
| Backend API | 8000 |
| Admin Dashboard | 3000 |
| PostgreSQL | 5432 |
| Redis (optional) | 6379 |

---

## متغيرات البيئة (اختياري)

```bash
# Backend .env
DATABASE_URL=postgresql://user:password@localhost:5432/inventory
SECRET_KEY=your-secret-key-here

# Admin .env.local
NEXT_PUBLIC_API_URL=http://localhost:8000
```

---

## Troubleshooting

**خطأ في الاتصال بقاعدة البيانات:**
```bash
docker-compose logs postgres
docker-compose restart postgres
```

**خطأ في npm install:**
```bash
cd admin
rm -rf node_modules package-lock.json
npm install
```

**خطأ CORS:**
تأكد إن Backend يشتغل على `0.0.0.0` مو `localhost`
