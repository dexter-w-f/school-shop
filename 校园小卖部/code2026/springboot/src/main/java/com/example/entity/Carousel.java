package com.example.entity;

public class Carousel {
    private Integer id;
    private Integer goodsId;
    private String img;
    private String goodsName;

    public Carousel() {
    }
    public Carousel(Integer id, Integer goodsId, String img, String goodsName) {
        this.id = id;
        this.goodsId = goodsId;
        this.img = img;
        this.goodsName = goodsName;
    }

    public String getGoodsName() {
        return goodsName;
    }

    public void setGoodsName(String goodsName) {
        this.goodsName = goodsName;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getGoodsId() {
        return goodsId;
    }

    public void setGoodsId(Integer goodsId) {
        this.goodsId = goodsId;
    }

    public String getImg() {
        return img;
    }

    public void setImg(String img) {
        this.img = img;
    }
}
