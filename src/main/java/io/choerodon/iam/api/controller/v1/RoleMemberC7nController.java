package io.choerodon.iam.api.controller.v1;

import io.choerodon.core.base.BaseController;
import io.choerodon.core.domain.Page;
import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.iam.api.vo.SimplifiedUserVO;
import io.choerodon.iam.api.vo.agile.RoleVO;
import io.choerodon.iam.app.service.ProjectUserService;
import io.choerodon.iam.app.service.RoleC7nService;
import io.choerodon.iam.app.service.UserC7nService;
import io.choerodon.iam.infra.config.C7nSwaggerApiConfig;
import io.choerodon.iam.infra.dto.RoleAssignmentSearchDTO;
import io.choerodon.iam.infra.dto.UserDTO;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;
import io.choerodon.swagger.annotation.CustomPageRequest;
import io.choerodon.swagger.annotation.Permission;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.hzero.iam.api.dto.RoleDTO;
import org.hzero.iam.domain.entity.MemberRole;
import org.hzero.iam.domain.entity.User;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.SortDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.validation.Valid;
import java.util.List;


/**
 * @author superlee
 * @author wuguokai
 */
@Api(tags = C7nSwaggerApiConfig.CHOERODON_ROLE_MEMBER)
@RestController
@RequestMapping(value = "/choerodon/v1")
public class RoleMemberC7nController extends BaseController {

    private RoleC7nService roleC7nService;
    private ProjectUserService projectUserService;
    private UserC7nService userC7nService;


    public RoleMemberC7nController(RoleC7nService roleC7nService,
                                   UserC7nService userC7nService,
                                   ProjectUserService projectUserService) {
        this.roleC7nService = roleC7nService;
        this.projectUserService = projectUserService;
        this.userC7nService = userC7nService;
    }

    /**
     * 查询project层角色,附带该角色下分配的用户数
     *
     * @return 查询结果
     */
    @Permission(level = ResourceLevel.PROJECT)
    @ApiOperation(value = "项目层查询角色列表以及该角色下的用户数量")
    @PostMapping(value = "/projects/{project_id}/role_members/users/count")
    public ResponseEntity<List<RoleVO>> listRolesWithUserCountOnProjectLevel(
            @PathVariable(name = "project_id") Long projectId,
            @RequestBody(required = false) @Valid RoleAssignmentSearchDTO roleAssignmentSearchDTO) {
        return ResponseEntity.ok(roleC7nService.listRolesWithUserCountOnProjectLevel(projectId, roleAssignmentSearchDTO));
    }

    /**
     * 项目层分页查询角色下的用户
     *
     * @param roleId
     * @param projectId
     * @param roleAssignmentSearchDTO
     * @param doPage                  是否分页，如果为false，则不分页
     * @return
     */
    @Permission(level = ResourceLevel.PROJECT)
    @ApiOperation(value = "项目层分页查询角色下的用户")
    @CustomPageRequest
    @PostMapping(value = "/projects/{project_id}/role_members/users")
    public ResponseEntity<Page<UserDTO>> pagingQueryUsersByRoleIdOnProjectLevel(
            @PathVariable(name = "project_id") Long projectId,
            @ApiIgnore
            @SortDefault(value = "id", direction = Sort.Direction.DESC) PageRequest pageRequest,
            @RequestParam(name = "role_id") Long roleId,
            @RequestBody(required = false) @Valid RoleAssignmentSearchDTO roleAssignmentSearchDTO,
            @RequestParam(defaultValue = "true") boolean doPage) {
        return ResponseEntity.ok(projectUserService.pagingQueryUsersByRoleIdOnProjectLevel(
                pageRequest, roleAssignmentSearchDTO, roleId, projectId, doPage));
    }

    /**
     * 查询用户在项目下拥有的角色
     */
    @Permission(level = ResourceLevel.PROJECT, permissionLogin = true)
    @ApiOperation(value = "查询用户在项目下拥有的角色")
    @GetMapping(value = "/projects/{project_id}/role_members/users/{user_id}")
    public ResponseEntity<List<RoleDTO>> getUserRolesByUserIdAndProjectId(@PathVariable(name = "project_id") Long projectId,
                                                                          @PathVariable(name = "user_id") Long userId) {
        return ResponseEntity.ok(projectUserService.listRolesByProjectIdAndUserId(projectId, userId));
    }

