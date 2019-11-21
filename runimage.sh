#! /bin/bash

sudo docker run -dti --rm --name sandbox -p 9292:1234 -p 9000:3000 -v `pwd`:/data carlabsandbox && docker exec sandbox bash /data/init.sh
