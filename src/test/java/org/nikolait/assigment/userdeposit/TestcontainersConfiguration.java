package org.nikolait.assigment.userdeposit;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.elasticsearch.ElasticsearchContainer;
import org.testcontainers.utility.DockerImageName;

@TestConfiguration(proxyBeanMethods = false)
class TestcontainersConfiguration {

    @Bean
    @ServiceConnection
    ElasticsearchContainer elasticsearchContainer() {
        ElasticsearchContainer container = new ElasticsearchContainer(DockerImageName.parse("elastic/elasticsearch:7.17.10")
                .asCompatibleSubstituteFor("docker.elastic.co/elasticsearch/elasticsearch")
        );
        container.withEnv("xpack.security.enabled", "false");
        return container;
    }

    @Bean
    @ServiceConnection
    PostgreSQLContainer<?> postgresContainer() {
        return new PostgreSQLContainer<>(DockerImageName.parse("postgres:17-alpine"));
    }

//	@Bean
//	@ServiceConnection(name = "redis")
//	GenericContainer<?> redisContainer() {
//		return new GenericContainer<>(DockerImageName.parse("redis:7.4-alpine")).withExposedPorts(6379);
//	}

}
