# SmartGate Guard Console

Netelsan staj projesi icin JavaFX tabanli guvenlik konsolu.

Bu masaustu uygulamasi IP interkom/kapi paneli ile TCP uzerinden haberlesir, kapi acma komutu gonderir, alarm bildirimlerini dinler, giris-cikis kayitlarini PostgreSQL'e yazar ve guvenlik gorevlisinin dairelerle mesajlasmasini saglar.

## Hedef Ozellikler

- Guvenlik dashboard ekrani
- Kapi acma komutu: `ope_type=12`
- Interkom handshake: `ope_type=42/43`
- Alarm dinleme: `ope_type=47`
- Giris-cikis kayitlari
- Daire ve sakin yonetimi
- Dairelere mesaj gonderme
- PostgreSQL veritabani
- Docker Compose ile lokal ortam
- Opsiyonel: canli H.264 video ve Ollama destekli raporlama

## Teknolojiler

- Java 17 veya 21
- JavaFX
- Maven
- PostgreSQL
- Docker Compose
- Gson
- JDBC
- VLCJ, opsiyonel video icin

## Ilk Kurulum

```bash
docker compose up -d
mvn javafx:run
```

Backend'i Docker icinde calistirmak icin IntelliJ'de calisan backend varsa once durdurun, sonra:

```bash
docker compose up -d backend-api
```

Backend kodu degistikce Docker'in otomatik rebuild/restart yapmasi icin:

```bash
docker compose watch backend-api
```

Backend API:

- Health: `GET http://localhost:8081/api/health`
- Cihazlar: `GET http://localhost:8081/api/devices`
- Cihaz ekleme: `POST http://localhost:8081/api/devices`
- Varsayilan cihazla kapi acma: `POST http://localhost:8081/api/door/unlock`
- Belirli cihazla kapi acma: `POST http://localhost:8081/api/devices/{deviceId}/door/unlock`

Adminer:

- URL: `http://localhost:8080`
- System: `PostgreSQL`
- Server: `postgres-db`
- Username: `smartgate_user`
- Password: `smartgate_password`
- Database: `smartgate_db`

## Portlar

| Islev | Port | Not |
| --- | ---: | --- |
| Interkom komut portu | 5432 | Cihaza TCP JSON komutlari gonderilir |
| PostgreSQL dis portu | 5433 | Cihaz portu ile cakismamasi icin |
| Adminer | 8080 | Veritabani web arayuzu |
| Video stream | 50556 | H.264 TCP frame alimi |
| Audio stream | 50557 | UDP ses, opsiyonel |

## Takim Calisma Akisi

1. Her ise baslamadan once `main` guncellenir.
2. Her ozellik icin yeni branch acilir.
3. Kod tamamlaninca Pull Request acilir.
4. Takim arkadasi inceleyip onaylamadan `main`'e merge edilmez.

Branch ornekleri:

```bash
git checkout main
git pull origin main
git checkout -b feature/door-unlock
```

Commit ornegi:

```bash
git add .
git commit -m "Add door unlock TCP client"
git push origin feature/door-unlock
```

## 10 Gunluk Plan

| Gun | Hedef |
| --- | --- |
| 1 | Repo, GitHub, Docker, PostgreSQL |
| 2 | JavaFX iskeleti ve DB semasi |
| 3 | TCP client ve JSON model |
| 4 | Handshake ve kapi acma |
| 5 | Alarm dinleme server'i |
| 6 | Daire/sakin yonetimi |
| 7 | Mesajlasma ve log ekranlari |
| 8 | Video stream altyapisi |
| 9 | Raporlama, hata yonetimi, UI cila |
| 10 | Demo, README, test ve teslim |
