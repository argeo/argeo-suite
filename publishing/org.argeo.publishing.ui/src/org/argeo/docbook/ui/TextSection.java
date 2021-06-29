package org.argeo.docbook.ui;

import javax.jcr.Node;

import org.argeo.cms.ui.util.CmsUiUtils;
import org.argeo.cms.ui.viewers.EditablePart;
import org.argeo.cms.ui.viewers.Section;
import org.argeo.cms.ui.widgets.TextStyles;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/** An editable section. */
public class TextSection extends Section {
	private static final long serialVersionUID = -8625209546243220689L;
	private String defaultTextStyle = TextStyles.TEXT_DEFAULT;
	private String titleStyle;

	private final boolean flat;

	private boolean titleReadOnly = false;

	private final int level;

	public TextSection(Composite parent, int style, Node node) {
		this(parent, findSection(parent), style, node);
	}

	public TextSection(TextSection section, int style, Node node) {
		this(section, section.getParentSection(), style, node);
	}

	private TextSection(Composite parent, Section parentSection, int style, Node node) {
		super(parent, parentSection, style, node);
		flat = SWT.FLAT == (style & SWT.FLAT);
		if (parentSection instanceof TextSection) {
			level = ((TextSection) parentSection).getLevel() + 1;
		} else {
			level = 0;
		}
		// CmsUiUtils.style(this, TextStyles.TEXT_SECTION);
	}

	public String getDefaultTextStyle() {
		return defaultTextStyle;
	}

	public boolean isFlat() {
		return flat;
	}

	/** The level of this section, similar to h1, h2, etc. in HTML. */
	public int getLevel() {
		return level;
	}

	public String getTitleStyle() {
		if (titleStyle != null)
			return titleStyle;
		// TODO make base H styles configurable
//		Integer relativeDepth = getRelativeDepth();
//		System.out.println("Level: "+getLevel());
		return getLevel() == 0 ? TextStyles.TEXT_TITLE : "h" + getLevel();
	}

	public void setDefaultTextStyle(String defaultTextStyle) {
		this.defaultTextStyle = defaultTextStyle;
	}

	public void setTitleStyle(String titleStyle) {
		this.titleStyle = titleStyle;
	}

	public boolean isTitleReadOnly() {
		return titleReadOnly;
	}

	public void setTitleReadOnly(boolean titleReadOnly) {
		this.titleReadOnly = titleReadOnly;
	}
}
