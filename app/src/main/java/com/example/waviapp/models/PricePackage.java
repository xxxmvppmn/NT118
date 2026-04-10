package com.example.waviapp.models;

public class PricePackage {
    private String name;
    private String originalPrice; // Giá gốc (để gạch ngang)
    private String currentPrice;  // Giá hiện tại (giá bán)
    private String promoTag;      // Tag ví dụ: "Hot Sale" hoặc "Tiết kiệm 20%"
    private boolean isBestChoice; // Để đánh dấu gói cần làm nổi bật nhất

    // Constructor đầy đủ
    public PricePackage(String name, String originalPrice, String currentPrice, String promoTag, boolean isBestChoice) {
        this.name = name;
        this.originalPrice = originalPrice;
        this.currentPrice = currentPrice;
        this.promoTag = promoTag;
        this.isBestChoice = isBestChoice;
    }

    // Getters (tự động tạo hoặc gõ tay)
    public String getName() { return name; }
    public String getOriginalPrice() { return originalPrice; }
    public String getCurrentPrice() { return currentPrice; }
    public String getPromoTag() { return promoTag; }
    public boolean isBestChoice() { return isBestChoice; }
}