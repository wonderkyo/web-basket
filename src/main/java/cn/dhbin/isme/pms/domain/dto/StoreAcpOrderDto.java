package cn.dhbin.isme.pms.domain.dto;

import cn.dhbin.mapstruct.helper.core.Convert;
import lombok.Data;

@Data
public class StoreAcpOrderDto implements Convert {
    private Long orderId;
    private Long ownerId;
}
