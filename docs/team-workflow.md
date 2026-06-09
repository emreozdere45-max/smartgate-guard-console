# Takim Calisma Rehberi

## Roller

- Takim lideri GitHub reposunu acar ve takim arkadasini collaborator olarak ekler.
- Her iki kisi de farkli branch'lerde calisir.
- Pull Request acilmadan `main` branch'ine kod girmez.

## Branch Isimleri

- `feature/docker-setup`
- `feature/javafx-dashboard`
- `feature/door-unlock`
- `feature/alarm-listener`
- `feature/resident-management`
- `feature/messaging`
- `feature/video-stream`
- `feature/reporting`

## Gunluk Rutin

```bash
git checkout main
git pull origin main
git checkout -b feature/kisa-ozellik-adi
```

Is bitince:

```bash
git status
git add .
git commit -m "Add short feature summary"
git push origin feature/kisa-ozellik-adi
```

Sonra GitHub uzerinden Pull Request acilir.

## Commit Mesaji Ornekleri

- `Add PostgreSQL schema`
- `Add intercom TCP client`
- `Add alarm listener skeleton`
- `Add resident management screen`
- `Fix door unlock JSON payload`

