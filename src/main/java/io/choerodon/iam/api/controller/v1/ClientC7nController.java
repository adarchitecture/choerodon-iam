package io.choerodon.iam.api.controller.v1;

import java.util.List;

import io.swagger.annotations.ApiOperation;
import org.hzero.core.base.BaseController;
import org.hzero.iam.app.service.ClientService;
import org.hzero.iam.domain.entity.Client;
import org.hzero.iam.domain.repository.ClientRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.iam.app.service.ClientC7nService;
import io.choerodon.swagger.annotation.Permission;


/**
 * @author wuguokai
 */
@RestController
@RequestMapping(value = "/choerodon/v1/organizations/{organization_id}/clients")
public class ClientC7nController extends BaseController {

    private final ClientService clientService;
    private final ClientC7nService clientC7nService;
    private final ClientRepository clientRepository;


    public ClientC7nController(ClientService clientService, ClientC7nService clientC7nService, ClientRepository clientRepository) {
        this.clientService = clientService;
        this.clientC7nService = clientC7nService;
        this.clientRepository = clientRepository;
    }

    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation(value = "随机的客户端创建信息生成")
    @GetMapping(value = "/createInfo")
    public ResponseEntity<Client> createInfo(@PathVariable("organization_id") Long organizationId) {
        return new ResponseEntity<>(clientC7nService.getDefaultCreateData(organizationId), HttpStatus.OK);
    }


    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation(value = "删除客户端")
    @DeleteMapping(value = "/{client_id}")
    public ResponseEntity delete(@PathVariable("organization_id") Long organizationId, @PathVariable("client_id") Long clientId) {
        Client client = new Client();
        client.setOrganizationId(organizationId);
        client.setId(clientId);
        clientService.delete(client);
        return new ResponseEntity(HttpStatus.OK);
    }


    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation(value = "客户端信息校验(新建校验不传id,更新校验传id)")
    @PostMapping(value = "/check")
    public void check(@PathVariable(name = "organization_id") Long organizationId,
                      @RequestBody Client client) {
        clientService.createCheck(client);
    }

    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation(value = "客户端分配角色")
    @PostMapping(value = "/{client_id}/assign_roles")
    public ResponseEntity<Client> assignRoles(@PathVariable("organization_id") Long organizationId,
                                              @PathVariable("client_id") Long clientId,
                                              @RequestBody List<Long> roleIds) {
        return ResponseEntity.ok(clientC7nService.assignRoles(organizationId, clientId, roleIds));
    }

    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation(value = "通过名称查询客户端")
    @GetMapping("/query_by_name")
    public ResponseEntity<Client> queryByName(@PathVariable("organization_id") Long organizationId, @RequestParam(value = "client_name") String clientName) {
        return new ResponseEntity<>(clientC7nService.queryByName(organizationId, clientName), HttpStatus.OK);
    }


}