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

    @Value("${spring.datasource.url:jdbc:mysql://localhost:3306/cybershield_db}")
    private String mysqlUrl;

    @Value("${spring.datasource.username:root}")
    private String mysqlUsername;

    @Value("${spring.datasource.password:root}")
    private String mysqlPassword;

    @Bean
    @Primary
    public DataSource dataSource() {
        boolean mysqlAvailable = isPortOpen("localhost", 3306, 1200);
        HikariConfig config = new HikariConfig();

        if (mysqlAvailable) {
            log.info("Detected MySQL server on port 3306. Connecting to MySQL cybershield_db...");
            config.setJdbcUrl(mysqlUrl);
            config.setUsername(mysqlUsername);
            config.setPassword(mysqlPassword);
            config.setDriverClassName("com.mysql.cj.jdbc.Driver");
        } else {
            log.warn("MySQL server not detected on localhost:3306. Automatically using in-memory demo database for presentation...");
            config.setJdbcUrl("jdbc:h2:mem:cybershield_db;MODE=MySQL;DB_CLOSE_DELAY=-1;DATABASE_TO_LOWER=TRUE");
            config.setUsername("root");
            config.setPassword("root");
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
