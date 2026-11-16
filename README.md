# Nexus Client (Fabric 1.21.11, client-side)

## Telefondan derleme
1. GitHub'da yeni bir repo aç, bu klasörün İÇERİĞİNİ (zip'i açıp) yükle (`.github` klasörü dahil).
2. Repo > **Actions** sekmesi > "Build Nexus Client" > **Run workflow** (push'ta da otomatik çalışır).
3. Bitince çalıştırmanın sayfasında **Artifacts > NexusClient-jar** indir (zip içinden `nexusclient-1.0.0.jar`).
4. PojavLauncher'da Minecraft 1.21.11 + Fabric Loader + **Fabric API** kur, jar'ı `mods/` klasörüne at.

`v1.0.0` gibi bir tag atarsan jar otomatik Release olarak da yayınlanır.

## Kullanım
- Sağ Shift: menü (Modüller / Temalar / Kozmetik / HUD Düzenleyici)
- C: zoom
- Performans sekmesi: Optimizer (ayarları FPS lehine çeker, uyarlanabilir render mesafesi, kapatınca geri yükler)
- Ayarlar: `config/nexusclient.json`
