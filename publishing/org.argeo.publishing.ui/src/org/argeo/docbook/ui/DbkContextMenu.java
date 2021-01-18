package org.argeo.docbook.ui;

import java.util.ArrayList;
import java.util.List;

import org.argeo.cms.text.Paragraph;
import org.argeo.cms.ui.util.CmsUiUtils;
import org.argeo.cms.ui.viewers.EditablePart;
import org.argeo.cms.ui.widgets.EditableText;
import org.argeo.cms.ui.widgets.TextStyles;
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
class DbkContextMenu implements TextStyles {
//	private final static String[] DEFAULT_TEXT_STYLES = { TextStyles.TEXT_DEFAULT, TextStyles.TEXT_PRE,
//			TextStyles.TEXT_QUOTE };

	private final AbstractDbkViewer textViewer;

//	private List<StyleButton> styleButtons = new ArrayList<DbkContextMenu.StyleButton>();
//
//	private Label deleteButton, publishButton, editButton;

	private EditablePart currentTextPart;

	private Shell shell;

	public DbkContextMenu(AbstractDbkViewer textViewer, Shell parentShell) {
//		shell = new Shell(display, SWT.NO_TRIM | SWT.BORDER | SWT.ON_TOP);
		shell = new Shell(parentShell, SWT.BORDER);
//		super(display, SWT.NO_TRIM | SWT.BORDER | SWT.ON_TOP);
		this.textViewer = textViewer;
		shell.setLayout(new GridLayout());
		// shell.setData(RWT.CUSTOM_VARIANT, TEXT_STYLED_TOOLS_DIALOG);

		shell.addShellListener(new ToolsShellListener());
	}

	public void show(EditablePart source, Point location, List<String> availableStyles) {
		if (shell.isVisible())
			shell.setVisible(false);
		CmsUiUtils.clear(shell);

		if (availableStyles.isEmpty())
			return;

		StyledToolMouseListener stml = new StyledToolMouseListener();
		List<StyleButton> styleButtons = new ArrayList<DbkContextMenu.StyleButton>();
		if (textViewer.getCmsEditable().isEditing()) {
			for (String style : availableStyles) {
				StyleButton styleButton = new StyleButton(shell, SWT.WRAP);
				if (!"".equals(style))
					styleButton.setStyle(style);
				else
					styleButton.setStyle(null);
//				if (!"".equals(style))
//					styleButton.getLabel().setData(RWT.CUSTOM_VARIANT, style);
//				styleButton.getLabel().setData(RWT.MARKUP_ENABLED, true);
				styleButton.setMouseListener(stml);
				styleButtons.add(styleButton);
			}

//			// Delete
//			deleteButton = new Label(shell, SWT.NONE);
//			deleteButton.setText("Delete");
//			deleteButton.addMouseListener(stml);
//
//			// Publish
//			publishButton = new Label(shell, SWT.NONE);
//			publishButton.setText("Publish");
//			publishButton.addMouseListener(stml);
		} else if (textViewer.getCmsEditable().canEdit()) {
			// Edit
			Label editButton = new Label(shell, SWT.NONE);
			editButton.setText("Edit");
			editButton.addMouseListener(stml);
		}

		this.currentTextPart = source;

		if (currentTextPart instanceof Paragraph) {
			final int size = 32;
			String text = textViewer.getRawParagraphText((Paragraph) currentTextPart);
			String textToShow = text.length() > size ? text.substring(0, size - 3) + "..." : text;
			for (StyleButton styleButton : styleButtons) {
				styleButton.setText((styleButton.style == null ? "default" : styleButton.style) + " : " + textToShow);
			}
		}
		shell.pack();
		shell.layout();
		if (source instanceof Control) {
			int height = shell.getSize().y;
			int parentShellHeight = shell.getShell().getSize().y;
			if ((location.y + height) < parentShellHeight) {
				shell.setLocation(((Control) source).toDisplay(location.x, location.y));
			} else {
				shell.setLocation(((Control) source).toDisplay(location.x, location.y - parentShellHeight));
			}
		}
		shell.open();
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

		@Override
		public void mouseDown(MouseEvent e) {
//			Object eventSource = e.getSource();
////			if (eventSource instanceof StyleButton) {
			// TODO make it more robust.
			Label sb = (Label) e.getSource();
			Object style = sb.getData(RWT.CUSTOM_VARIANT);
			textViewer.setParagraphStyle((Paragraph) currentTextPart, style == null ? null : style.toString());
//			}
//			} else if (eventSource == deleteButton) {
//				textViewer.deletePart((SectionPart) currentTextPart);
//			} else if (eventSource == editButton) {
//				textViewer.getCmsEditable().startEditing();
//			} else if (eventSource == publishButton) {
//				textViewer.getCmsEditable().stopEditing();
			shell.setVisible(false);
		}
	}

	class ToolsShellListener extends org.eclipse.swt.events.ShellAdapter {
		private static final long serialVersionUID = 8432350564023247241L;

		@Override
		public void shellDeactivated(ShellEvent e) {
			shell.setVisible(false);
		}

	}
}
