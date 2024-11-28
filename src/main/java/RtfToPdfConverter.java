import java.io.*;

public class RtfToPdfConverter {

    public static void main(String[] args) {
        // vytvoření instance Převaděče
        Prevadec prevadec = new Prevadec();

        /* PŘEVOD RTF DO PDF
        // Dynamická cesta k vstupnímu souboru RTF (změňte dle potřeby)
        String inputFilePathRtf = "cesta\\k\\vasi\\sledovane\\slozce";
        // Základní složka pro LocalConverter (stejná složka jako vstupní soubor)
        File baseFolderRtf = new File(new File(inputFilePathRtf).getParent());

        prevadec.prevadejRtfDoPdf(inputFilePathRtf, baseFolderRtf);
         */

        /* PŘEVOD PDF DO JPG
        // Cesta k PDF souboru
        String pdfFilePath = "cesta\\k\\vasi\\sledovane\\slozce";
        // Výstupní složka pro uložené obrázky
        String outputDirJpeg = pdfFilePath.replace(".pdf", "JPEG\\");

        prevadec.prevadejPdfDoJpeg(pdfFilePath, outputDirJpeg);
         */

        // cesty ke sledování a výstupu složek (doplňte si svoje cesty)
        String watchDirPath = "cesta\\k\\vasi\\sledovane\\slozce"; // Složka, kterou chcete sledovat
        String watchDirOutput = watchDirPath + "\\Jpeg\\"; // výstupní složka

        // Metoda k živému sledování složky
        prevadec.sledujSlozku(watchDirPath, watchDirOutput);

    }
}
