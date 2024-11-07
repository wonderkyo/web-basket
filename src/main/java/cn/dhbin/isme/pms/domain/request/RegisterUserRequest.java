package cn.dhbin.isme.pms.domain.request;

import cn.dhbin.mapstruct.helper.core.Convert;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

import lombok.Data;
import org.hibernate.validator.constraints.Length;

/**
 * 注册用户
 *
 * @author dhb
 */
@Data
public class RegisterUserRequest implements Convert {

    @Length(min = 6, max = 20, message = "用户名长度必须是6到20之间")
    private String username;

    @Length(min = 6, max = 20, message = "密码长度必须是6到20之间")
    private String password;

    @NotBlank(message = "用户角色不能为空")
    private String role;

    @Pattern(regexp = "^1\\d{10}$", message = "手机号格式有误")
    private String phone;

    private String avatar;


}
