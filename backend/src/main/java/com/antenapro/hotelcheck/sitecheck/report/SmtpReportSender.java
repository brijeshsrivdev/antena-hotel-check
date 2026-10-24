package com.antenapro.hotelcheck.sitecheck.report;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;

/** {@code hotelcheck.sitecheck.report-mode=smtp}: one email with the PDF attached, from the configured address. */
public class SmtpReportSender implements ReportSender {

    private final JavaMailSender mailSender;
    private final String from;

    public SmtpReportSender(JavaMailSender mailSender, String from) {
        this.mailSender = mailSender;
        this.from = from;
    }

    @Override
    public String send(String email, String host, byte[] pdf) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(from);
            helper.setTo(email);
            helper.setSubject("Your Hotel Site Check report for " + host);
            helper.setText("Hello,\n\nAttached is the Hotel Site Check report for " + host
                    + ": what stops the site ranking better in Google, with what we saw and the one change that fixes each item.\n\n"
                    + "You asked for this report on the Antena site and agreed that we may follow up by email about Antena. "
                    + "Reply to this email if you would rather we did not.\n\nAntena\nantenapro.com\n", false);
            helper.addAttachment("hotel-site-check-" + host + ".pdf", new ByteArrayResource(pdf), "application/pdf");
            mailSender.send(message);
            return "Report emailed to " + email + ".";
        } catch (MessagingException e) {
            throw new IllegalStateException("Could not send the site check report email", e);
        }
    }
}
