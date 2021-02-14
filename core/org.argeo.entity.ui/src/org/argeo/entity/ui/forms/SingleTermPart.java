package org.argeo.entity.ui.forms;

import java.util.List;

import javax.jcr.Item;

import org.argeo.cms.ui.forms.FormStyle;
import org.argeo.cms.ui.util.CmsUiUtils;
import org.argeo.cms.ui.viewers.EditablePart;
import org.argeo.cms.ui.widgets.ContextOverlay;
import org.argeo.eclipse.ui.MouseDoubleClick;
import org.argeo.eclipse.ui.MouseDown;
import org.argeo.eclipse.ui.Selected;
import org.argeo.entity.Term;
import org.argeo.entity.TermsManager;
import org.argeo.jcr.Jcr;
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

/** {@link EditablePart} for terms. */
public class SingleTermPart extends AbstractTermsPart {
	private static final long serialVersionUID = -4961135649177920808L;

	public SingleTermPart(Composite parent, int style, Item item, TermsManager termsManager, String typology) {
		super(parent, style, item, termsManager, typology);
	}

	@Override
	protected Control createControl(Composite box, String style) {
		if (isEditing()) {
			Composite block = new Composite(box, SWT.NONE);
			block.setLayout(CmsUiUtils.noSpaceGridLayout(3));

			createHighlight(block);

			Text txt = new Text(block, SWT.SINGLE | SWT.BORDER);
			CmsUiUtils.style(txt, style == null ? FormStyle.propertyText.style() : style);

			ToolBar toolBar = new ToolBar(block, SWT.HORIZONTAL);
			ToolItem deleteItem = new ToolItem(toolBar, SWT.PUSH);
			styleDelete(deleteItem);
			deleteItem.addSelectionListener((Selected) (e) -> {
				setValue(null);
				stopEditing();
			});
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
			block.setLayout(CmsUiUtils.noSpaceGridLayout(2));
			Term currentValue = getValue();
			if (currentValue != null) {
				Label lbl = new Label(block, SWT.SINGLE);
				String display = getTermLabel(currentValue);
				lbl.setText(display);
				CmsUiUtils.style(lbl, style == null ? FormStyle.propertyText.style() : style);
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
		CmsUiUtils.clear(contextArea);
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
		String id = Jcr.get(getNode(), property);
		Term term = termsManager.getTerm(id);

		return term;
	}

	protected void setValue(Term value) {
		String property = typology.getId();
		Jcr.set(getNode(), property, value != null ? value.getId() : null);
		Jcr.save(getNode());
	}
}
