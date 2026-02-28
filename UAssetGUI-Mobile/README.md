# UAssetGUI Mobile (Android Studio)

Bu proje Android Studio için düzenlenmiştir ve APK build almaya hazırdır.

## Hedefler
- Uygulama adı: **UAssetGUI**
- Paket: `com.urzuasset.gui`
- Min SDK: 24 (Android 7+)
- Target/Compile SDK: 34 (Android 14)
- Key doğrulanmadan uygulama içeriği açılamaz.
- Modüler ekran yapısı:
  - `LoginActivity` + `activity_login.xml`
  - `MainActivity` + `activity_main.xml`
  - `EditorActivity` + `activity_editor.xml`

## Repo politikası (binary)
- Bu repoda build çıktıları (`.apk`, `.aab`, `build/`) commit edilmez.
- `gradle/wrapper/gradle-wrapper.jar` de binary olduğu için repoya dahil edilmez.
- Android Studio/terminal ortamında gerekirse wrapper jar yerelde yeniden üretilebilir.

## Android Studio ile APK build
1. Android Studio > **Open** > `UAssetGUI-Mobile` klasörünü açın.
2. `local.properties.example` dosyasını `local.properties` olarak kopyalayın ve `sdk.dir` yolunu kendi Android SDK yolunuzla değiştirin.
3. Gradle Sync tamamlandıktan sonra:
   - Debug APK: **Build > Build APK(s)**
   - Release APK/AAB: **Build > Generate Signed Bundle / APK**
4. Çıktılar:
   - Debug: `app/build/outputs/apk/debug/app-debug.apk`
   - Release: `app/build/outputs/apk/release/`

## Terminal ile APK build
```bash
cd UAssetGUI-Mobile
cp local.properties.example local.properties
# sdk.dir yolunu local.properties içinde güncelle
./build-apk.sh
```

## Panel entegrasyonu
- Mobil uygulama key doğrulamasını şu endpoint üzerinden yapar:
  - `BuildConfig.KEY_PANEL_BASE_URL + /api/mobile/validate-key`
- Varsayılan değer `https://urazpanel/uassetvip.com/` olarak ayarlıdır, gerekirse `app/build.gradle.kts` içinde değiştirin.

## Not
Bu repo üretim seviyesi tam binary UAsset/UEXP editör motorunu değil, mobil mimari + key panel entegrasyon temelini sağlar.
