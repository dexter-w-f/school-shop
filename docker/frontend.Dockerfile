 # === 构建阶段 ===
 FROM node:20-alpine AS builder
 WORKDIR /app
 
 COPY 校园小卖部/code2026/vue/package*.json ./
 RUN npm ci
 
 COPY 校园小卖部/code2026/vue/ .
 RUN npm run build
 
 # === 运行阶段 ===
 FROM nginx:alpine
 COPY --from=builder /app/dist /usr/share/nginx/html
 COPY docker/nginx.conf /etc/nginx/conf.d/default.conf
 
 EXPOSE 80
 CMD ["nginx", "-g", "daemon off;"]
