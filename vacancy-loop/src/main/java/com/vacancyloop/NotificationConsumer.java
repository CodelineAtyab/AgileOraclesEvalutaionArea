package com.vacancyloop;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Types;

@Component
public class NotificationConsumer
        implements CommandLineRunner {

    private final DataSource dataSource;
    private final SlackService slackService;


    public NotificationConsumer(
            DataSource dataSource,
            SlackService slackService
    ) {
        this.dataSource = dataSource;
        this.slackService = slackService;
    }


    @Override
    public void run(String... args) throws Exception {

        System.out.println(
                "Starting notification consumer..."
        );


        try (
                Connection connection =
                        dataSource.getConnection();

                CallableStatement statement =
                        connection.prepareCall(
                                "{call get_pending_notifications(?)}"
                        )
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

                    int notificationId =
                            resultSet.getInt(
                                    "notification_id"
                            );

                    String subject =
                            resultSet.getString(
                                    "subject"
                            );

                    String body =
                            resultSet.getString(
                                    "body"
                            );


                    try {

                        System.out.println(
                                "Sending notification "
                                        + notificationId
                        );


                        boolean sent =
                                slackService.sendMessage(
                                        subject,
                                        body
                                );


                        if (sent) {

                            markAsSent(notificationId);

                            System.out.println(
                                    "Notification "
                                            + notificationId
                                            + " sent successfully."
                            );

                        } else {

                            System.out.println(
                                    "Slack rejected notification "
                                            + notificationId
                            );
                        }


                    } catch (Exception e) {

                        System.out.println(
                                "Failed notification "
                                        + notificationId
                        );

                        System.out.println(
                                e.getMessage()
                        );
                    }
                }
            }
        }


        System.out.println(
                "Consumer finished."
        );
    }


    private void markAsSent(
            int notificationId
    ) throws Exception {

        try (
                Connection connection =
                        dataSource.getConnection();

                CallableStatement statement =
                        connection.prepareCall(
                                "{call mark_notification_sent(?)}"
                        )
        ) {

            statement.setInt(
                    1,
                    notificationId
            );

            statement.execute();
        }
    }
}