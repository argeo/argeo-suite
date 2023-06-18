package org.argeo.app.swt.docbook;

import static org.argeo.app.docbook.DbkAcrUtils.isDbk;
import static org.argeo.app.docbook.DbkType.para;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.argeo.api.acr.Content;
import org.argeo.api.cms.ux.Cms2DSize;
import org.argeo.api.cms.ux.CmsEditable;
import org.argeo.app.docbook.DbkAttr;
import org.argeo.app.docbook.DbkType;
import org.argeo.cms.swt.CmsSwtUtils;
import org.argeo.cms.swt.SwtEditablePart;
import org.argeo.cms.swt.acr.AbstractPageViewer;
import org.argeo.cms.swt.acr.SwtSection;
import org.argeo.cms.swt.acr.SwtSectionPart;
import org.argeo.cms.swt.widgets.EditableText;
import org.argeo.cms.swt.widgets.StyledControl;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/** Displays DocBook content. */
public class DocBookViewer extends AbstractPageViewer {

	private TextInterpreter textInterpreter = new DbkTextInterpreter();
	private DbkImageManager imageManager;

	private TextSection mainSection;

	private boolean showMainTitle = true;

	private Integer maxMediaWidth = null;
	private String defaultSectionStyle;

	public DocBookViewer(Composite parent, int style, Content item, CmsEditable cmsEditable) {
		super(parent, style, cmsEditable);
		imageManager = new DbkImageManager(item);

		for (Content child : item) {
			if (child.hasContentClass(DbkType.article)) {
				if (mainSection != null)
					throw new IllegalStateException("Main section already created");
				mainSection = new TextSection(parent, 0, child);
				mainSection.setLayoutData(CmsSwtUtils.fillAll());
			}
		}
	}

	@Override
	protected void refresh(Control control) {
		if (!(control instanceof SwtSection))
			return;
//		long begin = System.currentTimeMillis();
		SwtSection section = (SwtSection) control;
		if (section instanceof TextSection) {
			CmsSwtUtils.clear(mainSection);
			refreshTextSection(mainSection);

		}
//		long duration = System.currentTimeMillis() - begin;
//		System.out.println(duration + " ms - " + DbkUtils.getTitle(section.getNode()));

	}

	protected void refreshTextSection(TextSection section) {
		Content sectionContent = section.getContent();
		// Style
		Optional<String> roleAttr = sectionContent.get(DbkAttr.role, String.class);
		String style = roleAttr.orElse(section.getDefaultTextStyle());
		if (style != null)
			CmsSwtUtils.style(section, style);

		// Title
		Optional<Content> titleContent = sectionContent.soleChild(DbkType.title.qName());

		if (titleContent.isPresent()) {
			boolean showTitle = getMainSection() == section ? showMainTitle : true;
			if (showTitle) {
				if (section.getHeader() == null)
					section.createHeader();
				DbkSectionTitle title = newSectionTitle(section, titleContent.get());
				title.setLayoutData(CmsSwtUtils.fillWidth());
				updateContent(title);
			}
		}

		boolean processingSubSections = false;
		for (Content child : section.getContent()) {
			if (child.hasContentClass(DbkType.section)) {
				processingSubSections = true;
				TextSection childSection = newTextSection(section, child); // new TextSection(section, 0, child);
				childSection.setLayoutData(CmsSwtUtils.fillWidth());
				refreshTextSection(childSection);
			} else {
				if (processingSubSections)
					throw new IllegalStateException(child + " is below a subsection");
				SwtSectionPart sectionPart = null;
				if (child.hasContentClass(DbkType.para)) {
					sectionPart = newParagraph(section, child);
				} else if (child.hasContentClass(DbkType.mediaobject)) {
					if (child.hasChild(DbkType.imageobject)) {
						sectionPart = newImg(section, child);
					} else if (child.hasChild(DbkType.videoobject)) {
						sectionPart = newVideo(section, child);
					} else {
						throw new IllegalArgumentException("Unsupported media object " + child);
					}
				} else if (isDbk(child, DbkType.info)) {
					// TODO enrich UI based on info
				} else if (isDbk(child, DbkType.title)) {
					// already managed
					// TODO check that it is first?
				} else {
					throw new IllegalArgumentException("Unsupported type for " + child);
				}
				if (sectionPart != null && sectionPart instanceof Control)
					((Control) sectionPart).setLayoutData(CmsSwtUtils.fillWidth());
			}
		}
	}

