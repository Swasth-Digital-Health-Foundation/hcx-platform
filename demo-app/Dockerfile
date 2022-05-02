FROM node:alpine
WORKDIR /usr/app
COPY . .
WORKDIR /usr/app/client
RUN npm install && npm run build
WORKDIR /usr/app/server
RUN npm install
CMD ["npm" ,"run", "debug"]
