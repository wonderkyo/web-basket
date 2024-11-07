package cn.dhbin.isme.pms.domain.request;

import cn.dhbin.mapstruct.helper.core.Convert;
import lombok.Data;

/**
 * 更新用户信息
 *
 * @author dhb
 */
@Data
public class UpdateProfileRequest implements Convert {

    private Long userId;

    private String nickName;

    private Integer gender;

    private String address;

    private String phone;

    private String carNo;

    private String avatar;

    private String locData;
}
