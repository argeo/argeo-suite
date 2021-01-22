package org.argeo.entity.ui.forms;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.Item;

import org.argeo.cms.ui.forms.FormStyle;
import org.argeo.cms.ui.util.CmsUiUtils;
import org.argeo.cms.ui.viewers.EditablePart;
import org.argeo.cms.ui.widgets.ContextOverlay;
import org.argeo.eclipse.ui.MouseDoubleClick;
import org.argeo.eclipse.ui.MouseDown;
import org.argeo.eclipse.ui.Selected;
import org.argeo.entity.TermsManager;
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

	public MultiTermsPart(Composite parent, int style, Item item, TermsManager termsManager, String typology) {
		super(parent, style, item, termsManager, typology);
	}

	@Override
	protected Control createControl(Composite box, String style) {
		Composite placeholder = new Composite(box, SWT.NONE);
		RowLayout rl = new RowLayout(SWT.HORIZONTAL | SWT.WRAP);
		placeholder.setLayout(rl);
		List<String> currentValue = Jcr.getMultiple(getNode(), typology);
		if (currentValue != null && !currentValue.isEmpty())
			for (String value : currentValue) {
				Composite block = new Composite(placeholder, SWT.NONE);
				block.setLayout(CmsUiUtils.noSpaceGridLayout(3));
				Label lbl = new Label(block, SWT.SINGLE);
				String display = getTermLabel(value);
				lbl.setText(display);
				CmsUiUtils.style(lbl, style == null ? FormStyle.propertyText.style() : style);
				if (editable)
					lbl.addMouseListener((MouseDoubleClick) (e) -> {
						startEditing();
					});
				if (isEditing()) {
					ToolBar toolBar = new ToolBar(block, SWT.HORIZONTAL);
					ToolItem deleteItem = new ToolItem(toolBar, SWT.FLAT);
					styleDelete(deleteItem);
					deleteItem.addSelectionListener((Selected) (e) -> {
						List<String> newValue = new ArrayList<>();
						for (String v : currentValue) {
							if (!v.equals(value))
								newValue.add(v);
						}
						Jcr.set(getNode(), typology, newValue);
						Jcr.save(getNode());
						block.dispose();
						layout(true, true);
					});

				}
			}
		else {// empty
			if (editable && !isEditing()) {
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
			block.setLayout(CmsUiUtils.noSpaceGridLayout(3));

			createHighlight(block);

			Text txt = new Text(block, SWT.SINGLE | SWT.BORDER);
			txt.setLayoutData(CmsUiUtils.fillWidth());
//			txt.setMessage("[new]");

			CmsUiUtils.style(txt, style == null ? FormStyle.propertyText.style() : style);

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
		CmsUiUtils.clear(contextArea);
		List<String> terms = termsManager.listAllTerms(typology);
		List<String> currentValue = Jcr.getMultiple(getNode(), typology);
		terms: for (String term : terms) {
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
					List<String> newValue = new ArrayList<>();
					if (currentValue != null)
						newValue.addAll(currentValue);
					newValue.add(term);
					Jcr.set(getNode(), typology, newValue);
					Jcr.save(getNode());
					contextArea.hide();
					stopEditing();
				});
		}
		contextArea.show();
	}

}
