# =============================================================================
# ⚠️ ARQUIVO OBSOLETO — NÃO É MAIS UTILIZADO
# =============================================================================
# Este Dockerfile foi criado quando o plano era dockerizar TUDO (banco + backend
# + frontend). A decisão atual é dockerizar APENAS o banco (ver docker-compose.yml
# na raiz). O frontend Angular agora roda localmente via IntelliJ (run config
# "RODAR ANGULAR" = npm run start) ou direto pelo terminal.
#
# Mantido aqui apenas para referência futura — caso queira voltar a dockerizar
# o frontend, este arquivo + o nginx.conf ao lado servem como ponto de partida.
# =============================================================================

# Estágio 1: Build do Angular
FROM node:16.20.2-alpine AS build
WORKDIR /app
# Instala as dependências
COPY package*.json ./
RUN npm install
# Copia o resto do código e faz a build de produção
COPY . .
RUN npm run build -- --configuration=production

# Estágio 2: Servidor Web (Nginx)
FROM nginx:alpine
# Remove a página padrão do Nginx
RUN rm -rf /usr/share/nginx/html/*
# Copia os ficheiros compilados do Angular para o Nginx
COPY --from=build /app/dist/frontend-angular /usr/share/nginx/html
# Substitui a configuração padrão do Nginx pela nossa (necessário para o roteamento SPA)
COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
