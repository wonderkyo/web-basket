package cn.dhbin.isme.pms.domain.dto;

import lombok.Data;

import java.util.List;

@Data
public class BasketTotalDto {
    private Long totalAmount; // 总数
    private Long occupiedAmount; //2-使用中
    private Long freeAmount; //1-空闲
    private Long brokenAmount; //3-损坏
    private Long returnAmount; // 4-归还在途
    private Long lostAmount; // 5-丢失的amount
    private List<BasketInfoDto> basketList;
}
