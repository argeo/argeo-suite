include ../sdk.mk

A2_CATEGORY = org.argeo.suite

BUNDLES = \
org.argeo.app.geo.js \

all: webpack osgi

webpack:
	npm run build-prod

webpack-dev:
	npm run build

clean:
	$(foreach bundle, $(BUNDLES), rm -rf $(bundle)/org) 

npm-install:
	cd org.argeo.app.geo.js && npm install
	
jsdoc:
	org.argeo.app.geo.js/node_modules/.bin/jsdoc -r org.argeo.app.geo.js/src/org.argeo.app.geo.js \
		-d $(SDK_BUILD_BASE)/jsdoc/org.argeo.app.geo.js

include  $(SDK_SRC_BASE)/sdk/argeo-build/osgi.mk
