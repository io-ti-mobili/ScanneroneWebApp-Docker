# Scannerone Web App

Scannerone Web App è una soluzione full-stack dockerizzata. L'architettura è suddivisa in un backend robusto basato su Java (Spring Boot) e un frontend moderno e reattivo sviluppato in Angular.

Il progetto è interamente gestito tramite Docker Compose per garantire un ambiente di sviluppo e rilascio coerente e pronto all'uso.

## 🏗 Architettura del Progetto

La repository è divisa nei seguenti componenti principali:

- **`ScanneroneWebFE/`**: Frontend applicativo sviluppato in Angular.
- **`ScanneroneWebBE/`**: Backend sviluppato in Java con Spring Boot.
- **`docker-compose.yml`**: Configurazione di orchestrazione per avviare l'intero stack (Frontend, Backend, Database).
- **`manage.sh`**: Script bash di utilità per semplificare le operazioni di deploy e rimozione.

> **📱 App Mobile:** L'app Android *Scannerone* collegata a questo ecosistema è disponibile alla repository [io-ti-mobili/Scannerone](https://github.com/io-ti-mobili/Scannerone).

---

## 📸 Screenshots

Di seguito alcune immagini dell'interfaccia dell'applicazione:

### Dashboard
![Dashboard](imgs/dashboard.png)

### Dashboard - Vista Dettagliata
![Dashboard 2](imgs/dashboard-2.png)

### Leaderboard
![Leaderboard](imgs/leaderboard.png)

---

## 🖥 Frontend (`ScanneroneWebFE`)

Il frontend offre l'interfaccia utente dell'applicazione, sfruttando tecnologie moderne per assicurare performance e un'esperienza utente fluida.

**Stack Tecnologico:**
- **Framework:** Angular 21
- **UI Library:** PrimeNG (con PrimeFlex e PrimeIcons) per componenti grafici avanzati e design responsivo.
- **Data Visualization:** Chart.js per la rappresentazione dei grafici.
- **Gestione Dati & Routing:** RxJS e moduli nativi Angular.
- **Internazionalizzazione:** ngx-translate.

**Accesso:** Una volta avviato, il frontend è esposto e raggiungibile all'indirizzo `http://localhost:4200`.

---

## ⚙️ Backend (`ScanneroneWebBE`)

Il backend espone le API REST consumate dal frontend, occupandosi della logica di business e della persistenza dei dati.

**Stack Tecnologico:**
- **Linguaggio:** Java 17
- **Framework:** Spring Boot (incluso Spring Web, Spring Data JPA, Spring Validation)
- **Database Connection:** PostgreSQL Driver
- **Tooling:** Lombok (per ridurre il codice boilerplate).

**Accesso:** Il backend è in ascolto sulla porta `8080` ed è accessibile via `http://localhost:8080`.

---

## 🗄 Database

L'applicazione utilizza **PostgreSQL 17** come base dati relazionale.
- Il database è denominato `scanneronedb` (credenziali default definite nel compose).
- I dati sono persistiti in modo sicuro sul volume locale Docker chiamato `db_data`.
- **Porta esposta:** `5432`.

---

## 🚀 Avvio Rapido

L'intero ambiente è containerizzato e non richiede installazioni locali di Java, Node o Postgres. Puoi gestire il ciclo di vita dell'applicazione utilizzando lo script `manage.sh`.

### Prerequisiti
- **Docker** e **Docker Compose** installati sul sistema.
- Assicurarsi che le porte `8080`, `4200` e `5432` siano libere.

### Comandi di Utilizzo (via `manage.sh`)

1. **Avvio e Deploy dell'applicazione:**
   Il comando effettua la build da zero delle immagini e avvia i container in background.
   ```bash
   ./manage.sh deploy
   ```

2. **Arresto e Rimozione totale:**
   Il comando ferma i container e rimuove i volumi associati (ATTENZIONE: **elimina i dati del database**).
   ```bash
   ./manage.sh remove
   ```

### Comandi Standard (via Docker Compose)

Se preferisci non usare lo script o vuoi semplicemente spegnere senza perdere i dati nel database:

- **Per avviare (mantenendo i dati preesistenti):**
  ```bash
  docker compose up -d
  ```
- **Per spegnere i container senza cancellare i volumi:**
  ```bash
  docker compose down
  ```

---

## 🔗 Rete Interna

Tutti i servizi comunicano internamente tramite una Docker Network dedicata di tipo bridge (`scannerone-network`). 
- Il backend si connette automaticamente al database utilizzando l'hostname del container (`scannerone-db`).
- Il frontend comunica col backend in dipendenza reciproca come descritto nel `docker-compose.yml`.

