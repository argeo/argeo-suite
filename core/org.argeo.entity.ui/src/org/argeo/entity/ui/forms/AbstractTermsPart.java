package org.argeo.entity.ui.forms;

import javax.jcr.Item;

import org.argeo.cms.Localized;
import org.argeo.cms.ui.CmsTheme;
import org.argeo.cms.ui.util.CmsIcon;
import org.argeo.cms.ui.util.CmsUiUtils;
import org.argeo.cms.ui.viewers.EditablePart;
import org.argeo.cms.ui.widgets.ContextOverlay;
import org.argeo.cms.ui.widgets.StyledControl;
import org.argeo.entity.Term;
import org.argeo.entity.TermsManager;
import org.argeo.entity.Typology;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolItem;

/** Common logic between single and mutliple terms editable part. */
public abstract class AbstractTermsPart extends StyledControl implements EditablePart {
	private static final long serialVersionUID = -5497097995341927710L;
	protected final TermsManager termsManager;
	protected final Typology typology;

	private final boolean editable;

	private CmsIcon deleteIcon;
	private CmsIcon addIcon;
	private CmsIcon cancelIcon;

	private Color highlightColor;
	private Composite highlight;

	protected final CmsTheme theme;
	
	public AbstractTermsPart(Composite parent, int style, Item item, TermsManager termsManager, String typology) {
		super(parent, style, item);
		if (item == null)
			throw new IllegalArgumentException("Item cannot be null");
		this.termsManager = termsManager;
		this.typology = termsManager.getTypology(typology);
		this.theme = CmsTheme.getCmsTheme(parent);
		editable = !(SWT.READ_ONLY == (style & SWT.READ_ONLY));
		highlightColor = parent.getDisplay().getSystemColor(SWT.COLOR_GRAY);
	}

	public boolean isEditable() {
		return editable;
	}

	protected void createHighlight(Composite block) {
		highlight = new Composite(block, SWT.NONE);
		highlight.setBackground(highlightColor);
		GridData highlightGd = new GridData(SWT.FILL, SWT.FILL, false, false);
		highlightGd.widthHint = 5;
		highlightGd.heightHint = 3;
		highlight.setLayoutData(highlightGd);

	}

	protected String getTermLabel(Term term) {
		if (term instanceof Localized)
			return ((Localized) term).lead();
		else
			return term.getName();

	}

	protected abstract void refresh(ContextOverlay contextArea, String filter, Text txt);

	protected boolean isTermSelectable(Term term) {
		return true;
	}

	protected void processTermListLabel(Term term, Label label) {

	}

	protected void setControlLayoutData(Control control) {
		control.setLayoutData(CmsUiUtils.fillAll());
	}

	protected void setContainerLayoutData(Composite composite) {
		composite.setLayoutData(CmsUiUtils.fillAll());
	}

	//
	// STYLING
	//
	public void setDeleteIcon(CmsIcon deleteIcon) {
		this.deleteIcon = deleteIcon;
	}

	public void setAddIcon(CmsIcon addIcon) {
		this.addIcon = addIcon;
	}

	public void setCancelIcon(CmsIcon cancelIcon) {
		this.cancelIcon = cancelIcon;
	}

	protected TermsManager getTermsManager() {
		return termsManager;
	}

	protected void styleDelete(ToolItem deleteItem) {
		if (deleteIcon != null)
			deleteItem.setImage(deleteIcon.getSmallIcon(theme));
		else
			deleteItem.setText("-");
	}

	protected void styleCancel(ToolItem cancelItem) {
		if (cancelIcon != null)
			cancelItem.setImage(cancelIcon.getSmallIcon(theme));
		else
			cancelItem.setText("X");
	}

	protected void styleAdd(ToolItem addItem) {
		if (addIcon != null)
			addItem.setImage(addIcon.getSmallIcon(theme));
		else
			addItem.setText("+");
	}
}
