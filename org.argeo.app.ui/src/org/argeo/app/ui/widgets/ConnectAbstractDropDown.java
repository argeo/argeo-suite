package org.argeo.app.ui.widgets;

import java.util.Arrays;
import java.util.List;

import org.argeo.cms.swt.CmsSwtUtils;
import org.argeo.eclipse.ui.EclipseUiUtils;
import org.eclipse.rap.rwt.widgets.DropDown;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Widget;

/**
 * Enable easy addition of a {@code DropDown} widget to a text with listeners
 * configured
 */
public abstract class ConnectAbstractDropDown {

	private final Text text;
	private final DropDown dropDown;
	private boolean modifyFromList = false;

	// Current displayed text
	private String userText = "";
	// Current displayed list items
	private String[] values;

	// Fine tuning
	boolean readOnly;
	boolean refreshOnFocus;

	/** Implementing classes should call refreshValues() after initialisation */
	public ConnectAbstractDropDown(Text text) {
		this(text, SWT.NONE, false);
	}

	/**
	 * Implementing classes should call refreshValues() after initialisation
	 * 
	 * @param text
	 * @param style
	 *            only SWT.READ_ONLY is understood, check if the entered text is
	 *            part of the legal choices.
	 */
	public ConnectAbstractDropDown(Text text, int style) {
		this(text, style, false);
	}

	/**
	 * Implementers should call refreshValues() once init has been done.
	 * 
	 * @param text
	 * @param style
	 *            only SWT.READ_ONLY is understood, check if the entered text is
	 *            part of the legal choices.
	 * @param refreshOnFocus
	 *            if true, the possible values are computed each time the focus is
	 *            gained. It enables, among other to fine tune the getFilteredValues
	 *            method depending on the current context
	 */
	public ConnectAbstractDropDown(Text text, int style, boolean refreshOnFocus) {
		this.text = text;
		dropDown = new DropDown(text);
		Object obj = dropDown;
		if (obj instanceof Widget)
			CmsSwtUtils.markup((Widget) obj);
		readOnly = (style & SWT.READ_ONLY) != 0;
		this.refreshOnFocus = refreshOnFocus;
		addListeners();
	}

	/**
	 * Overwrite to force the refresh of the possible values on focus gained event
	 */
	protected boolean refreshOnFocus() {
		return refreshOnFocus;
	}

	public String getText() {
		return text.getText();
	}

	public void init() {
		refreshValues();
	}

	public void reset(String value) {
		modifyFromList = true;
		if (EclipseUiUtils.notEmpty(value))
			text.setText(value);
		else
			text.setText("");
		refreshValues();
		modifyFromList = false;
	}

	/** Overwrite to provide specific filtering */
	protected abstract List<String> getFilteredValues(String filter);

	protected void refreshValues() {
		List<String> filteredValues = getFilteredValues(text.getText());
		values = filteredValues.toArray(new String[filteredValues.size()]);
		dropDown.setItems(values);
	}

	protected void addListeners() {
		addModifyListener();
		addSelectionListener();
		addDefaultSelectionListener();
		addFocusListener();
	}

	protected void addFocusListener() {
		text.addFocusListener(new FocusListener() {
			private static final long serialVersionUID = -7179112097626535946L;

			public void focusGained(FocusEvent event) {
				if (refreshOnFocus) {
					modifyFromList = true;
					refreshValues();
					modifyFromList = false;
				}
				dropDown.setVisible(true);
			}

			public void focusLost(FocusEvent event) {
				dropDown.setVisible(false);
				if (readOnly && values != null && !Arrays.asList(values).contains(userText)) {
					modifyFromList = true;
					text.setText("");
					refreshValues();
					modifyFromList = false;
				}
			}
		});
	}

	private void addSelectionListener() {
		Object obj = dropDown;
		if (obj instanceof Widget)
			((Widget) obj).addListener(SWT.Selection, new Listener() {
				private static final long serialVersionUID = -2357157809365135142L;

				public void handleEvent(Event event) {
					if (event.index != -1) {
						modifyFromList = true;
						text.setText(values[event.index]);
						modifyFromList = false;
						text.selectAll();
					} else {
						text.setText(userText);
						text.setSelection(userText.length(), userText.length());
						text.setFocus();
					}
				}
			});
	}

	private void addDefaultSelectionListener() {
		Object obj = dropDown;
		if (obj instanceof Widget)
			((Widget) obj).addListener(SWT.DefaultSelection, new Listener() {
				private static final long serialVersionUID = -5958008322630466068L;

				public void handleEvent(Event event) {
					if (event.index != -1) {
						text.setText(values[event.index]);
						text.setSelection(event.text.length());
						dropDown.setVisible(false);
					}
				}
			});
	}

	private void addModifyListener() {
		text.addListener(SWT.Modify, new Listener() {
			private static final long serialVersionUID = -4373972835244263346L;

			public void handleEvent(Event event) {
				if (!modifyFromList) {
					userText = text.getText();
					refreshValues();
					if (values.length == 1)
						dropDown.setSelectionIndex(0);
					dropDown.setVisible(true);
				}
			}
		});
	}
}
