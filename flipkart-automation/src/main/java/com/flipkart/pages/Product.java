package com.flipkart.pages;

/** One product card read from the results page. */
public class Product {
    public String id;
    public String name;
    public long price;

    public Product(String id, String name, long price) {
        this.id = id;
        this.name = name;
        this.price = price;
    }

    @Override
    public String toString() {
        return name + " | Rs " + price;
    }
}
