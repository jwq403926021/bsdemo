### 关于橙单
 **_橙单，具备极好代码生成能力的低代码工具_** 。您可以按需配置工程脚手架，导入业务数据表和各种复杂的表关联关系，跨库表关联也是 100% 支持。之后根据原型图，配置您的业务表单，如有所需，也可以在线配置流程图，并与静态路由表单进行绑定。当然如果您足够了解橙单，也可以继续配置数据同步和分组计算的定时任务代码，微服务还可以配置跨库实时数据同步的后台服务代码。最后，一键生成完完全全属于您的工程代码。全部包名均可自行定义，所有代码文件毫无任何橙单痕迹，因此生成后的全部工程代码，就如同贵公司最优秀的技术团队亲手打造而得，每一行代码都是那么有用，每一个封装都是那么合理，每一个设计都是那么优雅。
 **_欢迎加入我们的技术支持QQ群3 (392442075)，您的技术问题我们会尽快给出回复。如果橙单对您确有帮助，Fork 的同时，也请 Star 一下_** 。

### 最新功能
> 以下为 2.4 版本的最新功能列表。

- 工作流，支持自由驳回到可途径的审批节点。
- 工作流，完美实现减签，减签时可动态判断会签完成条件，满足条件将主动跳转下一节点。
- 工作流，工单列表增加最后审批状态字段，每一步审批操作均会同步到工单字段，以便在流程结束时可以准确判断，当前流程是否正常的审批通过
- 工作流，在线表单和路由表单均支持保存草稿功能。
- 工作流，支持流程审批人查看列表，列表可以显示所有候选组中的候选人列表，以及审批人的处理时间。
- 工作流，支持会签加签的审批人查看列表，以及会签人的处理时间。
- 工作流，支持同级兄弟部门的岗位指定。如北京分公司的研发部员工，可以指定提交北京的财务部某岗位审批，具体情况可根据流程提交人所在部门动态计算。
- 工作流，待办任务、已办任务和历史任务列表，新增支持显示发起人的 showName，之前只有 loginName。
- 工作流，任务转办，支持转办多人。
- 工作流，任务指派，支持指派多人。
- 工作流，工单列表支持自定义工单编号，可在配置流程时，指定工单编号的计算规则。
- 在线表单，运行时性能大幅优化，表单渲染和在线业务操作所需的内置表数据查询，均改为缓存操作。
- 在线表单，完美支持多数据源，业务表、字典表和在线表单内置表均实现独立分库。
- 在线表单，支持自定义规则编码字段的数据填充。
- 在线表单，下拉框支持多选过滤。
- 在线表单，上传下载支持 minio 存储。
- 在线表单，上传下载支持阿里云和腾讯云。
- 在线表单，空数据字段自动填充默认值。
- 基础功能，全部模块支持 Oracle。
- 基础功能，配置生成的路由表单，上传下载均已添加阿里云 OSS 和腾讯云 COS 的支持。
- 基础功能，数据权限过滤支持表别名。
- 基础功能，批量导入功能，支持基于常量字典和数据表字典的反向翻译。比如，Excel 中为“年级名称”，导入过程会自动翻译为“年级Id”并执行批量插入。
- 基础功能，新增基于 Redis 的生成流水号的工具方法。
- 基础功能，MyRequestBody 中移除 elementType 参数，让代码更加简洁。

### 仓库代码
仓库内包含多种不同类型的工程，均为橙单生成器动态生成而得。目前已覆盖橙单全部基础组件和部分架构场景，以及最主流的技术栈组合，更多架构会在随后的版本中，逐步推送至开源仓库，敬请关注。
- orange-demo-flowable 基于 Flowable 6.6 的完整工作流组件及其示例，同时包含完整的在线表单代码，前端工程包含流程编辑器和在线表单编辑器。
- orange-demo-activiti 基于 Activiti 7.x 的完整工作流组件及其示例，同时包含完整的在线表单代码，前端工程包含流程编辑器和在线表单编辑器。
- orange-demo-multi 基于 Nacos + Redis + Kafka + ELK + Skywalking 的全栈微服务架构。包含完整的用户权限和数据权限管理。
- orange-demo-multi-uaa 基于 OAuth2 + Nacos + Redis + Kafka + ELK + Skywalking 的全栈微服务单点登录架构，权限管理已去除对 Spring Security 的依赖。
- orange-demo-single 单体架构工程，如果项目中不需要工作流和在线表单，可使用该工程为模板进行二次开发，省去您框架裁剪的时间开销。
- orange-demo-single-pg 单体架构工程，支持 PostgreSQL。

