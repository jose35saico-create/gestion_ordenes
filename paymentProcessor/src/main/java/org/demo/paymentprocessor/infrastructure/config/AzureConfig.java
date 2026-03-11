package org.demo.paymentprocessor.infrastructure.config;

import com.azure.cosmos.CosmosClient;
import com.azure.cosmos.CosmosContainer;
import com.azure.cosmos.CosmosDatabase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AzureConfig {

    private final String DATABASE_NAME = "OrdersDB";
    private final String CONTAINER_NAME = "TransactionsContainer";

    @Bean
    public CosmosContainer cosmosContainer(CosmosClient cosmosClient) {
        cosmosClient.createDatabaseIfNotExists(DATABASE_NAME);
        CosmosDatabase database = cosmosClient.getDatabase(DATABASE_NAME);

        database.createContainerIfNotExists(CONTAINER_NAME, "/orderId");

        return database.getContainer(CONTAINER_NAME);
    }

}
