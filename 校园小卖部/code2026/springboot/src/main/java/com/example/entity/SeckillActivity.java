package com.example.entity;

import java.math.BigDecimal;

public class SeckillActivity {
    private Integer id;
    private Integer goodsId;
    private BigDecimal seckillPrice;
    private Integer totalStock;
    private String startTime;
    private String endTime;
    private String status;
    private String createTime;
    private String goodsName;
    private String goodsImg;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public Integer getGoodsId() { return goodsId; }
    public void setGoodsId(Integer goodsId) { this.goodsId = goodsId; }
    public BigDecimal getSeckillPrice() { return seckillPrice; }
    public void setSeckillPrice(BigDecimal seckillPrice) { this.seckillPrice = seckillPrice; }
    public Integer getTotalStock() { return totalStock; }
    public void setTotalStock(Integer totalStock) { this.totalStock = totalStock; }
    public String getStartTime() { return startTime; }
    public void setStartTime(String startTime) { this.startTime = startTime; }
    public String getEndTime() { return endTime; }
    public void setEndTime(String endTime) { this.endTime = endTime; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getCreateTime() { return createTime; }
    public void setCreateTime(String createTime) { this.createTime = createTime; }
    public String getGoodsName() { return goodsName; }
    public void setGoodsName(String goodsName) { this.goodsName = goodsName; }
    public String getGoodsImg() { return goodsImg; }
    public void setGoodsImg(String goodsImg) { this.goodsImg = goodsImg; }
}