    /**
     * 在项目层查询用户，用户包含拥有的project层的角色
     *
     * @param projectId               项目id
     * @param roleAssignmentSearchDTO 查询请求体，无查询条件需要传{}
     */
    @Permission(level = ResourceLevel.PROJECT)
    @ApiOperation(value = "项目层查询用户列表以及该用户拥有的角色")
    @PostMapping(value = "/projects/{project_id}/role_members/users/roles")
    public ResponseEntity<Page<UserDTO>> pagingQueryUsersWithProjectLevelRoles(
            @ApiIgnore
            @SortDefault(value = "id", direction = Sort.Direction.DESC) PageRequest pageRequest,
            @PathVariable(name = "project_id") Long projectId,
            @RequestBody(required = false) @Valid RoleAssignmentSearchDTO roleAssignmentSearchDTO) {
        return ResponseEntity.ok(projectUserService.pagingQueryUsersWithRoles(
                pageRequest, roleAssignmentSearchDTO, projectId));
    }

    @Permission(level = ResourceLevel.PROJECT)
    @ApiOperation(value = "项目层查询角色列表")
    @GetMapping(value = "/projects/{project_id}/roles")
    public ResponseEntity<List<RoleDTO>> listRolesOnProjectLevel(@PathVariable(name = "project_id") Long projectId,
                                                                 @RequestParam(name = "role_name") String roleName,
                                                                 @RequestParam(name = "only_select_enable", required = false, defaultValue = "true")
                                                                         Boolean onlySelectEnable) {
        return new ResponseEntity<>(projectUserService.listRolesByName(projectId, roleName, onlySelectEnable), HttpStatus.OK);
    }

    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation(value = "组织层查询启用状态的用户列表")
    @GetMapping(value = "/organizations/{organization_id}/enableUsers")
    public ResponseEntity<List<User>> listUsersOnOrganizationLevel(@PathVariable(name = "organization_id") Long organizationId,
                                                                   @RequestParam(name = "user_name") String userName) {
        return new ResponseEntity<>(userC7nService.listEnableUsersByName
                (ResourceLevel.ORGANIZATION.value(), organizationId, userName), HttpStatus.OK);
    }

    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation(value = "项目层查询启用状态的用户列表")
    @GetMapping(value = "/projects/{project_id}/enableUsers")
    public ResponseEntity<List<User>> listUsersOnProjectLevel(@PathVariable(name = "project_id") Long projectId,
                                                              @RequestParam(name = "user_name") String userName) {
        return new ResponseEntity<>(userC7nService.listEnableUsersByName
                (ResourceLevel.PROJECT.value(), projectId, userName), HttpStatus.OK);
    }


    @Permission(level = ResourceLevel.SITE)
    @ApiOperation(value = "全局层查询启用状态的用户列表")
    @GetMapping(value = "/site/enableUsers")
    public ResponseEntity<List<User>> listUsersOnSiteLevel(@RequestParam(name = "user_name") String userName) {
        return new ResponseEntity<>(userC7nService.listEnableUsersByName
                (ResourceLevel.SITE.value(), 0L, userName), HttpStatus.OK);
    }

    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation(value = "组织层查询角色列表")
    @GetMapping(value = "/organizations/{organization_id}/roles")
    public ResponseEntity<List<RoleDTO>> listRolesOnOrganizationLevel(@PathVariable(name = "organization_id") Long organizationId,
                                                                      @RequestParam(name = "role_name") String roleName,
                                                                      @RequestParam(name = "only_select_enable", required = false, defaultValue = "true")
                                                                              Boolean onlySelectEnable) {
        return new ResponseEntity<>(roleC7nService.listRolesByName(organizationId, roleName, onlySelectEnable), HttpStatus.OK);
    }

    @Permission(permissionPublic = true)
    @ApiOperation(value = "分页查询全平台层用户（未禁用）")
    @GetMapping(value = "/all/users")
    @CustomPageRequest
    public ResponseEntity<Page<SimplifiedUserVO>> queryAllUsers(@ApiIgnore
                                                                @SortDefault(value = "id", direction = Sort.Direction.DESC) PageRequest pageRequest,
                                                                @RequestParam(value = "organization_id") Long organizationId,
                                                                @RequestParam(value = "param", required = false) String param) {
        return new ResponseEntity<>(userC7nService.pagingQueryAllUser(pageRequest, param, organizationId), HttpStatus.OK);
    }
    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation(value = "组织层批量分配用户角色")
    @PostMapping(value = "/organizations/{organization_id}/users/assign_roles")
    public ResponseEntity<List<MemberRole>> assignUsersRolesOnOrganizationLevel(@PathVariable(name = "organization_id") Long organizationId,
                                                                                @RequestBody List<MemberRole> memberRoleDTOS) {
        return new ResponseEntity<>(userC7nService.assignUsersRoles(ResourceLevel.ORGANIZATION.value(), organizationId, memberRoleDTOS), HttpStatus.OK);
    }

}
