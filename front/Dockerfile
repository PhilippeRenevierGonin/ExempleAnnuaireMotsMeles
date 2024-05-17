FROM node:20

RUN mkdir /usr/src/app
WORKDIR /usr/src/app
RUN cd /usr/src/app

COPY package*.json ./
COPY server.js ./
COPY public ./public/
COPY front.sh ./

RUN chmod u+x ./front.sh
RUN npm install
# RUN bash -c "echo $URL_GENERATEUR > ./public/url.txt"
# RUN bash -c "printenv >> ./public/url.txt"

# Définir la commande pour démarrer l'application
ENTRYPOINT /usr/src/app/front.sh  $URL_GENERATEUR