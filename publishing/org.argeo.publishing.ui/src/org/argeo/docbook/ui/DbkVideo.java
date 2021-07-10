package org.argeo.docbook.ui;

import javax.jcr.Item;
import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.ValueFormatException;

import org.argeo.cms.ui.util.CmsUiUtils;
import org.argeo.cms.ui.viewers.NodePart;
import org.argeo.cms.ui.viewers.Section;
import org.argeo.cms.ui.viewers.SectionPart;
import org.argeo.cms.ui.widgets.StyledControl;
import org.argeo.docbook.DbkAttr;
import org.argeo.docbook.DbkType;
import org.argeo.jcr.JcrException;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

public class DbkVideo extends StyledControl implements SectionPart, NodePart {
	private static final long serialVersionUID = -8753232181570351880L;
	private Section section;

	private int width = 640;
	private int height = 360;

	public DbkVideo(Composite parent, int style, Node node) {
		this(Section.findSection(parent), parent, style, node);
	}

	DbkVideo(Section section, Composite parent, int style, Node node) {
		super(parent, style, node);
		this.section = section;
		setStyle(DbkType.videoobject.name());
	}

	@Override
	protected Control createControl(Composite box, String style) {
		Browser browser = new Browser(box, SWT.NONE);
		GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
		gd.widthHint = getWidth();
		gd.heightHint = getHeight();
		browser.setLayoutData(gd);
		return browser;
	}

	public void load(Control control) {
		Browser browser = (Browser) control;
		try {
			getNode().getSession();
			String src = getNode().getNode(DbkType.videoobject.get()).getNode(DbkType.videodata.get())
					.getProperty(DbkAttr.fileref.name()).getString();
			// TODO manage self-hosted videos
			// TODO for YouTube videos, check whether the URL starts with
			// https://www.youtube.com/embed/ and not https://www.youtube.com/watch?v=
			StringBuilder html = new StringBuilder();
			html.append(
					"<iframe frameborder=\"0\" allow=\"autoplay; fullscreen; picture-in-picture\" allowfullscreen=\"true\"");
			// TODO make size configurable
			html.append("width=\"").append(width).append("\" height=\"").append(height).append("\" ");
			html.append("src=\"").append(src).append("\" ");
			html.append("/>");
			browser.setText(html.toString());
		} catch (RepositoryException e) {
			throw new JcrException("Cannot retrieve src for video " + getNode(), e);
		}
	}

	@Override
	protected void setContainerLayoutData(Composite composite) {
		composite.setLayoutData(CmsUiUtils.fillAll());
	}

	@Override
	protected void setControlLayoutData(Control control) {
		control.setLayoutData(CmsUiUtils.fillAll());
	}

	@Override
	public Item getItem() throws RepositoryException {
		return getNode();
	}

	@Override
	public String getPartId() {
		return getNodeId();
	}

	@Override
	public Section getSection() {
		return section;
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

}
