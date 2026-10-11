# 供水设施管理后端

Java 17 及以上 / Spring Boot 3.5。配置 DB_URL、DB_USERNAME、DB_PASSWORD、APP_CORS_ALLOWED_ORIGINS 后启动。生产访问使用 HTTPS；数据库不默认使用 root。

所有业务接口需要 Authorization: Bearer 会话。现有 BCrypt 账号可继续登录，会话有效期 8 小时，退出和删除账号会撤销会话。adminRole=0 管理账号，其余账号可办理设施业务。旧 RSA 加密密码账号须由管理员重置为 BCrypt，前端注册入口已关闭。

停用水表使用 POST，必须提交 waterMeterId、wallId、reason。服务端按所属设施、设施类型和当前状态定位，停用与日志写入在同一事务中完成。消防栓和阀门井维修更新也改为 POST。

仓库中的证书私钥已移除；旧证书需要在服务器重新签发并撤销，删除源码不会让旧私钥失效。历史提交没有改写。

验证：mvn test 与 mvn package。测试使用模拟数据库接口，不连接生产数据库。
