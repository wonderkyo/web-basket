package cn.dhbin.isme.pms.domain.entity;

import cn.dhbin.mapstruct.helper.core.Convert;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 权限
 *
 * @author dhb
 */
@Data
@TableName("permission")
public class Permission implements Convert {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String path;

    private String role;

}
