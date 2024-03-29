package org.argeo.app.ui.docbook;

import static org.argeo.app.docbook.DbkType.para;
import static org.argeo.app.jcr.docbook.DbkJcrUtils.addDbk;
import static org.argeo.app.jcr.docbook.DbkJcrUtils.isDbk;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Observer;

import javax.jcr.Item;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.argeo.api.cms.CmsLog;
import org.argeo.api.cms.ux.Cms2DSize;
import org.argeo.api.cms.ux.CmsEditable;
import org.argeo.app.docbook.DbkAttr;
import org.argeo.app.docbook.DbkType;
import org.argeo.app.jcr.docbook.DbkJcrUtils;
import org.argeo.cms.swt.CmsSwtUtils;
import org.argeo.cms.swt.SwtEditablePart;
import org.argeo.cms.ui.viewers.AbstractPageViewer;
import org.argeo.cms.ui.viewers.NodePart;
import org.argeo.cms.ui.viewers.PropertyPart;
import org.argeo.cms.ui.viewers.Section;
import org.argeo.cms.ui.viewers.SectionPart;
import org.argeo.cms.ui.widgets.EditableText;
import org.argeo.cms.ui.widgets.StyledControl;
import org.argeo.jcr.Jcr;
import org.argeo.jcr.JcrException;
import org.argeo.jcr.JcrUtils;
import org.eclipse.rap.fileupload.FileDetails;
import org.eclipse.rap.fileupload.FileUploadEvent;
import org.eclipse.rap.fileupload.FileUploadHandler;
import org.eclipse.rap.fileupload.FileUploadListener;
import org.eclipse.rap.rwt.RWT;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

/** Base class for text viewers and editors. */
public abstract class AbstractDbkViewer extends AbstractPageViewer implements KeyListener, Observer {
	private static final long serialVersionUID = -2401274679492339668L;
	private final static CmsLog log = CmsLog.getLog(AbstractDbkViewer.class);

	private final Section mainSection;

	private TextInterpreter textInterpreter = new DbkTextInterpreter();
	private DbkImageManager imageManager;

	private FileUploadListener fileUploadListener;
	private DbkContextMenu styledTools;

	private final boolean flat;

	private boolean showMainTitle = true;

	private Integer maxMediaWidth = null;
	private String defaultSectionStyle;

	protected AbstractDbkViewer(Section parent, int style, CmsEditable cmsEditable) {
		super(parent, style, cmsEditable);
//		CmsView cmsView = CmsView.getCmsView(parent);
//		imageManager = cmsView.getImageManager();
		flat = SWT.FLAT == (style & SWT.FLAT);

		if (getCmsEditable().canEdit()) {
			fileUploadListener = new FUL();
			styledTools = new DbkContextMenu(this, parent.getShell());
		}
		this.mainSection = parent;
		Node baseFolder = Jcr.getParent(mainSection.getNode());
		imageManager = new DbkImageManager(baseFolder);
		initModelIfNeeded(mainSection.getNode());
		// layout(this.mainSection);
	}

	@Override
	public Control getControl() {
		return mainSection;
	}

