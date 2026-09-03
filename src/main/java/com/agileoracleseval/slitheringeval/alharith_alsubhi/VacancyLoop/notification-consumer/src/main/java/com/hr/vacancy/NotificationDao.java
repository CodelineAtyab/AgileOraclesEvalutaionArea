package com.hr.vacancy;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class NotificationDao {

    private final JdbcTemplate jdbc;

    public NotificationDao(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    // calls oracle procedure and walks the ref cursor
    public List<NotificationRow> fetchPending() {
        return jdbc.execute((Connection con) -> {
            List<NotificationRow> list = new ArrayList<>();

            try (CallableStatement cs = con.prepareCall("{ call get_pending_notifications(?) }")) {
                cs.registerOutParameter(1, Types.REF_CURSOR);
                cs.execute();

                try (ResultSet rs = (ResultSet) cs.getObject(1)) {
                    while (rs != null && rs.next()) {
                        NotificationRow row = new NotificationRow();
                        row.setId(rs.getLong("notification_id"));
                        row.setSubject(rs.getString("subject"));
                        row.setBody(rs.getString("body"));
                        row.setSourceType(rs.getString("source_type"));

                        long sid = rs.getLong("source_id");
                        if (rs.wasNull()) {
                            row.setSourceId(null);
                        } else {
                            row.setSourceId(sid);
                        }

                        list.add(row);
                    }
                }
            }

            return list;
        });
    }

    public void markSent(long id) {
        jdbc.execute((Connection con) -> {
            try (CallableStatement cs = con.prepareCall("{ call mark_notification_sent(?) }")) {
                cs.setLong(1, id);
                cs.execute();
            }
            return null;
        });
    }
}
