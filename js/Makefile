include ../sdk.mk

A2_CATEGORY = org.argeo.suite

BUNDLES = \
org.argeo.app.js \

all: npm-ci webpack osgi

webpack:
	npm run build-prod

webpack-dev:
	npm run build

clean:
	$(foreach bundle, $(BUNDLES), rm -rf $(bundle)/org) 

npm-ci:
	npm ci
	
npm-install:
	npm install
	
jsdoc:
	node_modules/.bin/jsdoc -r src \
		-d $(SDK_BUILD_BASE)/jsdoc/argeo-suite-js

include  $(SDK_SRC_BASE)/sdk/argeo-build/osgi.mk
