package cn.dhbin.isme.pms.domain.dto;

import cn.dhbin.isme.pms.domain.entity.Order;
import cn.dhbin.mapstruct.helper.core.Convert;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UnWareDto implements Convert {
    private Long orderId;
    private String productName;
    private int productQuant;
    private Long storeId;
    private LocalDateTime createTime;
    private String storePhone;
    private String storeAddress;
    private String storeNickName;
    private String storeUserName;
}
