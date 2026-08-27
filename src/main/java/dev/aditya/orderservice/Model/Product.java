package dev.aditya.orderservice.Model;

import dev.aditya.orderservice.DTO.ProductResponseDTO;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Product{
    private Long id;
    private String productName;
    private String productImageUrl;
    private Integer quantity;
    private Double price;

    public ProductResponseDTO convertToDto(){
        ProductResponseDTO productResponseDTO =new ProductResponseDTO();
        productResponseDTO.setProductName(productName);
        productResponseDTO.setPrice(price);
        productResponseDTO.setQuantity(quantity);
        productResponseDTO.setProductImageUrl(productImageUrl);
        return productResponseDTO;
    }
}
