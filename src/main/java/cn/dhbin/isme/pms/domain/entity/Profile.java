package cn.dhbin.isme.pms.domain.entity;

import cn.dhbin.mapstruct.helper.core.Convert;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 用户信息
 *
 * @author dhb
 */
@Data
@TableName("profile")
public class Profile implements Convert {

    @TableId("userId")
    private Long userId;

    @TableField("nickName")
    private String nickName;

    private Integer gender;

    private String address;

    private String carNo;

    private String avatar;

    private String phone;

    private String locData;


}
