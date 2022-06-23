package org.argeo.app.ui.forms;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.Item;

import org.argeo.api.cms.CmsLog;
import org.argeo.app.api.Term;
import org.argeo.app.api.TermsManager;
import org.argeo.cms.swt.CmsSwtUtils;
import org.argeo.cms.swt.EditablePart;
import org.argeo.cms.swt.MouseDoubleClick;
import org.argeo.cms.swt.MouseDown;
import org.argeo.cms.swt.Selected;
import org.argeo.cms.swt.widgets.ContextOverlay;
import org.argeo.cms.ui.forms.FormStyle;
import org.argeo.jcr.Jcr;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;

/** {@link EditablePart} for multiple terms. */
public class MultiTermsPart extends AbstractTermsPart {
	private static final long serialVersionUID = -4961135649177920808L;
	private final static CmsLog log = CmsLog.getLog(MultiTermsPart.class);

	public MultiTermsPart(Composite parent, int style, Item item, TermsManager termsManager, String typology) {
		super(parent, style, item, termsManager, typology);
	}

	@Override
	protected Control createControl(Composite box, String style) {
		Composite placeholder = new Composite(box, SWT.NONE);

		boolean vertical = SWT.VERTICAL == (getStyle() & SWT.VERTICAL);
		RowLayout rl = new RowLayout(vertical ? SWT.VERTICAL : SWT.HORIZONTAL);
		rl = CmsSwtUtils.noMarginsRowLayout(rl);
//		rl.wrap = true;
//		rl.justify = true;
		placeholder.setLayout(rl);
		List<Term> currentValue = getValue();
		if (currentValue != null && !currentValue.isEmpty()) {
			for (Term value : currentValue) {
				Composite block = new Composite(placeholder, SWT.NONE);
				block.setLayout(CmsSwtUtils.noSpaceGridLayout(3));
				Label lbl = new Label(block, SWT.NONE);
				String display = getTermLabel(value);
				lbl.setText(display);
				CmsSwtUtils.style(lbl, style == null ? FormStyle.propertyText.style() : style);
				processTermListLabel(value, lbl);
				if (isEditable())
					lbl.addMouseListener((MouseDoubleClick) (e) -> {
						startEditing();
					});
				if (isEditing()) {
					ToolBar toolBar = new ToolBar(block, SWT.HORIZONTAL);
					ToolItem deleteItem = new ToolItem(toolBar, SWT.FLAT);
					styleDelete(deleteItem);
					deleteItem.addSelectionListener((Selected) (e) -> {
						// we retrieve them again here because they may have changed
						List<Term> curr = getValue();
						List<Term> newValue = new ArrayList<>();
						for (Term v : curr) {
							if (!v.equals(value))
								newValue.add(v);
						}
						setValue(newValue);
						block.dispose();
						layout(true, true);
					});

				}
			}
		} else {// empty
			if (isEditable() && !isEditing()) {
				ToolBar toolBar = new ToolBar(placeholder, SWT.HORIZONTAL);
				ToolItem addItem = new ToolItem(toolBar, SWT.FLAT);
				styleAdd(addItem);
				addItem.addSelectionListener((Selected) (e) -> {
					startEditing();
				});
			}
		}

		if (isEditing()) {
			Composite block = new Composite(placeholder, SWT.NONE);
			block.setLayout(CmsSwtUtils.noSpaceGridLayout(3));

			createHighlight(block);

			Text txt = new Text(block, SWT.SINGLE | SWT.BORDER);
			txt.setLayoutData(CmsSwtUtils.fillWidth());
//			txt.setMessage("[new]");

			CmsSwtUtils.style(txt, style == null ? FormStyle.propertyText.style() : style);

			ToolBar toolBar = new ToolBar(block, SWT.HORIZONTAL);
			ToolItem cancelItem = new ToolItem(toolBar, SWT.FLAT);
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
			layout(new Control[] { txt });
			// getDisplay().asyncExec(() -> txt.setFocus());
		}
		return placeholder;
	}

	@Override
	protected void refresh(ContextOverlay contextArea, String filter, Text txt) {
		CmsSwtUtils.clear(contextArea);
		List<? extends Term> terms = termsManager.listAllTerms(typology.getId());
		List<Term> currentValue = getValue();
		terms: for (Term term : terms) {
			if (currentValue != null && currentValue.contains(term))
				continue terms;
			String display = getTermLabel(term);
			if (filter != null && !display.toLowerCase().contains(filter))
				continue terms;
			Label termL = new Label(contextArea, SWT.WRAP);
			termL.setText(display);
			processTermListLabel(term, termL);
			if (isTermSelectable(term))
				termL.addMouseListener((MouseDown) (e) -> {
					List<Term> newValue = new ArrayList<>();
					List<Term> curr = getValue();
					if (currentValue != null)
						newValue.addAll(curr);
					newValue.add(term);
					setValue(newValue);
					contextArea.hide();
					stopEditing();
				});
		}
		contextArea.show();
	}

	protected List<Term> getValue() {
		String property = typology.getId();
		List<String> curr = Jcr.getMultiple(getNode(), property);
		List<Term> res = new ArrayList<>();
		if (curr != null)
			terms: for (String str : curr) {
				Term term = termsManager.getTerm(str);
				if (term == null) {
					log.warn("Ignoring term " + str + " for " + getNode() + ", as it was not found.");
					continue terms;
				}
				res.add(term);
			}
		return res;
	}

	protected void setValue(List<Term> value) {
		String property = typology.getId();
		List<String> ids = new ArrayList<>();
		for (Term term : value) {
			ids.add(term.getId());
		}
		Jcr.set(getNode(), property, ids);
		Jcr.save(getNode());
	}

}
