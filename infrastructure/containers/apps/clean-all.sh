#!/bin/bash

#codesets-adapter
if [[ $(sudo docker ps -aqf "name=codesets-adapter") ]]; then
    sudo docker rm -f $(sudo docker ps -aqf "name=codesets-adapter")
fi
if [[ $(sudo docker images codesets-adapter -aq) ]]; then
   sudo docker rmi -f $(sudo docker images codesets-adapter -aq)
fi

#codesets-adapter-db
if [[ $(sudo docker ps -aqf "name=codesets-adapter-db") ]]; then
    sudo docker rm -f $(sudo docker ps -aqf "name=codesets-adapter-db")
fi
if [[ $(sudo docker images codesets-adapter-db -aq) ]]; then
   sudo docker rmi -f $(sudo docker images codesets-adapter-db -aq)
fi




