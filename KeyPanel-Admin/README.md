# UAssetGUI Admin Key Panel

Mobil uygulamadaki key doğrulama için HTTP panel servisidir.

## Başlatma
```bash
cd KeyPanel-Admin
npm install
ADMIN_TOKEN=super-secret-token PORT=8080 npm start
```

## Endpointler
- `POST /api/mobile/validate-key` -> mobil login doğrulama
- `GET /api/admin/keys` -> admin key listesi (`x-admin-token` gerekli)
- `POST /api/admin/keys` -> key ekleme
- `DELETE /api/admin/keys/:key` -> key silme

Panel arayüzü: `http://localhost:8080`
