#! /bin/bash

( cd carlabserver && ./bin/rails s -b 0.0.0.0 -p 1234 & )
( cd sandbox/green-gps/linkserver && ./bin/rails s -b 0.0.0.0 -p 8080 & )
( cd sandbox/green-gps/react && npm start & )
