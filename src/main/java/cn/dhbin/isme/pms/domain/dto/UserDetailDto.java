package cn.dhbin.isme.pms.domain.dto;

import java.time.LocalDateTime;
import lombok.Data;

/**
 * 用户详细信息
 *
 * @author dhb
 */
@Data
public class UserDetailDto {

    private Long id;

    private String username;

    private String phone;

    private String role;

    private Boolean enable;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    // 暂时可以先不写这个，以后再加
    //    private ProfileDto profile;

    private String nickName;

    private Integer gender;

    private String address;

    private String carNo;

    private String avatar;


}
