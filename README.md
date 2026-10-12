# 供水设施管理后端

Java 17 及以上 / Spring Boot 4.1.1。默认配置使用 MySQL：设置 DB_URL、DB_USERNAME、DB_PASSWORD、APP_CORS_ALLOWED_ORIGINS 后启动。生产访问使用 HTTPS；数据库不默认使用 root。

所有业务接口需要 Authorization: Bearer 会话。现有 BCrypt 账号可继续登录，会话有效期 8 小时，退出和删除账号会撤销会话。adminRole=0 管理账号，其余账号可办理设施业务。旧 RSA 加密密码账号须由管理员重置为 BCrypt，前端注册入口已关闭。

停用水表使用 POST，必须提交 waterMeterId、wallId、reason。服务端按所属设施、设施类型和当前状态定位，停用与日志写入在同一事务中完成。消防栓和阀门井维修更新也改为 POST。

仓库中的证书私钥已移除；旧证书需要在服务器重新签发并撤销，删除源码不会让旧私钥失效。历史提交没有改写。

## Java 维护与测试

使用 Java 17 运行 `mvn spotless:apply` 统一源码和测试格式，`mvn clean verify` 检查格式、执行测试并打包。Spotless 固定使用 google-java-format 1.24.0 的 AOSP 四空格格式，格式检查绑定到 `validate` 阶段。

`LocationController`、`GeocodeController` 负责 HTTP 参数和身份读取，业务校验、行锁及写入事务分别放在 `LocationService`、`GeocodeService`。固定响应使用 `LocationModels`、`WorkspaceResponses` 中的 record；动态业务字段保留 Map，字段排列及修订哈希算法不变。旧接口使用 `LegacyResponses`，保留已有状态码、提示语，以及不返回 `data` 和显式返回 `data: null` 的区别。

未预期异常仍返回通用 500 提示。服务端日志仅记录路由模板、异常类型链和有限的堆栈位置，不记录异常消息、实际请求地址、请求体或凭据。测试补充了响应字段与空值、修订哈希、审计内容、事务回滚、并发版本冲突及重复请求、日志敏感值排除等检查。

## 本地五表重建

`local` profile 使用 H2 2.4.240 文件库，只监听 `127.0.0.1:8097`。配置 `LOCAL_DATA_DIR`、`LOCAL_DB_PASSWORD`、`LOCAL_ADMIN_PASSWORD`；首次空库创建本地管理员。相邻前端仓库的 `tygs/scripts/local-tygs.mjs` 负责生成私有凭据、启动、停止、状态检查及停机备份。完整步骤和数据边界见 [前端说明](../tygs/README.md)。

`LocalDatabase` 按当前实体创建业务表，不声称恢复历史 MySQL schema。`ledger_source` 保存文件指纹和预期行数，`ledger_record` 保存逐行原始数据、映射结果、来源位置及新建业务记录的主键。原始编号不作为数据库主键。工作簿映射在 `src/main/resources/ledger-mapping.json` 中。

`/ledger/*` 仅在 local profile 可用，所有接口需要登录，导入和全量原始行读取需要管理员。每批最多 250 行，事务失败整批回滚；同源同位置的重复行跳过，内容冲突返回 409，禁止覆盖。维护与复核保留为独立的未关联记录，不写成新设施或既有设施的维修历史；混合井室不强制拆分归类。候选接口只读，不持久化已确认关联。

验证：`mvn test` 与 `mvn package`。测试使用隔离的 H2 内存库及模拟接口，不连接生产数据库。覆盖会话权限、业务停用/维修、来源留存、全字段导入、幂等与冲突、批次回滚、分类保留、分页详情和候选计数。生产 MySQL 的 schema 兼容性与导入尚需在真实环境单独验证。

## 本地业务补全

`workspace-modules.json` 显式定义九类模块的表和字段白名单。`/workspace/*` 仅在 local profile 启用：九类模块可新增；三供一业、户表、表卡与三类小区资料支持查询、详情、编辑和批量导入。原设施模块继续使用既有查询及维修流程。归档/恢复和导入限管理员，其余操作需已登录的业务账号。

新增使用提交标识防止同次重试重复写入；编辑、归档采用行锁和内容版本校验。`workspace_change` 保存修改前后内容、操作者和时间；接口展示最近 100 次操作。`workspace_import` 保存原始字段/单元格、文件 SHA-256、工作表与行号，来源数据不随业务编辑改变。每批导入最多 100 条，事务失败整批回滚，服务端重新校验字段映射；不同源文件不按编号合并。

`workspace-schema.sql` 增加来源、提交标识、归档状态、修改历史及 `facility_location` 表。启动迁移给旧户表 `sheet1` 增加独立自增主键，给 `card` 增加备注字段，不修改五份采集台账原始行或已有业务内容。升级前应停机备份。

`/locations/*` 提供四类设施的定位列表、统计、坐标保存及最近 100 次修改。坐标以业务表名和主键关联，只接受有限范围经纬度、明确坐标系和非空定位依据；保存检查版本冲突，并记录修改历史。原始地址/坐标文字字段保持不变，不自动定位或合并设施。

`WorkspaceTests` 覆盖九类字段保存、提交幂等、编辑冲突、归档恢复、来源留存、重复与冲突导入、批次回滚、权限、查询通配符转义以及坐标范围/坐标系/证据和版本校验。

## 分页与查询开销

新增登录后可用的 `GET /facilities/{rooms|meters|hydrants|valves}/records`，返回 `rows/total/page/size`。默认 `page=1&size=30`，每页最多 500 条，页码最多 100,000；按主键升序读取。字段名来自每类设施的白名单，筛选值使用参数绑定，包含匹配转义 `%`、`_` 与转义符。`caliber`、`wellChamberType` 支持重复查询参数。各行沿用既有实体 JSON 字段，原全量接口继续保留，用于已有调用及原始数据核验。

台账列表只读取展示字段，`rawJson/mappedJson` 在详情和来源核验接口中读取；计数不再连接来源表。台账总数复用分类汇总，地图统计合并为一次条件聚合。`ledger-schema.sql` 新增 `(kind,id)`、`(linkStatus,kind,id)`、`(sourceSha,id)` 索引，不改写来源行。local profile 对超过 2 KB 的 JSON/文本响应启用压缩。

本机 H2 的水表间首屏响应从全量 12,534,200 B 降至 28,860 B。升级后同一进程内，每接口首样后再取 5 次请求的中位数，旧全量接口为 44.35 ms，新 30 条分页接口为 2.13 ms；耗时包含回环 HTTP 读取响应，不含浏览器渲染。该比较反映减少读取和传输的数据量，不是同等工作量的数据库压测。原始采样保存在相邻 `local-tygs-data/performance-after.json`。

2026-10-12 完整后端测试 37 项通过，新增覆盖四类分页、末页、与旧接口字段一致性、大小写/字面通配符筛选、多选、非法参数和权限。重启后重新核对 26,512 条原始记录和 483,859 个映射字段，全部一致。新分页 SQL 在本地 H2 上验证；生产 MySQL 的表结构、排序规则及实际负载尚未验证，不能直接沿用这里的性能数字。
