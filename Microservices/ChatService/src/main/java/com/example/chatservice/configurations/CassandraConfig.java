package com.example.chatservice.configurations;

import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.cassandra.CassandraProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.cassandra.config.AbstractCassandraConfiguration;
import org.springframework.data.cassandra.config.CqlSessionFactoryBean;
import org.springframework.data.cassandra.config.SchemaAction;
import org.springframework.data.cassandra.core.cql.keyspace.CreateKeyspaceSpecification;
import org.springframework.data.cassandra.core.cql.keyspace.KeyspaceOption;
import org.springframework.data.cassandra.repository.config.EnableCassandraRepositories;

import java.util.List;

@Configuration
@EnableCassandraRepositories(basePackages = "com.example.chatservice.repositories")
public class CassandraConfig extends AbstractCassandraConfiguration {

    @Value("${spring.cassandra.keyspace-name}")
    private String keyspaceName;

    @Value("${spring.cassandra.contact-points}")
    private String contactPoints;

    @Value("${spring.cassandra.port}")
    private int port;

    @Value("${spring.cassandra.local-datacenter}")
    private String dataCenter;

    @Value("${spring.cassandra.schema-action}")
    private SchemaAction schemaAction;


    public CassandraConfig(CassandraProperties cassandraProperties) {
        this.keyspaceName = cassandraProperties.getKeyspaceName();
        this.contactPoints = String.join(",", cassandraProperties.getContactPoints());
        this.port = cassandraProperties.getPort();

    }

    @Bean
    @Override
    public @NotNull CqlSessionFactoryBean cassandraSession() {
        final CqlSessionFactoryBean cqlSessionFactoryBean = new CqlSessionFactoryBean();
        cqlSessionFactoryBean.setLocalDatacenter(getLocalDataCenter());
        cqlSessionFactoryBean.setContactPoints(getContactPoints());
        cqlSessionFactoryBean.setKeyspaceName(getKeyspaceName());
        cqlSessionFactoryBean.setPort(getPort());

        CreateKeyspaceSpecification createKeyspaceSpecification = CreateKeyspaceSpecification
                .createKeyspace(getKeyspaceName())
                .ifNotExists()
                .with(KeyspaceOption.DURABLE_WRITES, true)
                .withSimpleReplication(1);

        cqlSessionFactoryBean.setKeyspaceCreations(List.of(createKeyspaceSpecification));

        return cqlSessionFactoryBean;
    }

    @Override
    public @NotNull String getKeyspaceName() {
        return keyspaceName;
    }

    @Override
    public @NotNull SchemaAction getSchemaAction() {
        return SchemaAction.valueOf(schemaAction.name());
    }

    @Override
    public @NotNull String[] getEntityBasePackages() {
        return new String[] { "com.example.chatservice.models" };
    }

    @Override
    public @NotNull String getContactPoints() {
        return contactPoints;
    }

    @Override
    public int getPort() {
        return port;
    }

    @Override
    public @NotNull String getLocalDataCenter() {
        return dataCenter;
    }
}