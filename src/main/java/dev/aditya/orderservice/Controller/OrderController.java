package dev.aditya.orderservice.Controller;

import dev.aditya.orderservice.DTO.NewOrderRequestDTO;
import dev.aditya.orderservice.DTO.OrderHistoryRequestDTO;
import dev.aditya.orderservice.DTO.OrderResponseDTO;
import dev.aditya.orderservice.DTO.PaymentResponseDTO;
import dev.aditya.orderservice.Model.User;
import dev.aditya.orderservice.Service.IOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
public class OrderController {

    @Autowired
    IOrderService orderService;


    @PostMapping("/new")
    public ResponseEntity<String> generateNewOrder(@RequestBody NewOrderRequestDTO newOrderRequestDTO){
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String paymentLink = orderService.generateNewOrder(user,newOrderRequestDTO.getDeliveryAddress()
                                                           ,newOrderRequestDTO.getProducts()
                                                           ,newOrderRequestDTO.getPaymentMethod()
                                                           ,newOrderRequestDTO.getTotalAmount());
        return new ResponseEntity<>(paymentLink, HttpStatus.CREATED);
    }

    @GetMapping("/history")
    public Page<OrderResponseDTO> getOrderHistory(@RequestBody OrderHistoryRequestDTO orderHistoryRequestDTO){

        return null;
    }

    @GetMapping("/status/{orderId}")
    public ResponseEntity<OrderResponseDTO> getOrderStatus(@PathVariable("orderId") Long orderId){
        return null;
    }

    //This to update the order status after any change in payment status.
    // Will always be initiated by Payment Service, can later add in a value in header to check whether the request is coming from Payment Service or not.
    @PutMapping("/status")
    public ResponseEntity<String> updateOrderStatus(@RequestBody PaymentResponseDTO paymentResponseDTO){
        return null;
    }
}
