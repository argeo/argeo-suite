package org.argeo.app.swt.docbook;

import org.argeo.api.acr.Content;
import org.argeo.app.docbook.DbkType;
import org.argeo.cms.swt.CmsSwtUtils;
import org.argeo.cms.swt.acr.SwtSection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

/** An editable section. */
public class TextSection extends SwtSection {
	private static final long serialVersionUID = -8625209546243220689L;
	private String defaultTextStyle = DbkType.para.name();
	private String titleStyle;

	private final boolean flat;

	private boolean titleReadOnly = false;

	private final int level;

	public TextSection(Composite parent, int style, Content node) {
		this(parent, findSection(parent), style, node);
	}

	public TextSection(TextSection section, int style, Content node) {
		this(section, section.getParentSection(), style, node);
	}

	private TextSection(Composite parent, SwtSection parentSection, int style, Content node) {
		super(parent, parentSection, style, node);
		flat = SWT.FLAT == (style & SWT.FLAT);
		if (parentSection instanceof TextSection) {
			level = ((TextSection) parentSection).getLevel() + 1;
		} else {
			level = 0;
		}
		CmsSwtUtils.style(this, DbkType.section.name());
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
//		System.out.println("Level: " + getLevel());
		return "h" + (getLevel() + 1);
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
