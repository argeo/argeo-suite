include sdk.mk
.PHONY: clean all osgi

all: osgi
	
install: osgi-install

uninstall: osgi-uninstall

A2_CATEGORY = org.argeo.suite

BUNDLES = \
org.argeo.app.api \
org.argeo.app.core \
org.argeo.app.jcr \
org.argeo.app.servlet.odk \
org.argeo.app.servlet.publish \
org.argeo.app.theme.default \
org.argeo.app.profile.acr.fs \
org.argeo.app.profile.acr.jcr \
swt/org.argeo.app.swt \
swt/org.argeo.app.ui \
org.argeo.suite.knowledge \

DEP_CATEGORIES = \
org.argeo.tp \
org.argeo.tp.httpd \
org.argeo.tp.jcr \
org.argeo.tp.utils \
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

include  $(SDK_SRC_BASE)/sdk/argeo-build/osgi.mk