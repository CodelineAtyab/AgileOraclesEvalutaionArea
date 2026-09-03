package com.example.vacancyconsumer.repository;

import com.example.vacancyconsumer.model.NotificationMessage;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

@Repository
public class NotificationRepository {

    private final DataSource dataSource;

    public NotificationRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void consumePending(
            NotificationHandler handler
    ) throws SQLException {

        String sql =
                "{ call get_pending_notifications(?) }";

        try (
                Connection connection =
                        dataSource.getConnection();

                CallableStatement statement =
                        connection.prepareCall(sql)
        ) {

            statement.registerOutParameter(
                    1,
                    Types.REF_CURSOR
            );

            statement.execute();

            try (
                    ResultSet resultSet =
                            (ResultSet) statement.getObject(1)
            ) {

                while (resultSet.next()) {

                    NotificationMessage notification =
                            new NotificationMessage(
                                    resultSet.getLong(
                                            "notification_id"
                                    ),
                                    resultSet.getString(
                                            "subject"
                                    ),
                                    resultSet.getString(
                                            "body"
                                    ),
                                    resultSet.getTimestamp(
                                            "created_at"
                                    )
                            );

                    handler.handle(notification);
                }
            }
        }
    }


    public void markAsSent(long notificationId)
            throws SQLException {

        String sql =
                "{ call mark_notification_sent(?) }";

        try (
                Connection connection =
                        dataSource.getConnection();

                CallableStatement statement =
                        connection.prepareCall(sql)
        ) {

            connection.setAutoCommit(false);

            try {
                statement.setLong(
                        1,
                        notificationId
                );

                statement.execute();

                connection.commit();

            }
            catch (SQLException exception) {

                connection.rollback();
                throw exception;
            }
        }
    }


    @FunctionalInterface
    public interface NotificationHandler {

        void handle(
                NotificationMessage notification
        );
    }
}