	protected void refresh(Control control) throws RepositoryException {
		if (!(control instanceof Section))
			return;
		long begin = System.currentTimeMillis();
		Section section = (Section) control;
		if (section instanceof TextSection) {
			CmsSwtUtils.clear(section);
			Node node = section.getNode();
			TextSection textSection = (TextSection) section;
			String style = node.hasProperty(DbkAttr.role.name()) ? node.getProperty(DbkAttr.role.name()).getString()
					: getDefaultSectionStyle();
			if (style != null)
				CmsSwtUtils.style(textSection, style);

			// Title
			Node titleNode = null;
			// We give priority to ./title vs ./info/title, like the DocBook XSL
			if (node.hasNode(DbkType.title.get())) {
				titleNode = node.getNode(DbkType.title.get());
			} else if (node.hasNode(DbkType.info.get() + '/' + DbkType.title.get())) {
				titleNode = node.getNode(DbkType.info.get() + '/' + DbkType.title.get());
			}

			if (titleNode != null) {
				boolean showTitle = getMainSection() == section ? showMainTitle : true;
				if (showTitle) {
					if (section.getHeader() == null)
						section.createHeader();
					DbkSectionTitle title = newSectionTitle(textSection, titleNode);
					title.setLayoutData(CmsSwtUtils.fillWidth());
					updateContent(title);
				}
			}

			// content
			for (NodeIterator ni = node.getNodes(); ni.hasNext();) {
				Node child = ni.nextNode();
				SectionPart sectionPart = null;
				if (isDbk(child, DbkType.mediaobject)) {
					if (child.hasNode(DbkType.imageobject.get())) {
						sectionPart = newImg(textSection, child);
					} else if (child.hasNode(DbkType.videoobject.get())) {
						sectionPart = newVideo(textSection, child);
					} else {
						throw new IllegalArgumentException("Unsupported media object " + child);
					}
				} else if (isDbk(child, DbkType.info)) {
					// TODO enrich UI based on info
				} else if (isDbk(child, DbkType.title)) {
					// already managed
				} else if (isDbk(child, para)) {
					sectionPart = newParagraph(textSection, child);
				} else if (isDbk(child, DbkType.section)) {
					sectionPart = newSectionPart(textSection, child);
//					if (sectionPart == null)
//						throw new IllegalArgumentException("Unsupported node " + child);
					// TODO list node types in exception
				} else {
					throw new IllegalArgumentException("Unsupported node type for " + child);
				}
				if (sectionPart != null && sectionPart instanceof Control)
					((Control) sectionPart).setLayoutData(CmsSwtUtils.fillWidth());
			}

//			if (!flat)
			for (NodeIterator ni = section.getNode().getNodes(DbkType.section.get()); ni.hasNext();) {
				Node child = ni.nextNode();
				if (isDbk(child, DbkType.section)) {
					TextSection newSection = newTextSection(section, child);
					newSection.setLayoutData(CmsSwtUtils.fillWidth());
					refresh(newSection);
				}
			}
		} else {
			for (Section s : section.getSubSections().values())
				refresh(s);
		}
		// section.layout(true, true);
		long duration = System.currentTimeMillis() - begin;
//		System.out.println(duration + " ms - " + DbkUtils.getTitle(section.getNode()));
	}

	/** To be overridden in order to provide additional SectionPart types */
	protected TextSection newTextSection(Section section, Node node) {
		return new TextSection(section, SWT.NONE, node);
	}

	/** To be overridden in order to provide additional SectionPart types */
	protected SectionPart newSectionPart(TextSection textSection, Node node) {
		return null;
	}

	// CRUD
	protected Paragraph newParagraph(TextSection parent, Node node) throws RepositoryException {
		Paragraph paragraph = new Paragraph(parent, parent.getStyle(), node);
		updateContent(paragraph);
		paragraph.setLayoutData(CmsSwtUtils.fillWidth());
		paragraph.setMouseListener(getMouseListener());
		paragraph.setFocusListener(getFocusListener());
		return paragraph;
	}

	protected DbkImg newImg(TextSection parent, Node node) {
		try {
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
		} catch (RepositoryException e) {
			throw new JcrException("Cannot add new image " + node, e);
		}
	}

	protected DbkVideo newVideo(TextSection parent, Node node) {
		try {
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
		} catch (RepositoryException e) {
			throw new JcrException("Cannot add new image " + node, e);
		}
	}