	protected void updateContent(SwtEditablePart part) {
		if (part instanceof SwtSectionPart) {
			SwtSectionPart sectionPart = (SwtSectionPart) part;
			Content partContent = sectionPart.getContent();

			if (part instanceof StyledControl && (sectionPart.getSection() instanceof TextSection)) {
				TextSection section = (TextSection) sectionPart.getSection();
				StyledControl styledControl = (StyledControl) part;
				if (isDbk(partContent, para)) {
					Optional<String> roleAttr = partContent.get(DbkAttr.role.qName(), String.class);
					String style = roleAttr.orElse(section.getDefaultTextStyle());
					styledControl.setStyle(style);
				}
			}
			// use control AFTER setting style, since it may have been reset

			if (part instanceof EditableText) {
				EditableText paragraph = (EditableText) part;
				if (paragraph == getEdited())
					paragraph.setText(textInterpreter.raw(partContent));
				else
					paragraph.setText(textInterpreter.readSimpleHtml(partContent));
				// paragraph.setText(textInterpreter.readSimpleHtml(partContent));

			} else if (part instanceof DbkImg) {
				DbkImg editableImage = (DbkImg) part;
				imageManager.load(partContent, part.getControl(), editableImage.getPreferredImageSize());
			} else if (part instanceof DbkVideo) {
				DbkVideo video = (DbkVideo) part;
				video.load(part.getControl());
			}
		} else if (part instanceof DbkSectionTitle) {
			DbkSectionTitle title = (DbkSectionTitle) part;
			title.setStyle(title.getSection().getTitleStyle());
			// use control AFTER setting style
			if (title == getEdited())
				title.setText(textInterpreter.read(title.getContent()));
			else
				title.setText(textInterpreter.readSimpleHtml(title.getContent()));
		}
	}

	/** To be overridden in order to provide additional SectionPart types */
	protected TextSection newTextSection(SwtSection section, Content node) {
		return new TextSection(section, SWT.NONE, node);
	}

	protected Paragraph newParagraph(TextSection parent, Content node) {
		Paragraph paragraph = new Paragraph(parent, parent.getStyle(), node);
		updateContent(paragraph);
		paragraph.setLayoutData(CmsSwtUtils.fillWidth());
		paragraph.setMouseListener(getMouseListener());
		paragraph.setFocusListener(getFocusListener());
		return paragraph;
	}

	protected DbkSectionTitle newSectionTitle(TextSection parent, Content titleNode) {
		int style = parent.getStyle();
		Composite titleParent = newSectionHeader(parent);
		if (parent.isTitleReadOnly())
			style = style | SWT.READ_ONLY;
		DbkSectionTitle title = new DbkSectionTitle(titleParent, style, titleNode);
		updateContent(title);
		title.setMouseListener(getMouseListener());
		title.setFocusListener(getFocusListener());
		return title;
	}

	protected DbkImg newImg(TextSection parent, Content node) {
		DbkImg img = new DbkImg(parent, parent.getStyle(), node, imageManager);
		GridData imgGd;
		if (maxMediaWidth != null) {
			imgGd = new GridData(SWT.CENTER, SWT.FILL, false, false);
			imgGd.widthHint = maxMediaWidth;
			img.setPreferredSize(new Cms2DSize(maxMediaWidth, 0));
		} else {
			imgGd = CmsSwtUtils.grabWidth(SWT.CENTER, SWT.DEFAULT);
		}
		img.setLayoutData(imgGd);
		updateContent(img);
		img.setMouseListener(getMouseListener());
		img.setFocusListener(getFocusListener());
		return img;
	}

	protected DbkVideo newVideo(TextSection parent, Content node) {
		DbkVideo video = new DbkVideo(parent, getCmsEditable().canEdit() ? SWT.NONE : SWT.READ_ONLY, node);
		GridData gd;
		if (maxMediaWidth != null) {
			gd = new GridData(SWT.CENTER, SWT.FILL, false, false);
			// TODO, manage size
//				gd.widthHint = maxMediaWidth;
//				gd.heightHint = (int) (gd.heightHint * 0.5625);
		} else {
			gd = new GridData(SWT.CENTER, SWT.FILL, false, false);
//				gd.widthHint = video.getWidth();
//				gd.heightHint = video.getHeight();
		}
		video.setLayoutData(gd);
		updateContent(video);
		return video;
	}

	/**
	 * To be overridden in order to provide additional processing at the section
	 * level.
	 * 
	 * @return the parent to use for the {@link DbkSectionTitle}, by default
	 *         {@link SwtSection#getHeader()}
	 */
	protected Composite newSectionHeader(TextSection section) {
		return section.getHeader();
	}

	protected List<String> getAvailableStyles(SwtEditablePart editablePart) {
		return new ArrayList<>();
	}

	public TextSection getMainSection() {
		return mainSection;
	}

	public void setShowMainTitle(boolean showMainTitle) {
		this.showMainTitle = showMainTitle;
	}

	public String getDefaultSectionStyle() {
		return defaultSectionStyle;
	}

	public void setDefaultSectionStyle(String defaultSectionStyle) {
		this.defaultSectionStyle = defaultSectionStyle;
	}

	public void setMaxMediaWidth(Integer maxMediaWidth) {
		this.maxMediaWidth = maxMediaWidth;
	}

	@Override
	public Control getControl() {
		return mainSection;
	}

}
