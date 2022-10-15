include sdk.mk
.PHONY: clean all osgi

all: osgi

A2_CATEGORY = org.argeo.suite

BUNDLES = \
org.argeo.app.api \
org.argeo.app.core \
org.argeo.app.servlet.odk \
org.argeo.app.servlet.publish \
org.argeo.app.swt \
org.argeo.app.ui \
org.argeo.app.theme.default \
org.argeo.app.profile.acr.fs \
org.argeo.app.profile.acr.jcr \

A2_OUTPUT = $(SDK_BUILD_BASE)/a2
A2_BASE = $(A2_OUTPUT)

DEP_CATEGORIES = \
org.argeo.tp \
org.argeo.tp.apache \
org.argeo.tp.jetty \
osgi/api/org.argeo.tp.osgi \
osgi/equinox/org.argeo.tp.eclipse \
swt/rap/org.argeo.tp.swt \
swt/rap/org.argeo.tp.swt.workbench \
org.argeo.tp.jcr \
org.argeo.tp.formats \
org.argeo.tp.gis \
org.argeo.cms \
org.argeo.cms.jcr \
swt/org.argeo.cms \
swt/rap/org.argeo.cms \

clean:
	rm -rf $(BUILD_BASE)

include  $(SDK_SRC_BASE)/sdk/argeo-build/osgi.mk