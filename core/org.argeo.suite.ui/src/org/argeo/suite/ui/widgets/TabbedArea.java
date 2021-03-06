package org.argeo.suite.ui.widgets;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;

import org.argeo.cms.ui.CmsUiProvider;
import org.argeo.cms.ui.util.CmsUiUtils;
import org.argeo.cms.ui.viewers.Section;
import org.argeo.eclipse.ui.Selected;
import org.argeo.jcr.Jcr;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;

/** Manages {@link Section} in a tab-like structure. */
public class TabbedArea extends Composite {
	private static final long serialVersionUID = 8659669229482033444L;

	private Composite headers;
	private Composite body;

	private List<Section> sections = new ArrayList<>();

	private Node previousNode;
	private CmsUiProvider previousUiProvider;
	private CmsUiProvider currentUiProvider;

	private String tabStyle;
	private String tabSelectedStyle;
	private String bodyStyle;
	private Image closeIcon;

	private StackLayout stackLayout;

	private boolean singleTab = false;

	public TabbedArea(Composite parent, int style) {
		super(parent, style);
		CmsUiUtils.style(parent, bodyStyle);

		setLayout(CmsUiUtils.noSpaceGridLayout());

		// TODO manage tabs at bottom or sides
		headers = new Composite(this, SWT.NONE);
		headers.setLayoutData(CmsUiUtils.fillWidth());
		body = new Composite(this, SWT.NONE);
		body.setLayoutData(CmsUiUtils.fillAll());
		// body.setLayout(new FormLayout());
		stackLayout = new StackLayout();
		body.setLayout(stackLayout);
		emptyState();
	}

	protected void refreshTabHeaders() {
		int tabCount = sections.size() > 0 ? sections.size() : 1;
		for (Control tab : headers.getChildren())
			tab.dispose();

		headers.setLayout(CmsUiUtils.noSpaceGridLayout(new GridLayout(tabCount, true)));

		if (sections.size() == 0) {
			Composite emptyHeader = new Composite(headers, SWT.NONE);
			emptyHeader.setLayoutData(CmsUiUtils.fillAll());
			emptyHeader.setLayout(new GridLayout());
			Label lbl = new Label(emptyHeader, SWT.NONE);
			lbl.setText("");
			lbl.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, false));

		}

		Section currentSection = getCurrentSection();
		for (Section section : sections) {
			boolean selected = section == currentSection;
			Composite sectionHeader = section.createHeader(headers);
			CmsUiUtils.style(sectionHeader, selected ? tabSelectedStyle : tabStyle);
			int headerColumns = singleTab ? 1 : 2;
			sectionHeader.setLayout(new GridLayout(headerColumns, false));
			sectionHeader.setLayout(CmsUiUtils.noSpaceGridLayout(headerColumns));
			Button title = new Button(sectionHeader, SWT.FLAT);
			CmsUiUtils.style(title, selected ? tabSelectedStyle : tabStyle);
			title.setLayoutData(CmsUiUtils.fillWidth());
			title.addSelectionListener((Selected) (e) -> showTab(tabIndex(section.getNode())));
			Node node = section.getNode();
			String titleStr = Jcr.getTitle(node);
			// TODO internationalize
			title.setText(titleStr);
			if (!singleTab) {
				ToolBar toolBar = new ToolBar(sectionHeader, SWT.NONE);
				ToolItem closeItem = new ToolItem(toolBar, SWT.FLAT);
				if (closeIcon != null)
					closeItem.setImage(closeIcon);
				else
					closeItem.setText("X");
				CmsUiUtils.style(closeItem, selected ? tabSelectedStyle : tabStyle);
				closeItem.addSelectionListener((Selected) (e) -> closeTab(section));
			}
		}

	}

	public void view(CmsUiProvider uiProvider, Node context) {
		if (body.isDisposed())
			return;
		int index = tabIndex(context);
		if (index >= 0) {
			showTab(index);
			previousNode = context;
			previousUiProvider = uiProvider;
			return;
		}
		Section section = (Section) body.getChildren()[0];
		previousNode = section.getNode();
		if (previousNode == null) {// empty state
			previousNode = context;
			previousUiProvider = uiProvider;
		} else {
			previousUiProvider = currentUiProvider;
		}
		currentUiProvider = uiProvider;
		section.setNode(context);
		// section.setLayoutData(CmsUiUtils.coverAll());
		build(section, uiProvider, context);
		if (sections.size() == 0)
			sections.add(section);
		refreshTabHeaders();
		index = tabIndex(context);
		showTab(index);
		layout(true, true);
	}

	public void open(CmsUiProvider uiProvider, Node context) {
		if (singleTab)
			throw new UnsupportedOperationException("Open is not supported in single tab mode.");

		if (previousNode != null && Jcr.getIdentifier(previousNode).equals(Jcr.getIdentifier(context))) {
			// does nothing
			return;
		}
		if (sections.size() == 0)
			CmsUiUtils.clear(body);
		Section currentSection = getCurrentSection();
		int currentIndex = sections.indexOf(currentSection);
		Section previousSection = new Section(body, SWT.NONE, context);
		build(previousSection, previousUiProvider, previousNode);
		// previousSection.setLayoutData(CmsUiUtils.coverAll());
		int index = currentIndex + 1;
		sections.add(index, previousSection);
		showTab(index);
		refreshTabHeaders();
		layout(true, true);
	}

	public void showTab(int index) {
		Section sectionToShow = sections.get(index);
		// sectionToShow.moveAbove(null);
		stackLayout.topControl = sectionToShow;
		refreshTabHeaders();
		layout(true, true);
	}

	protected void build(Section section, CmsUiProvider uiProvider, Node context) {
		for (Control child : section.getChildren())
			child.dispose();
		CmsUiUtils.style(section, bodyStyle);
		section.setNode(context);
		uiProvider.createUiPart(section, context);

	}

	private int tabIndex(Node node) {
		for (int i = 0; i < sections.size(); i++) {
			Section section = sections.get(i);
			if (Jcr.getIdentifier(section.getNode()).equals(Jcr.getIdentifier(node)))
				return i;
		}
		return -1;
	}

	public void closeTab(Section section) {
		int currentIndex = sections.indexOf(section);
		int nextIndex = currentIndex == 0 ? 0 : currentIndex - 1;
		sections.remove(section);
		section.dispose();
		if (sections.size() == 0) {
			emptyState();
			refreshTabHeaders();
			layout(true, true);
			return;
		}
		refreshTabHeaders();
		showTab(nextIndex);
	}

	protected void emptyState() {
		new Section(body, SWT.NONE, null);
		refreshTabHeaders();
	}

	public Composite getCurrent() {
		return getCurrentSection();
	}

	protected Section getCurrentSection() {
		return (Section) stackLayout.topControl;
	}

	public void setTabStyle(String tabStyle) {
		this.tabStyle = tabStyle;
	}

	public void setTabSelectedStyle(String tabSelectedStyle) {
		this.tabSelectedStyle = tabSelectedStyle;
	}

	public void setBodyStyle(String bodyStyle) {
		this.bodyStyle = bodyStyle;
	}

	public void setCloseIcon(Image closeIcon) {
		this.closeIcon = closeIcon;
	}

	public void setSingleTab(boolean singleTab) {
		this.singleTab = singleTab;
	}

}
