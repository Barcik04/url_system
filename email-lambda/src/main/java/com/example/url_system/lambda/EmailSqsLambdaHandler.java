package com.example.url_system.lambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import com.amazonaws.services.lambda.runtime.events.SQSBatchResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sesv2.SesV2Client;
import software.amazon.awssdk.services.sesv2.model.*;

import java.util.ArrayList;
import java.util.List;

public class EmailSqsLambdaHandler implements RequestHandler<SQSEvent, SQSBatchResponse> {

    private static final ObjectMapper mapper = new ObjectMapper();

    private static final SesV2Client ses = SesV2Client.builder()
            .region(Region.of(getEnv("AWS_REGION", "eu-north-1")))
            .build();

    private static final String FROM_EMAIL = getEnv("FROM_EMAIL", "");
    private static final String CONFIG_SET = System.getenv("SES_CONFIGURATION_SET");




    @Override
    public SQSBatchResponse handleRequest(SQSEvent event, Context context) {
        List<SQSBatchResponse.BatchItemFailure> failures = new ArrayList<>();

        for (SQSEvent.SQSMessage msg : event.getRecords()) {
            try {
                EmailPayload p = mapper.readValue(msg.getBody(), EmailPayload.class);

                String subject = safe(p.subject());
                String contentHtml = p.body();

                if (subject.toLowerCase().contains("verify")) {
                    contentHtml = buildVerifyHtml(p.body());
                }

                sendEmailSes(p.toEmail(), subject, contentHtml);

                context.getLogger().log("Email sent to: " + p.toEmail() + "\n");
            } catch (Exception ex) {
                failures.add(new SQSBatchResponse.BatchItemFailure(msg.getMessageId()));
                context.getLogger().log("FAILED messageId=" + msg.getMessageId() + " error=" + ex + "\n");
            }
        }

        return new SQSBatchResponse(failures);
    }




    private static void sendEmailSes(String to, String subject, String html) {
        if (FROM_EMAIL.isBlank()) {
            throw new IllegalStateException("FROM_EMAIL env var is empty");
        }

        Destination destination = Destination.builder()
                .toAddresses(to)
                .build();

        Content subj = Content.builder().data(subject).build();
        Content htmlContent = Content.builder().data(html).build();

        Body body = Body.builder()
                .html(htmlContent)
                .build();

        Message message = Message.builder()
                .subject(subj)
                .body(body)
                .build();

        EmailContent emailContent = EmailContent.builder()
                .simple(message)
                .build();

        SendEmailRequest.Builder req = SendEmailRequest.builder()
                .fromEmailAddress(FROM_EMAIL)
                .destination(destination)
                .content(emailContent);

        if (CONFIG_SET != null && !CONFIG_SET.isBlank()) {
            req.configurationSetName(CONFIG_SET);
        }

        ses.sendEmail(req.build());
    }





    private static String buildVerifyHtml(String verifyUrl) {
        return """
            <div style="font-family: Arial, sans-serif; line-height: 1.5;">
              <h2>Verify your email</h2>
              <p>Click the button below to verify your email address.</p>
              <p>
                <a href="%s"
                   style="display:inline-block;padding:12px 18px;text-decoration:none;border-radius:8px;">
                   Verify email
                </a>
              </p>
              <p>If the button doesn't work, paste this link into your browser:</p>
              <p><a href="%s">%s</a></p>
            </div>
            """.formatted(verifyUrl, verifyUrl, verifyUrl);
    }

    private static String safe(String s) {
        return s == null ? "" : s;
    }

    private static String getEnv(String key, String def) {
        String v = System.getenv(key);
        return (v == null || v.isBlank()) ? def : v;
    }
}

