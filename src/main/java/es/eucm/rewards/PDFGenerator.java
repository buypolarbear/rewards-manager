package es.eucm.rewards;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Font;
import com.itextpdf.text.Font.FontFamily;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.BarcodeQRCode;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.qrcode.EncodeHintType;
import com.itextpdf.text.pdf.qrcode.ErrorCorrectionLevel;

public class PDFGenerator {

	private RewardsManager rewards;
	
	public PDFGenerator(RewardsManager rewards) {
		this.rewards = rewards;
	}

	public void generate(int numPages) throws FileNotFoundException, IOException,
			DocumentException {
		DateFormat df = new SimpleDateFormat("YYYYMMdd_HHmmSS");
		String tstamp = df.format(new Date());
		List<RewardToken> newTokens = rewards.generate(numPages);
		
		FileOutputStream out = new FileOutputStream(tstamp+"_codes.pdf");
		createPdf(out, newTokens);
		out.close();
	}
	
	/**
	 * @throws DocumentException
	 * @throws IOException
	 */
	private void createPdf(OutputStream out, List<RewardToken> tokens) throws IOException,
			DocumentException {
		// step 1
		Document document = new Document(PageSize.A4);
		// step 2
		PdfWriter.getInstance(document, out);
		// step 2
		document.open();

		// step 3
		PdfPTable table = new PdfPTable(2);
		table.setWidths(new int[] { 5, 5 });
		table.setWidthPercentage(100);
		PdfPCell cell;

		Map<EncodeHintType, Object> hints = new HashMap<>();
		hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M);

		for( RewardToken token : tokens ) {
			String code = token.toString();
			PdfPTable innerTable = new PdfPTable(2);
			innerTable.setWidths(new int[] { 2, 5 });
			innerTable.setWidthPercentage(100);

			BarcodeQRCode qrcode = new BarcodeQRCode(code, 1, 1, hints);
			Image img = qrcode.getImage();

			cell = new PdfPCell(img, false);
			cell.setPadding(5);
			cell.setBorder(PdfPCell.NO_BORDER);
			innerTable.addCell(cell);

			Paragraph p = new Paragraph(code, new Font(FontFamily.COURIER, 9,
					Font.NORMAL));
			cell = new PdfPCell(p);
			cell.setPadding(5);
			cell.setBorder(PdfPCell.NO_BORDER);
			innerTable.addCell(cell);

			table.addCell(innerTable);
		}
		document.add(table);

		// step 5
		document.close();
	}

	public static void main(String[] args) throws Exception {
		RewardsManager rewards = new RewardsManager(true);

		PDFGenerator generator = new PDFGenerator(rewards);
		generator.generate(20);
		
		rewards.save();

	}
}
