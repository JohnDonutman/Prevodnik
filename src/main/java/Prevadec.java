import com.documents4j.api.DocumentType;
import com.documents4j.api.IConverter;
import com.documents4j.job.LocalConverter;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class Prevadec {

    public void prevadejRtfDoPdf(String inputFilePath, File baseFolder) {
        // Automatická změna přípony na .pdf
        String outputFilePath = inputFilePath.replace(".rtf", ".pdf");

        try (
                InputStream in = new BufferedInputStream(new FileInputStream(inputFilePath));
                ByteArrayOutputStream bo = new ByteArrayOutputStream();
                OutputStream outputStream = new FileOutputStream(outputFilePath)) {

            // Nastavení konvertoru
            IConverter converter = LocalConverter.builder()
                    .baseFolder(baseFolder) // Základní složka
                    .workerPool(20, 25, 2, TimeUnit.SECONDS) // Počet vláken
                    .processTimeout(5, TimeUnit.SECONDS) // Timeout pro proces
                    .build();

            // Naplánování převodu
            Future<Boolean> conversion = converter
                    .convert(in).as(DocumentType.RTF)
                    .to(bo).as(DocumentType.PDF)
                    .prioritizeWith(1000) // Priorita (volitelné)
                    .schedule();

            // Počkat na dokončení převodu
            if (conversion.get()) {
                // Zapsání výsledku do PDF souboru
                bo.writeTo(outputStream);
                System.out.println("Soubor byl úspěšně převeden do PDF: " + outputFilePath);

                // Ukončení programu po úspěšném převodu
                System.exit(0);
            } else {
                System.err.println("Převod se nezdařil.");
            }

        } catch (FileNotFoundException e) {
            System.err.println("Soubor nebyl nalezen: " + e.getMessage());
            System.exit(1); // Ukončení programu s chybovým kódem
        } catch (IOException | InterruptedException | ExecutionException e) {
            System.err.println("Chyba při převodu: " + e.getMessage());
            System.exit(1); // Ukončení programu s chybovým kódem
        }
    }

    public void prevadejPdfDoJpeg(String pdfFilePath, String outputDir) {
        try {
            // Ověření a vytvoření výstupní složky, pokud neexistuje
            File outputDirectory = new File(outputDir);
            if (!outputDirectory.exists()) {
                boolean created = outputDirectory.mkdirs(); // Vytvoří složku včetně rodičovských složek
                if (created) {
                    System.out.println("Výstupní složka byla vytvořena: " + outputDir);
                } else {
                    System.out.println("Nepodařilo se vytvořit výstupní složku: " + outputDir);
                    return; // Ukončí program, pokud složku nelze vytvořit
                }
            }

            // Načtení PDF dokumentu
            PDDocument document = Loader.loadPDF(new File(pdfFilePath));

            // Vytvoření rendereru pro vykreslení PDF na obrázky
            PDFRenderer pdfRenderer = new PDFRenderer(document);

            // Procházíme všechny stránky PDF
            for (int page = 0; page < document.getNumberOfPages(); page++) {
                // Vykreslení stránky jako obrázek
                BufferedImage bufferedImage = pdfRenderer.renderImageWithDPI(page, 300, ImageType.RGB);

                // Cesta a název výstupního souboru
                String outputFileName = outputDir + "page-" + (page + 1) + ".jpg";

                // Uložení obrázku jako JPG
                ImageIO.write(bufferedImage, "jpg", new File(outputFileName));
                System.out.println("Uloženo: " + outputFileName);
            }

            // Zavření dokumentu
            document.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sledujSlozku(String watchDirPath, String outputDir) {
        // Ověření a vytvoření výstupní složky, pokud neexistuje
        File outputDirectory = new File(outputDir);
        if (!outputDirectory.exists()) {
            boolean created = outputDirectory.mkdirs(); // Vytvoří složku včetně rodičovských složek
            if (created) {
                System.out.println("Výstupní složka byla vytvořena: " + outputDir);
            } else {
                System.out.println("Nepodařilo se vytvořit výstupní složku: " + outputDir);
                return; // Ukončí program, pokud složku nelze vytvořit
            }
        }
        // Nastavení WatchService
        try (WatchService watchService = FileSystems.getDefault().newWatchService()) {
            Path watchDir = Paths.get(watchDirPath);
            watchDir.register(watchService, StandardWatchEventKinds.ENTRY_CREATE);

            System.out.println("Sledování složky: " + watchDirPath);

            // Nekonečný cyklus pro sledování změn
            while (true) {
                WatchKey key = watchService.take(); // Čeká na události

                for (WatchEvent<?> event : key.pollEvents()) {
                    WatchEvent.Kind<?> kind = event.kind();

                    if (kind == StandardWatchEventKinds.ENTRY_CREATE) {
                        Path fileName = (Path) event.context();
                        File file = watchDir.resolve(fileName).toFile();

                        if (file.getName().endsWith(".pdf")) {
                            System.out.println("Nový PDF soubor detekován: " + file.getName());
                            prevadejPdfDoJpeg(String.valueOf(file), outputDir);
                        }
                    }
                }

                // Reset WatchKey pro další události
                if (!key.reset()) {
                    break;
                }
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
