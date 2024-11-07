package cn.dhbin.isme.pms.domain.entity;

import cn.dhbin.mapstruct.helper.core.Convert;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("basket_order")
public class BasketOrder implements Convert {
    @TableId(type = IdType.AUTO)
    private Long id;

    // 数据库里面是寸的json
    @TableField("basket_list")
    private String basketList;

    @TableField("order_id")
    private Long orderId;

    @TableField(value="create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
