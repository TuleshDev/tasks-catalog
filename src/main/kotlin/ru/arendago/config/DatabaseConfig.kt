package ru.arendago.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.jdbc.core.simple.JdbcClient
import javax.sql.DataSource

@Configuration
class DatabaseConfig {

    @Bean
    fun jdbcClient(dataSource: DataSource): JdbcClient =
        JdbcClient.create(dataSource)
}
