package io.choerodon.iam.app.service.impl;

import io.choerodon.core.domain.Page;
import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.core.oauth.DetailsHelper;
import io.choerodon.iam.api.vo.ClientRoleQueryVO;
import io.choerodon.iam.api.vo.RoleNameAndEnabledVO;
import io.choerodon.iam.api.vo.UserRoleVO;
import io.choerodon.iam.api.vo.agile.RoleUserCountVO;
import io.choerodon.iam.app.service.RoleC7nService;
import io.choerodon.iam.infra.constant.LabelC7nConstants;
import io.choerodon.iam.infra.dto.ProjectDTO;
import io.choerodon.iam.infra.dto.RoleAssignmentSearchDTO;
import io.choerodon.iam.infra.dto.RoleC7nDTO;
import io.choerodon.iam.infra.enums.RoleLabelEnum;
import io.choerodon.iam.infra.mapper.*;
import io.choerodon.iam.infra.utils.ConvertUtils;
import io.choerodon.iam.infra.utils.PageUtils;
import io.choerodon.iam.infra.utils.ParamUtils;
import io.choerodon.mybatis.pagehelper.PageHelper;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;

import org.hzero.core.exception.NotLoginException;
import org.hzero.iam.api.dto.RoleDTO;
import org.hzero.iam.domain.entity.Label;
import org.hzero.iam.domain.entity.Role;
import org.hzero.iam.domain.vo.RoleVO;
import org.hzero.iam.infra.mapper.RoleMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2020/5/12 17:00
 */
@Service
public class RoleC7nServiceImpl implements RoleC7nService {

    private RoleC7nMapper roleC7nMapper;
    private ProjectUserMapper projectUserMapper;
    private ProjectMapper projectMapper;
    private RoleMapper roleMapper;
    private UserC7nMapper userC7nMapper;
    private ClientC7nMapper clientC7nMapper;

    public RoleC7nServiceImpl(RoleC7nMapper roleC7nMapper, UserC7nMapper userC7nMapper, ProjectUserMapper projectUserMapper, ProjectMapper projectMapper, ClientC7nMapper clientC7nMapper, RoleMapper roleMapper) {
        this.roleC7nMapper = roleC7nMapper;
        this.projectUserMapper = projectUserMapper;
        this.projectMapper = projectMapper;
        this.roleMapper = roleMapper;
        this.userC7nMapper = userC7nMapper;
        this.clientC7nMapper = clientC7nMapper;
    }

    @Override
    public List<io.choerodon.iam.api.vo.agile.RoleVO> listRolesWithUserCountOnProjectLevel(Long projectId, RoleAssignmentSearchDTO roleAssignmentSearchDTO) {
        ProjectDTO projectDTO = projectMapper.selectByPrimaryKey(projectId);
        RoleVO param = new RoleVO();
        if (roleAssignmentSearchDTO != null) {
            param.setName(roleAssignmentSearchDTO.getRoleName());
        }
        List<Role> roles = roleC7nMapper.listRolesByTenantIdAndLableWithOptions(projectDTO.getOrganizationId(), LabelC7nConstants.PROJECT_ROLE, param);
        List<io.choerodon.iam.api.vo.agile.RoleVO> roleVOList = ConvertUtils.convertList(roles, io.choerodon.iam.api.vo.agile.RoleVO.class);
        List<RoleUserCountVO> roleUserCountVOS = projectUserMapper.countProjectRoleUser(projectId);
        Map<Long, io.choerodon.iam.api.vo.agile.RoleVO> roleMap = roleVOList.stream().collect(Collectors.toMap(RoleDTO::getId, v -> v));
        // 给角色添加用户数
        roleUserCountVOS.forEach(v -> {
            io.choerodon.iam.api.vo.agile.RoleVO roleVO = roleMap.get(v.getRoleId());
            if (roleVO != null) {
                roleVO.setUserCount(v.getUserNumber());
            }
        });
        return roleVOList;
    }

    @Override
    public Page<RoleC7nDTO> listRole(PageRequest pageRequest, Long tenantId, String name, String level, String params) {
        Long userId = Optional.ofNullable(DetailsHelper.getUserDetails()).orElseThrow(NotLoginException::new).getUserId();
        List<RoleC7nDTO> roleDTOList = new ArrayList<>();

        Page<UserRoleVO> result = PageHelper.doPageAndSort(pageRequest, () -> roleC7nMapper.selectRoles(userId, name, level, params));
        result.getContent().forEach(i -> {
            String[] roles = i.getRoleNames().split(",");
            List<RoleNameAndEnabledVO> list = new ArrayList<>(roles.length);
            for (String role : roles) {
                String[] nameAndEnabled = role.split("\\|");
                boolean roleEnabled = true;
                if (nameAndEnabled[2].equals("0")) {
                    roleEnabled = false;
                }
                list.add(new RoleNameAndEnabledVO(nameAndEnabled[0], nameAndEnabled[1], roleEnabled));
            }
            RoleC7nDTO roleC7nDTO = ConvertUtils.convertObject(i, RoleC7nDTO.class);
            roleC7nDTO.setRoles(list);
            if (ResourceLevel.PROJECT.value().equals(i.getLevel())) {
                roleC7nDTO.setTenantId(projectMapper.selectByPrimaryKey(i.getId()).getOrganizationId());
            }
            roleDTOList.add(roleC7nDTO);
        });
        return PageUtils.copyPropertiesAndResetContent(result, roleDTOList);
    }

