package com.zero.constant;

/**
 * @author Zero
 * @date 2022/2/7 20:32
 * @description
 * @since 1.8
 **/
public class LoginConstant {

    /**
     * 后台管理员
     */
    public static final String ADMIN_TYPE = "admin_type";

    /**
     * 用户
     */
    public static final String MEMBER_TYPE = "member_type";

    /**
     * 使用用户名查询管理员
     */
    public static final String QUERY_ADMIN_SQL = "select `id`, `username`, `password`, `status` from sys_user where username = ?";

    /**
     * 判断用户是否为超级管理员
     */
    public static final String QUERY_ROLE_CODE_SQL = "select `code` from sys_role left join sys_user_role on sys_role.id = sys_user_role.role_id where sys_user_role.user_id = ?";

    /**
     * 查询所有权限名称
     */
    public static final String QUERY_ALL_PERMISSIONS = "SELECT `name` from sys_privilege";

    /**
     * 非超级管理员，需要先查询role->permissionId->permission
     */
    public static final String QUERY_PERMISSION_SQL = "SELECT * FROM sys_privilege LEFT JOIN sys_role_privilege ON sys_role_privilege.privilege_id = sys_privilege.id LEFT JOIN sys_user_role  ON sys_role_privilege.role_id = sys_user_role.role_id WHERE sys_user_role.user_id = ?";

    /**
     * 普通用户的查询
     */
    public static final String QUERY_MEMBER_SQL= "select `id`, `password`, `status` from user where mobile = ? or email = ?";

    public static final String QUERY_ADMIN_USER_WITH_ID = "select username from sys_user where id = ?";

    public static final String QUERY_MEMBER_USER_WITH_ID = "select mobile from user where id = ?";
}
