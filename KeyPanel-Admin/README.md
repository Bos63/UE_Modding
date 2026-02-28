# UAssetGUI Key Panel (Premium)

Modern, mobil öncelikli admin panel:
- Frontend: React + TypeScript + Tailwind
- Backend: Node.js (Express) + MongoDB (Mongoose)
- Auth: Admin token -> JWT

## Özellikler
- Dashboard
- Key Oluştur (Saatlik/Günlük/Haftalık/Aylık, Random veya Özel İsimli)
- Key Listesi (yalnızca KEY ve TARİH)
- Yöneticiler (SuperAdmin/Admin/Viewer)
- Kullanıcılar (aktif/pasif)
- Risales Panel (kategori/başlık/içerik)
- Ayarlar (panel link, token yenileme, log seviyesi)
- Sistem / Güncelleme (Remote Config):
  - maintenance_mode
  - min_app_version
  - force_update
  - message_banner

Panel link: `https://urazpanel/uassetvip.com/`

## Kurulum
```bash
cd KeyPanel-Admin
npm install
cp server/.env.example server/.env
cp web/.env.example web/.env
```

## Geliştirme
```bash
npm run dev:all
```
- API: http://localhost:8080
- Web: http://localhost:5173

## Build
```bash
npm run build
npm run start
```

## REST API
- `POST /auth/login` `{ token }`
- `POST /mobile/validate-key` `{ key }`
- `POST /keys` `{ type, mode, customName? }`
- `GET /keys?type=&q=&page=`
- `DELETE /keys/:key`
- `GET/POST /remote-config`
- `GET/POST/DELETE /admins`
- `GET/POST/PATCH /users`
- `GET/POST/PUT/DELETE /risales`
- `GET/POST /settings`
