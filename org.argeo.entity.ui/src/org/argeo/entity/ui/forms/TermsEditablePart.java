package org.argeo.entity.ui.forms;

import java.util.List;

import javax.jcr.Item;

import org.argeo.cms.ui.forms.FormStyle;
import org.argeo.cms.ui.util.CmsUiUtils;
import org.argeo.cms.ui.viewers.EditablePart;
import org.argeo.cms.ui.widgets.ContextOverlay;
import org.argeo.cms.ui.widgets.StyledControl;
import org.argeo.eclipse.ui.MouseDoubleClick;
import org.argeo.eclipse.ui.MouseDown;
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

/** {@link EditablePart} for terms. */
public class TermsEditablePart extends StyledControl implements EditablePart {
	private static final long serialVersionUID = -4961135649177920808L;
	private TermsManager termsManager;
	private String typology;

	public TermsEditablePart(Composite parent, int style, Item item, TermsManager termsManager, String typology) {
		super(parent, style, item);
		this.termsManager = termsManager;
		this.typology = typology;
	}

	@Override
	protected Control createControl(Composite box, String style) {
		if (isEditing()) {
			Text txt = new Text(box, SWT.SINGLE | SWT.BORDER);
			CmsUiUtils.style(txt, style == null ? FormStyle.propertyText.style() : style);

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
			getDisplay().asyncExec(() -> txt.setFocus());
			return txt;
		} else {
			Label lbl = new Label(box, SWT.SINGLE);
		//	lbl.setEditable(false);
			String currentValue = Jcr.get(getNode(), typology);
			if (currentValue != null) {
				String display = getTermLabel(currentValue);
				lbl.setText(display);
			} else
				lbl.setText("[" + typology + "]");
			CmsUiUtils.style(lbl, style == null ? FormStyle.propertyText.style() : style);

			lbl.addMouseListener((MouseDoubleClick) (e) -> {
				startEditing();
			});
			return lbl;
		}
	}

	protected String getTermLabel(String name) {
		return name;
	}

	protected void refresh(ContextOverlay contextArea, String filter, Text txt) {
		CmsUiUtils.clear(contextArea);
		List<String> terms = termsManager.listAllTerms(typology);
		terms: for (String term : terms) {
			String display = getTermLabel(term);
			if (filter != null && !display.toLowerCase().contains(filter))
				continue terms;
			Label termL = new Label(contextArea, SWT.WRAP);
			termL.setText(display);
			termL.addMouseListener((MouseDown) (e) -> {
				Jcr.set(getNode(), typology, term);
				Jcr.save(getNode());
				contextArea.hide();
				stopEditing();
			});
		}
		contextArea.show();
		// txt.setFocus();
	}

}
