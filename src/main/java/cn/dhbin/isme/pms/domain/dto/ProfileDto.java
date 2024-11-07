package cn.dhbin.isme.pms.domain.dto;

import lombok.Data;

/**
 * 用户信息
 *
 * @author dhb
 */
@Data
public class ProfileDto {

    private Long userId;

    private String nickName;

    private Integer gender;

    private String address;

    private String carNo;

    private String avatar;

    private String phone;
    private String locData;
}
