package za.co.interfile.bas.notify;

import com.google.common.base.Joiner;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import java.io.File;
import java.util.Collection;
import java.util.Date;
import java.util.Properties;
import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BASNotifier {
    private static final String TREASURY_BODY_FORMAT = "Good day,%n%n"
        + "%d BAS files have been uploaded to the FTP server%n%n"
        + "%s%n%n"  // list the files
        + "This is an automated message.%n"
        + "--%n"
        + "The Basic Accounting System from Interfile on behalf of the Department of Home Affairs";

    private static final String OPERATIONS_BODY_FORMAT = "Good day,%n%n"
        + "%d response files have been downloaded from the FTP server%n%n"
        + "%s%n%n"  // list the files
        + "This is an automated message.%n"
        + "--%n"
        + "The Basic Accounting System from Interfile on behalf of the Department of Home Affairs";

    private static final String HOME_AFFAIRS_BODY_FORMAT = "Good day,%n%n"
        + "The BAS file report is attached as a CSV file. This file can be opened with a spreadsheet program, e.g. Microsoft Excel.%n%n"
        + "This is an automated message.%n"
        + "--%n"
        + "The Basic Accounting System from Interfile on behalf of the Department of Home Affairs";

    private final Logger logger = LoggerFactory.getLogger(BASNotifier.class);
    private final Properties mailProperties;
    private final String from;
    private final InternetAddress[] replyTo;
    private final InternetAddress[] treasury;
    private final InternetAddress[] homeAffairs;
    private final InternetAddress[] interfile;

    @Inject
    public BASNotifier(@Named("smtp.properties") Properties mailProperties,
        @Named("mail.from") String from,
        @Named("addresses.reply.to") InternetAddress[] replyTo,
        @Named("addresses.treasury") InternetAddress[] treasury,
        @Named("addresses.home.affairs") InternetAddress[] homeAffairs,
        @Named("addresses.interfile") InternetAddress[] interfile) {
        this.mailProperties = mailProperties;
        this.from = from;
        this.replyTo = replyTo;
        this.treasury = treasury;
        this.homeAffairs = homeAffairs;
        this.interfile = interfile;
    }

    public void notifyTreasury(Collection<String> basFilenames) {
        try {
            Message message = this.prepareTreasuryMessage(basFilenames);

            this.logger.debug("Sending notification e-mail to Treasury");
            Transport.send(message);
        }
        catch (MessagingException me) {
            this.logger.error("Failure sending notification e-mail", me);
        }
    }

    public void notifyOperations(Collection<String> responseFilenames) {
        try {
            Message message = this.prepareOperationsMessage(responseFilenames);

            this.logger.debug("Sending notification e-mail to Operations");
            Transport.send(message);
        }
        catch (MessagingException me) {
            this.logger.error("Failure sending notification e-mail", me);
        }
    }

    public boolean notifyHomeAffairs(File csvFile) {
        try {
            Message message = this.prepareHomeAffairsMessage(csvFile);

            this.logger.debug("Sending report e-mail to Home Affairs");
            Transport.send(message);
            return true;
        }
        catch (MessagingException me) {
            this.logger.error("Failure sending report e-mail", me);
        }
        return false;
    }

    private Message prepareTreasuryMessage(Collection<String> basFilenames) throws MessagingException {
        Message message = new MimeMessage(Session.getInstance(this.mailProperties));
        Date now = new Date();

        this.logger.debug("Preparing treasury e-mail message");
        message.setFrom(new InternetAddress(this.from));
        message.addRecipients(Message.RecipientType.TO, this.treasury);
        message.addRecipients(Message.RecipientType.CC, this.interfile);
        message.setReplyTo(this.replyTo);
        message.setSubject(String.format("Uploaded BAS files (%1$tF %1$tT)", now));
        message.setText(String.format(TREASURY_BODY_FORMAT,
            basFilenames.size(),
            Joiner.on(System.getProperty("line.separator")).join(basFilenames)));
        message.setSentDate(now);
        return message;
    }

    private Message prepareOperationsMessage(Collection<String> responseFilenames) throws MessagingException {
        Message message = new MimeMessage(Session.getInstance(this.mailProperties));
        Date now = new Date();

        this.logger.debug("Preparing operations e-mail message");
        message.setFrom(new InternetAddress(this.from));
        message.addRecipients(Message.RecipientType.TO, this.interfile);
        message.setSubject(String.format("Downloaded response files (%1$tF %1$tT)", now));
        message.setText(String.format(OPERATIONS_BODY_FORMAT,
            responseFilenames.size(),
            Joiner.on(System.getProperty("line.separator")).join(responseFilenames)));
        message.setSentDate(now);
        return message;
    }

    private Message prepareHomeAffairsMessage(File csvFile) throws MessagingException {
        Message message = new MimeMessage(Session.getInstance(this.mailProperties));
        Date now = new Date();

        this.logger.debug("Preparing home affairs e-mail message");
        message.setFrom(new InternetAddress(this.from));
        message.addRecipients(Message.RecipientType.TO, this.homeAffairs);
        message.addRecipients(Message.RecipientType.CC, this.interfile);
        message.setReplyTo(this.replyTo);
        message.setSubject(String.format("BAS Report (%1$tF %1$tT)", now));
        message.setSentDate(now);

        MimeBodyPart body = new MimeBodyPart();
        body.setText(String.format(HOME_AFFAIRS_BODY_FORMAT));

        MimeBodyPart attachment = new MimeBodyPart();
        attachment.setDataHandler(new DataHandler(new FileDataSource(csvFile)));
        attachment.setFileName(csvFile.getName());

        Multipart multiPart = new MimeMultipart();
        multiPart.addBodyPart(body);
        multiPart.addBodyPart(attachment);
        message.setContent(multiPart);

        return message;
    }
}
