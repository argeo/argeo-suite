include sdk.mk
.PHONY: clean all osgi

all: osgi

A2_CATEGORY = org.argeo.suite

BUNDLES = \
org.argeo.app.api \
org.argeo.app.core \
org.argeo.app.servlet.odk \
org.argeo.app.servlet.publish \
org.argeo.app.ui \
org.argeo.app.theme.default \
org.argeo.app.ui.rap \

A2_OUTPUT = $(SDK_BUILD_BASE)/a2
A2_BASE = $(A2_OUTPUT)

DEP_CATEGORIES = \
org.argeo.tp \
org.argeo.tp.apache \
org.argeo.tp.jetty \
org.argeo.tp.eclipse.equinox \
org.argeo.tp.eclipse.rap \
org.argeo.tp.jcr \
org.argeo.cms \
org.argeo.cms.eclipse.rap \

clean:
	rm -rf $(BUILD_BASE)

include  $(SDK_SRC_BASE)/sdk/argeo-build/osgi.mk