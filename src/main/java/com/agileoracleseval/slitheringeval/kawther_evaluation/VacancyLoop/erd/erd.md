# ERD — The Vacancy Loop

```mermaid
erDiagram
    VACANT_JOBS ||--o{ JOB_APPLICATIONS : "receives"
    VACANT_JOBS {
        NUMBER    job_id PK
        VARCHAR2  job_title
        VARCHAR2  job_description
        TIMESTAMP posted_on
        TIMESTAMP expires_at
        CHAR      active_flag
    }

    JOBS_POSTING_ARCHIVE {
        NUMBER    job_id PK
        VARCHAR2  job_title
        VARCHAR2  job_description
        TIMESTAMP posted_on
        TIMESTAMP expires_at
        TIMESTAMP archived_on
    }

    NOTIFICATION_OUTBOX {
        NUMBER    notification_id PK
        NUMBER    job_id "no FK - see note"
        VARCHAR2  msg_category
        VARCHAR2  msg_subject
        VARCHAR2  msg_body
        TIMESTAMP generated_on
        CHAR      delivered_flag
        TIMESTAMP delivered_on
    }

    JOB_APPLICATIONS {
        NUMBER    application_id PK
        NUMBER    job_id FK
        VARCHAR2  candidate_name
        VARCHAR2  candidate_email
        TIMESTAMP submitted_on
        CHAR      digested_flag
    }
```

## Design notes (one line each, for the defense)

- **`JOBS_POSTING_ARCHIVE` has no FK back to `VACANT_JOBS`.** It's a historical copy taken at the moment a posting is purged; by definition the source row is gone a moment later, so a live FK would be pointless (and would break the delete).
- **`NOTIFICATIONS.job_id` is a plain column, not a foreign key.** A notification can still be sitting unsent when its posting gets archived and purged (steps 08/09 run every minute; the consumer might not have drained the queue yet). A hard FK would make the purge job fail with an integrity-constraint error the moment that happens. We keep `job_id` for traceability but don't enforce it.
- **`JOB_APPLICATIONS.job_id` is a real FK to `VACANT_JOBS`, `ON DELETE CASCADE`.** Applications only make sense while their posting is live. Once a posting is purged from `VACANT_JOBS` (already archived), its applications go with it rather than becoming orphan rows with no parent to join to.
- Cardinality: one `VACANT_JOBS` row → many `JOB_APPLICATIONS` rows (many candidates can apply to the same posting; not every posting gets an application).
- **`JOB_APPLICATIONS.digested_flag`** marks whether an application has already been counted into a daily-digest notification. Without it, running `send_daily_application_digest` twice would count the same application twice - this flag is what makes that procedure safe to run any number of times.
