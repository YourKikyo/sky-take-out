# PROJECT.md - 苍穹外卖 sky-take-out 项目记忆

> 每次开始搭建/修改项目前，先通读本文件；技术栈或约定发生变化时，同步更新本文件。

## 1. 项目概览与技术栈

- Spring Boot 2.7.3 + Maven 多模块：
  - `sky-common`：通用工具（JWT / OSS / 微信支付 / HttpClient）、常量、异常、Result / PageResult、ThreadLocal 上下文、配置属性类、JSON 映射器
  - `sky-pojo`：entity（对应表）、dto（入参）、vo（出参）
  - `sky-server`：controller / service / mapper / config / handler / interceptor / aspect / `resources/mapper/*.xml`
- 持久层：MyBatis（注解 SQL + XML 动态 SQL）、MySQL 8（Druid 连接池）、PageHelper 分页
- 其他已引入依赖（供后续模块使用）：Lombok、Knife4j / Swagger、jjwt、Fastjson、Redis / Spring Cache、WebSocket、POI、阿里云 OSS、微信支付
- 运行环境：JDK 17（Liberica）；`application.yml` + `application-dev.yml`，激活 dev profile，端口 8080；本地库 `sky_take_out`（root / wjz123456，仅开发用）
- 密钥管理：OSS 等敏感值**不入库**。取值优先级为「环境变量（`SKY_ALIOSS_*`）> `application-local.yml` > 默认值」；`application-local.yml` 已被 `.gitignore` 忽略，仓库内只保留 `application-local.yml.example` 模板，由 `spring.config.import: optional:classpath:application-local.yml` 静默加载（确认：`startup-nolocal` 验证无该文件也能正常启动，仅 OSS 上传不可用）

## 2. 代码与注释风格

- 注释一律中文：类 / 方法 javadoc 带 `@param` / `@return`；行内步骤注释用 `//1、` `//2、`；允许保留被注释掉的旧代码
- Lombok：实体 / DTO / VO 用 `@Data`；实体和 VO 用 `@Data @Builder @NoArgsConstructor @AllArgsConstructor`；日志用 `@Slf4j` + `log.info("中文描述：{}", 对象)`
- 分层约定：
  - Controller 只收参并返回 `Result<T>`，不写业务逻辑；`@RestController` + `@RequestMapping("/admin/xxx"` 或 `/user/xxx)`
  - Service 接口放 `service`，实现放 `service/impl`（`XxxService` / `XxxServiceImpl`）
  - Mapper 接口放 `mapper`；简单 SQL 用注解，动态 SQL 用 XML（namespace = 接口全限定名）
  - DTO 收参、VO 返回、entity 对应数据库表
- 依赖注入用 `@Autowired` 字段注入；DTO 转实体用 `BeanUtils.copyProperties`；实体构建用 Lombok builder
- 状态与提示信息统一放 `constant` 常量类（`StatusConstant`、`MessageConstant` 等），不写魔法数
- 业务异常继承 `BaseException`，由 `GlobalExceptionHandler` 统一转 `Result.error(msg)`
- Swagger 注解：Controller 用 `@Api` / `@ApiOperation`，VO 用 `@ApiModel` / `@ApiModelProperty`
- Git commit message：简短中文动词短语（如“新增员工业务代码开发”）

## 3. 工程准则

- 统一返回 `Result<T>`：code=1 成功、0 失败；分页用 `PageHelper.startPage(...)` 后紧跟 mapper 调用，返回 `PageResult(total, records)`
- 公共字段 `create_time / update_time / create_user / update_user`：insert / update 的 Mapper 方法标注 `@AutoFill(OperationType.INSERT/UPDATE)`，由 `AutoFillAspect` 反射填充；实体必须含对应字段与 setter
- 时间统一用 `LocalDateTime`；JSON 时间格式由 `JacksonObjectMapper` 统一为 `yyyy-MM-dd HH:mm`（注意：不含秒）
- JWT：管理端 `/admin/**` 由 `JwtTokenAdminInterceptor` 校验，token 头名 `token`，放行 `/admin/employee/login`；用户端后续按同模式增加
- 多表写操作加 `@Transactional`（启动类已开启注解事务）
- 编码：源文件均为 UTF-8 无 BOM；Maven CLI 平台编码为 GBK 且 pom 未声明 `project.build.sourceEncoding`，后续在父 pom 补 UTF-8 声明，避免中文乱码；建议显式固定 `java.version`（当前 CLI 产出 Java 8 字节码，IDEA 语言级别为 17）
- 构建：在仓库根目录执行 `mvn -DskipTests compile`（已验证可用）
- Git：`.gitignore` 已忽略 `.idea/`、`target/`、测试文件

## 4. 当前状态（2026-08-05，随进度更新）

已完成：
- 员工模块：登录 / 登出 / 新增 / 分页查询 / 启用禁用 / 编辑
- 分类模块：增删改查 / 启用禁用 / 按类型查询
- 基础能力：AutoFill AOP、JWT 管理端拦截器、全局异常处理、Result / PageResult、PageHelper、Knife4j（`/doc.html`）、Jackson 时间格式化

待建（DTO / VO / 实体大多已建好，Controller / Service 大多未建）：
- 菜品 / 口味、套餐、用户端（C 端）、购物车、地址簿、订单与支付、数据统计报表

已知问题：
- `AutoFillAspect` 的 INSERT 分支漏调 `setCreateUser`，且重复调用了 `setUpdateUser`
- `WebMvcConfiguration` 注释声称 Long 转 String 防前端精度丢失，但 `JacksonObjectMapper` 实际未实现
- `application-dev.yml` 采用环境变量占位符 + `application-local.yml` 的方式管理 OSS 密钥；用户端 JWT、微信支付配置仍缺失，后续模块需补充
