package es.eucm.rewards;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Image;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.BarcodeQRCode;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfWriter;

public class GeneratePDF {

	public void generate() throws FileNotFoundException, IOException, DocumentException {
		createPdf(new FileOutputStream("test.pdf"));
	}
	
	/**
     * Creates a PDF document.
     * @param filename the path to the new PDF document
     * @throws    DocumentException 
     * @throws    IOException
     */
    public void createPdf(OutputStream out) throws IOException, DocumentException {
        // step 1
        Document document = new Document(new Rectangle(340, 842));
        // step 2
        PdfWriter writer = PdfWriter.getInstance(document, out);
        // step 3
        document.open();
        // step 4
        PdfContentByte cb = writer.getDirectContent();
 
        
        document.add(new Paragraph("Barcode QRCode"));
        BarcodeQRCode qrcode = new BarcodeQRCode("Moby Dick by Herman Melville", 1, 1, null);
        Image img = qrcode.getImage();
        document.add(img);
 
        // step 5
        document.close();
    }
    
    public static void main(String[] args) throws Exception {
    	new GeneratePDF().generate();
    }
}
