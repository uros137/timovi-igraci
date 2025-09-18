# Timovi i Igrači — Spring Boot + MySQL + Frontend

## Zahtevi
- JDK 17+
- Maven 3.9+
- MySQL/MariaDB

## 1) Kreiraj bazu i korisnika
```sql
CREATE DATABASE timovi_igraci CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER 'appuser'@'localhost' IDENTIFIED BY 'sifra123';
GRANT ALL PRIVILEGES ON timovi_igraci.* TO 'appuser'@'localhost';
FLUSH PRIVILEGES;
```
U `src/main/resources/application.properties` proveri URL/korisnika/lozinku.

## 2) Pokretanje
```bash
mvn spring-boot:run
```
- Aplikacija: `http://localhost:8080/`
- Swagger: `http://localhost:8080/swagger-ui.html`

## 3) API (primeri)
- `GET /api/teams`
- `POST /api/teams` body:
```json
{ "name": "Partizan", "city": "Beograd" }
```
- `GET /api/players?page=0&size=10&position=MF&teamId=1`
- `POST /api/players/team/1` body:
```json
{ "fullName": "Marko Marković", "age": 22, "position": "MF" }
```
- `POST /api/players/5/move/2`

## 4) GitHub push (ukratko)
```bash
git init
git add .
git commit -m "Initial commit"
git branch -M main
git remote add origin https://github.com/<username>/timovi-igraci.git
git push -u origin main
```
