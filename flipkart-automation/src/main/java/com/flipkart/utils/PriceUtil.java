package com.flipkart.utils;

import com.flipkart.pages.Product;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.TreeSet;

public class PriceUtil {

    /** "₹17,999" -> 17999 (numeric, never compared as text). */
    public static long parse(String text) {
        String digits = text.replaceAll("[^0-9]", "");
        if (digits.isEmpty()) {
            throw new IllegalArgumentException("No price found in: " + text);
        }
        return Long.parseLong(digits);
    }

    /** Every price must be less than or equal to the next one. */
    public static boolean isAscending(List<Long> prices) {
        for (int i = 0; i < prices.size() - 1; i++) {
            if (prices.get(i) > prices.get(i + 1)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Third-highest DISTINCT price. Tie rule: if several products have that price,
     * the first one in listing order is chosen.
     */
    public static Product thirdHighest(List<Product> products) {
        TreeSet<Long> distinct = new TreeSet<>();
        for (Product p : products) {
            distinct.add(p.price);
        }
        if (distinct.size() < 3) {
            throw new IllegalStateException("Need at least 3 distinct prices, found " + distinct.size());
        }
        List<Long> desc = new ArrayList<>(distinct);
        Collections.reverse(desc);
        long target = desc.get(2);
        for (Product p : products) {
            if (p.price == target) {
                return p;
            }
        }
        throw new IllegalStateException("Product not found for price " + target);
    }
}
