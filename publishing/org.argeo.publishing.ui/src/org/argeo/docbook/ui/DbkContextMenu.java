package org.argeo.docbook.ui;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;

import org.argeo.cms.ui.CmsEditable;
import org.argeo.cms.ui.util.CmsUiUtils;
import org.argeo.cms.ui.viewers.EditablePart;
import org.argeo.cms.ui.viewers.NodePart;
import org.argeo.cms.ui.viewers.Section;
import org.argeo.cms.ui.viewers.SectionPart;
import org.argeo.cms.ui.widgets.EditableText;
import org.argeo.cms.ui.widgets.Img;
import org.argeo.docbook.DbkMsg;
import org.argeo.docbook.DbkUtils;
import org.argeo.eclipse.ui.MouseDown;
import org.argeo.jcr.Jcr;
import org.eclipse.rap.rwt.RWT;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

/** Dialog to edit a text part. */
class DbkContextMenu {
	private final AbstractDbkViewer textViewer;

	private Shell shell;

	DbkContextMenu(AbstractDbkViewer textViewer, Shell parentShell) {
//		shell = new Shell(display, SWT.NO_TRIM | SWT.BORDER | SWT.ON_TOP);
		shell = new Shell(parentShell, SWT.BORDER);
//		super(display, SWT.NO_TRIM | SWT.BORDER | SWT.ON_TOP);
		this.textViewer = textViewer;
		shell.setLayout(new GridLayout());
		// shell.setData(RWT.CUSTOM_VARIANT, TEXT_STYLED_TOOLS_DIALOG);

		shell.addShellListener(new ToolsShellListener());
	}

	void show(EditablePart editablePart, Point location, List<String> availableStyles) {
		if (shell.isVisible())
			shell.setVisible(false);
		CmsUiUtils.clear(shell);
		Composite parent = shell;
		CmsEditable cmsEditable = textViewer.getCmsEditable();
//		if (availableStyles.isEmpty())
//			return;

		if (editablePart instanceof Paragraph) {
			Paragraph paragraph = (Paragraph) editablePart;
			deletePartB(parent, DbkMsg.deleteParagraph.lead(), paragraph);
			insertMediaB(parent,  paragraph);

		} else if (editablePart instanceof Img) {
			Img img = (Img) editablePart;
			deletePartB(parent, DbkMsg.deleteMedia.lead(), img);
			insertMediaB(parent, img);
			insertParagraphB(parent, DbkMsg.insertParagraph.lead(), img);

		} else if (editablePart instanceof DbkSectionTitle) {
			DbkSectionTitle sectionTitle = (DbkSectionTitle) editablePart;
			TextSection section = sectionTitle.getSection();
			if (!section.isTitleReadOnly()) {
				Label deleteB = new Label(shell, SWT.NONE);
				deleteB.setText(DbkMsg.deleteSection.lead());
				deleteB.addMouseListener((MouseDown) (e) -> {
					textViewer.deleteSection(section);
					hide();
				});
			}
			insertMediaB(parent,  sectionTitle.getSection(), sectionTitle);
		}

		StyledToolMouseListener stml = new StyledToolMouseListener(editablePart);
		List<StyleButton> styleButtons = new ArrayList<DbkContextMenu.StyleButton>();
		if (cmsEditable.isEditing()) {
			for (String style : availableStyles) {
				StyleButton styleButton = new StyleButton(shell, SWT.WRAP);
				if (!"".equals(style))
					styleButton.setStyle(style);
				else
					styleButton.setStyle(null);
				styleButton.setMouseListener(stml);
				styleButtons.add(styleButton);
			}
		} else if (cmsEditable.canEdit()) {
			// Edit
//			Label editButton = new Label(shell, SWT.NONE);
//			editButton.setText("Edit");
//			editButton.addMouseListener(stml);
		}

		if (editablePart instanceof Paragraph) {
			final int size = 32;
			String text = textViewer.getRawParagraphText((Paragraph) editablePart);
			String textToShow = text.length() > size ? text.substring(0, size - 3) + "..." : text;
			for (StyleButton styleButton : styleButtons) {
				styleButton.setText((styleButton.style == null ? "default" : styleButton.style) + " : " + textToShow);
			}
		}

		shell.pack();
		shell.layout();
		if (editablePart instanceof Control) {
			int height = shell.getSize().y;
			int parentShellHeight = shell.getShell().getSize().y;
			if ((location.y + height) < parentShellHeight) {
				shell.setLocation(((Control) editablePart).toDisplay(location.x, location.y));
			} else {
				shell.setLocation(((Control) editablePart).toDisplay(location.x, location.y - parentShellHeight));
			}
		}

		if (shell.getChildren().length != 0)
			shell.open();
	}

