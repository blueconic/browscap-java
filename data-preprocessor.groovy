log.info "Sorting input CSV for faster startup"

import com.univocity.parsers.common.record.Record
import com.univocity.parsers.csv.CsvParser
import com.univocity.parsers.csv.CsvParserSettings
import com.univocity.parsers.csv.CsvWriter
import com.univocity.parsers.csv.CsvWriterSettings

import java.nio.file.Files
import java.security.DigestInputStream
import java.security.DigestOutputStream
import java.security.MessageDigest
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipOutputStream

import static java.nio.charset.StandardCharsets.UTF_8

class Row {
    String pattern
    Record record

    Row(String pattern, Record record) {
        this.pattern = pattern
        this.record = record
    }
}

/*
 * Checking the resources for rules to sort
 */
def resources = new File("src/main/resources/");
if(resources.isDirectory()) {
    for(File resource in resources.listFiles()) {
        if(resource.getName().matches("browscap-\\d+\\.zip")) {
            sortRules(resource)
        }
    }
}


/**
 * Sorts the CSV files inside the given ZIP file based on specific rules and writes the sorted contents
 * back into the ZIP file. The sorting is performed in the following order:
 * 1. By the length of the "pattern" value in descending order.
 * 2. By the "pattern" value in ascending lexicographical order, as a secondary sorting criterion.
 *
 * @param rulesZip A {@link File} representing the ZIP file containing the input CSV files to sort.
 * @throws RuntimeException If an {@link IOException} occurs while processing the ZIP or CSV files.
 */
def sortRules(File rulesZip) {
    def settings = new CsvParserSettings(lineSeparatorDetectionEnabled: true)
    boolean hasChanges = false
    def sortedZip = File.createTempFile("browscap-", ".zip")
    try(def zip = new ZipFile(rulesZip)) {
        def entries = zip.entries()
        def entryMap = new HashMap<String, List<Row>>()
        def entryHashMap = new HashMap<String, byte[]>()
        while (entries.hasMoreElements()) {
            ZipEntry entry = entries.nextElement()
            if (entry.getName().endsWith(".csv") && !entry.isDirectory()) {
                log.info "\tSorting " + entry.getName()
                def rows = new ArrayList<Row>()
                MessageDigest md = MessageDigest.getInstance("MD5")
                try (DigestInputStream dis = new DigestInputStream(zip.getInputStream(entry), md)
                     def reader = new InputStreamReader(dis, UTF_8)
                     def br = new BufferedReader(reader)) {

                    def csvParser = new CsvParser(settings)
                    csvParser.beginParsing(br)
                    Record record
                    while ((record = csvParser.parseNextRecord()) != null) {
                        rows.add(new Row(record.getString(0), record))
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e)
                }

                // Sorting rows by pattern length in descending order, then by the pattern value
                rows.sort { r1, r2 ->
                    r2.pattern.length() <=> r1.pattern.length() ?: r1.pattern <=> r2.pattern
                }

                entryMap.put(entry.getName(), rows)
                entryHashMap.put(entry.getName(), md.digest())
            }
        }
        def writerSettings = new CsvWriterSettings()
        MessageDigest md = MessageDigest.getInstance("MD5")

        try (def fos = new FileOutputStream(sortedZip)
             def zipOut = new ZipOutputStream(fos)
             def dig = new DigestOutputStream(zipOut, md)
             def writer = new OutputStreamWriter(dig, UTF_8)) {
            for (def csvEntry : entryMap.entrySet()) {
                def outCsv = new ZipEntry(csvEntry.getKey())
                zipOut.putNextEntry(outCsv)

                def csvWriter = new CsvWriter(writer, writerSettings)
                csvEntry.getValue().each { row ->
                    csvWriter.writeRow(row.record.values)
                }
                csvWriter.flush()
                if (!Arrays.equals(entryHashMap.get(csvEntry.getKey()), md.digest())) {
                    hasChanges = true
                }
            }
        }
    }

    if(hasChanges) {
        if (rulesZip.isFile()) {
            Files.delete(rulesZip.toPath())
        }
        Files.move(sortedZip.toPath(), rulesZip.toPath())
    } else {
        sortedZip.delete()
    }
}