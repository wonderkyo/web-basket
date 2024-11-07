package cn.dhbin.isme.pms.service.impl;

import cn.dev33.satoken.stp.SaLoginConfig;
import cn.dev33.satoken.stp.SaTokenInfo;
import cn.dev33.satoken.stp.StpUtil;
import cn.dhbin.isme.common.auth.SaTokenConfigure;
import cn.dhbin.isme.common.exception.BizException;
import cn.dhbin.isme.common.preview.PreviewProperties;
import cn.dhbin.isme.common.response.BizResponseCode;
import cn.dhbin.isme.common.response.Page;
import cn.dhbin.isme.pms.domain.dto.*;
import cn.dhbin.isme.pms.domain.entity.*;
import cn.dhbin.isme.pms.domain.request.*;
import cn.dhbin.isme.pms.mapper.UserMapper;
import cn.dhbin.isme.pms.service.CaptchaService;
import cn.dhbin.isme.pms.service.ProfileService;
import cn.dhbin.isme.pms.service.RoleService;
import cn.dhbin.isme.pms.service.UserRoleService;
import cn.dhbin.isme.pms.service.UserService;
import cn.dhbin.isme.pms.util.Constants;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.BCrypt;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import java.util.ArrayList;
import java.util.List;

import com.github.yulichang.wrapper.MPJLambdaWrapper;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * User Service impl
 *
 * @author dhb
 */
