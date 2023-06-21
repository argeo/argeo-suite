package org.argeo.app.ui;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.apache.commons.io.IOUtils;
import org.argeo.api.cms.ux.CmsEditable;
import org.argeo.api.cms.ux.CmsStyle;
import org.argeo.app.api.EntityNames;
import org.argeo.app.api.EntityType;
import org.argeo.app.swt.ux.SuiteSwtUtils;
import org.argeo.app.swt.ux.SwtArgeoApp;
import org.argeo.app.ux.SuiteUxEvent;
import org.argeo.cms.jcr.acr.JcrContent;
import org.argeo.cms.swt.CmsSwtUtils;
import org.argeo.cms.swt.dialogs.LightweightDialog;
import org.argeo.cms.ui.util.CmsLink;
import org.argeo.cms.ui.util.CmsUiUtils;
import org.argeo.jcr.Jcr;
import org.argeo.jcr.JcrUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

/** UI utilities around SWT and JCR. */
@Deprecated
public class SuiteUiUtils {
	public static Text addFormLine(Composite parent, String label, Node node, String property,
			CmsEditable cmsEditable) {
		Composite lineComposite = SuiteSwtUtils.addLineComposite(parent, 2);
		SuiteSwtUtils.addFormLabel(lineComposite, label);
		String text = Jcr.get(node, property);
		Text txt = SuiteSwtUtils.addFormTextField(lineComposite, text, null, SWT.WRAP);
		if (cmsEditable != null && cmsEditable.isEditing()) {
			txt.addModifyListener((e) -> {
				Jcr.set(node, property, txt.getText());
				Jcr.save(node);
			});
		} else {
			txt.setEditable(false);
		}
		txt.setLayoutData(CmsSwtUtils.fillWidth());
		return txt;
	}

	public static Text addFormColumn(Composite parent, String label, Node node, String property,
			CmsEditable cmsEditable) {
		SuiteSwtUtils.addFormLabel(parent, label);
		String text = Jcr.get(node, property);
		Text txt = SuiteSwtUtils.addFormTextField(parent, text, null, SWT.WRAP);
		if (cmsEditable != null && cmsEditable.isEditing()) {
			txt.addModifyListener((e) -> {
				Jcr.set(node, property, txt.getText());
				Jcr.save(node);
			});
		} else {
			txt.setEditable(false);
		}
		txt.setLayoutData(CmsSwtUtils.fillWidth());
		return txt;
	}

//	public static Label addFormPicture(Composite parent, String label, Node fileNode) throws RepositoryException {
//		Composite lineComposite = SuiteSwtUtils.addLineComposite(parent, 2);
//		SuiteSwtUtils.addFormLabel(lineComposite, label);
//
//		return addPicture(lineComposite, fileNode);
//	}

	public static Label addPicture(Composite parent, Node fileNode) throws RepositoryException {
		return addPicture(parent, fileNode, null);
	}

	public static Label addPicture(Composite parent, Node fileNode, Integer maxWidth) throws RepositoryException {
		return addPicture(parent, fileNode, maxWidth, null);
	}

