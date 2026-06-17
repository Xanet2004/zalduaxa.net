FROM mcr.microsoft.com/playwright:v1.53.0-noble

WORKDIR /app

COPY package.json ./
RUN npm install --omit=dev

COPY src ./src

ENV NODE_ENV=production
ENV QUALITY_RUNNER_PORT=9102

EXPOSE 9102

CMD ["node", "src/server.js"]
