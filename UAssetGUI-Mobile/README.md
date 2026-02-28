# UAssetGUI Mobile (Android Studio)

Bu sürümde uygulama **tek activity** olarak çalışır: `EditorActivity`.

## Çalışma Klasörü
Uygulama yalnızca şu klasör ile çalışır:

`/storage/emulated/0/Download/URAZMOD_UASSETGUİ`

- `DOSYA AÇ`: Bu klasördeki `.uasset` dosyalarını listeler.
- Eşleşen `.uexp` yoksa dosya açılmaz.
- Dump çıktısı aynı klasöre `<orijinal_ad>_dump.txt` olarak yazılır.

## Ekran Akışı
- Uygulama açılır açılmaz editör ekranı gelir.
- Login/Main ekranı yoktur.
- Satır listesi gerçek dosya içeriğinden üretilir.
- Satıra tıklayınca değer düzenlenebilir.
- NameMap düzenleme, karşılaştırma ve HEX EDITOR menüden erişilir.

## Notlar
- Düzenleme sırasında orijinal dosya için `.bak` yedek oluşturulur.
- Büyük dosyalarda dump işlemi metin çıktısı üretir.
