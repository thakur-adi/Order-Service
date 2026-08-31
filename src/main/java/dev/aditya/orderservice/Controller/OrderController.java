package dev.aditya.orderservice.Controller;

import dev.aditya.orderservice.DTO.NewOrderRequestDTO;
import dev.aditya.orderservice.DTO.OrderHistoryRequestDTO;
import dev.aditya.orderservice.DTO.OrderResponseDTO;
import dev.aditya.orderservice.DTO.OrderPaymentUpdateDTO;
import dev.aditya.orderservice.Model.Order;
import dev.aditya.orderservice.Model.User;
import dev.aditya.orderservice.Service.IOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class OrderController {

    @Autowired
    IOrderService orderService;


    @PostMapping("/new")
    public ResponseEntity<String> generateNewOrder(@RequestBody NewOrderRequestDTO newOrderRequestDTO){

        String paymentLink = orderService.generateNewOrder(getUserDetails(),newOrderRequestDTO.getDeliveryAddress()
                                                           ,newOrderRequestDTO.getProducts()
                                                           ,newOrderRequestDTO.getPaymentMethod()
                                                           ,newOrderRequestDTO.getTotalAmount());
        return new ResponseEntity<>(paymentLink, HttpStatus.CREATED);
    }

    @GetMapping("/history")
    public Page<OrderResponseDTO> getOrderHistory(@RequestBody OrderHistoryRequestDTO orderHistoryRequestDTO){
        Page<Order> orderHistoryPage = orderService.getOrderHistory(getUserDetails().getUserId(), orderHistoryRequestDTO.getPageNumber(),orderHistoryRequestDTO.getPageSize());
        return new PageImpl<>(convertOrderToOrderDto(orderHistoryPage),orderHistoryPage.getPageable(),orderHistoryPage.getTotalElements());
    }


    @GetMapping("/status/{orderId}")
    public ResponseEntity<OrderResponseDTO> getOrderStatus(@PathVariable("orderId") Long orderId){
        Order order = orderService.getOrderDetails(getUserDetails().getUserId(),orderId);
        return new ResponseEntity<>(order.convertToOrderDto(),HttpStatus.FOUND);
    }

    //This to update the order status after any change in payment status.
    // Will always be initiated by Payment Service, can later add in a value in header to check whether the request is coming from Payment Service or not.
    @PutMapping("/status")
    public ResponseEntity<String> updateOrderPaymentDetails(@RequestBody OrderPaymentUpdateDTO orderPaymentUpdateDTO){
        orderService.updateOrderPaymentDetails(orderPaymentUpdateDTO.getOrderId(),orderPaymentUpdateDTO.getPaymentStatus(),orderPaymentUpdateDTO.getPaymentId(),orderPaymentUpdateDTO.getPaymentMethod());
        return new ResponseEntity<>("Payment Status has been updated!",HttpStatus.OK);
    }




    //Helper Methods

    private List<OrderResponseDTO> convertOrderToOrderDto(Page<Order> orderHistoryPage) {
        return orderHistoryPage.stream().map(order -> order.convertToOrderDto()).toList();
    }

    private User getUserDetails(){
        return (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }
}
