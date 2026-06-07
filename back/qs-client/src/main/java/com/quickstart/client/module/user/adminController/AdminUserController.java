package com.quickstart.client.module.user.adminController;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.quickstart.common.annotation.NoNeedLogin;
import com.quickstart.common.domain.user.dto.AdminLoginRequest;
import com.quickstart.common.domain.user.dto.AdminUserCreateRequest;
import com.quickstart.common.domain.user.vo.LoginResponse;
import com.quickstart.common.domain.user.vo.UserInfoVO;
import com.quickstart.common.domain.user.User;
import com.quickstart.common.domain.ResponseDTO;
import com.quickstart.common.security.SecurityUserContext;
import com.quickstart.client.module.user.service.AuthService;
import com.quickstart.client.module.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@Tag(name = "管理员模块")
@RestController
public class AdminUserController {

    @Resource
    private UserService userService;
    @Resource
    private AuthService authService;



    @GetMapping("/admin/page")
    @Operation(summary = "分页查询用户")
    @PreAuthorize("hasAuthority('system:user:list')")
    public ResponseDTO<IPage<UserInfoVO>> page(@RequestParam(defaultValue = "1") @Min(1) long pageNo,
                                               @RequestParam(defaultValue = "10") @Min(1) long pageSize,
                                               @RequestParam(required = false) String userName,
                                               @RequestParam(required = false) String phone,
                                               @RequestParam(required = false) Integer status) {
        IPage<UserInfoVO> responsePage = userService.pageAdminUsers(pageNo, pageSize, userName, phone, status)
                .convert(UserInfoVO::fromEntity);
        return ResponseDTO.ok(responsePage);
    }

    @GetMapping("/admin/{memberCode}")
    @Operation(summary = "查询用户详情")
    @PreAuthorize("hasAuthority('system:user:query')")
    public ResponseDTO<UserInfoVO> detail(@PathVariable String memberCode) {
        User user = userService.findByMemberCode(memberCode);
        if (user == null) {
            return ResponseDTO.userErrorParam("用户不存在");
        }
        return ResponseDTO.ok(UserInfoVO.fromEntity(user));
    }

    @PostMapping("/admin")
    @Operation(summary = "管理员新增用户")
    @PreAuthorize("hasAuthority('system:user:add')")
    public ResponseDTO<UserInfoVO> create(@RequestBody @Valid AdminUserCreateRequest request) {
        User user = userService.createByAdmin(request);
        return new ResponseDTO<>(ResponseDTO.OK_CODE, null, true, "新增成功", UserInfoVO.fromEntity(user));
    }

    @PostMapping("/admin/user/adminLogin")
    @Operation(summary = "管理员登录")
    @NoNeedLogin
    public ResponseDTO<LoginResponse> adminLogin(@RequestBody @Valid AdminLoginRequest request) {
        return new ResponseDTO<>(ResponseDTO.OK_CODE, null, true, "登录成功", authService.adminLogin(request));
    }

    @GetMapping("/admin/me")
    @Operation(summary = "获取当前管理员信息")
    public ResponseDTO<UserInfoVO> currentUser() {

        String memberCode = SecurityUserContext.getCurrentMemberCode();
        UserInfoVO vo = authService.currentUser(memberCode);
        return ResponseDTO.ok(vo);
    }

    @PutMapping("/admin/{memberCode}/status")
    @Operation(summary = "更新用户状态")
    @PreAuthorize("hasAuthority('system:user:edit')")
    public ResponseDTO<Void> updateStatus(@PathVariable String memberCode, @RequestParam @NotNull Integer status) {
        boolean success = userService.updateStatusByMemberCode(memberCode, status);
        if (!success) {
            return ResponseDTO.userErrorParam("用户不存在");
        }
        return new ResponseDTO<>(ResponseDTO.OK_CODE, null, true, "状态更新成功", null);
    }
}
