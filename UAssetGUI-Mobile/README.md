# UAssetGUI Mobile (Android Studio)

Bu sürümde uygulama **tek activity** olarak çalışır: `EditorActivity`.

## Dosya Açma (Android 11–15 uyumlu)
Uygulama dosya erişimi için **Storage Access Framework (SAF)** kullanır.

- `DOSYA AÇ` butonu → sistem dosya seçiciyi açar (`ACTION_OPEN_DOCUMENT`).
- MIME: `*/*` (her dosya türü seçilebilir).
- Seçilen dosya için kalıcı okuma izni alınır (`takePersistableUriPermission`).
- Son seçilen URI SharedPreferences içine kaydedilir.
- Dosya okuma `ContentResolver.openInputStream(uri)` üzerinden yapılır.

> Not: Bu yaklaşımda Android ayarlar ekranında klasik "Dosyalar" runtime izni beklenmez. Erişim kullanıcı dosya seçtiğinde URI bazlı olarak verilir.

## Ekran Akışı
- Uygulama açılır açılmaz editör ekranı gelir.
- Login/Main ekranı yoktur.
- Satır listesi seçilen dosyadan üretilir.
- Satıra tıklayınca değer düzenlenebilir.
- NameMap düzenleme, karşılaştırma ve HEX EDITOR menüden erişilir.
