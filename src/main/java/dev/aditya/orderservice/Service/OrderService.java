package dev.aditya.orderservice.Service;

import dev.aditya.orderservice.DTO.InitiatePaymentDTO;
import dev.aditya.orderservice.Exceptions.CustomPaymentGenerationException;
import dev.aditya.orderservice.Model.Order;
import dev.aditya.orderservice.Model.OrderStatus;
import dev.aditya.orderservice.Model.Product;
import dev.aditya.orderservice.Model.User;
import dev.aditya.orderservice.Repository.OrderRepo;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
public class OrderService implements IOrderService{

    private final OrderRepo orderRepo;
    private RestTemplate restTemplate;

    public OrderService(@Qualifier("LoadBalancedRestTemplate") RestTemplate restTemplate, OrderRepo orderRepo){
        this.restTemplate = restTemplate;
        this.orderRepo = orderRepo;
    }

    @Override
    public String generateNewOrder(User user, String deliveryAddress, List<Product> products, String paymentMethod, Double totalAmount) {
        //Create new Order
        Order newOrder = new Order();
        newOrder.setUserId(user.getUserId());
        newOrder.setProducts(products);
        newOrder.setPaymentMethod(paymentMethod);
        newOrder.setTotalAmount(totalAmount);
        newOrder.setOrderStatus(OrderStatus.PAYMENT_INITIATED);
        orderRepo.save(newOrder);

        //Create Payment DTO
        InitiatePaymentDTO paymentDTO = new InitiatePaymentDTO();
        paymentDTO.setAmount(totalAmount);
        paymentDTO.setOrderId(newOrder.getId());
        paymentDTO.setUserId(user.getUserId());
        paymentDTO.setUsername(user.getUserName());
        paymentDTO.setPhoneNumber(user.getPhoneNumber());
        paymentDTO.setUserEmail(user.getEmail());

        //Attach payload to headers
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<InitiatePaymentDTO> httpRequestEntity = new HttpEntity<>(paymentDTO,httpHeaders);

        //Call the payment microservice
        ResponseEntity<String> paymentResponse = restTemplate.postForEntity("http://Payment-Service/pay",httpRequestEntity,String.class);

        if(!paymentResponse.getStatusCode().is2xxSuccessful()){
            throw new CustomPaymentGenerationException("Couldn't generate payment link! Please try again later!");
        }
        newOrder.setOrderStatus(OrderStatus.PAYMENT_IN_PROGRESS);
        orderRepo.save(newOrder);
        return paymentResponse.getBody();
    }
}
