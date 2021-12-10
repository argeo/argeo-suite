package org.argeo.docbook.ui;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;

import javax.jcr.Item;
import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.argeo.cms.swt.CmsSwtUtils;
import org.argeo.cms.swt.Selected;
import org.argeo.cms.ui.viewers.NodePart;
import org.argeo.cms.ui.viewers.Section;
import org.argeo.cms.ui.viewers.SectionPart;
import org.argeo.cms.ui.widgets.StyledControl;
import org.argeo.docbook.DbkAttr;
import org.argeo.docbook.DbkType;
import org.argeo.docbook.DbkUtils;
import org.argeo.jcr.Jcr;
import org.argeo.jcr.JcrException;
import org.argeo.naming.NamingUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;

public class DbkVideo extends StyledControl implements SectionPart, NodePart {
	private static final long serialVersionUID = -8753232181570351880L;
	private Section section;

	private int width = 640;
	private int height = 360;

	private boolean editable;

	public DbkVideo(Composite parent, int style, Node node) {
		this(Section.findSection(parent), parent, style, node);
	}

	DbkVideo(Section section, Composite parent, int style, Node node) {
		super(parent, style, node);
		editable = !(SWT.READ_ONLY == (style & SWT.READ_ONLY));
		this.section = section;
		setStyle(DbkType.videoobject.name());
	}

	@Override
	protected Control createControl(Composite box, String style) {
		Node mediaobject = getNode();
		Composite wrapper = new Composite(box, SWT.NONE);
		wrapper.setLayout(CmsSwtUtils.noSpaceGridLayout());

		Composite browserC = new Composite(wrapper, SWT.NONE);
		browserC.setLayout(CmsSwtUtils.noSpaceGridLayout());
		GridData gd = new GridData(SWT.CENTER, SWT.FILL, true, true);
		gd.widthHint = getWidth();
		gd.heightHint = getHeight();
		browserC.setLayoutData(gd);
//		wrapper.setLayoutData(CmsUiUtils.fillAll());
		Browser browser = new Browser(browserC, SWT.NONE);

		if (editable) {
			Composite editor = new Composite(wrapper, SWT.BORDER);
			editor.setLayout(new GridLayout(3, false));
			editor.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
			String fileref = DbkUtils.getMediaFileref(mediaobject);
			Text text = new Text(editor, SWT.SINGLE);
			if (fileref != null)
				text.setText(fileref);
			else
				text.setMessage("Embed URL of the video");
			text.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
			Button updateB = new Button(editor, SWT.FLAT);
			updateB.setText("Update");
			updateB.addSelectionListener(new Selected() {

				@Override
				public void widgetSelected(SelectionEvent e) {
					try {
						Node videodata = mediaobject.getNode(DbkType.videoobject.get())
								.getNode(DbkType.videodata.get());
						String txt = text.getText();
						URI uri;
						try {
							uri = new URI(txt);
						} catch (URISyntaxException e1) {
							text.setText("");
							text.setMessage("Invalid URL");
							return;
						}

						// Transform watch URL in embed
						// YouTube
						String videoId = null;
						if ("www.youtube.com".equals(uri.getHost()) || "youtube.com".equals(uri.getHost())
								|| "youtu.be".equals(uri.getHost())) {
							if ("www.youtube.com".equals(uri.getHost()) || "youtube.com".equals(uri.getHost())) {
								if ("/watch".equals(uri.getPath())) {
									Map<String, List<String>> map = NamingUtils.queryToMap(uri);
									videoId = map.get("v").get(0);
								}
							} else if ("youtu.be".equals(uri.getHost())) {
								videoId = uri.getPath().substring(1);
							}
							if (videoId != null) {
								try {
									uri = new URI("https://www.youtube.com/embed/" + videoId);
									text.setText(uri.toString());
								} catch (URISyntaxException e1) {
									throw new IllegalStateException(e1);
								}
							}
						}

						// Vimeo
						if ("vimeo.com".equals(uri.getHost())) {
							videoId = uri.getPath().substring(1);
							if (videoId != null) {
								try {
									uri = new URI("https://player.vimeo.com/video/" + videoId);
									text.setText(uri.toString());
								} catch (URISyntaxException e1) {
									throw new IllegalStateException(e1);
								}
							}
						}

						videodata.setProperty(DbkAttr.fileref.name(), uri.toString());
						// TODO better integrate it in the edition lifecycle
						videodata.getSession().save();
						load(browser);
					} catch (RepositoryException e1) {
						throw new JcrException("Cannot update " + mediaobject, e1);
					}

				}
			});

			Button deleteB = new Button(editor, SWT.FLAT);
			deleteB.setText("Delete");
			deleteB.addSelectionListener(new Selected() {

				@Override
				public void widgetSelected(SelectionEvent e) {
					try {
						mediaobject.remove();
						mediaobject.getSession().save();
						dispose();
						getSection().getParent().layout(true, true);
					} catch (RepositoryException e1) {
						throw new JcrException("Cannot update " + mediaobject, e1);
					}

				}
			});
		}

		// TODO caption
		return browser;
	}

	public void load(Control control) {
		try {
			if (control instanceof Browser) {
				Browser browser = (Browser) control;
				getNode().getSession();
				String fileref = DbkUtils.getMediaFileref(getNode());
				if (fileref != null) {
					// TODO manage self-hosted videos
					// TODO for YouTube videos, check whether the URL starts with
					// https://www.youtube.com/embed/ and not https://www.youtube.com/watch?v=
					StringBuilder html = new StringBuilder();
					html.append(
							"<iframe frameborder=\"0\" allow=\"autoplay; fullscreen; picture-in-picture\" allowfullscreen=\"true\"");
					// TODO make size configurable
					html.append("width=\"").append(width).append("\" height=\"").append(height).append("\" ");
					html.append("src=\"").append(fileref).append("\" ");
					html.append("/>");
					browser.setText(html.toString());
				}
			}
		} catch (RepositoryException e) {
			throw new JcrException("Cannot retrieve src for video " + getNode(), e);
		}
	}

	@Override
	protected void setContainerLayoutData(Composite composite) {
		composite.setLayoutData(new GridData(SWT.CENTER, SWT.FILL, true, true));
	}

	@Override
	protected void setControlLayoutData(Control control) {
		control.setLayoutData(CmsSwtUtils.fillAll());
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
