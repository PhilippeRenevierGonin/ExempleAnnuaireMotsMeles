#! /bin/bash
sleep 40s
docker exec exempleannuairemotsmeles_ws_1 bash -c "/stopjava.sh"
sleep 20s
docker exec exempleannuairemotsmeles_ws_2 bash -c "/stopjava.sh"
sleep 20s
docker exec exempleannuairemotsmeles_ws_3 bash -c "/stopjava.sh"
sleep 20s
docker-compose stop