	void hide() {
		shell.setVisible(false);
	}

	protected void insertMediaB(Composite parent, SectionPart sectionPart) {
		insertMediaB(parent,  sectionPart.getSection(), sectionPart);
	}

	protected void insertMediaB(Composite parent, Section section, NodePart nodePart) {
		Label insertPictureB = new Label(parent, SWT.NONE);
		insertPictureB.setText(DbkMsg.insertPicture.lead());
		insertPictureB.addMouseListener((MouseDown) (e) -> {
			Node newNode = DbkUtils.insertImageAfter(nodePart.getNode());
			Jcr.save(newNode);
			textViewer.insertPart(section, newNode);
			hide();
		});
		Label insertVideoB = new Label(parent, SWT.NONE);
		insertVideoB.setText(DbkMsg.insertVideo.lead());
		insertVideoB.addMouseListener((MouseDown) (e) -> {
			Node newNode = DbkUtils.insertVideoAfter(nodePart.getNode());
			Jcr.save(newNode);
			textViewer.insertPart(section, newNode);
			hide();
		});

	}

	protected void insertParagraphB(Composite parent, String msg, SectionPart sectionPart) {
		Label insertMediaB = new Label(parent, SWT.NONE);
		insertMediaB.setText(msg);
		insertMediaB.addMouseListener((MouseDown) (e) -> {
			textViewer.addParagraph(sectionPart, null);
			hide();
		});
	}

	protected void deletePartB(Composite parent, String msg, SectionPart sectionPart) {
		Label deleteB = new Label(shell, SWT.NONE);
		deleteB.setText(msg);
		deleteB.addMouseListener((MouseDown) (e) -> {
			textViewer.deletePart(sectionPart);
			hide();
		});
	}

	class StyleButton extends EditableText {
		private static final long serialVersionUID = 7731102609123946115L;

		String style;

		public StyleButton(Composite parent, int style) {
			super(parent, style);
		}

		@Override
		public void setStyle(String style) {
			this.style = style;
			super.setStyle(style);
		}

//		private Label label;
//
//		public StyleButton(Composite parent, int swtStyle) {
//			super(parent, SWT.NONE);
//			setLayout(new GridLayout());
//			label = new Label(this, swtStyle);
//		}
//
//		public Label getLabel() {
//			return label;
//		}

	}

	class StyledToolMouseListener extends MouseAdapter {
		private static final long serialVersionUID = 8516297091549329043L;
		private EditablePart editablePart;

		public StyledToolMouseListener(EditablePart editablePart) {
			super();
			this.editablePart = editablePart;
		}

		@Override
		public void mouseDown(MouseEvent e) {
			// TODO make it more robust.
			Label sb = (Label) e.getSource();
			Object style = sb.getData(RWT.CUSTOM_VARIANT);
			textViewer.setParagraphStyle((Paragraph) editablePart, style == null ? null : style.toString());
			hide();
		}
	}

	class ToolsShellListener extends org.eclipse.swt.events.ShellAdapter {
		private static final long serialVersionUID = 8432350564023247241L;

		@Override
		public void shellDeactivated(ShellEvent e) {
			hide();
		}

	}
}
