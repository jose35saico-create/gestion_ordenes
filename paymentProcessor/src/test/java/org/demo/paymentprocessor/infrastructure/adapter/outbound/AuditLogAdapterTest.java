package org.demo.paymentprocessor.infrastructure.adapter.outbound;

import com.azure.core.util.BinaryData;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.demo.paymentprocessor.domain.model.Item;
import org.demo.paymentprocessor.domain.model.Order;
import org.demo.paymentprocessor.infrastructure.exception.AuditLogException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.Mockito.when;

class AuditLogAdapterTest {

    private BlobContainerClient blobContainerClient;
    private BlobClient blobClient;
    private ObjectMapper objectMapper;
    private AuditLogAdapter adapter;

    @BeforeEach
    void setUp() {
        blobContainerClient = Mockito.mock(BlobContainerClient.class);
        blobClient = Mockito.mock(BlobClient.class);
        objectMapper = Mockito.mock(ObjectMapper.class);

        adapter = new AuditLogAdapter(blobContainerClient, objectMapper);
    }

    @Test
    void shouldSaveOrderAuditSuccessfully() throws JsonProcessingException {
        Order order = new Order(
            "order-123",
            "customer-1",
            List.of(new Item("item-1", 2),
                new Item("item-2", 1)),
            BigDecimal.valueOf(100.0)
        );

        when(blobContainerClient.getBlobClient(order.getOrderId() + ".json")).thenReturn(blobClient);
        when(objectMapper.writeValueAsString(order)).thenReturn("{\"orderId\":\"order-123\"}");

        Mono<Void> result = adapter.saveOrderAudit(order);

        StepVerifier.create(result)
            .verifyComplete();

        Mockito.verify(blobContainerClient).createIfNotExists();
        Mockito.verify(blobContainerClient).getBlobClient(order.getOrderId() + ".json");
        Mockito.verify(blobClient).upload(Mockito.any(BinaryData.class), Mockito.eq(true));


    }

    @Test
    void shouldThrowAuditLogExceptionWhenSerializationFails() throws JsonProcessingException {
        Order order = new Order(
            "order-123",
            "customer-1",
            List.of(new Item("item-1", 2)),
            BigDecimal.valueOf(50.0)
        );

        when(objectMapper.writeValueAsString(order))
            .thenThrow(new JsonProcessingException("Error JSON") {});

        Mono<Void> result = adapter.saveOrderAudit(order);

        StepVerifier.create(result)
            .expectErrorMatches(throwable ->
                throwable instanceof AuditLogException &&
                    throwable.getMessage().contains("Error serializando la orden"))
            .verify();
    }

    @Test
    void shouldThrowAuditLogExceptionWhenBlobUploadFails() throws Exception {
        Order order = new Order(
            "order-123",
            "customer-1",
            List.of(new Item("item-1", 2)),
            BigDecimal.valueOf(50.0)
        );

        when(blobContainerClient.getBlobClient(order.getOrderId() + ".json")).thenReturn(blobClient);
        when(objectMapper.writeValueAsString(order)).thenReturn("{\"orderId\":\"order-123\"}");
        Mockito.doThrow(new RuntimeException("Upload failed"))
            .when(blobClient).upload(Mockito.any(BinaryData.class), Mockito.eq(true));

        Mono<Void> result = adapter.saveOrderAudit(order);

        StepVerifier.create(result)
            .expectErrorMatches(throwable ->
                throwable instanceof AuditLogException &&
                    throwable.getMessage().contains("Error al guardar la auditoria"))
            .verify();
    }

}
