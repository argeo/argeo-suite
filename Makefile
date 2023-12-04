include sdk.mk
.PHONY: clean all osgi web

all: web osgi
	
install: osgi-install

uninstall: osgi-uninstall

A2_CATEGORY = org.argeo.suite

BUNDLES = \
org.argeo.app.api \
org.argeo.app.core \
org.argeo.app.jcr \
org.argeo.app.geo \
org.argeo.app.servlet.odk \
org.argeo.app.servlet.publish \
org.argeo.app.theme.default \
org.argeo.app.profile.acr.fs \
org.argeo.app.profile.acr.jcr \
org.argeo.suite.knowledge \
swt/org.argeo.app.swt \
swt/org.argeo.app.geo.swt \
swt/org.argeo.app.ui \

DEP_CATEGORIES = \
org.argeo.tp \
org.argeo.tp.httpd \
org.argeo.tp.sys \
org.argeo.tp.sdk \
org.argeo.tp.jcr \
org.argeo.tp.utils \
org.argeo.tp.img \
org.argeo.tp.publish \
org.argeo.tp.math \
org.argeo.tp.earth \
osgi/equinox/org.argeo.tp.osgi \
osgi/equinox/org.argeo.tp.eclipse \
swt/rap/org.argeo.tp.swt \
swt/rap/org.argeo.tp.swt.workbench \
org.argeo.cms \
org.argeo.cms.jcr \
swt/org.argeo.cms \
swt/org.argeo.cms.jcr \
swt/rap/org.argeo.cms \

clean:
	rm -rf $(BUILD_BASE)
	make -C js clean

native-deps-debian:
	sudo apt install npm

## WEB
web:
	make -C js all

include  $(SDK_SRC_BASE)/sdk/argeo-build/osgi.mk
