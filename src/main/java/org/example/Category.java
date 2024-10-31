package org.example;

import java.util.ArrayList;
import java.util.List;

public class Category {
    private final String name;
    private final List<BotBaraxolka.Product> products;

    public Category(String name){
        this.name = name;
        this.products = new ArrayList<>();
    }

    public String getName(){
        return name;
    }

    public List<BotBaraxolka.Product> getProducts(){
        return products;
    }
    public void addProduct(BotBaraxolka.Product product){
        products.add(product);
    }
}
