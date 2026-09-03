package com.VacancyApp.repository;

import com.VacancyApp.model.ApplicationCount;
import com.VacancyApp.model.DashboardSnapshot;
import com.VacancyApp.model.DashboardSummary;
import com.VacancyApp.model.Vacancy;
import com.VacancyApp.model.VacancyApplication;
import oracle.jdbc.OracleTypes;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.SqlOutParameter;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Reads from and writes to the vacancy/application tables.
 * Read paths call Oracle stored procedures (business logic stays in the DB);
 * the two writes are trivial inserts that Oracle has no procedure for.
 */
@Repository
public class VacancyRepository {

    private final JdbcTemplate jdbcTemplate;

    public VacancyRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    // --- Row mappers -------------------------------------------------------

    // Shared by GET_AVAILABLE_VACANCIES (all 7 columns) and GET_VACANCY_DASHBOARD's jobs cursor
    // (only job_id, title, description, department, expires_at) — so created_at and status are optional.
    static final RowMapper<Vacancy> VACANCY_MAPPER = (rs, i) -> new Vacancy(
            rs.getLong("JOB_ID"),
            rs.getString("TITLE"),
            rs.getString("DESCRIPTION"),
            rs.getString("DEPARTMENT"),
            hasColumn(rs, "CREATED_AT") ? toLocalDateTime(rs, "CREATED_AT") : null,
            toLocalDateTime(rs, "EXPIRES_AT"),
            hasColumn(rs, "STATUS") ? rs.getString("STATUS") : null
    );

    private static final RowMapper<DashboardSummary> SUMMARY_MAPPER = (rs, i) -> new DashboardSummary(
            rs.getLong("ACTIVE_JOBS"),
            rs.getLong("TOTAL_APPLICATIONS"),
            rs.getLong("NEW_NOTIFICATIONS")
    );

    private static final RowMapper<ApplicationCount> APP_COUNT_MAPPER = (rs, i) -> new ApplicationCount(
            rs.getLong("JOB_ID"),
            rs.getString("TITLE"),
            rs.getLong("APPLICATION_COUNT")
    );

    // --- Stored-procedure reads -------------------------------------------

    /** Calls GET_AVAILABLE_VACANCIES (one SYS_REFCURSOR OUT). */
    @SuppressWarnings("unchecked")
    public List<Vacancy> findAvailable() {
        SimpleJdbcCall call = new SimpleJdbcCall(jdbcTemplate)
                .withProcedureName("GET_AVAILABLE_VACANCIES")
                .withoutProcedureColumnMetaDataAccess()
                .declareParameters(
                        new SqlOutParameter("P_VACANCIES", OracleTypes.CURSOR, VACANCY_MAPPER));
        Map<String, Object> out = call.execute();
        return (List<Vacancy>) out.get("P_VACANCIES");
    }

    /** Calls GET_VACANCY_DASHBOARD (three SYS_REFCURSOR OUT params). */
    @SuppressWarnings("unchecked")
    public DashboardSnapshot fetchDashboard() {
        SimpleJdbcCall call = new SimpleJdbcCall(jdbcTemplate)
                .withProcedureName("GET_VACANCY_DASHBOARD")
                .withoutProcedureColumnMetaDataAccess()
                .declareParameters(
                        new SqlOutParameter("P_SUMMARY", OracleTypes.CURSOR, SUMMARY_MAPPER),
                        new SqlOutParameter("P_JOBS", OracleTypes.CURSOR, VACANCY_MAPPER),
                        new SqlOutParameter("P_APP_COUNTS", OracleTypes.CURSOR, APP_COUNT_MAPPER));
        Map<String, Object> out = call.execute();

        List<DashboardSummary> summaries = (List<DashboardSummary>) out.get("P_SUMMARY");
        DashboardSummary summary = summaries.isEmpty()
                ? new DashboardSummary(0, 0, 0)
                : summaries.get(0);
        List<Vacancy> jobs = (List<Vacancy>) out.get("P_JOBS");
        List<ApplicationCount> counts = (List<ApplicationCount>) out.get("P_APP_COUNTS");
        return new DashboardSnapshot(summary, jobs, counts);
    }

    // --- Direct writes (no Oracle procedure exists for these) --------------

    /** Looks up a vacancy by id, or empty if it does not exist. */
    public Optional<Vacancy> findById(long jobId) {
        List<Vacancy> rows = jdbcTemplate.query(
                "SELECT job_id, title, description, department, created_at, expires_at, status "
                        + "FROM vacant_jobs WHERE job_id = ?",
                VACANCY_MAPPER, jobId);
        return rows.stream().findFirst();
    }

    /** Inserts a new vacancy and returns its generated job_id. */
    public long createVacancy(String title, String description, String department, LocalDateTime expiresAt) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(
                    "INSERT INTO vacant_jobs (title, description, department, expires_at) "
                            + "VALUES (?, ?, ?, ?)",
                    new String[]{"JOB_ID"});
            ps.setString(1, title);
            ps.setString(2, description);
            ps.setString(3, department);
            ps.setTimestamp(4, Timestamp.valueOf(expiresAt));
            return ps;
        }, keyHolder);
        Number key = keyHolder.getKey();
        if (key == null) {
            throw new IllegalStateException("Vacancy insert did not return a generated job_id");
        }
        return key.longValue();
    }

    /** Inserts a candidate application and returns the created row. */
    public VacancyApplication insertApplication(long jobId, String applicantName, String applicantEmail) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(
                    "INSERT INTO job_applications (job_id, applicant_name, applicant_email) "
                            + "VALUES (?, ?, ?)",
                    new String[]{"APPLICATION_ID"});
            ps.setLong(1, jobId);
            ps.setString(2, applicantName);
            ps.setString(3, applicantEmail);
            return ps;
        }, keyHolder);
        Number key = keyHolder.getKey();
        long applicationId = key == null ? 0L : key.longValue();
        return new VacancyApplication(applicationId, jobId, applicantName, applicantEmail, LocalDateTime.now());
    }

    // --- Helpers -----------------------------------------------------------

    private static LocalDateTime toLocalDateTime(ResultSet rs, String column) throws SQLException {
        Timestamp ts = rs.getTimestamp(column);
        return ts == null ? null : ts.toLocalDateTime();
    }

    private static boolean hasColumn(ResultSet rs, String column) {
        try {
            rs.findColumn(column);
            return true;
        } catch (SQLException e) {
            return false;
        }
    }
}
