package cn.dhbin.isme.pms.domain.dto;

import cn.dhbin.mapstruct.helper.core.Convert;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class QueryOrderDto implements Convert {
    private String carId; // 车牌
    private Long orderId;// orderDriver表
    private LocalDateTime arriveTime;// 到达时间

    private String status;//订单状态
    private String productName;//货名，order表
    private int productQuant;//数量，order表
    private LocalDateTime downTime;// 签收时间，order表里面
    private String address; // 收货地址
    private String recName;
    private String recPhone;
    private LocalDateTime createTime; // 下单时间

    private String wareAddress; // 仓库地址
}
