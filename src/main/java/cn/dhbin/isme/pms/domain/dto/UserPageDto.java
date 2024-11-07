package cn.dhbin.isme.pms.domain.dto;
import cn.dhbin.mapstruct.helper.core.Convert;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Data;

/**
 * 用户分页数据
 */
@Data
public class UserPageDto implements Convert {

    private Long id;

    private String username;

    private Boolean enable;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    private Integer gender;

    private String avatar;

    private String address;

    private String email;

    private List<RoleDto> roles;


}