	public static Label addPicture(Composite parent, Node fileNode, Integer maxWidth, Node link)
			throws RepositoryException {
		Node content = fileNode.getNode(Node.JCR_CONTENT);

		boolean test = false;
		if (test) {
			try (InputStream in = JcrUtils.getFileAsStream(fileNode);
					OutputStream out = Files.newOutputStream(
							Paths.get(System.getProperty("user.home") + "/tmp/" + fileNode.getName()));) {
//				BufferedImage img = ImageIO.read(in);
//				System.out.println(fileNode.getName() + ": width=" + img.getWidth() + ", height=" + img.getHeight());
				IOUtils.copy(in, out);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}

//			try (InputStream in = JcrUtils.getFileAsStream(fileNode);) {
//				ImageData imageData = new ImageData(in);
//				System.out.println(fileNode.getName() + ": width=" + imageData.width + ", height=" + imageData.height);
//			} catch (IOException e) {
//				throw new RuntimeException(e);
//			}
		}
		// TODO move it deeper in the middleware.
		if (!content.isNodeType(EntityType.box.get())) {
			if (content.getSession().hasPermission(content.getPath(), Session.ACTION_SET_PROPERTY)) {
				try (InputStream in = JcrUtils.getFileAsStream(fileNode)) {
					ImageData imageData = new ImageData(in);
					content.addMixin(EntityType.box.get());
					content.setProperty(EntityNames.SVG_WIDTH, imageData.width);
					content.setProperty(EntityNames.SVG_HEIGHT, imageData.height);
					content.getSession().save();
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
		}

		// TODO optimise
		Long width;
		Long height;
		if (content.isNodeType(EntityType.box.get())) {
			width = content.getProperty(EntityNames.SVG_WIDTH).getLong();
			height = content.getProperty(EntityNames.SVG_HEIGHT).getLong();
		} else {
			try (InputStream in = JcrUtils.getFileAsStream(fileNode)) {
				ImageData imageData = new ImageData(in);
				width = Long.valueOf(imageData.width);
				height = Long.valueOf(imageData.height);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}

		if (maxWidth != null && width > maxWidth) {
			Double ratio = maxWidth.doubleValue() / width.doubleValue();
			width = maxWidth.longValue();
			height = Math.round(ratio * height);
		}
		Label img = new Label(parent, SWT.NONE);
		CmsSwtUtils.markup(img);
		StringBuffer txt = new StringBuffer();
		String target = toLink(link);
		if (target != null)
			txt.append("<a href='").append(target).append("'>");
		txt.append(CmsUiUtils.img(fileNode, width.toString(), height.toString()));
		if (target != null)
			txt.append("</a>");
		img.setText(txt.toString());
		if (parent.getLayout() instanceof GridLayout) {
			GridData gd = new GridData(SWT.CENTER, SWT.CENTER, false, false);
			gd.widthHint = width.intValue();
			gd.heightHint = height.intValue();
			img.setLayoutData(gd);
		}

		if (target == null)
			img.addMouseListener(new MouseListener() {
				private static final long serialVersionUID = -1362242049325206168L;

				@Override
				public void mouseUp(MouseEvent e) {
				}

				@Override
				public void mouseDown(MouseEvent e) {
				}

				@Override
				public void mouseDoubleClick(MouseEvent e) {
					LightweightDialog dialog = new LightweightDialog(img.getShell()) {

						@Override
						protected Control createDialogArea(Composite parent) {
							parent.setLayout(new GridLayout());
							ScrolledComposite scroll = new ScrolledComposite(parent, SWT.H_SCROLL | SWT.V_SCROLL);
							scroll.setLayoutData(CmsSwtUtils.fillAll());
							scroll.setLayout(CmsSwtUtils.noSpaceGridLayout());
							scroll.setExpandHorizontal(true);
							scroll.setExpandVertical(true);
							// scroll.setAlwaysShowScrollBars(true);

							Composite c = new Composite(scroll, SWT.NONE);
							scroll.setContent(c);
							c.setLayout(new GridLayout());
							c.setLayoutData(CmsSwtUtils.fillAll());
							Label bigImg = new Label(c, SWT.NONE);
							CmsSwtUtils.markup(bigImg);
							bigImg.setText(CmsUiUtils.img(fileNode, Jcr.get(content, EntityNames.SVG_WIDTH),
									Jcr.get(content, EntityNames.SVG_HEIGHT)));
							bigImg.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, true));
							return bigImg;
						}

						@Override
						protected Point getInitialSize() {
							Point shellSize = img.getShell().getSize();
							return new Point(shellSize.x - 100, shellSize.y - 100);
						}

					};
					dialog.open();
				}
			});
		return img;
	}

	public static String toLink(Node node) {
		return node != null ? "#" + CmsSwtUtils.cleanPathForUrl(SwtArgeoApp.nodeToState(JcrContent.nodeToContent(node)))
				: null;
	}

	public static Control addLink(Composite parent, String label, Node node, CmsStyle style)
			throws RepositoryException {
		String target = toLink(node);
		CmsLink link = new CmsLink(label, target, style);
		return link.createUi(parent, node);
	}

	@Deprecated
	public static Map<String, Object> eventProperties(Node node) {
		Map<String, Object> properties = new HashMap<>();
		String contentPath = '/' + Jcr.getWorkspaceName(node) + Jcr.getPath(node);
		properties.put(SuiteUxEvent.CONTENT_PATH, contentPath);
		return properties;
	}

	/** Singleton. */
	private SuiteUiUtils() {
	}

}
