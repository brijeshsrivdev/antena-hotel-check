package com.antenapro.hotelcheck.sitecheck.report;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;

/**
 * The local default ({@code hotelcheck.sitecheck.report-mode=log}): writes the PDF under the report directory and
 * logs where, so a developer machine without SMTP still produces the report. Nothing leaves the machine.
 */
public class FileReportSender implements ReportSender {

    private static final Logger log = LoggerFactory.getLogger(FileReportSender.class);

    private final Path directory;

    public FileReportSender(Path directory) {
        this.directory = directory;
    }

    @Override
    public String send(String email, String host, byte[] pdf) {
        try {
            Files.createDirectories(directory);
            Path file = directory.resolve(host.replaceAll("[^a-z0-9.-]", "_") + "-" + Instant.now().toEpochMilli() + ".pdf");
            Files.write(file, pdf);
            log.info("Site check report for {} written to {} (report-mode=log; would be emailed to the address the visitor typed)", host, file);
            return "Report saved to " + file + " (email delivery is off on this machine).";
        } catch (IOException e) {
            throw new IllegalStateException("Could not write the site check PDF", e);
        }
    }
}