    @Override
    public Page<io.choerodon.iam.api.vo.RoleVO> pagingSearch(PageRequest pageRequest, Long tenantId, String name, String code, String roleLevel, Boolean builtIn, Boolean enabled, String params) {
        String labelName = null;

        if (ResourceLevel.ORGANIZATION.value().equals(roleLevel)) {
            labelName = RoleLabelEnum.TENANT_ROLE.value();
        }
        if (ResourceLevel.PROJECT.value().equals(roleLevel)) {
            labelName = RoleLabelEnum.PROJECT_ROLE.value();
        }
        String finalLabelName = labelName;
        Page<io.choerodon.iam.api.vo.RoleVO> page = PageHelper.doPage(pageRequest, () -> roleC7nMapper.fulltextSearch(tenantId, name, code, ResourceLevel.ORGANIZATION.value(), builtIn, enabled, finalLabelName, params));

        if (!CollectionUtils.isEmpty(page.getContent())) {
            page.getContent().stream().forEach(roleVO -> {
                List<Label> labels = roleC7nMapper.listRoleLabels(roleVO.getId());
                if (!CollectionUtils.isEmpty(labels)) {
                    labels.forEach(label -> {
                        if (RoleLabelEnum.TENANT_ROLE.value().equals(label.getName())) {
                            roleVO.setRoleLevel(ResourceLevel.ORGANIZATION.value());
                        }
                        if (RoleLabelEnum.PROJECT_ROLE.value().equals(label.getName())) {
                            roleVO.setRoleLevel(ResourceLevel.PROJECT.value());
                        }
                    });
                }
            });
        }
        return page;
    }

    @Override
    public Role getTenantAdminRole(Long organizationId) {
        return roleC7nMapper.getTenantAdminRole(organizationId);
    }

    @Override
    public List<RoleDTO> listRolesByName(Long organizationId, String roleName, Boolean onlySelectEnable) {
        return roleC7nMapper.listRolesByName(organizationId, roleName, onlySelectEnable);
    }

    @Override
    public List<RoleC7nDTO> listRolesWithUserCountOnOrganizationLevel(RoleAssignmentSearchDTO roleAssignmentSearchDTO, Long sourceId) {
        List<RoleC7nDTO> roles = ConvertUtils.convertList(
                roleC7nMapper.fuzzySearchRolesByName(roleAssignmentSearchDTO.getRoleName(), sourceId, ResourceLevel.ORGANIZATION.value(), RoleLabelEnum.TENANT_ROLE.value(), false),
                RoleC7nDTO.class);
        String param = ParamUtils.arrToStr(roleAssignmentSearchDTO.getParam());
        roles.forEach(r -> {
            Integer count = userC7nMapper.selectUserCountFromMemberRoleByOptions(r.getId(),
                    "user", sourceId, ResourceLevel.ORGANIZATION.value(), roleAssignmentSearchDTO, param);
            r.setUserCount(count);
        });
        return roles;
    }

    @Override
    public List<RoleC7nDTO> listRolesWithClientCountOnProjectLevel(ClientRoleQueryVO clientRoleQueryVO, Long sourceId) {
        List<RoleC7nDTO> roles = ConvertUtils.convertList(
                roleC7nMapper.fuzzySearchRolesByName(clientRoleQueryVO.getRoleName(), sourceId, ResourceLevel.PROJECT.value(), RoleLabelEnum.PROJECT_ROLE.value(), false),
                RoleC7nDTO.class);
        String param = ParamUtils.arrToStr(clientRoleQueryVO.getParam());
        roles.forEach(r -> {
            Integer count = clientC7nMapper.selectClientCountFromMemberRoleByOptions(
                    r.getId(), ResourceLevel.PROJECT.value(), sourceId, clientRoleQueryVO, param);
            r.setUserCount(count);
        });
        return roles;
    }


    @Override
    public List<RoleC7nDTO> listRolesWithClientCountOnOrganizationLevel(ClientRoleQueryVO clientRoleQueryVO, Long sourceId) {
        List<RoleC7nDTO> roles = ConvertUtils.convertList(
                roleC7nMapper.fuzzySearchRolesByName(clientRoleQueryVO.getRoleName(), sourceId, ResourceLevel.ORGANIZATION.value(), RoleLabelEnum.TENANT_ROLE.value(), false),
                RoleC7nDTO.class);
        String param = ParamUtils.arrToStr(clientRoleQueryVO.getParam());
        roles.forEach(r -> {
            Integer count = clientC7nMapper.selectClientCountFromMemberRoleByOptions(
                    r.getId(), ResourceLevel.ORGANIZATION.value(), sourceId, clientRoleQueryVO, param);
            r.setUserCount(count);
        });
        return roles;
    }

    @Override
    public List<Long> queryIdsByLabelNameAndLabelType(String labelName, String labelType) {
        List<Role> roles = roleC7nMapper.selectRolesByLabelNameAndType(labelName, labelType, null);
        return roles.stream().map(Role::getId).collect(Collectors.toList());
    }

}
