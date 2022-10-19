package org.argeo.app.ui.people;

import ezvcard.Ezvcard;
import ezvcard.VCard;

public class VCardExporter {

	public static void main(String[] args) {
		String str = "BEGIN:VCARD\r\n" + "VERSION:4.0\r\n" + "N:Doe;Jonathan;;Mr;\r\n" + "FN:John Doe\r\n"
				+ "END:VCARD\r\n";

		VCard vcard = Ezvcard.parse(str).first();
		String fullName = vcard.getFormattedName().getValue();
		String lastName = vcard.getStructuredName().getFamily();
	}

}
