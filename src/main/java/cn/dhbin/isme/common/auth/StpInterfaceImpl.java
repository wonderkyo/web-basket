package cn.dhbin.isme.common.auth;

import cn.dev33.satoken.stp.StpInterface;
import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.collection.CollUtil;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 * 鉴权
 *
 * @author dhb
 */
@Component
public class StpInterfaceImpl implements StpInterface {

    // loginType：账号体系标识，此处可以暂时忽略
    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        return null;
    }

    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        String role = (String) StpUtil.getExtra(SaTokenConfigure.JWT_ROLE_KEY);
        return CollUtil.newArrayList(role);
    }
}