package org.argeo.app.swt.terms;

import java.util.List;

import org.argeo.api.acr.Content;
import org.argeo.app.api.Term;
import org.argeo.app.api.TermsManager;
import org.argeo.app.swt.forms.FormStyle;
import org.argeo.cms.swt.CmsSwtUtils;
import org.argeo.cms.swt.MouseDoubleClick;
import org.argeo.cms.swt.MouseDown;
import org.argeo.cms.swt.Selected;
import org.argeo.cms.swt.SwtEditablePart;
import org.argeo.cms.swt.widgets.ContextOverlay;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;

/** {@link SwtEditablePart} for terms. */
public class SingleTermPart extends AbstractTermsPart {
	private static final long serialVersionUID = -4961135649177920808L;

	public SingleTermPart(Composite parent, int style, Content item, TermsManager termsManager, String typology) {
		super(parent, style, item, termsManager, typology);
	}

	@Override
	protected Control createControl(Composite box, String style) {
		if (isEditing()) {
			Composite block = new Composite(box, SWT.NONE);
			block.setLayout(CmsSwtUtils.noSpaceGridLayout(3));

			createHighlight(block);

			Text txt = new Text(block, SWT.SINGLE | SWT.BORDER);
			CmsSwtUtils.style(txt, style == null ? FormStyle.propertyText.style() : style);

			ToolBar toolBar = new ToolBar(block, SWT.HORIZONTAL);
			if (isCanDelete()) {
				ToolItem deleteItem = new ToolItem(toolBar, SWT.PUSH);
				styleDelete(deleteItem);
				deleteItem.addSelectionListener((Selected) (e) -> {
					setValue(null);
					stopEditing();
				});
			}
			ToolItem cancelItem = new ToolItem(toolBar, SWT.PUSH);
			styleCancel(cancelItem);
			cancelItem.addSelectionListener((Selected) (e) -> {
				stopEditing();
			});

			ContextOverlay contextOverlay = new ContextOverlay(txt, SWT.NONE) {
				private static final long serialVersionUID = -7980078594405384874L;

				@Override
				protected void onHide() {
					stopEditing();
				}
			};
			contextOverlay.setLayout(new GridLayout());
			// filter
			txt.addModifyListener((e) -> {
				String filter = txt.getText().toLowerCase();
				if ("".equals(filter.trim()))
					filter = null;
				refresh(contextOverlay, filter, txt);
			});
			txt.addFocusListener(new FocusListener() {
				private static final long serialVersionUID = -6024501573409619949L;

				@Override
				public void focusLost(FocusEvent event) {
//					if (!contextOverlay.isDisposed() && contextOverlay.isShellVisible())
//						getDisplay().asyncExec(() -> stopEditing());
				}

				@Override
				public void focusGained(FocusEvent event) {
					// txt.setText("");
					if (!contextOverlay.isDisposed() && !contextOverlay.isShellVisible())
						refresh(contextOverlay, null, txt);
				}
			});
			layout(new Control[] { block });
			getDisplay().asyncExec(() -> txt.setFocus());
			return block;
		} else {
			Composite block = new Composite(box, SWT.NONE);
			block.setLayout(CmsSwtUtils.noSpaceGridLayout(2));
			Term currentValue = getValue();
			if (currentValue != null) {
				Label lbl = new Label(block, SWT.SINGLE);
				String display = getTermLabel(currentValue);
				lbl.setText(display);
				CmsSwtUtils.style(lbl, style == null ? FormStyle.propertyText.style() : style);
				processTermListLabel(currentValue, lbl);
				if (isEditable()) {
					lbl.addMouseListener((MouseDoubleClick) (e) -> {
						startEditing();
					});
				}
			} else {
				if (isEditable()) {
					ToolBar toolBar = new ToolBar(block, SWT.HORIZONTAL);
					ToolItem addItem = new ToolItem(toolBar, SWT.FLAT);
					styleAdd(addItem);
					addItem.addSelectionListener((Selected) (e) -> {
						startEditing();
					});
				}
			}
			return block;
		}
	}

	@Override
	protected void refresh(ContextOverlay contextArea, String filter, Text txt) {
		CmsSwtUtils.clear(contextArea);
		List<? extends Term> terms = termsManager.listAllTerms(typology.getId());
		terms: for (Term term : terms) {
			String display = getTermLabel(term);
			if (filter != null && !display.toLowerCase().contains(filter))
				continue terms;
			Label termL = new Label(contextArea, SWT.WRAP);
			termL.setText(display);
			processTermListLabel(term, termL);
			if (isTermSelectable(term))
				termL.addMouseListener((MouseDown) (e) -> {
					setValue(term);
					contextArea.hide();
					stopEditing();
				});
		}
		contextArea.show();
		// txt.setFocus();
	}

	protected Term getValue() {
		String property = typology.getId();
		String id = getContent().attr(property);
		Term term = termsManager.getTerm(id);

		return term;
	}

	protected void setValue(Term value) {
		String property = typology.getId();
		if (value == null)
			getContent().remove(property);
		else
			getContent().put(property, value.getId());
//		Jcr.set(getNode(), property, value != null ? value.getId() : null);
//		Jcr.save(getNode());
	}
}
