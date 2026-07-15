package com.pahappa.inventory.model;

import java.io.Serializable;

public class Product implements Serializable {

    private Long id;
    private String name;
    private String category;
    private Integer quantity;
    private Double price;

    public Product() {}

    public Long getId() {return id;}
    public void setId(Long id) {this.id = id;}

    public String getName() {return name;}
    public void setName(String name) {this.name = name;}

    public String getCategory() {return category;}
    public void setCategory(String category) {this.category = category;}

    public Integer getQuantity() {return quantity;}
    public void setQuantity(Integer quantity) {this.quantity = quantity;}

    public Double getPrice(){return price;}
    public void setPrice(Double price) {this.price = price;}
}
