package com.pahappa.inventory.bean;

import com.pahappa.inventory.model.Product;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Named;

import java.io.Serializable;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;

@Named("inventoryBean")
@ApplicationScoped
public class InventoryBean implements Serializable {

    private final List<Product> products = new CopyOnWriteArrayList<>();
    private final AtomicLong idSequence = new AtomicLong(1);

    private Product product = new Product();

    public String addProduct() {
        product.setId(idSequence.getAndIncrement());
        products.add(product);
        product = new Product();
        return "table?faces-redirect=true";
    }

    public void removeProduct(Product target){
        products.remove(target);
    }

    public List<Product> getProducts(){
        return products;
    }

    public Product getProduct(){
        return product;
    }

    public void setProduct(Product product){
        this.product = product;
    }
}
