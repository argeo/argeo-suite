package org.argeo.app.swt.forms;

import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.TemporalAccessor;

import org.argeo.api.cms.CmsLog;
import org.argeo.eclipse.ui.EclipseUiUtils;

/** Utilitary methods to ease implementation of CMS forms */
public class FormUtils {
	private final static CmsLog log = CmsLog.getLog(FormUtils.class);

	public final static String DEFAULT_SHORT_DATE_FORMAT = "dd/MM/yyyy";

	/** Best effort to convert a String to a calendar. Fails silently */
	public static TemporalAccessor parseDate(DateTimeFormatter dateFormat, String calStr) {
		if (EclipseUiUtils.notEmpty(calStr)) {
			try {
				return dateFormat.parse(calStr);
//				cal = new GregorianCalendar();
//				cal.setTime(date);
			} catch (DateTimeParseException pe) {
				// Silent
				log.warn("Unable to parse date: " + calStr + " - msg: " + pe.getMessage());
				throw pe;
			}
		}
		return null;
	}

//	/** Add a double click listener on tables that display a JCR node list */
//	public static void addCanonicalDoubleClickListener(final TableViewer v) {
//		v.addDoubleClickListener(new IDoubleClickListener() {
//
//			@Override
//			public void doubleClick(DoubleClickEvent event) {
//				CmsView cmsView = CmsUiUtils.getCmsView();
//				Node node = (Node) ((IStructuredSelection) event.getSelection())
//						.getFirstElement();
//				try {
//					cmsView.navigateTo(node.getPath());
//				} catch (RepositoryException e) {
//					throw new CmsException("Unable to get path for node "
//							+ node + " before calling navigateTo(path)", e);
//				}
//			}
//		});
//	}

	// MANAGE ERROR DECORATION

//	public static ControlDecoration addDecoration(final Text text) {
//		final ControlDecoration dynDecoration = new ControlDecoration(text,
//				SWT.LEFT);
//		Image icon = getDecorationImage(FieldDecorationRegistry.DEC_ERROR);
//		dynDecoration.setImage(icon);
//		dynDecoration.setMarginWidth(3);
//		dynDecoration.hide();
//		return dynDecoration;
//	}
//
//	public static void refreshDecoration(Text text, ControlDecoration deco,
//			boolean isValid, boolean clean) {
//		if (isValid || clean) {
//			text.setBackground(null);
//			deco.hide();
//		} else {
//			text.setBackground(new Color(text.getDisplay(), 250, 200, 150));
//			deco.show();
//		}
//	}
//
//	public static Image getDecorationImage(String image) {
//		FieldDecorationRegistry registry = FieldDecorationRegistry.getDefault();
//		return registry.getFieldDecoration(image).getImage();
//	}
//
//	public static void addCompulsoryDecoration(Label label) {
//		final ControlDecoration dynDecoration = new ControlDecoration(label,
//				SWT.RIGHT | SWT.TOP);
//		Image icon = getDecorationImage(FieldDecorationRegistry.DEC_REQUIRED);
//		dynDecoration.setImage(icon);
//		dynDecoration.setMarginWidth(3);
//	}

	// TODO the read only generation of read only links for various contact type
	// should be factorised in the cms Utils.
	/**
	 * Creates the read-only HTML snippet to display in a label with styling enabled
	 * in order to provide a click-able phone number
	 */
	public static String getPhoneLink(String value) {
		return getPhoneLink(value, value);
	}

	/**
	 * Creates the read-only HTML snippet to display in a label with styling enabled
	 * in order to provide a click-able phone number
	 * 
	 * @param value
	 * @param label a potentially distinct label
	 * @return the link
	 */
	public static String getPhoneLink(String value, String label) {
		StringBuilder builder = new StringBuilder();
		builder.append("<a href=\"tel:");
		builder.append(value).append("\" target=\"_blank\" >").append(label).append("</a>");
		return builder.toString();
	}

	/**
	 * Creates the read-only HTML snippet to display in a label with styling enabled
	 * in order to provide a click-able mail
	 */
	public static String getMailLink(String value) {
		return getMailLink(value, value);
	}

	/**
	 * Creates the read-only HTML snippet to display in a label with styling enabled
	 * in order to provide a click-able mail
	 * 
	 * @param value
	 * @param label a potentially distinct label
	 * @return the link
	 */
	public static String getMailLink(String value, String label) {
		StringBuilder builder = new StringBuilder();
		value = replaceAmpersand(value);
		builder.append("<a href=\"mailto:");
		builder.append(value).append("\" >").append(label).append("</a>");
		return builder.toString();
	}

	/**
	 * Creates the read-only HTML snippet to display in a label with styling enabled
	 * in order to provide a click-able link
	 */
	public static String getUrlLink(String value) {
		return getUrlLink(value, value);
	}

	/**
	 * Creates the read-only HTML snippet to display in a label with styling enabled
	 * in order to provide a click-able link
	 */
	public static String getUrlLink(String value, String label) {
		StringBuilder builder = new StringBuilder();
		value = replaceAmpersand(value);
		label = replaceAmpersand(label);
		if (!(value.startsWith("http://") || value.startsWith("https://")))
			value = "http://" + value;
		builder.append("<a href=\"");
		builder.append(value + "\" target=\"_blank\" >" + label + "</a>");
		return builder.toString();
	}

	private static String AMPERSAND = "&#38;";

	/**
	 * Cleans a String by replacing any '&#38;' by its HTML encoding '&#38;#38;' to
	 * avoid <code>SAXParseException</code> while rendering HTML with RWT
	 */
	public static String replaceAmpersand(String value) {
		value = value.replaceAll("&(?![#a-zA-Z0-9]+;)", AMPERSAND);
		return value;
	}

	// Prevents instantiation
	private FormUtils() {
	}
}
