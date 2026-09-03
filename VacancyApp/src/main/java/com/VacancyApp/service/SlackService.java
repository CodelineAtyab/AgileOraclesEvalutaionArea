package com.VacancyApp.service;

import com.VacancyApp.config.SlackProperties;
import com.VacancyApp.exception.SlackDeliveryException;
import com.VacancyApp.model.ApplicationCount;
import com.VacancyApp.model.DashboardSnapshot;
import com.VacancyApp.model.Notification;
import com.VacancyApp.model.Vacancy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Builds richly-formatted Slack Block Kit messages (headers, fields, dividers, context) and posts
 * them to the incoming webhook. The dashboard is one message; each notification is its own message.
 */
@Service
public class SlackService {

    private static final Logger log = LoggerFactory.getLogger(SlackService.class);
    private static final DateTimeFormatter TS = DateTimeFormatter.ofPattern("dd MMM yyyy · HH:mm");
    /** Slack caps a message at 50 blocks; keep well under that for the dashboard. */
    private static final int MAX_VACANCY_BLOCKS = 20;

    private final RestClient slackRestClient;
    private final SlackProperties properties;

    public SlackService(RestClient slackRestClient, SlackProperties properties) {
        this.slackRestClient = slackRestClient;
        this.properties = properties;
    }

    // --- Message builders (Block Kit) --------------------------------------

    /** Builds the vacancy dashboard as a list of Block Kit blocks. */
    public List<Map<String, Object>> buildDashboardBlocks(DashboardSnapshot dashboard) {
        List<Map<String, Object>> blocks = new ArrayList<>();
        blocks.add(header("📋 Vacancy Dashboard"));
        blocks.add(fields(List.of(
                "*🟢 Available jobs*\n" + dashboard.summary().activeJobs(),
                "*🔔 New notifications*\n" + dashboard.summary().newNotifications(),
                "*📨 Total applications*\n" + dashboard.summary().totalApplications())));
        blocks.add(divider());

        Map<Long, Long> countByJob = dashboard.applicationCounts().stream()
                .collect(Collectors.toMap(ApplicationCount::jobId, ApplicationCount::applicationCount, (a, b) -> a));

        List<Vacancy> jobs = dashboard.jobs();
        if (jobs.isEmpty()) {
            blocks.add(section("_No current vacancies._"));
            return blocks;
        }

        int shown = Math.min(jobs.size(), MAX_VACANCY_BLOCKS);
        for (int i = 0; i < shown; i++) {
            Vacancy v = jobs.get(i);
            long count = countByJob.getOrDefault(v.jobId(), 0L);
            String text = "*" + safe(v.title()) + "*   ·   🏢 " + safe(orDash(v.department())) + "\n"
                    + "> " + safe(v.description()) + "\n"
                    + "⏰ Expires *" + format(v.expiresAt()) + "*   ·   📨 *" + count + "* application(s)";
            blocks.add(section(text));
            if (i < shown - 1) {
                blocks.add(divider());
            }
        }
        if (jobs.size() > shown) {
            blocks.add(context("… and " + (jobs.size() - shown) + " more vacanc"
                    + (jobs.size() - shown == 1 ? "y" : "ies")));
        }
        return blocks;
    }

    /** Builds a single notification as its own list of Block Kit blocks. */
    public List<Map<String, Object>> buildNotificationBlocks(Notification n) {
        List<Map<String, Object>> blocks = new ArrayList<>();
        blocks.add(header("🔔 " + truncate(safe(n.subject()), 148)));
        blocks.add(section("> " + safe(n.body())));
        blocks.add(context(
                "🆔 *ID:* " + n.notificationId()
                        + "   ·   " + statusBadge(n.status())
                        + "   ·   🕒 *Created:* " + format(n.createdAt())
                        + "   ·   📤 *Sent:* " + (n.sentAt() == null ? "—" : format(n.sentAt()))));
        return blocks;
    }

    // --- Transport ---------------------------------------------------------

    /** Sends the dashboard message. @throws SlackDeliveryException on failure. */
    public void sendDashboard(DashboardSnapshot dashboard) {
        post(buildDashboardBlocks(dashboard), "Vacancy Dashboard");
    }

    /** Sends one notification as its own message. @throws SlackDeliveryException on failure. */
    public void sendNotification(Notification n) {
        post(buildNotificationBlocks(n), "Notification #" + n.notificationId() + ": " + safe(n.subject()));
    }

    /**
     * Posts a Block Kit message to the Slack webhook.
     * @param fallbackText plain-text shown in notifications/previews that can't render blocks.
     */
    private void post(List<Map<String, Object>> blocks, String fallbackText) {
        if (!properties.isEnabled() || properties.getUrl() == null || properties.getUrl().isBlank()) {
            log.info("Slack disabled — message not sent: {}", fallbackText);
            return;
        }
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("text", fallbackText);
        payload.put("blocks", blocks);
        try {
            slackRestClient.post()
                    .uri(properties.getUrl())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(payload)
                    .retrieve()
                    .toBodilessEntity();
            log.debug("Slack message delivered: {}", fallbackText);
        } catch (RestClientException e) {
            throw new SlackDeliveryException("Failed to deliver Slack message: " + fallbackText, e);
        }
    }

    // --- Block Kit helpers -------------------------------------------------

    private static Map<String, Object> header(String text) {
        return Map.of("type", "header",
                "text", Map.of("type", "plain_text", "text", text, "emoji", true));
    }

    private static Map<String, Object> section(String mrkdwn) {
        return Map.of("type", "section",
                "text", Map.of("type", "mrkdwn", "text", mrkdwn));
    }

    private static Map<String, Object> fields(List<String> mrkdwnFields) {
        List<Map<String, Object>> fieldObjects = mrkdwnFields.stream()
                .map(f -> Map.<String, Object>of("type", "mrkdwn", "text", f))
                .collect(Collectors.toList());
        return Map.of("type", "section", "fields", fieldObjects);
    }

    private static Map<String, Object> context(String mrkdwn) {
        return Map.of("type", "context",
                "elements", List.of(Map.of("type", "mrkdwn", "text", mrkdwn)));
    }

    private static Map<String, Object> divider() {
        return Map.of("type", "divider");
    }

    // --- Formatting helpers ------------------------------------------------

    private static String statusBadge(String status) {
        String s = status == null ? "" : status;
        String emoji = switch (s) {
            case "SENT" -> "✅";
            case "FAILED" -> "❌";
            case "PENDING" -> "⏳";
            default -> "📌";
        };
        return emoji + " *Status:* " + (s.isEmpty() ? "—" : s);
    }

    private static String format(LocalDateTime value) {
        return value == null ? "—" : value.format(TS);
    }

    private static String orDash(String value) {
        return value == null || value.isBlank() ? "—" : value;
    }

    /** Escapes the three characters Slack treats specially in mrkdwn text. */
    private static String safe(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    private static String truncate(String value, int max) {
        return value.length() <= max ? value : value.substring(0, max - 1) + "…";
    }
}