### 选择橙单
- **理念**，对开发者极为友好的低代码生成理念，与功能堆砌相比，我们更关注逻辑、数据关联关系以及应用场景，他们的组合既是业务功能良好实现的保障，更是卓越架构高可靠运行的基石。
- **基础**，开放成熟的基础架构，沉淀于大型企业应用项目，又经过众多中小型项目的历练，逐步发展至今。
- **架构**，主流技术栈正交组合，具有极强的可裁剪性，相信总有一种组合适合您的企业。
- **设计**，拥抱云原生的架构设计理念，更多单表查询，有效提升缓存利用率，业务服务自动组装各种关联数据 (含跨服务调用)，彻底释放数据库压力，让微服务下的服务拆分和库表拆分更加得心应手。
- **逻辑**，顶级的代码强度，全覆盖的关联数据合法性验证，数据可见性防越权验证，完整规范且可追溯的日志体系，无限层级的树形数据高效读写。
- **代码**，感动开源中国的代码质量，让你快速上手二开。吃透橙单，收入翻番，效率乘三。

#### 在线资源
- 网站首页。[http://www.orangeforms.com](http://www.orangeforms.com)
- 演示配置。[http://config.orangeforms.com](http://config.orangeforms.com)
- 环境搭建启动。[http://www.orangeforms.com/development-doc/edu-single/](http://www.orangeforms.com/development-doc/edu-single/)
- 后端代码文档。[http://www.orangeforms.com/development-doc/](http://www.orangeforms.com/development-doc/)
- 前端代码文档。[http://www.orangeforms.com/development-vue/](http://www.orangeforms.com/development-vue/)
- 生成器操作指南。[http://www.orangeforms.com/orange-doc/](http://www.orangeforms.com/orange-doc/)
- 完整演示项目 (不含工作流)。[http://demo.orangeforms.com](http://demo.orangeforms.com)
- 在线表单和工作流演示项目。[http://demo.orangeforms.com/flow](http://demo.orangeforms.com/flow)

#### 工作流
-  **支持静态表单 + 工作流的代码生成。用户可在生成器配置表单和流程，再生成相关的完整业务代码，生成后代码对于二次开发极为友好** 。
- 前后端代码无一丝混淆，完全交付，代码质量超高，同时支持单体和微服务架构。
- 同时集成 Flowable 6.6 和 Activiti 7.x 作为工作流引擎，配置工程时可自行二选一。
- 集成工作流和在线表单，无需写一行代码即可配置出相对复杂的工作流业务。
- 支持同一流程的多版本发布，同时支持版本切换。
- 支持完整的工作流工单管理，稍作修改即可支持分布式操作。
- 支持基于角色、部门、岗位、本部门岗位、上级部门岗位、同级部门岗位、指定部门岗位、本部门领导岗位、上级部门领导岗位等候选组。
- 支持工单号自定义编码、工单撤销和终止、会签加签减签、待办已办转办、催办、抄送和传阅、保存草稿、撤回和驳回、历史任务、审批详情列表、包含候选组的审批人列表、附件上传下载等。
- 支持一个表单的多次提交，新增数据和修改数据后均可提交审批流程。
- 工作流支持审批中数据和最终发布数据分离，防止审批中数据污染其他业务关联表的数据，并在审批完成后执行主表及各关联从表的数据同步。
- 工作流支持主表及一对一、一对多、多对多从表数据的级联增删改查。
- 工作流前端已集成美观的流程编辑器，同时支持流程图高亮及任务跟踪。
- 严格且合理的数据安全校验，流程敏感数据 (如合同信息) 不会被越权访问和下载。
- 已去除 Spring Security 的缺省依赖，并与现有权限系统完美整合。
- 工作流与在线表单演示环境，全部代码由橙单生成器直接生成，未经任何修改。 [http://demo.orangeforms.com/flow](http://demo.orangeforms.com/flow)。

#### 静态表单
- 前后端支持单表增删改查、字典列表、分组聚合查询、部分更新、批量删除、导入导出、上传下载。
- 批量导入支持常量字典和数据表字典的反向翻译，比如文件存储的是“一年级”字符串，导入过程中会批量替换为对应的Id后插入数据表。
- 前后端支持主表与一对一、一对多、多对多关联表的关联查询，分组聚合查询、虚拟字段的聚合计算，数据导出、级联添加、级联更新、级联删除。
- 前后端支持主表与多对多中间表的批量插入、批量删除、单条删除、关联列表数据查询、未关联列表数据查询。
- 前后端支持主表与字典表数据、一对一、一对多和多对多与字典表数据的关联查询。
- 后台接口支持主表、一对一从表、一对多从表、多对多从表关联数据的单条和批量验证，会根据配置生成本地服务或跨服务远程调用的高性能数据验证。
- 以上所有功能，均支持分布式跨库跨服务操作，接口保持不变，需要分布式事务的场景，会自动生成 Seata 相关的代码注解。需要远程数据关联查询的，会生成 FeignClient 调用接口，并自动完成数据组装。
- 以上所有功能，均具备极高的可配置性，并生成标准化的调用接口和数据格式。前后端即配即得，开箱即用，与细粒度的操作权限和数据过滤权限完美结合。

#### 在线表单
- 前后端代码完全交付，甚至可配置为自己的项目包名。
- 前后端代码无一丝混淆，且代码质量超高，极易学习和二次开发，同时支持单体和微服务架构。
- 可视化的拖拉拽编辑器，目前已经支持大部分常用组件。
- 目前已支持 MySQL、PostgreSQL 和 Oracle。
- 支持主从表联动。
- 支持富文本、多图、多附件的上传和下载。
- 支持主表数据、一对一从表及其字典数据的可配置性导出。
- 支持主表数据和一对多从表数据的批量删除。
- 可视化接口配置编辑器，支持多数据库、数据表、一对一表关联、一对多表关联和多种数据字典等。
- 支持字典过滤、主表字段过滤、一对一从表字段过滤，同时支持范围、模糊和等于查询方式。
- 支持主表连同一对一、一对多从表的级联插入和更新。
- 支持一对多的聚合字段计算，在主表列表中可动态计算并显示从表的聚合计算结果。
- 支持主表字段排序、一对一从表字段排序。
- 完美支持数据权限过滤和操作权限控制。
- 接口参数均为数据源 ID，不暴露任何后台数据表细节，从机制上彻底消除了 SQL 注入的风险。
- 运行时效率极高，动态表单关键数据结构均缓存 Redis，从而有效提升数据接口的应答效率。

#### 多应用
- 生成器支持多应用功能，应用和服务之间保持多对多关系，服务池功能已基本支持，后续版本会持续优化。
- 单体工程可同时创建 WebAdmin 后台应用和面向前端 App 的 WebApi 应用。
- 微服务工程可创建非常典型的单体后台 WebAdmin 应用，及面向前端 App 的 WebApi 微服务应用。补充说明，WebAdmin 后台应用也可以配置为微服务应用，并可与 WebApi 应用共享服务池中的通用业务服务。
- 对于上述介绍的 WebAdmin 后台服务，与之前版本一致，仍然提供表单和权限功能。而 WebApi 作为面向前端的接口应用，为了降低架构师们的工程裁剪工作量，该类应用将不提供表单和权限功能。

#### 多租户
- 多租户工程可同时创建三个应用，分别为租户运营管理后台应用 TenantAdmin，租户运营后台应用，以及面向租户前端 App 的 WebApi 应用。
- 对于多租户运营管理后台应用 TenantAdmin，不仅内置了自身的权限管理和租户运营管理等功能，同时也支持配置自定义的业务表单和租户统计表单。
- 支持全局公用字典和租户字典，前者由租户运营管理后台统一管理，租户字典数据可由租户管理员自行维护。为了保证整体运行时效率，两者均支持缓存 Redis。
- 租户数据支持逻辑隔离、物理隔离和混合隔离等多种方式，租户权限数据由租户运营管理服务统一管理，实时同步到多个租户运行系统数据库中。不仅非常有利于租户数据的迁移。同时也保证了运行时效率。业务逻辑代码与非多租户系统相比，差异也降至最低。
- 可配置定时任务 Job 服务，并将不同租户数据库中的业务行为数据，分组统计后刷新到租户运营管理数据库中，再由配置的统计表单进行显示。
- 可与现有的单点登录服务 (uaa) 无缝集成。

#### 技术支持
- 欢迎加入我们的技术交流 QQ 群，如遇任何使用中的问题我们都将第一时间为您答疑。群3:[![加入QQ群3](https://img.shields.io/badge/392442075-red.svg)](https://qm.qq.com/cgi-bin/qm/qr?k=peNBWGDSQxgKzvZVDtPfjvTCD6MOqTMX&jump_from=webapi)
![QQ群3](orange-qq-group.png)

### 生成后工程
>可无限制的用于学习、培训、接私活、公司自用和开发商业项目等场景，其中基础框架代码将永久免费，并持续更新。

#### 开箱即用
项目信息如您所愿，工程名称、目录结构、基础包名、common模块、代码注释中的 @author 信息等，在创建工程时即已配置，不会留有橙单的任何信息。因此无需二次修改，前后端直接编译运行即可。如编译期和运行时出现问题，那一定是我们的 bug，在得到您反馈后，我们将及时修复。

#### 开源工程部署
- 单体环境。[http://www.orangeforms.com/development-doc/edu-single/](http://www.orangeforms.com/development-doc/edu-single/)
- 微服务。[http://www.orangeforms.com/development-doc/edu-multi/](http://www.orangeforms.com/development-doc/edu-multi/)
- 多租户。[http://www.orangeforms.com/development-doc/edu-tenant/](http://www.orangeforms.com/development-doc/edu-tenant/)

#### 技术选型
- 前端: Element (Vue) / Ant Design (React) + ECharts / AntV + Axios + Webpack。
- 后端: Spring Boot / Spring Cloud / Spring Cloud Alibaba + Spring Security OAuth2 + Mybatis + Jwt。
- 数据库：MySQL + PostgreSQL + Oracle。
- 工具库: Activiti + Flowable + Hutool + Guava + Caffeine + Lombok + MapStruct + Mybatis Plus + Knife4j + qdox。
- 服务组件: Redis + Zookeeper + Nacos + Consul + XXL-Job + Quartz + Seata + Minio + Canal + RocketMQ + Kafka +  Sentinel。
- 系统监控: ELK + PinPoint / SkyWalking + Grafana + Prometheus。

#### 基础功能
- 前端框架：单页面、多标签、多栏目和子路由，多套高颜值样式主题可供选择。
- 前端能力：列表编辑、统计图表、多表联动、明细数据下钻、上传下载、导入导出、自定义打印样式模板、富文本等。
- 页面布局：支持基于 Fragment 和 Block 的灵活布局方式，通过配置即可生成多样化的表单页面，并可预览。
- 接口规范：微服务和单体服务的接口命名和参数定义规范完全一致，便于日后的平滑升级。
- 在线表单：支持可视化拖来拽编辑器，表单、后台接口和数据字典均可动态配置，完美集成数据权限过滤和操作权限控制，即配即得。
- 流程管理：功能完整且前后端全部开源。
- 后台架构：分布式锁、分布式 Id、分布式缓存、分布式事务、分布式限流和灰度发布等，按需集成。
- 用户管理：支持基于 OAuth2 的单点登录。
- 操作权限：前端控制可精确到按钮级的操作和标签级的显示，同时提供了多维度的权限分配路径查询能力。
- 数据权限：基于 Mybatis 拦截器 + JSqlParser 的实现方式，配置更灵活，代码侵入性更低。
- 租户管理：租户权限管理数据、字典等通用数据，均由租户运营管理服务统一管理，并实时同步到多个租户运营库，具有极高的数据库级别容错性。
- 多数据源：支持简单和复杂两种多数据源注解。复杂注解可灵活自定义，并应对复杂的业务场景。
- 数据组装：Java 注解方式实现数据组装，支持统一接口的**服务内和跨服务**的一对一、一对多、多对多、字典、聚合计算等关系数据组合。
- 定时任务：我们不仅提供了多套 Job 基础框架的集成，更能生成灵活可配、高度优化、便于二次开发的 Job 业务逻辑代码。
- 系统监控：基于 Kafka + ELK 的日志收集，基于 PinPoint/SkyWalking 的服务链路跟踪，基于 GPE 的服务性能指标监控。
- 接口文档：目前已集成 Knife4j，同时支持基于 qdox 逆推 Java 工程代码，实现 0 注解导出 Postman 接口文件和 Markdown 文档。
- 操作日志：灵活可配置。统一拦截每次请求调用的输入输出，及各种调用数据细节，以便于后期的统计分析和问题定位。微服务工程由 Kafka 消费者服务统一批量处理，并与 ELK + SkyWalking/PinPoint 等日志监控系统完全打通。单体工程则异步的存入数据库表中。
- 缓存同步：同时集成了 Canal-Admin 和 Canal-Server，以便于用户的 Canal Instance 管理。目前已实现字典变化数据到 Redis 的实时缓存同步。

#### 代码质量
此为在线演示工程的代码审查报告，而非当前开源示例工程。前者代码量更大，结构更复杂。
1. SonarQube 扫描
- 基于 SonarQube8.2 缺省最严格的代码扫描规则，其中代码复杂度要求为 15。
- 有气味代码共90处，其中85处为DTO、Model、常量字典、RPC接口等定义出现重复名称所致，均与模拟实际业务有关。
- 其余气味代码，是在权衡性能、可读性和易修改性等因素后保留下来的，具体见图4。
<table>
<tr>
  <td><img src="https://images.gitee.com/uploads/images/2020/0504/132431_a28ba412_7431510.png"/></td>
  <td><img src="https://images.gitee.com/uploads/images/2020/0504/133330_6a7564a1_7431510.png"/></td>
</tr>
</table>
<table>
<tr>
  <td><img src="https://images.gitee.com/uploads/images/2020/0504/141440_c5b8e3c1_7431510.png"/></td>
  <td><img src="https://images.gitee.com/uploads/images/2020/0504/141124_df278683_7431510.png"/></td>
</tr>
</table>

2. Alibaba Code Guide 扫描
- 下载最新版本IDEA插件，同时打开所有审查条件。
- 全部代码扫描通过。
<table>
<tr>
  <td><img src="https://images.gitee.com/uploads/images/2020/0504/134052_c3196376_7431510.png"/></td>
  <td><img src="https://images.gitee.com/uploads/images/2020/0904/092352_3fa8f2e7_7431510.png"/></td>
</tr>
</table>

3. Statistic 代码统计
- 生成代码总量约为86000多行，主要包括Java、XML、YAML和SQL初始化脚本等。
- Java代码覆盖率为37%。
<table>
<tr>
  <td><img src="https://images.gitee.com/uploads/images/2020/0904/092417_b280457b_7431510.png"/></td>
  <td><img src="https://images.gitee.com/uploads/images/2020/0904/092434_f718c982_7431510.png"/></td>
</tr>
</table>

### 价值理念
都看到这里了，如果看的仔细，至少要花 20 分钟，非常感谢您的坚持和耐心，希望能再花 1 秒钟的时间在下面 star 一下。