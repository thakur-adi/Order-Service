package dev.aditya.orderservice.Service;

import dev.aditya.orderservice.DTO.InitiatePaymentDTO;
import dev.aditya.orderservice.Exceptions.CustomPaymentGenerationException;
import dev.aditya.orderservice.Exceptions.OrderNotFoundException;
import dev.aditya.orderservice.Model.Order;
import dev.aditya.orderservice.Model.OrderStatus;
import dev.aditya.orderservice.Model.Product;
import dev.aditya.orderservice.Model.User;
import dev.aditya.orderservice.Repository.OrderRepo;
import dev.aditya.orderservice.Repository.ProductRepo;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class OrderService implements IOrderService{

    private final OrderRepo orderRepo;
    private final ProductRepo productRepo;
    private RestTemplate restTemplate;

    public OrderService(@Qualifier("LoadBalancedRestTemplate") RestTemplate restTemplate, OrderRepo orderRepo, ProductRepo productRepo){
        this.restTemplate = restTemplate;
        this.orderRepo = orderRepo;
        this.productRepo = productRepo;
    }

    @Override
    public String generateNewOrder(User user, String deliveryAddress, List<Product> products, String paymentMethod, Double totalAmount) {
        //Create new Order
        Order newOrder = new Order();
        newOrder.setUserId(user.getUserId());
        newOrder.setDeliveryAddress(deliveryAddress);
        newOrder.setProducts(createProducts(products));
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
        ResponseEntity<String> paymentResponseLink = restTemplate.postForEntity("http://Payment-Service/payment/pay",httpRequestEntity,String.class);
        /*
        By default, whenever RestTemplate receives any response status code that is NOT a 2xx (Success),
         it immediately throws a RestClientResponseException (like HttpClientErrorException$NotFound).
        It intercepts the non-2xx status and crashes right at your calling line, completely skipping your normal variable assignment or response parsing logic.

        That's why this will never get called. Better to handle it in Global handler or update the RestTemplate Bean.

        if(!(paymentResponseLink.getStatusCode().is2xxSuccessful())){
            throw new CustomPaymentGenerationException("Couldn't generate payment link! Please try again later!");
        }

         */
        newOrder.setOrderStatus(OrderStatus.PAYMENT_IN_PROGRESS);
        orderRepo.save(newOrder);
        return paymentResponseLink.getBody();
    }


    @Override
    public Page<Order> getOrderHistory(Long userid,int pageNumber,int pageSize) {
        Sort sort = Sort.by("createdAt").descending();
        Pageable pageable = PageRequest.of(pageNumber,pageSize,sort);
        Page<Order> orderPage =  orderRepo.findAllByUserId(userid,pageable);
        if(orderPage.isEmpty()){
            throw new OrderNotFoundException("You haven't ordered anything yet!");
        }
        else return orderPage;
    }

    @Override
    public Order getOrderDetails(Long userId,Long orderId) {
        Optional<Order> orderOptional =  orderRepo.findOrderByIdAndUserId(orderId,userId);
        if(orderOptional.isEmpty()){
            throw new OrderNotFoundException("Order doesn't exist! Please provide appropriate Order Id and User Id!!");
        }
        return orderOptional.get();
    }

    @Override
    public void updateOrderPaymentDetails(Long orderId, String orderStatus, Long paymentId, String paymentMethod, String paymentGateway) {
        Optional<Order> orderOptional = orderRepo.findById(orderId);
        if(orderOptional.isEmpty()){
            throw new OrderNotFoundException("Order doesn't exist! Please provide appropriate Order Id and User Id!!");
        }
        else{
            Order order = orderOptional.get();
            order.setOrderStatus(convertToOrderStatus(orderStatus));
            order.setPaymentId(paymentId);
            order.setPaymentMethod(paymentMethod);
            order.setPaymentGateway(paymentGateway);
            orderRepo.save(order);
        }
    }




    //Helper Methods

    private List<Product> createProducts(List<Product> products) {
        List<Product> updatedProducts = new ArrayList<>();
        for(Product p:products){
            Optional<Product> optionalProduct = productRepo.findProductByProductIdAndQuantityAndPrice(p.getProductId()
                    , p.getQuantity()
                    , p.getPrice());
            Product newProduct = new Product();
            if(optionalProduct.isEmpty()){
                newProduct.setProductId(p.getProductId());
                newProduct.setProductName(p.getProductName());
                newProduct.setPrice(p.getPrice());
                newProduct.setQuantity(p.getQuantity());
                newProduct.setProductImageUrl(p.getProductImageUrl());
                productRepo.save(newProduct);
            }
            else{
                newProduct = optionalProduct.get();
            }
            updatedProducts.add(newProduct);
        }
        return updatedProducts;

    }

    private OrderStatus convertToOrderStatus(String orderStatus) {
        switch(orderStatus.toUpperCase()){
            case "PROCESSING":
                return OrderStatus.PAYMENT_IN_PROGRESS;

            case"SUCCESS":
                return  OrderStatus.IN_TRANSIT;

            default:
                return OrderStatus.PAYMENT_FAILED;
        }
    }
}
