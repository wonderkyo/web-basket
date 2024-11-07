package cn.dhbin.isme.pms.domain.entity;

import cn.dhbin.mapstruct.helper.core.Convert;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

//门店信息
@Data
@TableName("store")
public class Store implements Convert {
    @TableId(type = IdType.AUTO)
    private Long store_id;

    private String address;

    private Long user_id;
}
