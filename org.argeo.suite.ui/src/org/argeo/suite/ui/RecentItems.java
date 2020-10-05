package org.argeo.suite.ui;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.argeo.cms.ui.CmsTheme;
import org.argeo.cms.ui.CmsUiProvider;
import org.argeo.cms.ui.util.CmsUiUtils;
import org.argeo.connect.ui.widgets.DelayedText;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

/** List recent items. */
public class RecentItems implements CmsUiProvider {
	int SEARCH_TEXT_DELAY = 800;
	private CmsTheme theme;

	@Override
	public Control createUi(Composite parent, Node context) throws RepositoryException {
		theme = CmsTheme.getCmsTheme(parent);
		parent.setLayout(new GridLayout());

		Composite top = new Composite(parent, SWT.NONE);
		top.setLayoutData(CmsUiUtils.fillWidth());
		top.setLayout(new GridLayout(2, false));

		Label search = new Label(top, SWT.NONE);
		search.setImage(SuiteIcon.search.getSmallIcon(theme));
		Text text = new Text(top, SWT.SINGLE | SWT.BORDER);
		text.setLayoutData(CmsUiUtils.fillWidth());
//		DelayedText delayedText = new DelayedText(top, SWT.SINGLE | SWT.BORDER, SEARCH_TEXT_DELAY);
//		delayedText.getText().setLayoutData(CmsUiUtils.fillWidth());

		TableViewer viewer = new TableViewer(parent);
		viewer.getTable().setLayoutData(CmsUiUtils.fillAll());
		return text;
	}

}
