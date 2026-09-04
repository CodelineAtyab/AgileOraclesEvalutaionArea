package HRRecruitmentManagementSystem.HRRecruitmentManagementSystem;

import java.sql.Timestamp;

public record NotificationRecord(
        long id,
        long jobId,
        String subject,
        String body,
        String status,
        Timestamp createdAt
) {
}