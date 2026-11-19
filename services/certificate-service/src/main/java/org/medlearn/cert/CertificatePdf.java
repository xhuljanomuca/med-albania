package org.medlearn.cert;

import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;
import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import com.google.zxing.*;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;

import java.io.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;

@Component
public class CertificatePdf {
    @Value("${medlearn.verification-base}")
    String base;

    public byte[] render(Certificate c) {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            Document doc = new Document(PageSize.A4.rotate(), 45, 45, 45, 45);
            PdfWriter.getInstance(doc, out);
            doc.open();
            Font title = new Font(Font.HELVETICA, 30, Font.BOLD, new java.awt.Color(13, 89, 139));
            Font body = new Font(Font.HELVETICA, 17);
            Paragraph head = new Paragraph("MEDLEARN — CERTIFICATE OF COMPLETION", title);
            head.setAlignment(Element.ALIGN_CENTER);
            doc.add(head);
            doc.add(new Paragraph(" "));
            Paragraph name = new Paragraph(c.recipientName, new Font(Font.HELVETICA, 26, Font.BOLD));
            name.setAlignment(Element.ALIGN_CENTER);
            doc.add(name);
            Paragraph course = new Paragraph(c.courseTitleEn, body);
            course.setAlignment(Element.ALIGN_CENTER);
            doc.add(course);
            doc.add(new Paragraph("Final examination score: " + c.scorePercent + "%"));
            doc.add(new Paragraph("Issued: " + c.issuedAt.toString()));
            doc.add(new Paragraph("Verification ID: " + c.verificationCode));
            doc.add(new Paragraph("Status: " + c.effectiveStatus()));
            if (c.accredited && c.creditHours != null)
                doc.add(new Paragraph("CME/CPD: " + c.creditHours + " credits — " + c.accreditingBody));
            BitMatrix matrix = new MultiFormatWriter().encode(base + "/" + c.verificationCode, BarcodeFormat.QR_CODE, 140, 140);
            BufferedImage qr = MatrixToImageWriter.toBufferedImage(matrix);
            ByteArrayOutputStream img = new ByteArrayOutputStream();
            ImageIO.write(qr, "PNG", img);
            Image image = Image.getInstance(img.toByteArray());
            image.scaleToFit(115, 115);
            doc.add(image);
            doc.add(new Paragraph("Verify current status online; downloaded copies may be revoked or expired."));
            doc.close();
            return out.toByteArray();
        } catch (Exception e) {
            throw new IllegalStateException("Unable to render certificate", e);
        }
    }
}
