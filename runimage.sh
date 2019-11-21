#! /bin/bash

sudo docker run -dti --rm --name sandbox -v `pwd`:/data carlabsandbox && docker exec sandbox bash /data/init.sh
