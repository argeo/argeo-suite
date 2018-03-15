package org.argeo.suite.workbench.commands;

import java.io.IOException;
import java.io.InputStream;

import org.argeo.connect.ConnectException;
import org.argeo.eclipse.ui.EclipseUiUtils;

import jxl.Cell;
import jxl.CellType;
import jxl.JXLException;
import jxl.Sheet;
import jxl.Workbook;
import jxl.WorkbookSettings;

/** Centralise useful methods to simplify development with JXL library */
class JxlUtils {

	public static boolean isEmptyCell(Sheet sheet, int x, int y) {
		Cell cell = sheet.getCell(x, y);
		CellType type = cell.getType();
		return type == CellType.EMPTY;
	}

	public static String getStringValue(Sheet sheet, int x, int y) {
		Cell cell = sheet.getCell(x, y);
		CellType type = cell.getType();
		String stringValue = null;
		if (type == CellType.LABEL || type == CellType.NUMBER)
			stringValue = cell.getContents();
		return stringValue;
	}

	public static String getCompulsoryStringValue(Sheet sheet, int x, int y) {
		Cell cell = sheet.getCell(x, y);
		CellType type = cell.getType();
		String stringValue = null;
		if (type == CellType.LABEL)
			stringValue = cell.getContents();
		else if (type == CellType.NUMBER)
			stringValue = cell.getContents();
		if (EclipseUiUtils.isEmpty(stringValue))
			throw new ConnectException("No name defined at [" + x + "," + y + "], cannot parse indicator file");
		return stringValue;
	}

	public static Double getNumberValue(Sheet sheet, int x, int y) {
		Cell cell = sheet.getCell(x, y);
		CellType type = cell.getType();
		if (type == CellType.NUMBER)
			return new Double(cell.getContents());
		else if (type == CellType.EMPTY)
			return null;
		else
			throw new ConnectException("Not a number at [" + x + "," + y + "]: " + type.toString());
	}

	public static Sheet getOnlySheet(InputStream in, String encoding) throws IOException {
		Workbook wkb = toWorkbook(in, encoding);
		Sheet sheet = wkb.getSheet(0);
		return sheet;
	}

	public static Sheet getSheet(InputStream in, String encoding, int index) throws IOException {
		Workbook wkb = toWorkbook(in, encoding);
		return wkb.getSheet(index);
	}

	public static Workbook toWorkbook(InputStream in, String encoding) throws IOException {
		try {
			WorkbookSettings ws = new WorkbookSettings();
			ws.setEncoding(encoding);
			return Workbook.getWorkbook(in, ws);
		} catch (JXLException e) {
			throw new ConnectException("Unable to open XLS file", e);
		}
	}

	// Prevents instantiation
	private JxlUtils() {
	}
}