	protected DbkSectionTitle newSectionTitle(TextSection parent, Node titleNode) throws RepositoryException {
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

	/**
	 * To be overridden in order to provide additional processing at the section
	 * level.
	 * 
	 * @return the parent to use for the {@link DbkSectionTitle}, by default
	 *         {@link Section#getHeader()}
	 */
	protected Composite newSectionHeader(TextSection section) {
		return section.getHeader();
	}

	protected DbkSectionTitle prepareSectionTitle(Section newSection, String titleText) throws RepositoryException {
		Node sectionNode = newSection.getNode();
		Node titleNode = DbkJcrUtils.getOrAddDbk(sectionNode, DbkType.title);
		getTextInterpreter().write(titleNode, titleText);
		if (newSection.getHeader() == null)
			newSection.createHeader();
		DbkSectionTitle sectionTitle = newSectionTitle((TextSection) newSection, sectionNode);
		return sectionTitle;
	}

	protected void updateContent(SwtEditablePart part) throws RepositoryException {
		if (part instanceof SectionPart) {
			SectionPart sectionPart = (SectionPart) part;
			Node partNode = sectionPart.getNode();

			if (part instanceof StyledControl && (sectionPart.getSection() instanceof TextSection)) {
				TextSection section = (TextSection) sectionPart.getSection();
				StyledControl styledControl = (StyledControl) part;
				if (isDbk(partNode, para)) {
					String style = partNode.hasProperty(DbkAttr.role.name())
							? partNode.getProperty(DbkAttr.role.name()).getString()
							: section.getDefaultTextStyle();
					styledControl.setStyle(style);
				}
			}
			// use control AFTER setting style, since it may have been reset

			if (part instanceof EditableText) {
				EditableText paragraph = (EditableText) part;
				if (paragraph == getEdited())
					paragraph.setText(textInterpreter.raw(partNode));
				else
					paragraph.setText(textInterpreter.readSimpleHtml(partNode));
			} else if (part instanceof DbkImg) {
				DbkImg editableImage = (DbkImg) part;
				// imageManager.load(partNode, part.getControl(),
				// editableImage.getPreferredImageSize());
			} else if (part instanceof DbkVideo) {
				DbkVideo video = (DbkVideo) part;
				video.load(part.getControl());
			}
		} else if (part instanceof DbkSectionTitle) {
			DbkSectionTitle title = (DbkSectionTitle) part;
			title.setStyle(title.getSection().getTitleStyle());
			// use control AFTER setting style
			if (title == getEdited())
				title.setText(textInterpreter.read(title.getNode()));
			else
				title.setText(textInterpreter.readSimpleHtml(title.getNode()));
		}
	}

	// OVERRIDDEN FROM PARENT VIEWER
	@Override
	protected void save(SwtEditablePart part) throws RepositoryException {
		if (part instanceof EditableText) {
			EditableText et = (EditableText) part;
			if (!et.getEditable())
				return;
			String text = ((Text) et.getControl()).getText();

			// String[] lines = text.split("[\r\n]+");
			String[] lines = { text };
			assert lines.length != 0;
			saveLine(part, lines[0]);
			if (lines.length > 1) {
				ArrayList<Control> toLayout = new ArrayList<Control>();
				if (part instanceof Paragraph) {
					Paragraph currentParagraph = (Paragraph) et;
					Section section = currentParagraph.getSection();
					Node sectionNode = section.getNode();
					Node currentParagraphN = currentParagraph.getNode();
					for (int i = 1; i < lines.length; i++) {
						Node newNode = addDbk(sectionNode, para);
						// newNode.addMixin(CmsTypes.CMS_STYLED);
						saveLine(newNode, lines[i]);
						// second node was create as last, if it is not the next
						// one, it
						// means there are some in between and we can take the
						// one at
						// index+1 for the re-order
						if (newNode.getIndex() > currentParagraphN.getIndex() + 1) {
							sectionNode.orderBefore(p(newNode.getIndex()), p(currentParagraphN.getIndex() + 1));
						}
						Paragraph newParagraph = newParagraph((TextSection) section, newNode);
						newParagraph.moveBelow(currentParagraph);
						toLayout.add(newParagraph);

						currentParagraph = newParagraph;
						currentParagraphN = newNode;
					}
				}
				// TODO or rather return the created paragraphs?
				layout(toLayout.toArray(new Control[toLayout.size()]));
			}
			persistChanges(et.getNode());
		}
	}

	protected void saveLine(SwtEditablePart part, String line) {
		if (part instanceof NodePart) {
			saveLine(((NodePart) part).getNode(), line);
		} else if (part instanceof PropertyPart) {
			saveLine(((PropertyPart) part).getProperty(), line);
		} else {
			throw new IllegalArgumentException("Unsupported part " + part);
		}
	}

	protected void saveLine(Item item, String line) {
		line = line.trim();
		textInterpreter.write(item, line);
	}

	@Override
	protected void prepare(SwtEditablePart part, Object caretPosition) {
		Control control = part.getControl();
		if (control instanceof Text) {
			Text text = (Text) control;
			if (caretPosition != null)
				if (caretPosition instanceof Integer)
					text.setSelection((Integer) caretPosition);
				else if (caretPosition instanceof Point) {
//					layout(text);
//					// TODO find a way to position the caret at the right place
//					Point clickLocation = (Point) caretPosition;
//					Point withinText = text.toControl(clickLocation);
//					Rectangle bounds = text.getBounds();
//					int width = bounds.width;
//					int height = bounds.height;
//					int textLength = text.getText().length();
//					float area = width * height;
//					float proportion = withinText.y * width + withinText.x;
//					int pos = (int) (textLength * (proportion / area));
//					text.setSelection(pos);
				}
			text.setData(RWT.ACTIVE_KEYS, new String[] { "BACKSPACE", "ESC", "TAB", "SHIFT+TAB", "ALT+ARROW_LEFT",
					"ALT+ARROW_RIGHT", "ALT+ARROW_UP", "ALT+ARROW_DOWN", "RETURN", "CTRL+RETURN", "ENTER", "DELETE" });
			text.setData(RWT.CANCEL_KEYS, new String[] { "RETURN", "ALT+ARROW_LEFT", "ALT+ARROW_RIGHT" });
			text.addKeyListener(this);
		} else if (part instanceof DbkImg) {
			((DbkImg) part).setFileUploadListener(fileUploadListener);
		}
	}

	// REQUIRED BY CONTEXT MENU
	void setParagraphStyle(Paragraph paragraph, String style) {
		try {
			Node paragraphNode = paragraph.getNode();
			if (style == null) {// default
				if (paragraphNode.hasProperty(DbkAttr.role.name()))
					paragraphNode.getProperty(DbkAttr.role.name()).remove();
			} else {
				paragraphNode.setProperty(DbkAttr.role.name(), style);
			}
			persistChanges(paragraphNode);
			updateContent(paragraph);
			layoutPage();
		} catch (RepositoryException e1) {
			throw new JcrException("Cannot set style " + style + " on " + paragraph, e1);
		}
	}

	SectionPart insertPart(Section section, Node node) {
		try {
			refresh(section);
			layoutPage();
			for (Control control : section.getChildren()) {
				if (control instanceof SectionPart) {
					SectionPart sectionPart = (SectionPart) control;
					Node partNode = sectionPart.getNode();
					if (partNode.getPath().equals(node.getPath()))
						return sectionPart;
				}
			}
			throw new IllegalStateException("New section part " + node + "not found");
		} catch (RepositoryException e) {
			throw new JcrException("Cannot insert part " + node + " in section " + section.getNode(), e);
		}
	}

	void addParagraph(SectionPart partBefore, String txt) {
		Section section = partBefore.getSection();
		SectionPart nextSectionPart = section.nextSectionPart(partBefore);
		Node newNode = addDbk(section.getNode(), para);
		textInterpreter.write(newNode, txt != null ? txt : "");
		if (nextSectionPart != null) {
			try {
				Node nextNode = nextSectionPart.getNode();
				section.getNode().orderBefore(Jcr.getIndexedName(newNode), Jcr.getIndexedName(nextNode));
			} catch (RepositoryException e) {
				throw new JcrException("Cannot order " + newNode + " before " + nextSectionPart.getNode(), e);
			}
		}
		Jcr.save(newNode);
		Paragraph paragraph = (Paragraph) insertPart(partBefore.getSection(), newNode);
		edit(paragraph, 0);
	}

	void deletePart(SectionPart sectionPart) {
		try {
			Node node = sectionPart.getNode();
			Session session = node.getSession();
			if (sectionPart instanceof DbkImg) {
				if (!isDbk(node, DbkType.mediaobject))
					throw new IllegalArgumentException("Node " + node + " is not a media object.");
			}
			node.remove();
			session.save();
			if (sectionPart instanceof Control)
				((Control) sectionPart).dispose();
			layoutPage();
		} catch (RepositoryException e1) {
			throw new JcrException("Cannot delete " + sectionPart, e1);
		}
	}

	void deleteSection(Section section) {
		try {
			Node node = section.getNode();
			Session session = node.getSession();
			node.remove();
			session.save();
			section.dispose();
			layoutPage();
		} catch (RepositoryException e1) {
			throw new JcrException("Cannot delete " + section, e1);
		}
	}

	String getRawParagraphText(Paragraph paragraph) {
		return textInterpreter.raw(paragraph.getNode());
	}

	// COMMANDS
	protected void splitEdit() {
		checkEdited();
		try {
			if (getEdited() instanceof Paragraph) {
				Paragraph paragraph = (Paragraph) getEdited();
				Text text = (Text) paragraph.getControl();
				int caretPosition = text.getCaretPosition();
				String txt = text.getText();
				String first = txt.substring(0, caretPosition);
				String second = txt.substring(caretPosition);
				Node firstNode = paragraph.getNode();
				Node sectionNode = firstNode.getParent();

				// FIXME set content the DocBook way
				// firstNode.setProperty(CMS_CONTENT, first);
				Node secondNode = addDbk(sectionNode, para);
				// secondNode.addMixin(CmsTypes.CMS_STYLED);

				// second node was create as last, if it is not the next one, it
				// means there are some in between and we can take the one at
				// index+1 for the re-order
				if (secondNode.getIndex() > firstNode.getIndex() + 1) {
					sectionNode.orderBefore(p(secondNode.getIndex()), p(firstNode.getIndex() + 1));
				}

				// if we die in between, at least we still have the whole text
				// in the first node
				try {
					textInterpreter.write(secondNode, second);
					textInterpreter.write(firstNode, first);
				} catch (Exception e) {
					// so that no additional nodes are created:
					JcrUtils.discardUnderlyingSessionQuietly(firstNode);
					throw e;
				}

				persistChanges(firstNode);

				Paragraph secondParagraph = paragraphSplitted(paragraph, secondNode);
				edit(secondParagraph, 0);
			} else if (getEdited() instanceof DbkSectionTitle) {
				DbkSectionTitle sectionTitle = (DbkSectionTitle) getEdited();
				Text text = (Text) sectionTitle.getControl();
				String txt = text.getText();
				int caretPosition = text.getCaretPosition();
				Section section = sectionTitle.getSection();
				Node sectionNode = section.getNode();
				Node paragraphNode = addDbk(sectionNode, para);
				// paragraphNode.addMixin(CmsTypes.CMS_STYLED);

				textInterpreter.write(paragraphNode, txt.substring(caretPosition));
				textInterpreter.write(sectionNode.getNode(DbkType.title.get()), txt.substring(0, caretPosition));
				sectionNode.orderBefore(p(paragraphNode.getIndex()), p(1));
				persistChanges(sectionNode);

				Paragraph paragraph = sectionTitleSplitted(sectionTitle, paragraphNode);
				// section.layout();
				edit(paragraph, 0);
			}
		} catch (RepositoryException e) {
			throw new JcrException("Cannot split " + getEdited(), e);
		}
	}

	protected void mergeWithPrevious() {
		checkEdited();
		try {
			Paragraph paragraph = (Paragraph) getEdited();
			Text text = (Text) paragraph.getControl();
			String txt = text.getText();
			Node paragraphNode = paragraph.getNode();
			if (paragraphNode.getIndex() == 1)
				return;// do nothing
			Node sectionNode = paragraphNode.getParent();
			Node previousNode = sectionNode.getNode(p(paragraphNode.getIndex() - 1));
			String previousTxt = textInterpreter.read(previousNode);
			textInterpreter.write(previousNode, previousTxt + txt);
			paragraphNode.remove();
			persistChanges(sectionNode);

			Paragraph previousParagraph = paragraphMergedWithPrevious(paragraph, previousNode);
			edit(previousParagraph, previousTxt.length());
		} catch (RepositoryException e) {
			throw new JcrException("Cannot stop editing", e);
		}
	}

	protected void mergeWithNext() {
		checkEdited();
		try {
			Paragraph paragraph = (Paragraph) getEdited();
			Text text = (Text) paragraph.getControl();
			String txt = text.getText();
			Node paragraphNode = paragraph.getNode();
			Node sectionNode = paragraphNode.getParent();
			NodeIterator paragraphNodes = sectionNode.getNodes(DbkType.para.get());
			long size = paragraphNodes.getSize();
			if (paragraphNode.getIndex() == size)
				return;// do nothing
			Node nextNode = sectionNode.getNode(p(paragraphNode.getIndex() + 1));
			String nextTxt = textInterpreter.read(nextNode);
			textInterpreter.write(paragraphNode, txt + nextTxt);

			Section section = paragraph.getSection();
			Paragraph removed = (Paragraph) section.getSectionPart(nextNode.getIdentifier());

			nextNode.remove();
			persistChanges(sectionNode);

			paragraphMergedWithNext(paragraph, removed);
			edit(paragraph, txt.length());
		} catch (RepositoryException e) {
			throw new JcrException("Cannot stop editing", e);
		}
	}

	protected synchronized void upload(SwtEditablePart part) {
		try {
			if (part instanceof SectionPart) {
				SectionPart sectionPart = (SectionPart) part;
				Node partNode = sectionPart.getNode();
				int partIndex = partNode.getIndex();
				Section section = sectionPart.getSection();
				Node sectionNode = section.getNode();

				if (part instanceof Paragraph) {
					// FIXME adapt to DocBook
//					Node newNode = sectionNode.addNode(DocBookNames.DBK_MEDIAOBJECT, NodeType.NT_FILE);
//					newNode.addNode(Node.JCR_CONTENT, NodeType.NT_RESOURCE);
//					JcrUtils.copyBytesAsFile(sectionNode, p(newNode.getIndex()), new byte[0]);
//					if (partIndex < newNode.getIndex() - 1) {
//						// was not last
//						sectionNode.orderBefore(p(newNode.getIndex()), p(partIndex - 1));
//					}
//					// sectionNode.orderBefore(p(partNode.getIndex()),
//					// p(newNode.getIndex()));
//					persistChanges(sectionNode);
//					DbkImg img = newImg((TextSection) section, newNode);
//					edit(img, null);
//					layout(img.getControl());
				} else if (part instanceof DbkImg) {
					if (getEdited() == part)
						return;
					edit(part, null);
					layoutPage();
				}
			}
		} catch (RepositoryException e) {
			throw new JcrException("Cannot upload", e);
		}
	}

	protected void deepen() {
		if (flat)
			return;
		checkEdited();
		try {
			if (getEdited() instanceof Paragraph) {
				Paragraph paragraph = (Paragraph) getEdited();
				Text text = (Text) paragraph.getControl();
				String txt = text.getText();
				Node paragraphNode = paragraph.getNode();
				Section section = paragraph.getSection();
				Node sectionNode = section.getNode();
				// main title
				if (section == mainSection && section instanceof TextSection && paragraphNode.getIndex() == 1
						&& !sectionNode.hasNode(DbkType.title.get())) {
					DbkSectionTitle sectionTitle = prepareSectionTitle(section, txt);
					edit(sectionTitle, 0);
					return;
				}
				Node newSectionNode = addDbk(sectionNode, DbkType.section);
				// newSectionNode.addMixin(NodeType.MIX_TITLE);
				sectionNode.orderBefore(h(newSectionNode.getIndex()), h(1));

				int paragraphIndex = paragraphNode.getIndex();
				String sectionPath = sectionNode.getPath();
				String newSectionPath = newSectionNode.getPath();
				while (sectionNode.hasNode(p(paragraphIndex + 1))) {
					Node parag = sectionNode.getNode(p(paragraphIndex + 1));
					sectionNode.getSession().move(sectionPath + '/' + p(paragraphIndex + 1),
							newSectionPath + '/' + DbkType.para.get());
					SectionPart sp = section.getSectionPart(parag.getIdentifier());
					if (sp instanceof Control)
						((Control) sp).dispose();
				}
				// create title
				Node titleNode = DbkJcrUtils.addDbk(newSectionNode, DbkType.title);
				// newSectionNode.addNode(DocBookType.TITLE, DocBookType.TITLE);
				getTextInterpreter().write(titleNode, txt);

				TextSection newSection = new TextSection(section, section.getStyle(), newSectionNode);
				newSection.setLayoutData(CmsSwtUtils.fillWidth());
				newSection.moveBelow(paragraph);

				// dispose
				paragraphNode.remove();
				paragraph.dispose();

				refresh(newSection);
				newSection.getParent().layout();
				layout(newSection);
				persistChanges(sectionNode);
			} else if (getEdited() instanceof DbkSectionTitle) {
				DbkSectionTitle sectionTitle = (DbkSectionTitle) getEdited();
				Section section = sectionTitle.getSection();
				Section parentSection = section.getParentSection();
				if (parentSection == null)
					return;// cannot deepen main section
				Node sectionN = section.getNode();
				Node parentSectionN = parentSection.getNode();
				if (sectionN.getIndex() == 1)
					return;// cannot deepen first section
				Node previousSectionN = parentSectionN.getNode(h(sectionN.getIndex() - 1));
				NodeIterator subSections = previousSectionN.getNodes(DbkType.section.get());
				int subsectionsCount = (int) subSections.getSize();
				previousSectionN.getSession().move(sectionN.getPath(),
						previousSectionN.getPath() + "/" + h(subsectionsCount + 1));
				section.dispose();
				TextSection newSection = new TextSection(section, section.getStyle(), sectionN);
				refresh(newSection);
				persistChanges(previousSectionN);
			}
		} catch (RepositoryException e) {
			throw new JcrException("Cannot deepen " + getEdited(), e);
		}
	}

	protected void undeepen() {
		if (flat)
			return;
		checkEdited();
		try {
			if (getEdited() instanceof Paragraph) {
				upload(getEdited());
			} else if (getEdited() instanceof DbkSectionTitle) {
				DbkSectionTitle sectionTitle = (DbkSectionTitle) getEdited();
				Section section = sectionTitle.getSection();
				Node sectionNode = section.getNode();
				Section parentSection = section.getParentSection();
				if (parentSection == null)
					return;// cannot undeepen main section

				// choose in which section to merge
				Section mergedSection;
				if (sectionNode.getIndex() == 1)
					mergedSection = section.getParentSection();
				else {
					Map<String, Section> parentSubsections = parentSection.getSubSections();
					ArrayList<Section> lst = new ArrayList<Section>(parentSubsections.values());
					mergedSection = lst.get(sectionNode.getIndex() - 1);
				}
				Node mergedNode = mergedSection.getNode();
				boolean mergedHasSubSections = mergedNode.hasNode(DbkType.section.get());

				// title as paragraph
				Node newParagrapheNode = addDbk(mergedNode, para);
				// newParagrapheNode.addMixin(CmsTypes.CMS_STYLED);
				if (mergedHasSubSections)
					mergedNode.orderBefore(p(newParagrapheNode.getIndex()), h(1));
				String txt = getTextInterpreter().read(sectionNode.getNode(DbkType.title.get()));
				getTextInterpreter().write(newParagrapheNode, txt);
				// move
				NodeIterator paragraphs = sectionNode.getNodes(para.get());
				while (paragraphs.hasNext()) {
					Node p = paragraphs.nextNode();
					SectionPart sp = section.getSectionPart(p.getIdentifier());
					if (sp instanceof Control)
						((Control) sp).dispose();
					mergedNode.getSession().move(p.getPath(), mergedNode.getPath() + '/' + para.get());
					if (mergedHasSubSections)
						mergedNode.orderBefore(p(p.getIndex()), h(1));
				}

				Iterator<Section> subsections = section.getSubSections().values().iterator();
				// NodeIterator sections = sectionNode.getNodes(CMS_H);
				while (subsections.hasNext()) {
					Section subsection = subsections.next();
					Node s = subsection.getNode();
					mergedNode.getSession().move(s.getPath(), mergedNode.getPath() + '/' + DbkType.section.get());
					subsection.dispose();
				}

				// remove section
				section.getNode().remove();
				section.dispose();

				refresh(mergedSection);
				mergedSection.getParent().layout();
				layout(mergedSection);
				persistChanges(mergedNode);
			}
		} catch (RepositoryException e) {
			throw new JcrException("Cannot undeepen " + getEdited(), e);
		}
	}

	// UI CHANGES
	protected Paragraph paragraphSplitted(Paragraph paragraph, Node newNode) throws RepositoryException {
		Section section = paragraph.getSection();
		updateContent(paragraph);
		Paragraph newParagraph = newParagraph((TextSection) section, newNode);
		newParagraph.setLayoutData(CmsSwtUtils.fillWidth());
		newParagraph.moveBelow(paragraph);
		layout(paragraph.getControl(), newParagraph.getControl());
		return newParagraph;
	}

	protected Paragraph sectionTitleSplitted(DbkSectionTitle sectionTitle, Node newNode) throws RepositoryException {
		updateContent(sectionTitle);
		Paragraph newParagraph = newParagraph(sectionTitle.getSection(), newNode);
		// we assume beforeFirst is not null since there was a sectionTitle
		newParagraph.moveBelow(sectionTitle.getSection().getHeader());
		layout(sectionTitle.getControl(), newParagraph.getControl());
		return newParagraph;
	}

	protected Paragraph paragraphMergedWithPrevious(Paragraph removed, Node remaining) throws RepositoryException {
		Section section = removed.getSection();
		removed.dispose();

		Paragraph paragraph = (Paragraph) section.getSectionPart(remaining.getIdentifier());
		updateContent(paragraph);
		layout(paragraph.getControl());
		return paragraph;
	}

	protected void paragraphMergedWithNext(Paragraph remaining, Paragraph removed) throws RepositoryException {
		removed.dispose();
		updateContent(remaining);
		layout(remaining.getControl());
	}

	// UTILITIES
	protected String p(Integer index) {
		StringBuilder sb = new StringBuilder(6);
		sb.append(para.get()).append('[').append(index).append(']');
		return sb.toString();
	}

	protected String h(Integer index) {
		StringBuilder sb = new StringBuilder(5);
		sb.append(DbkType.section.get()).append('[').append(index).append(']');
		return sb.toString();
	}

	// GETTERS / SETTERS
	public Section getMainSection() {
		return mainSection;
	}

	public boolean isFlat() {
		return flat;
	}

	public TextInterpreter getTextInterpreter() {
		return textInterpreter;
	}

	// KEY LISTENER
	@Override
	public void keyPressed(KeyEvent ke) {
		if (log.isTraceEnabled())
			log.trace(ke);

		if (getEdited() == null)
			return;
		boolean altPressed = (ke.stateMask & SWT.ALT) != 0;
		boolean shiftPressed = (ke.stateMask & SWT.SHIFT) != 0;
		boolean ctrlPressed = (ke.stateMask & SWT.CTRL) != 0;

		try {
			// Common
			if (ke.keyCode == SWT.ESC) {
//				cancelEdit();
				saveEdit();
			} else if (ke.character == '\r') {
				if (!shiftPressed)
					splitEdit();
			} else if (ke.character == 'z') {
				if (ctrlPressed)
					cancelEdit();
			} else if (ke.character == 'S') {
				if (ctrlPressed)
					saveEdit();
			} else if (ke.character == '\t') {
				if (!shiftPressed) {
					deepen();
				} else if (shiftPressed) {
					undeepen();
				}
			} else {
				if (getEdited() instanceof Paragraph) {
					Paragraph paragraph = (Paragraph) getEdited();
					Section section = paragraph.getSection();
					if (altPressed && ke.keyCode == SWT.ARROW_RIGHT) {
						edit(section.nextSectionPart(paragraph), 0);
					} else if (altPressed && ke.keyCode == SWT.ARROW_LEFT) {
						edit(section.previousSectionPart(paragraph), 0);
					} else if (ke.character == SWT.BS) {
						Text text = (Text) paragraph.getControl();
						int caretPosition = text.getCaretPosition();
						if (caretPosition == 0) {
							mergeWithPrevious();
						}
					} else if (ke.character == SWT.DEL) {
						Text text = (Text) paragraph.getControl();
						int caretPosition = text.getCaretPosition();
						int charcount = text.getCharCount();
						if (caretPosition == charcount) {
							mergeWithNext();
						}
					}
				}
			}
		} catch (Exception e) {
			ke.doit = false;
			notifyEditionException(e);
		}
	}

	@Override
	public void keyReleased(KeyEvent e) {
	}

	// MOUSE LISTENER
	@Override
	protected MouseListener createMouseListener() {
		return new ML();
	}

	private class ML extends MouseAdapter {
		private static final long serialVersionUID = 8526890859876770905L;

		@Override
		public void mouseDoubleClick(MouseEvent e) {
			if (e.button == 1) {
				Control source = (Control) e.getSource();
				SwtEditablePart composite = findDataParent(source);
				Point point = new Point(e.x, e.y);
				if (composite instanceof DbkImg) {
					if (getCmsEditable().canEdit()) {
						if (getCmsEditable().isEditing() && !(getEdited() instanceof DbkImg)) {
							if (source == mainSection)
								return;
							SwtEditablePart part = findDataParent(source);
							upload(part);
						} else {
							getCmsEditable().startEditing();
						}
					}
				} else if (source instanceof Label) {
					Label lbl = (Label) source;
					Rectangle bounds = lbl.getBounds();
					float width = bounds.width;
					float height = bounds.height;
					float textLength = lbl.getText().length();
					float area = width * height;
					float charArea = area / textLength;
					float lines = textLength / width;
					float proportion = point.y * width + point.x;
					int pos = (int) (textLength * (proportion / area));
					// TODO refine it
					edit(composite, (Integer) pos);
				} else {
					edit(composite, source.toDisplay(point));
				}
			}
		}

		@Override
		public void mouseDown(MouseEvent e) {
			if (getCmsEditable().isEditing()) {
				if (e.button == 3) {
					SwtEditablePart composite = findDataParent((Control) e.getSource());
					if (styledTools != null) {
						List<String> styles = getAvailableStyles(composite);
						styledTools.show(composite, new Point(e.x, e.y), styles);
					}
				}
			}
		}

		@Override
		public void mouseUp(MouseEvent e) {
		}
	}

	protected List<String> getAvailableStyles(SwtEditablePart editablePart) {
		return new ArrayList<>();
	}

	public void setMaxMediaWidth(Integer maxMediaWidth) {
		this.maxMediaWidth = maxMediaWidth;
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

	// FILE UPLOAD LISTENER
	private class FUL implements FileUploadListener {
		public void uploadProgress(FileUploadEvent event) {
			// TODO Monitor upload progress
		}

		public void uploadFailed(FileUploadEvent event) {
			throw new RuntimeException("Upload failed " + event, event.getException());
		}

		public void uploadFinished(FileUploadEvent event) {
			for (FileDetails file : event.getFileDetails()) {
				if (log.isDebugEnabled())
					log.debug("Received: " + file.getFileName());
			}
			mainSection.getDisplay().syncExec(new Runnable() {
				@Override
				public void run() {
					saveEdit();
				}
			});
			FileUploadHandler uploadHandler = (FileUploadHandler) event.getSource();
			uploadHandler.dispose();
		}
	}
}