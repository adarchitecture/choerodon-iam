package io.choerodon.iam.app.service;

import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.iam.infra.dto.RoleAssignmentDeleteDTO;
import io.choerodon.iam.infra.dto.payload.UserMemberEventPayload;
import org.hzero.iam.domain.entity.MemberRole;
import org.hzero.iam.domain.entity.User;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * @author superlee
 * @author wuguokai
 * @author zmf
 */
public interface RoleMemberService {

    MemberRole insertSelective(MemberRole memberRoleDTO);

    List<MemberRole> createOrUpdateRolesByMemberIdOnOrganizationLevel(
            Boolean isEdit, Long organizationId, List<Long> memberIds, List<MemberRole> memberRoleDTOList, String memberType);

    List<MemberRole> createOrUpdateRolesByMemberIdOnProjectLevel(
            Boolean isEdit, Long projectId, List<Long> memberIds, List<MemberRole> memberRoleDTOList, String memberType);

    /**
     * @param roleAssignmentDeleteDTO
     * @param syncAll                 删除子项目所有权限
     */
    void deleteOnProjectLevel(RoleAssignmentDeleteDTO roleAssignmentDeleteDTO, Boolean syncAll);

    void deleteOnProjectLevel(RoleAssignmentDeleteDTO roleAssignmentDeleteDTO);

    List<MemberRole> insertOrUpdateRolesOfUserByMemberId(
            Boolean isEdit, Long sourceId, Long memberId, List<MemberRole> memberRoles, String sourceType);

    List<MemberRole> insertOrUpdateRolesOfUserByMemberId(
            Boolean isEdit, Long sourceId, Long memberId, List<MemberRole> memberRoles, String sourceType, Boolean syncAll);

    List<MemberRole> insertOrUpdateRolesOfClientByMemberId(
            Boolean isEdit, Long sourceId, Long memberId, List<MemberRole> memberRoles, String sourceType);

    /**
     * 批量删除客户端及角色之间的关系
     *
     * @param roleAssignmentDeleteDTO 数据
     * @param sourceType              sourceType
     */
    void deleteClientAndRole(RoleAssignmentDeleteDTO roleAssignmentDeleteDTO, String sourceType);

    void delete(RoleAssignmentDeleteDTO roleAssignmentDeleteDTO, String sourceType);

    void insertAndSendEvent(Long fromUserId, User userDTO, MemberRole memberRole, String loginName);

    List<Long> insertOrUpdateRolesByMemberIdExecute(Long fromUserId, Boolean isEdit, Long sourceId,
                                                    Long memberId, String sourceType,
                                                    List<MemberRole> memberRoleList,
                                                    List<MemberRole> returnList, String memberType);

    ResponseEntity<Resource> downloadTemplatesByResourceLevel(String suffix, String resourceLevel);

    void import2MemberRole(Long sourceId, String sourceType, MultipartFile file);

    void updateMemberRole(List<UserMemberEventPayload> userMemberEventPayloads, ResourceLevel level, Long sourceId);

    void deleteMemberRoleForSaga(Long userId, List<UserMemberEventPayload> userMemberEventPayloads, ResourceLevel level, Long sourceId);
}
