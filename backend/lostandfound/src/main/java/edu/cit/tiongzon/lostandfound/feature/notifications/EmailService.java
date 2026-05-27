package edu.cit.tiongzon.lostandfound.feature.notifications;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailService {
    private final JavaMailSender javaMailSender;

    public EmailService(JavaMailSender javaMailSender) {
        this.javaMailSender = javaMailSender;
    }

    public void sendHtmlEmail(String to, String subject, String title, String messageContent) {
        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(to);
            helper.setSubject(subject);
            helper.setFrom("beteaj9@gmail.com");

            String html = buildEmailTemplate(title, messageContent);
            helper.setText(html, true);

            javaMailSender.send(message);
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }

    public void sendWelcomeEmail(String to, String name) {
        sendHtmlEmail(to, "Welcome to Lost & Found!", "Welcome, " + name + "!", 
            "We are thrilled to have you on the Lost & Found platform. You can now easily report lost items or claim things you've found on campus. Log in to get started!");
    }

    public void sendClaimNotification(String to, String itemName, String claimantName) {
        sendHtmlEmail(to, "New Claim: " + itemName, "New Claim Received", 
            claimantName + " has just submitted a claim for the item you posted: <strong>" + itemName + "</strong>.\n\n" +
            "Please log in to your account and navigate to the claim to review the submitted proof.");
    }
    
    public void sendClaimStatusUpdate(String to, String itemName, boolean approved) {
        String title = approved ? "Claim Approved!" : "Claim Rejected";
        String action = approved ? "approved" : "rejected";
        sendHtmlEmail(to, "Claim Status Update: " + itemName, title,
            "Your claim for the item <strong>" + itemName + "</strong> has been " + action + " by the poster.\n\n" +
            (approved ? "If payment is required, please proceed to checkout in the app." : "Unfortunately, the proof provided did not match."));
    }
    
    public void sendPaymentNotification(String to, String itemName, String reference) {
        sendHtmlEmail(to, "Payment Completed: " + itemName, "Payment Confirmed!",
            "Great news! The payment for the item <strong>" + itemName + "</strong> has been confirmed via Stripe.\n\n" +
            "Transaction Reference: " + reference + "\n\n" +
            "You may now arrange the physical handover of the item.");
    }

    private String buildEmailTemplate(String title, String messageContent) {
        return "<!DOCTYPE html>" +
                "<html>" +
                "<head>" +
                "<meta charset=\"utf-8\">" +
                "<style>" +
                "  body { font-family: 'Inter', -apple-system, sans-serif; background-color: #F8FAFC; color: #0F172A; margin: 0; padding: 40px; }" +
                "  .container { max-width: 600px; margin: 0 auto; background: #FFFFFF; border-radius: 12px; box-shadow: 0 4px 6px -1px rgba(0,0,0,0.1); overflow: hidden; }" +
                "  .header { background: linear-gradient(135deg, #9F1239 0%, #D97706 100%); padding: 30px; text-align: center; color: white; }" +
                "  .header h1 { margin: 0; font-size: 24px; font-weight: 700; }" +
                "  .content { padding: 30px; line-height: 1.6; font-size: 16px; color: #334155; }" +
                "  .footer { padding: 20px; text-align: center; font-size: 13px; color: #94A3B8; background: #F1F5F9; }" +
                "</style>" +
                "</head>" +
                "<body>" +
                "  <div class=\"container\">" +
                "    <div class=\"header\">" +
                "      <h1>Lost & Found Platform</h1>" +
                "    </div>" +
                "    <div class=\"content\">" +
                "      <h2 style=\"color: #0F172A; margin-top: 0;\">" + title + "</h2>" +
                "      <p>" + messageContent.replace("\n", "<br>") + "</p>" +
                "    </div>" +
                "    <div class=\"footer\">" +
                "      &copy; 2026 Lost & Found Platform. All rights reserved." +
                "    </div>" +
                "  </div>" +
                "</body>" +
                "</html>";
    }
}
