package org.argeo.app.swt.docbook;

import static org.argeo.app.docbook.DbkAcrUtils.isDbk;
import static org.argeo.app.docbook.DbkType.para;

import java.util.Optional;

import org.argeo.api.acr.Content;
import org.argeo.api.cms.ux.CmsEditable;
import org.argeo.app.docbook.DbkAttr;
import org.argeo.app.docbook.DbkType;
import org.argeo.cms.swt.CmsSwtUtils;
import org.argeo.cms.swt.SwtEditablePart;
import org.argeo.cms.swt.acr.AbstractPageViewer;
import org.argeo.cms.swt.acr.SwtSection;
import org.argeo.cms.swt.acr.SwtSectionPart;
import org.argeo.cms.swt.widgets.EditableText;
import org.argeo.cms.swt.widgets.StyledControl;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

public class DocBookViewer extends AbstractPageViewer {

	private TextInterpreter textInterpreter = new DbkTextInterpreter();

	private TextSection mainSection;

	public DocBookViewer(Composite parent, int style, Content item, CmsEditable cmsEditable) {
		super(parent, style, cmsEditable);
		for (Content child : item) {
			if (child.hasContentClass(DbkType.article.qName())) {
				if (mainSection != null)
					throw new IllegalStateException("Main section already created");
				mainSection = new TextSection(parent, 0, child);
				mainSection.setLayoutData(CmsSwtUtils.fillAll());
			}
		}
	}

	@Override
	protected void refresh(Control control) {
		if (!(control instanceof SwtSection))
			return;
		long begin = System.currentTimeMillis();
		SwtSection section = (SwtSection) control;
		if (section instanceof TextSection) {
			CmsSwtUtils.clear(mainSection);
			refreshTextSection(mainSection);

		}
		long duration = System.currentTimeMillis() - begin;
//		System.out.println(duration + " ms - " + DbkUtils.getTitle(section.getNode()));

	}

	protected void refreshTextSection(TextSection section) {
		for (Content child : section.getContent()) {
			if (child.hasContentClass(DbkType.section.qName())) {
				TextSection childSection = new TextSection(section, 0, child);
				childSection.setLayoutData(CmsSwtUtils.fillAll());
				refreshTextSection(childSection);
			} else if (child.hasContentClass(DbkType.para.qName())) {
				Paragraph para = new Paragraph(section, 0, child);
				para.setLayoutData(CmsSwtUtils.fillWidth());
				updateContent(para);
			}

		}
	}

	protected void updateContent(SwtEditablePart part) {
		if (part instanceof SwtSectionPart) {
			SwtSectionPart sectionPart = (SwtSectionPart) part;
			Content partContent = sectionPart.getContent();

			if (part instanceof StyledControl && (sectionPart.getSection() instanceof TextSection)) {
				TextSection section = (TextSection) sectionPart.getSection();
				StyledControl styledControl = (StyledControl) part;
				if (isDbk(partContent, para)) {
					Optional<String> roleAttr = partContent.get(DbkAttr.role.qName(), String.class);
					String style = roleAttr.orElse(section.getDefaultTextStyle());
					styledControl.setStyle(style);
				}
			}
			// use control AFTER setting style, since it may have been reset

			if (part instanceof EditableText) {
				EditableText paragraph = (EditableText) part;
				if (paragraph == getEdited())
					paragraph.setText(textInterpreter.raw(partContent));
				else
					paragraph.setText(textInterpreter.raw(partContent));
					//paragraph.setText(textInterpreter.readSimpleHtml(partContent));
				
//			} else if (part instanceof DbkImg) {
//				DbkImg editableImage = (DbkImg) part;
//				// imageManager.load(partNode, part.getControl(),
//				// editableImage.getPreferredImageSize());
//			} else if (part instanceof DbkVideo) {
//				DbkVideo video = (DbkVideo) part;
//				video.load(part.getControl());
//			}
			}
		}
//			else if (part instanceof DbkSectionTitle) {
//			DbkSectionTitle title = (DbkSectionTitle) part;
//			title.setStyle(title.getSection().getTitleStyle());
//			// use control AFTER setting style
//			if (title == getEdited())
//				title.setText(textInterpreter.read(title.getNode()));
//			else
//				title.setText(textInterpreter.readSimpleHtml(title.getNode()));
//		}
	}

	@Override
	public Control getControl() {
		return mainSection;
	}

}
