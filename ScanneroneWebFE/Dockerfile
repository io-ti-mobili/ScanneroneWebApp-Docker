
FROM node:22
WORKDIR /app

# Installa le dipendenze
COPY package*.json ./
RUN npm install

# Copia il resto del codice
COPY . .

# Espone la porta di default di Angular
EXPOSE 4200

# Avvia il server di sviluppo bindando su tutti gli IP del container
CMD ["npx", "ng", "serve", "--host", "0.0.0.0"]
