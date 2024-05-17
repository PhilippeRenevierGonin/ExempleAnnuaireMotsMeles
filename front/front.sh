#! /bin/bash
cd /usr/src/app
echo $1
echo $1 > ./public/url.txt
node server.js