package org.argeo.app.ui.publish;

import java.awt.image.BufferedImage;
import java.nio.file.Paths;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.argeo.eclipse.ui.specific.BufferedImageDisplay;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

public class PdfViewer {
	public static void main(String[] args) throws Exception {
		PDDocument doc = PDDocument.load(Paths.get(args[0]).toFile());
		PDFRenderer renderer = new PDFRenderer(doc);

		BufferedImage image = renderer.renderImageWithDPI(0, 300);

		Display display = new Display();
		Shell shell = new Shell(display);
		shell.setLayout(new FillLayout());

		shell.setSize(200, 200);

		BufferedImageDisplay imageDisplay = new BufferedImageDisplay(shell, SWT.NONE);
		imageDisplay.setImage(image);

		shell.open();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}
		display.dispose();
	}
}
