package main.Laporan;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

import be.quodlibet.boxable.BaseTable;
import be.quodlibet.boxable.Cell;
import be.quodlibet.boxable.Row;

import main.Transaksi.*;;

public class LaporanFixedBill extends Laporan{
    private ArrayList<ElemenDetailTransaksi> listFixedBill; 

    public LaporanFixedBill(ArrayList<ElemenDetailTransaksi> listFixedBill, String fileName) {
        super(fileName);
        this.listFixedBill = listFixedBill;
    }

    @Override
    public void generatePDF() throws IOException {
        PDDocument document = new PDDocument();
        PDPage page = new PDPage(PDRectangle.A4);
        PDPageContentStream contentStream = new PDPageContentStream(document, page);
        // PageContentStreamOptimized streamOptimized = new PageContentStreamOptimized(contentStream);
        
        // Image image = new Image(ImageIO.read(new File(System.getProperty("user.dir") + "\\img\\icon\\images\\logo.png")));
        // float imageWidth = 75;
        // image = image.scaleByWidth(imageWidth);
        // float xPos = 50;
        // float yPos = page.getMediaBox().getHeight() - image.getHeight() - 50;
        // image.draw(document,streamOptimized, xPos, yPos);

        contentStream.beginText();
        contentStream.setFont(PDType1Font.HELVETICA_BOLD, 22);
        contentStream.newLineAtOffset(200, 750);
        contentStream.showText("Laporan Fixed Bill");
        contentStream.endText();

        float margin = 40;
        float yStartNewPage = page.getMediaBox().getHeight() - (2 * margin);
        float tableWidth = page.getMediaBox().getWidth() - (2 * margin);
        boolean drawContent = true;
        float bottomMargin = 70;
        float yPosition = 680;
        BaseTable table = new BaseTable(yPosition, yStartNewPage, bottomMargin, tableWidth, margin + 50, document, page, true, drawContent);
        Row<PDPage> headerRow = table.createRow(15f);
        Cell<PDPage> cell = headerRow.createCell(5, "No.");
        cell.setFont(PDType1Font.HELVETICA_BOLD);
        cell = headerRow.createCell(10, "Id Barang");
        cell.setFont(PDType1Font.HELVETICA_BOLD);
        cell = headerRow.createCell(30, "Nama Barang");
        cell.setFont(PDType1Font.HELVETICA_BOLD);
        cell = headerRow.createCell(15, "Jumlah Barang");
        cell.setFont(PDType1Font.HELVETICA_BOLD);
        cell = headerRow.createCell(15, "Harga");
        cell.setFont(PDType1Font.HELVETICA_BOLD);
        table.addHeaderRow(headerRow);
        int count = 1;
        Double total = 0.0;
        for (ElemenDetailTransaksi penjualanFix : listFixedBill) {
            total += penjualanFix.getSubTotal();
            Row<PDPage> row = table.createRow(10f);
            cell = row.createCell(5, String.valueOf(count++));
            cell = row.createCell(10, String.valueOf(penjualanFix.getIdBarang()));
            cell = row.createCell(30, String.valueOf(penjualanFix.getNamaBarang()));
            cell = row.createCell(15, String.valueOf(penjualanFix.getJumlahBarang()));
            cell = row.createCell(15, String.valueOf(penjualanFix.getSubTotal()));
        }
        Row<PDPage> row = table.createRow(10f);
        cell = row.createCell(60, String.valueOf("Total"));
        cell = row.createCell(50, String.valueOf(total));
        table.draw();

        contentStream.beginText();
        contentStream.setFont(PDType1Font.HELVETICA_BOLD, 12);
        contentStream.newLineAtOffset(50, 50);
        contentStream.showText("Tanggal pembuatan laporan: " + getCurrentDate());
        contentStream.endText();

        contentStream.close();
        document.addPage(page);
        document.save(new File("../docs/" + getFileName()+".pdf"));
        document.close();
    }
}