@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
    private static final Logger logger = LogManager.getLogger(UserServiceImpl.class);

    private final RoleService roleService;

    private final ProfileService profileService;

    private final UserRoleService userRoleService;

    private final CaptchaService captchaService;

    private final PreviewProperties previewProperties;

    @Override
    public LoginTokenDto login(LoginRequest request) {
        User user = lambdaQuery().eq(User::getUsername, request.getUsername()).one();
        if (user == null) {
            throw new BizException(BizResponseCode.ERR_10002);
        }
        // 预览环境下可快速登录，不用验证码
//        if (Boolean.TRUE.equals(request.getIsQuick()) && Boolean.TRUE.equals(previewProperties.getPreview())) {
//            return login(request, user);
//        }
        if (Boolean.TRUE.equals(request.getIsQuick())) {
            return login(request, user);
        }
        if (StrUtil.isBlank(request.getCaptchaKey())
                || !captchaService.verify(request.getCaptchaKey(), request.getCaptcha())) {
            throw new BizException(BizResponseCode.ERR_10003);
        }
        return login(request, user);
    }

    private LoginTokenDto login(LoginRequest request, User user) {
        boolean checkPw = BCrypt.checkpw(request.getPassword(), user.getPassword());
        if (checkPw) {
            // 我这里只写了一个role，而且role存在了user表里面，所以这里不用这样查
            return generateToken(user);
//            // 查询用户的角色
//            List<Role> roles = roleService.findRolesByUserId(user.getId());
//
//            return generateToken(user, roles, roles.isEmpty() ? "" : roles.getFirst().getCode());
        } else {
            throw new BizException(BizResponseCode.ERR_10002);
        }
    }

    @Override
    public UserDetailDto detail(Long userId) {
        User user = getById(userId);
        UserDetailDto userDetailDto = user.convert(UserDetailDto.class);
//        暂时先不写这个profile
        ProfileDto profileDto = profileService.findByUserId(userId).convert(ProfileDto.class);
        userDetailDto.setNickName(profileDto.getNickName());
        userDetailDto.setGender(profileDto.getGender());
        userDetailDto.setAddress(profileDto.getAddress());
        userDetailDto.setCarNo(profileDto.getCarNo());
        userDetailDto.setAvatar(profileDto.getAvatar());
        userDetailDto.setPhone(profileDto.getPhone());
        return userDetailDto;
    }

    @Override
    public LoginTokenDto switchRole(Long userId, String roleCode) {
        User user = getById(userId);
        List<Role> roles = roleService.findRolesByUserId(userId);
        Role currentRole = null;
        for (Role role : roles) {
            if (roleCode.equals(role.getCode())) {
                currentRole = role;
            }
        }
        if (currentRole == null) {
            throw new BizException(BizResponseCode.ERR_11005);
        }
//        return generateToken(user, roles, currentRole.getCode());
        return generateToken(user);

    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void register(RegisterUserRequest request) {
        boolean exists = lambdaQuery().eq(User::getUsername, request.getUsername()).exists();
        if (exists) {
            throw new BizException(BizResponseCode.ERR_10001);
        }
        User user = request.convert(User.class);
        user.setPassword(BCrypt.hashpw(user.getPassword()));
        this.save(user);

        Profile profile = (new RegisterUserProfileRequest()).convert(Profile.class);
        profile.setUserId(user.getId());
        profile.setPhone(request.getPhone());
        profile.setNickName(request.getUsername());

        profileService.save(profile);
    }

    @Override
    public void modifyPassword(Long modifyId) {
        User user = new User();
        user.setId(modifyId);
        user.setPassword(BCrypt.hashpw("12345678"));
        getBaseMapper().updateById(user);
    }

    @Override
    public LoginTokenDto refreshToken() {
        SaTokenInfo tokenInfo = StpUtil.getTokenInfo();
        StpUtil.login(tokenInfo.getLoginId(), SaLoginConfig
                        .setExtra(SaTokenConfigure.JWT_USER_ID_KEY,
                                StpUtil.getExtra(SaTokenConfigure.JWT_USER_ID_KEY))
                        .setExtra(SaTokenConfigure.JWT_USERNAME_KEY,
                                StpUtil.getExtra(SaTokenConfigure.JWT_USERNAME_KEY))
//                .setExtra(SaTokenConfigure.JWT_CURRENT_ROLE_KEY,
//                        StpUtil.getExtra(SaTokenConfigure.JWT_CURRENT_ROLE_KEY))
//                .setExtra(SaTokenConfigure.JWT_ROLE_LIST_KEY,
//                        StpUtil.getExtra(SaTokenConfigure.JWT_ROLE_LIST_KEY))
        );
        SaTokenInfo newTokenInfo = StpUtil.getTokenInfo();
        LoginTokenDto dto = new LoginTokenDto();
        dto.setAccessToken(newTokenInfo.getTokenValue());
        return dto;
    }

    @Override
    public void changePassword(ChangePasswordRequest request) {
        String username = (String) StpUtil.getExtra(SaTokenConfigure.JWT_USERNAME_KEY);
        User user = lambdaQuery().select(User::getPassword).eq(User::getUsername, username).one();
        if (!BCrypt.checkpw(request.getOldPassword(), user.getPassword())) {
            throw new BizException(BizResponseCode.ERR_10004);
        }
        user.setPassword(BCrypt.hashpw(request.getNewPassword()));
        lambdaUpdate().set(User::getPassword, BCrypt.hashpw(request.getNewPassword()))
                .eq(User::getUsername, username)
                .update();
        StpUtil.logout();
    }

    @Override
    public String searchRoleOfId(Long id) {
        User user = getBaseMapper().selectById(id);
        if (user != null) {
            return user.getRole();
        } else {
            return "";
        }
    }

    @Override
    public Page<UserPageDto> queryPage(UserPageRequest request) {
        IPage<User> qp = request.toPage();
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(StrUtil.isNotBlank(request.getUsername()), User::getUsername, request.getUsername())
                .or()
                .eq(ObjectUtil.isNotNull(request.getEnable()), User::getEnable, request.getEnable());

        IPage<UserPageDto> ret = getBaseMapper().pageDetail(qp,
                        request.getUsername(),
                        request.getGender(),
                        request.getEnable())
                .convert(dto -> {
                    List<RoleDto> roleDtoList = roleService.findRolesByUserId(dto.getId()).stream()
                            .map(role -> role.convert(RoleDto.class)).toList();
                    dto.setRoles(roleDtoList);
                    return dto;
                });
        return Page.convert(ret);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeUser(Long id) {
        if (id == 1) {
            throw new BizException(BizResponseCode.ERR_11006, "不能删除根用户");
        }
        removeById(id);
        profileService.lambdaUpdate().eq(Profile::getUserId, id).remove();
    }

    @Override
    public void resetPassword(Long userId, UpdatePasswordRequest request) {
        String newPw = BCrypt.hashpw(request.getPassword());
        lambdaUpdate().eq(User::getId, userId)
                .set(User::getPassword, newPw)
                .update();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addRoles(Long userId, AddUserRolesRequest request) {
        userRoleService.lambdaUpdate().eq(UserRole::getUserId, userId).remove();
        List<UserRole> list = request.getRoleIds().stream()
                .map(roleId -> {
                    UserRole userRole = new UserRole();
                    userRole.setUserId(userId);
                    userRole.setRoleId(roleId);
                    return userRole;
                }).toList();
        userRoleService.saveBatch(list);
    }

    @Override
    public void updateProfile(Long id, UpdateProfileRequest request) {
        Profile profile = request.convert(Profile.class);
        if (request.getLocData() != null && !request.getLocData().equals("")) {
            profile.setLocData(request.getLocData());
        }
        profile.setUserId(id);
        profileService.updateById(profile);
    }

    @Override
    public void updateDriverLoc(String locData, Long userId){
        Profile profile = new Profile();
        profile.setUserId(userId);
        profile.setLocData(locData);
        profileService.updateById(profile);
    }

    private LoginTokenDto generateToken(User user) {
        // 密码验证成功
//        StpUtil.login(user.getId(),
//                SaLoginConfig.setExtra(SaTokenConfigure.JWT_USER_ID_KEY, user.getId())
//                        .setExtra(SaTokenConfigure.JWT_USERNAME_KEY, user.getUsername())
//                        .setExtra(SaTokenConfigure.JWT_CURRENT_ROLE_KEY, currentRoleCode)
//                        .setExtra(SaTokenConfigure.JWT_ROLE_LIST_KEY, roles));
        StpUtil.login(user.getId(),
                SaLoginConfig.setExtra(SaTokenConfigure.JWT_USER_ID_KEY, user.getId())
                        .setExtra(SaTokenConfigure.JWT_USERNAME_KEY, user.getUsername())
                        .setExtra(SaTokenConfigure.JWT_ROLE_KEY, user.getRole()));
        SaTokenInfo tokenInfo = StpUtil.getTokenInfo();
        logger.info("Info Message!:{}", tokenInfo);
        LoginTokenDto dto = new LoginTokenDto();
        dto.setAccessToken(tokenInfo.getTokenValue());
        return dto;
    }

    @Override
    public Page<UserDetailDto> queryAllUsers(queryUserRequest request) {
        IPage<UserDetailDto> userParam = request.toPage();
        String queryColumn = request.getQueryColumn();
        String queryValue = request.getQueryValue();
        MPJLambdaWrapper<User> objectMPJLambdaWrapper = new MPJLambdaWrapper<>();
        objectMPJLambdaWrapper.selectAll(User.class)
                .selectAs(Profile::getAddress, "address")
                .selectAs(Profile::getNickName, "nickName")
                .selectAs(Profile::getPhone, "phone")
                .selectAs(Profile::getGender, "gender")
                .leftJoin(Profile.class, Profile::getUserId, User::getId).orderByAsc(User::getId);
        if (!(queryColumn == null || queryColumn.isEmpty()) && !(queryValue == null || queryValue.isEmpty())) {
            if ("id".equals(queryColumn)) {
                objectMPJLambdaWrapper.eq(User::getId, Long.parseLong(queryValue));
            } else if ("username".equals(queryColumn)) {
                objectMPJLambdaWrapper.eq(User::getUsername, queryValue);
            } else if ("phone".equals(queryColumn)) {
                objectMPJLambdaWrapper.eq(Profile::getPhone, queryValue);
            } else if ("nickName".equals(queryColumn)) {
                objectMPJLambdaWrapper.eq(Profile::getNickName, queryValue);
            } else if ("address".equals(queryColumn)) {
                objectMPJLambdaWrapper.eq(Profile::getAddress, queryValue);
            } else if ("role".equals(queryColumn)) {
                objectMPJLambdaWrapper.eq(User::getRole, queryValue);
            }
        }
        userParam = getBaseMapper().selectJoinPage(userParam, UserDetailDto.class, objectMPJLambdaWrapper);
        Page<UserDetailDto> userPage = new Page<>();
        userPage.setPageData(userParam.getRecords());
        userPage.setTotal(userParam.getTotal());
        return userPage;
    }

    @Data
    public class LocationData {
        private Double lat;
        private Double lng;
    }

    // 查询所有的仓库位置
    @Override
    public Object getAllUserLatLng() {
        List<LatLngDto> locDepot = getBaseMapper().getLatLng(Constants.ROLE_DEPOT);
        List<LatLngDto> locStore = getBaseMapper().getLatLng(Constants.ROLE_STORE);
        List<LatLngDto> locDriver = getBaseMapper().getLatLng(Constants.ROLE_DRIVER);
        List<LatLngDto> res = new ArrayList<>();
        res.addAll(locDepot);
        res.addAll(locStore);
        res.addAll(locDriver);

        Gson gson = new Gson();
        List<LatLngDto> locations = new ArrayList<>();
        for (LatLngDto rawLocation : res) {
            LatLngDto location = new LatLngDto();
            location.setId(rawLocation.getId());
            location.setStyleId(rawLocation.getStyleId());
            String locData = rawLocation.getLocData();


            LocationData locationData = gson.fromJson(locData, LocationData.class);
            if (locationData != null) {
                location.setLat(locationData.lat);
                location.setLng(locationData.lng);
                locations.add(location);
            } else {
                System.out.println("Failed to parse JSON: " + locData);
            }

        }
        return locations;
    }
}