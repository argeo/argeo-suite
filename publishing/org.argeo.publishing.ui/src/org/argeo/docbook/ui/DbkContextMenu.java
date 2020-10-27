package org.argeo.docbook.ui;

import java.util.ArrayList;
import java.util.List;

import org.argeo.cms.text.Paragraph;
import org.argeo.cms.ui.viewers.EditablePart;
import org.argeo.cms.ui.viewers.SectionPart;
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
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

/** Dialog to edit a text part. */
class DbkContextMenu implements TextStyles {
	private final static String[] DEFAULT_TEXT_STYLES = { TextStyles.TEXT_DEFAULT, TextStyles.TEXT_PRE,
			TextStyles.TEXT_QUOTE };

	private final AbstractDbkViewer textViewer;

	private List<StyleButton> styleButtons = new ArrayList<DbkContextMenu.StyleButton>();

	private Label deleteButton, publishButton, editButton;

	private EditablePart currentTextPart;

	private Shell shell;

	public DbkContextMenu(AbstractDbkViewer textViewer, Display display) {
		shell = new Shell(display, SWT.NO_TRIM | SWT.BORDER | SWT.ON_TOP);
//		super(display, SWT.NO_TRIM | SWT.BORDER | SWT.ON_TOP);
		this.textViewer = textViewer;
		shell.setLayout(new GridLayout());
		shell.setData(RWT.CUSTOM_VARIANT, TEXT_STYLED_TOOLS_DIALOG);

		StyledToolMouseListener stml = new StyledToolMouseListener();
		if (textViewer.getCmsEditable().isEditing()) {
			for (String style : DEFAULT_TEXT_STYLES) {
				StyleButton styleButton = new StyleButton(shell, SWT.WRAP);
				styleButton.getLabel().setData(RWT.CUSTOM_VARIANT, style);
				styleButton.getLabel().setData(RWT.MARKUP_ENABLED, true);
				styleButton.getLabel().addMouseListener(stml);
				styleButtons.add(styleButton);
			}

			// Delete
			deleteButton = new Label(shell, SWT.NONE);
			deleteButton.setText("Delete");
			deleteButton.addMouseListener(stml);

			// Publish
			publishButton = new Label(shell, SWT.NONE);
			publishButton.setText("Publish");
			publishButton.addMouseListener(stml);
		} else if (textViewer.getCmsEditable().canEdit()) {
			// Edit
			editButton = new Label(shell, SWT.NONE);
			editButton.setText("Edit");
			editButton.addMouseListener(stml);
		}
		shell.addShellListener(new ToolsShellListener());
	}

	public void show(EditablePart source, Point location) {
		if (shell.isVisible())
			shell.setVisible(false);

		this.currentTextPart = source;

		if (currentTextPart instanceof Paragraph) {
			final int size = 32;
			String text = textViewer.getRawParagraphText((Paragraph) currentTextPart);
			String textToShow = text.length() > size ? text.substring(0, size - 3) + "..." : text;
			for (StyleButton styleButton : styleButtons) {
				styleButton.getLabel().setText(textToShow);
			}
		}
		shell.pack();
		shell.layout();
		if (source instanceof Control)
			shell.setLocation(((Control) source).toDisplay(location.x, location.y));
		shell.open();
	}

	class StyleButton extends Composite {
		private static final long serialVersionUID = 7731102609123946115L;
		private Label label;

		public StyleButton(Composite parent, int swtStyle) {
			super(parent, SWT.NONE);
			label = new Label(this, swtStyle);
		}

		public Label getLabel() {
			return label;
		}

	}

	class StyledToolMouseListener extends MouseAdapter {
		private static final long serialVersionUID = 8516297091549329043L;

		@Override
		public void mouseDown(MouseEvent e) {
			Object eventSource = e.getSource();
			if (eventSource instanceof StyleButton) {
				StyleButton sb = (StyleButton) e.getSource();
				String style = sb.getData(RWT.CUSTOM_VARIANT).toString();
				textViewer.setParagraphStyle((Paragraph) currentTextPart, style);
			} else if (eventSource == deleteButton) {
				textViewer.deletePart((SectionPart) currentTextPart);
			} else if (eventSource == editButton) {
				textViewer.getCmsEditable().startEditing();
			} else if (eventSource == publishButton) {
				textViewer.getCmsEditable().stopEditing();
			}
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
