//package com.api.bankApi.config;
//
//import io.r2dbc.spi.ConnectionFactories;
//import io.r2dbc.spi.ConnectionFactory;
//import io.r2dbc.spi.ConnectionFactoryOptions;
//import static io.r2dbc.spi.ConnectionFactoryOptions.CONNECT_TIMEOUT;
//import static io.r2dbc.spi.ConnectionFactoryOptions.DATABASE;
//import static io.r2dbc.spi.ConnectionFactoryOptions.DRIVER;
//import static io.r2dbc.spi.ConnectionFactoryOptions.HOST;
//import static io.r2dbc.spi.ConnectionFactoryOptions.PASSWORD;
//import static io.r2dbc.spi.ConnectionFactoryOptions.PORT;
//import static io.r2dbc.spi.ConnectionFactoryOptions.USER;
//import io.r2dbc.spi.Option;
//import java.time.Duration;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.context.annotation.Primary;
//import org.springframework.data.r2dbc.config.AbstractR2dbcConfiguration;
//import org.springframework.data.r2dbc.core.DatabaseClient;
//
///**
// *
// * @author martin
// */
//@Configuration
//public class DbConfig extends AbstractR2dbcConfiguration {
//
//    @Bean
//    @Override
//    public ConnectionFactory connectionFactory() {
//        ConnectionFactoryOptions options = ConnectionFactoryOptions.builder()
//                .option(DRIVER, "mysql")
//                .option(HOST, "localhost")
//                .option(USER, "testcontainerUser")
//                .option(PORT, 3306) // optional, default 3306
//                .option(PASSWORD, "qwerty1234") // optional, default null, null means has no password
//                .option(DATABASE, "bank_db") // optional, default null, null means not specifying the database
//                .option(CONNECT_TIMEOUT, Duration.ofSeconds(3)) // optional, default null, null means no timeout
//                .option(Option.valueOf("socketTimeout"), Duration.ofSeconds(4)) // optional, default null, null means no timeout
//                .option(Option.valueOf("sslMode"), "disabled")
//                .build();
//        return ConnectionFactories.get(options);
//    }
//
//    @Bean
//    @Primary
//    public DatabaseClient r2dbcDatabaseClient(ConnectionFactory connectionFactory) {
//        connectionFactory = connectionFactory();
//        return DatabaseClient.create(connectionFactory);
//    }
//
//}
