package es.eucm.rewards;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

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

	public static final String SECRET_PROPERTY = "secret";
	
	public static final int DEFAULT_SECRET_SIZE = 60;
	
	public static final String DEFAULT_CONFIG_FILENAME = "generator.properties";
	
	public static final String DEFAULT_GENERATED_FILENAME = "generator.csv";
	
	private RewardsTokenGenerator generator;

	private Set<RewardToken> tokens;
	
	public PDFGenerator(String secret) {
		this.generator = new RewardsTokenGenerator(secret);
		this.tokens = new HashSet<>();
	}

	public void generate(int numPages) throws FileNotFoundException, IOException,
			DocumentException {
		DateFormat df = new SimpleDateFormat("YYYYMMdd_HHmmSS");
		String tstamp = df.format(new Date());
		List<RewardToken> newTokens = generateCodes(numPages);
		createPdf(new FileOutputStream(tstamp+"_codes.pdf"), newTokens);
	}
	
	private List<RewardToken> generateCodes(int numPages) {
		Iterator<RewardToken> it = generator.iterator();
		List<RewardToken> newTokens = new ArrayList<>();
		int quantity = 14 * 2 * numPages;
		int i = 0;
		while (i < quantity) {
			RewardToken token = it.next();
			if (tokens.add(token)) {
				newTokens.add(token);
				i++;
			}
		}
		return newTokens;
	}

	public void save(OutputStream out) throws IOException {
		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out));
		for(RewardToken token : tokens) {
			writer.write(token.toString());
			writer.newLine();
		}
		writer.flush();
	}
	
	public void load(InputStream in) throws IOException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(in));
		String line = reader.readLine();
		while (line != null) {
			tokens.add(RewardToken.parse(line));
			line = reader.readLine();
		}
	}

	/**
	 * Creates a PDF document.
	 * 
	 * @param filename
	 *            the path to the new PDF document
	 * @throws DocumentException
	 * @throws IOException
	 */
	public void createPdf(OutputStream out, List<RewardToken> tokens) throws IOException,
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
		Properties properties = new Properties();
		File file = new File(DEFAULT_CONFIG_FILENAME);
		if (file.exists()) {
			InputStream input = new FileInputStream(file);
			properties.load(input);
			input.close();
		}

		String secret = properties.getProperty(SECRET_PROPERTY);
		if (secret == null) {
			secret = RewardsTokenGenerator.generateKey(DEFAULT_SECRET_SIZE);
			properties.put(SECRET_PROPERTY, secret);
			OutputStream out = new FileOutputStream(file);
			properties.store(out, "Generator properties");
			out.close();
		}
		PDFGenerator generator = new PDFGenerator(secret);
		
		file = new File(DEFAULT_GENERATED_FILENAME);
		if (file.exists()) {
			InputStream input = new FileInputStream(file);
			generator.load(input);
			input.close();
		}
		generator.generate(20);
		OutputStream out = new FileOutputStream(file);
		generator.save(out);
		out.close();
	}
}
