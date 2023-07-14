package com.example.provider.controller;

import com.example.provider.model.PermissionModel;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.api.GetACLBuilder;
import org.apache.curator.framework.api.GetDataBuilder;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.data.ACL;
import org.apache.zookeeper.data.Id;
import org.apache.zookeeper.data.Stat;
import org.apache.zookeeper.server.auth.DigestAuthenticationProvider;
import org.apache.zookeeper.server.quorum.flexible.QuorumVerifier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
public class TestController {

    @Autowired
    private CuratorFramework curatorFramework;

    @GetMapping("/test")
    public void test() throws Exception {
        byte[] bytes = curatorFramework.getData().forPath("/test");
        System.out.println(new String(bytes));
    }

    @GetMapping("/test2")
    public void test2() throws Exception {
        byte[] bytes = curatorFramework.getData().forPath("/test2");
        System.out.println(new String(bytes));
    }

    @GetMapping("/create")
    public void create() throws Exception {

        List<ACL> list = new ArrayList<>();
        // 将明文账户密码通过api生成密文
        String digest = DigestAuthenticationProvider.generateDigest("user:password");
        ACL acl = new ACL(ZooDefs.Perms.ALL, new Id("digest", digest));
        list.add(acl);

        curatorFramework.create().withACL(list).forPath("/test3");
    }

    /**
     * 测试，通过人为添加节点，检测能否访问。
     * 使用预设账号密码创建/test
     * 使用如下账号创建/test2
     * @param permissionModel {
     *     "scheme": "digest",
     *     "name": "abc",
     *     "password": "abc",
     *     "path": "/test2",
     *     "permissions": [
     *         "WRITE",
     *         "READ",
     *         "DELETE"
     *     ]
     * }
     * @return
     */
    @PostMapping("/setAcl")
    public String create(@RequestBody PermissionModel permissionModel) {
        System.out.println("1");
        try {
            System.out.println(2);
            String digest = DigestAuthenticationProvider.generateDigest(permissionModel.getName() + ":" + permissionModel.getPassword());
            Id user = new Id(permissionModel.getScheme(), digest);
            List<ACL> aclList = new ArrayList<>();
            int permissionValue = 0;
            for (String permission : permissionModel.getPermissions()) {
                permissionValue |= getPermissionValue(permission);
            }
            if (permissionValue != 0) {
                ACL acl = new ACL(permissionValue, user);
                aclList.add(acl);
                Stat stat = curatorFramework.checkExists().forPath(permissionModel.getPath());
                if (stat != null) {
                    curatorFramework.setACL().withACL(aclList).forPath(permissionModel.getPath());
                } else {
                    curatorFramework.create().creatingParentsIfNeeded().withACL(aclList).forPath(permissionModel.getPath());
                }
                return "设置成功";
            }else {
                throw new RuntimeException("权限列表异常");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "权限设置异常";
        }
    }

    private int getPermissionValue(String permission) {
        switch (permission) {
            case "READ":
                return ZooDefs.Perms.READ;
            case "WRITE":
                return ZooDefs.Perms.WRITE;
            case "DELETE":
                return ZooDefs.Perms.DELETE;
            case "CREATE":
                return ZooDefs.Perms.CREATE;
            case "ADMIN":
                return ZooDefs.Perms.ADMIN;
            case "ALL":
                return ZooDefs.Perms.ALL;
            default:
                return 0;
        }
    }
}

