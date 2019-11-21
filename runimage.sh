#! /bin/bash

sudo docker run -dti --rm --name sandbox -p 108:1234 -p 1008:3000 -v `pwd`:/data carlabsandbox && docker exec sandbox bash /data/init.sh
