# UAssetGUI Admin Key Panel

Mobil uygulama key doğrulama paneli.

## Başlatma
```bash
cd KeyPanel-Admin
npm install
ADMIN_TOKEN=super-secret-token PORT=8080 npm start
```

## Panel Linki (mobil entegrasyon)
- `https://urazpanel/uassetvip.com/`

## Endpointler
- `POST /api/mobile/validate-key` -> mobil login doğrulama
- `GET /api/admin/keys` -> key listesi (`x-admin-token` gerekli)
- `POST /api/admin/keys` -> key oluştur
  - `randomKey: true|false`
  - `customKey: string` (özel isimli key için)
  - `durationType: hourly|daily|weekly|monthly|custom_days`
  - `customDays: number` (custom_days için)
- `DELETE /api/admin/keys/:key` -> key sil
