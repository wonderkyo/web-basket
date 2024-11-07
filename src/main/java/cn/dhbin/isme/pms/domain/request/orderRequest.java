package cn.dhbin.isme.pms.domain.request;

import cn.dhbin.mapstruct.helper.core.Convert;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class orderRequest implements Convert {
    @NotNull(message = "订单编号不能为空")
    private Long orderId;
}
