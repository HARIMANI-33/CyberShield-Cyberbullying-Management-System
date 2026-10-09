package com.cybershield.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;
import java.net.InetSocketAddress;
import java.net.Socket;

@Configuration
public class DataSourceConfig {

    private static final Logger log = LoggerFactory.getLogger(DataSourceConfig.class);

    @Value("${spring.datasource.url:jdbc:postgresql://localhost:5432/cybershield_db}")
    private String postgresUrl;

    @Value("${spring.datasource.username:postgres}")
    private String postgresUsername;

    @Value("${spring.datasource.password:Hari@2008}")
    private String postgresPassword;

    @Value("${spring.datasource.mysql.url:jdbc:mysql://localhost:3306/cybershield_db}")
    private String mysqlUrl;

    @Value("${spring.datasource.mysql.username:root}")
    private String mysqlUsername;

    @Value("${spring.datasource.mysql.password:root}")
    private String mysqlPassword;

    @Bean
    @Primary
    public DataSource dataSource() {
        boolean postgresAvailable = isPortOpen("localhost", 5432, 1200);
        boolean mysqlAvailable = isPortOpen("localhost", 3306, 1200);
        HikariConfig config = new HikariConfig();

        if (postgresAvailable) {
            log.info("Detected PostgreSQL server on port 5432. Connecting to PostgreSQL cybershield_db...");
            config.setJdbcUrl(postgresUrl);
            config.setUsername(postgresUsername);
            config.setPassword(postgresPassword);
            config.setDriverClassName("org.postgresql.Driver");
        } else if (mysqlAvailable) {
            log.info("Detected MySQL server on port 3306. Connecting to MySQL cybershield_db...");
            config.setJdbcUrl(mysqlUrl);
            config.setUsername(mysqlUsername);
            config.setPassword(mysqlPassword);
            config.setDriverClassName("com.mysql.cj.jdbc.Driver");
        } else {
            log.warn("Neither PostgreSQL nor MySQL server detected. Automatically using in-memory demo database for presentation...");
            config.setJdbcUrl("jdbc:h2:mem:cybershield_db;MODE=PostgreSQL;DB_CLOSE_DELAY=-1;DATABASE_TO_LOWER=TRUE");
            config.setUsername("sa");
            config.setPassword("");
            config.setDriverClassName("org.h2.Driver");
        }

        config.setMaximumPoolSize(10);
        return new HikariDataSource(config);
    }

    private boolean isPortOpen(String host, int port, int timeoutMs) {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(host, port), timeoutMs);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
