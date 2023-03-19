package fso.decor;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.tools.imageio.ImageIOUtil;

import java.awt.image.BufferedImage;
import java.io.*;
import java.math.BigInteger;
import java.security.DigestOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class PdfManager {
    final private File source;
    final private File destFolder;
    final private PDDocument pdfDocument;
    final private PDFRenderer renderer;
    private String hash;

    public PdfManager(File source, File destFolder) {
        this.source = source;
        this.destFolder = destFolder;

        try {
            pdfDocument = PDDocument.load(source);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }

        getPdfHash();

        renderer = new PDFRenderer(pdfDocument);
    }

    public PdfManager(File source, File destFolder, boolean convertImmediately) {
        this(source, destFolder);
        if (!convertImmediately)
            return;
        try {
            this.convertPdfToJpg();
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public void convertPdfToJpg() throws IOException {
        int dpi = 200;
        for (int x = 0; x < pdfDocument.getNumberOfPages(); x++) {
            String formatted = String.format(destFolder.getAbsolutePath() + "/" + hash + "_" + "%07d" + ".%s", x, "jpg");
            File page = new File(formatted);
            if (!page.exists()) {
                BufferedImage bImage = renderer.renderImageWithDPI(x, dpi, ImageType.RGB);
                ImageIOUtil.writeImage(bImage, formatted, dpi);
            }
        }
    }

    public String getPdfHash() {
        if (hash != null)
            return hash;
        String algorithm = "SHA-512";
        try {
            hash = hashFile(algorithm, source);
        } catch (IOException | NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        return hash;
    }

    private static String hashFile(String algorithm, File f) throws IOException, NoSuchAlgorithmException {
        // https://stackoverflow.com/questions/304268/getting-a-files-md5-checksum-in-java
        MessageDigest md = MessageDigest.getInstance(algorithm);

        try(BufferedInputStream in = new BufferedInputStream((new FileInputStream(f)));
            DigestOutputStream out = new DigestOutputStream(OutputStream.nullOutputStream(), md)) {
            in.transferTo(out);
        }

        String fx = "%0" + (md.getDigestLength()*2) + "x";
        return String.format(fx, new BigInteger(1, md.digest()));
    }
}
