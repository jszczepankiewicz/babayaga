package com.github.jszczepankiewicz.babayaga.config

import com.zaxxer.hikari.HikariDataSource
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.jdbc.datasource.DataSourceTransactionManager
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.annotation.EnableTransactionManagement
import javax.sql.DataSource

/**
 * @author jszczepankiewicz
 * @since 2016-09-07
 */
@Configuration
@EnableTransactionManagement
open class JdbcDataSourceConfig {

    @Bean
    open fun txManager(): PlatformTransactionManager {
        return DataSourceTransactionManager(jdbcDataSource())
    }

    @Bean
    open fun jdbcDataSource(): DataSource {

        val ds = HikariDataSource()
        ds.jdbcUrl = "jdbc:postgresql:babayaga_tst"
        ds.username = "postgres"
        ds.password = "postgres"

        return ds
    }
}
