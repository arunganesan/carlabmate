PROJECT ?= obstacle-detection

runreact:
	( cd sandbox/$(PROJECT)/react && npm start & )

runrails:
	( cd carlabserver && ./bin/rails s -b 0.0.0.0 -p 1234 & )
	( cd sandbox/$(PROJECT)/linkserver && ./bin/rails s -b 0.0.0.0 -p 8080 & )

runpy:
	( cd sandbox/$(PROJECT)/python && ./packaged.py & )

build:
	( cd library/react/libcarlab && npm run build )
	( cd library/react/user-input && npm run build )


kill:
	pkill -9 -f 'rb-fsevent|rails|spring|puma|node|python3.7'
