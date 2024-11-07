package cn.dhbin.isme.pms.domain.request;

import cn.dhbin.mapstruct.helper.core.Convert;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateOrderRequest implements Convert {
    private Long storeId;

    @NotBlank(message = "货物名称不能为空")
    private String productName;

    @NotNull(message = "货物数量不能为空")
    private int productQuant;

    @NotBlank(message = "地址不能为空")
    private String address;

    @NotBlank(message = "收货人姓名不能为空")
    private String recName;

    @NotBlank(message = "收货人电话不能为空")
    private String recPhone;

}
