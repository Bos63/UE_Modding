# UAssetGUI Mobile (Android Studio)

Bu sürümde uygulama tek ekran (`EditorActivity`) ile çalışır ve **SAF (Storage Access Framework)** kullanır.

## Dosya Açma Kuralı
- `OPEN FILE` butonu sistem dosya seçiciyi açar (`ACTION_OPEN_DOCUMENT`, MIME `*/*`).
- Kullanıcı `.uasset` seçtiğinde uygulama **aynı klasörde aynı base-name** ile `.uexp` arar.
- Örnek: `M_Body_03_A.uasset` için `M_Body_03_A.uexp` zorunludur.
- Bulunursa parse başlar; bulunamazsa şu uyarı gösterilir:
  - `Geçerli aynı isimde .uexp dosyası bulunamadı. Lütfen aynı klasörde aynı isimde olan dosyayı veriniz.`
- Varsa `<base>.ubulk` da ek kaynak olarak okunur.

## Güvenlik / Policy
- Android 11–15 için storage runtime izinleri yerine URI bazlı SAF kullanılır.
- `READ/WRITE/MANAGE_EXTERNAL_STORAGE` kullanılmaz.
- Persist edilen URI okuma izni `takePersistableUriPermission` ile alınır.

## Dump TXT
- `DUMP TXT` butonu kayıt yeri için SAF `CreateDocument` açar.
- Çıktı stream olarak yazılır (`BufferedWriter`) ve büyük dosyalarda bellek tüketimi kontrollüdür.

## Not
- Uygulama read-only çalışır: dosya içeriğini değiştirme/yazma yapmaz, sadece görüntüler ve metin raporu üretir.
