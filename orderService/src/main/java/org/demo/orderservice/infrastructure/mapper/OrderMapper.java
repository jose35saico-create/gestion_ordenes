package org.demo.orderservice.infrastructure.mapper;

import org.demo.orderservice.domain.model.Item;
import org.demo.orderservice.domain.model.Order;
import org.demo.orderservice.infrastructure.controller.dto.request.ItemRequest;
import org.demo.orderservice.infrastructure.controller.dto.request.OrderRequest;

import java.util.List;

public class OrderMapper {

    public static Order toDomain(OrderRequest request) {
        List<Item> items = request.getItems().stream()
            .map(OrderMapper::toDomain)
            .toList();

        return new Order(
            request.getOrderId(),
            request.getCustomerId(),
            items,
            request.getTotalAmount()
        );
    }

    public static Item toDomain(ItemRequest itemRequest) {
        return new Item(itemRequest.getProductId(), itemRequest.getQuantity());
    }

}
