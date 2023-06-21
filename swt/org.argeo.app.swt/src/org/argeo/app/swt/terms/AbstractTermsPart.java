package org.argeo.app.swt.terms;

import org.argeo.api.acr.Content;
import org.argeo.api.cms.ux.CmsIcon;
import org.argeo.app.api.Term;
import org.argeo.app.api.TermsManager;
import org.argeo.app.api.Typology;
import org.argeo.cms.Localized;
import org.argeo.cms.swt.CmsSwtTheme;
import org.argeo.cms.swt.CmsSwtUtils;
import org.argeo.cms.swt.SwtEditablePart;
import org.argeo.cms.swt.widgets.ContextOverlay;
import org.argeo.cms.swt.widgets.StyledControl;
import org.argeo.cms.ux.acr.ContentPart;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolItem;

/** Common logic between single and mutliple terms editable part. */
public abstract class AbstractTermsPart extends StyledControl implements SwtEditablePart, ContentPart {
	private static final long serialVersionUID = -5497097995341927710L;
	protected final TermsManager termsManager;
	protected final Typology typology;

	private final boolean editable;

	private CmsIcon deleteIcon;
	private CmsIcon addIcon;
	private CmsIcon cancelIcon;

	private Color highlightColor;
	private Composite highlight;

	private boolean canDelete = true;

	protected final CmsSwtTheme theme;

	@SuppressWarnings("rawtypes")
	private Class<? extends Enum> localized;

	public AbstractTermsPart(Composite parent, int style, Content item, TermsManager termsManager, String typology) {
		super(parent, style);
		if (item == null)
			throw new IllegalArgumentException("Item cannot be null");
		setData(item);
		this.termsManager = termsManager;
		this.typology = termsManager.getTypology(typology);
		this.theme = CmsSwtUtils.getCmsTheme(parent);
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
		if (term instanceof Localized) {
			return ((Localized) term).lead();
		} else {
			if (localized != null) {
				@SuppressWarnings("unchecked")
				String msg = ((Localized) Enum.valueOf(localized, term.getName())).lead();
				return msg;
			} else {
				return term.getName();
			}
		}

	}

	protected abstract void refresh(ContextOverlay contextArea, String filter, Text txt);

	protected boolean isTermSelectable(Term term) {
		return true;
	}

	protected void processTermListLabel(Term term, Label label) {

	}

	protected void setControlLayoutData(Control control) {
		control.setLayoutData(CmsSwtUtils.fillAll());
	}

	protected void setContainerLayoutData(Composite composite) {
		composite.setLayoutData(CmsSwtUtils.fillAll());
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
			deleteItem.setImage(theme.getSmallIcon(deleteIcon));
		else
			deleteItem.setText("-");
	}

	protected void styleCancel(ToolItem cancelItem) {
		if (cancelIcon != null)
			cancelItem.setImage(theme.getSmallIcon(cancelIcon));
		else
			cancelItem.setText("X");
	}

	protected void styleAdd(ToolItem addItem) {
		if (addIcon != null)
			addItem.setImage(theme.getSmallIcon(addIcon));
		else
			addItem.setText("+");
	}

	@Override
	public Content getContent() {
		return (Content) getData();
	}

	public void setCanDelete(boolean canDelete) {
		this.canDelete = canDelete;
	}

	public boolean isCanDelete() {
		return canDelete;
	}

	@SuppressWarnings("rawtypes")
	public void setLocalized(Class<? extends Enum> localized) {
		this.localized = localized;
	}

}
