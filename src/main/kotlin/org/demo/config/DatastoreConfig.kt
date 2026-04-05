package org.demo.config

import io.r2dbc.spi.ConnectionFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.ClassPathResource
import org.springframework.data.r2dbc.connectionfactory.init.CompositeDatabasePopulator
import org.springframework.data.r2dbc.connectionfactory.init.ConnectionFactoryInitializer
import org.springframework.data.r2dbc.connectionfactory.init.ResourceDatabasePopulator
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories

/**
 * Configures the reactive database connection factory and initializes the schema.
 *
 * On application startup, [initializer] runs `schema.sql` (DDL) followed by
 * `data.sql` (seed data) against the configured R2DBC data source. This approach
 * is manual but explicit — easy to reason about when learning.
 *
 * Learning note: R2DBC does not support traditional JDBC `DataSource`-based
 * initialization. Spring Data R2DBC provides [ConnectionFactoryInitializer] as
 * the equivalent mechanism for reactive environments.
 *
 * Design note: For production systems, schema migrations should be managed
 * by a dedicated tool such as Flyway or Liquibase, which offer versioning,
 * rollback support, and audit trails. The simple SQL-file approach used here
 * is intentionally simplified for demo purposes.
 */
@Configuration
@EnableR2dbcRepositories
class DatastoreConfig {

    /**
     * Registers a [ConnectionFactoryInitializer] that applies DDL and seed data
     * scripts to the database when the Spring context starts.
     *
     * Execution order: `schema.sql` → `data.sql`.
     */
    @Bean
    fun initializer(connectionFactory: ConnectionFactory): ConnectionFactoryInitializer {
        val initializer = ConnectionFactoryInitializer()
        initializer.setConnectionFactory(connectionFactory)
        val populator = CompositeDatabasePopulator().apply {
            addPopulators(ResourceDatabasePopulator(ClassPathResource("schema.sql")))
            addPopulators(ResourceDatabasePopulator(ClassPathResource("data.sql")))
        }
        initializer.setDatabasePopulator(populator)
        return initializer
    }
}
