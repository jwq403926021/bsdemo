### 新版功能
> 以下为 2.6 版本的最新功能列表。

- 报表打印，新增线上演示环境 [http://demo.orangeforms.com/flow](http://demo.orangeforms.com/flow)(集中访问会相对较慢)。
- 报表打印，统计报表新增支持透视表组件。
- 报表打印，打印模板新增支持边框颜色。
- 报表打印，打印模板新增支持表数据中条形码打印。
- 报表打印，支持带有动态参数的SQL数据集，其余基础功能和Table数据集完全一致。
- 报表打印，报表拖拽页面支持可嵌套的块组件，使页面布局更加灵活。
- 报表打印，数据集支持数据权限。
- 基础架构，数据权限支持精确到菜单，同时支持菜单Id和后台接口的关联推演，防止手动篡改 MenuId 的数据越权访问。
- 基础架构，数据权限性能优化，在原有二级缓存基础上支持基于 Caffeine 的一级缓存，以提升该高频调用的性能。
- 基础架构，全局编码字典支持 show_order。
- 基础架构，优化部门层级变更的逻辑，优化后性能与部门层级深度近乎无关。
- 工作流，流程用户任务支持自动邮件通知的设置，同时提供良好的插件接口，可按需实现自定义通知。
- 工作流，支持流程干预，可以指定跳转的节点，以及跳转后的新审批人。
- 工作流，支持审批人直接终止当前审批流程，如：结束(同意)和结束(拒绝)，审批状态自动同步到工单表。
- 工作流，在线表单工作流，支持流程状态同步到业务主表，以便于业务数据的统计分析。
- 生成器，支持仅生成工程脚手架的功能，该功能会忽略生成业务代码，方便用户进行原始框架的快速搭建。

### 最新通知

- 橙单单体和微服务企业版优惠券，在 2.6 版本发布前仍然可用，目前单体 109、微服务 309，即可获得最新版工作流和在线表单的全部源码。
- 橙单报表及打印模块，第一版售价 400 元，仅为最终定价的 1/8，后续无需加价。全部源码交付，项目数量无限制，永久免费升级，
- 橙单多租户企业版优惠券已取消。

### 选择橙单
 **_橙单，具备极好代码生成能力的低代码工具_**。生成后工程全部源码交付，所有代码文件无任何橙单痕迹。就如同贵公司最优秀的技术团队亲手打造而得。欢迎加入我们的技术支持QQ群3 (**_392442075_**)，您的技术问题我们会尽快给出回复。

- **承诺**，全网最低价和最宽松的商业版授权。让每一位开发者和每一个技术团队，都可拥有一套全部源码可控、商业授权无限制且功能完整的基础架构。
- **理念**，强大的代码生成能力，75% 以上的业务代码无需手写，即便面对极为复杂的功能需求，在高质量的代码之上进行二次开发，也能快速搞定。
- **功能**，工作流、在线表单、统计报表、自定义打印、多租户、用户权限、高级数据过滤权限、全类型数据字典等，全部深度支持，绝非简单集成。
- **架构**，拥抱云原生的架构设计理念，接口可正交化组合，业务服务自动组装各种关联数据 (含跨服务调用)，让微服务下的服务拆分和库表拆分更加得心应手，彻底释放系统的弹性扩充能力。
- **代码**，顶级的代码强度，全覆盖的关联数据合法性验证，数据可见性防越权验证，完整规范且可追溯的日志体系，无限层级的树形数据高效读写。
- **支持**，30 万字的线上免费文档和技术专栏，助您快速吃透橙单。活跃的微信群技术支持，就连开车等灯时都在答疑。向日葵、TeamViewer 和 ToDesk，必要时可为您提供远程协助服务。

### 技术专栏
> **授人以鱼不如授人以渔**。我们的专栏，是基于橙单低代码的基础架构，并对其进行了深入浅出和鞭辟入里的分析。专栏中的每一行代码，每一段注释，每一个技术点和每一步的思考过程，都是产品级强度的。请坚信 **吃透橙单、收入翻番、效率乘三**。

- 用户权限操作。[http://www.orangeforms.com/special-column/](http://www.orangeforms.com/special-column/)
- 权限模块设计。[http://www.orangeforms.com/special-column/perm/](http://www.orangeforms.com/special-column/perm/)
- 部门组织结构。[http://www.orangeforms.com/special-column/dept/](http://www.orangeforms.com/special-column/dept/)
- 数据权限设计。[http://www.orangeforms.com/special-column/data-perm/](http://www.orangeforms.com/special-column/data-perm/)
- 多表关联接口。[http://www.orangeforms.com/special-column/form-api/](http://www.orangeforms.com/special-column/form-api/)
- 多表关联注解。[http://www.orangeforms.com/special-column/data-relation/](http://www.orangeforms.com/special-column/data-relation/)
- 数据字典详解。[http://www.orangeforms.com/special-column/dict/](http://www.orangeforms.com/special-column/dict/)
- 实时数据同步。[http://www.orangeforms.com/special-column/data-sync/](http://www.orangeforms.com/special-column/data-sync/)
- 批量导入优化。[http://www.orangeforms.com/special-column/import/](http://www.orangeforms.com/special-column/import/)
- 安全上传下载。[http://www.orangeforms.com/special-column/upload-download/](http://www.orangeforms.com/special-column/upload-download/)
- 租户混合隔离。[http://www.orangeforms.com/special-column/tenant-mix-arch/](http://www.orangeforms.com/special-column/tenant-mix-arch/)
- 租户权限管理。[http://www.orangeforms.com/special-column/tenant-perm/](http://www.orangeforms.com/special-column/tenant-perm/)
- 租户编码字典。[http://www.orangeforms.com/special-column/tenant-global-dict/](http://www.orangeforms.com/special-column/tenant-global-dict/)
- 租户数据同步。[http://www.orangeforms.com/special-column/tenant-datasync/](http://www.orangeforms.com/special-column/tenant-datasync/)
- 更多专栏文章 ... ...

### 在线演示
- 网站首页。[http://www.orangeforms.com](http://www.orangeforms.com)
- 演示配置。[http://config.orangeforms.com](http://config.orangeforms.com)
- 环境搭建启动。[http://www.orangeforms.com/development-doc/edu-single/](http://www.orangeforms.com/development-doc/edu-single/)
- 后端代码文档。[http://www.orangeforms.com/development-doc/](http://www.orangeforms.com/development-doc/)
- 前端代码文档。[http://www.orangeforms.com/development-vue/](http://www.orangeforms.com/development-vue/)
- 生成器操作指南。[http://www.orangeforms.com/orange-doc/](http://www.orangeforms.com/orange-doc/)
- 完整演示项目 (不含工作流)。[http://demo.orangeforms.com](http://demo.orangeforms.com)
- 统计打印、在线表单和工作流演示项目。[http://demo.orangeforms.com/flow](http://demo.orangeforms.com/flow)

### 功能介绍

#### 工作流
- **优势能力**，成熟的工作流产品价格昂贵，普通脚手架所集成的工作流模块功能有限，而橙单通过支持 **静态路由表单工作流** 的方式，有效的弥补了这一尴尬。
- **代码生成**，支持静态路由表单 + 工作流的代码生成。用户可在生成器配置表单和流程，并生成该流程的完整前后端业务代码。对于有些复杂的业务需求，可通过便利的二次开发搞定。
- **在线表单**，集成工作流和在线表单，无需写一行代码即可配置出相对复杂的工作流业务。
- **工单管理**，支持完整的工作流工单管理，可为不同流程配置独立的工单菜单和管理页面，并与现有的数据过滤权限完美整合。
- **用户选择**，支持基于角色、部门、岗位、本部门岗位、上级部门岗位、同级部门岗位、指定部门岗位、本部门领导岗位、上级部门领导岗位等候选组。
- **基础功能**，支持工单号的自定义编码、自定义流程节点状态、工单撤销和终止、流程干预、会签加签减签、待办已办转办、催办、抄送和传阅、保存草稿、撤回和驳回、待办任务自动通知、历史任务、审批详情列表、包含候选组的审批人列表、附件上传下载等。前端已集成美观的流程编辑器，同时支持流程图高亮及任务跟踪。
- **业务数据**，工作流支持主表及一对一、一对多、多对多从表数据的级联增删改查。同一表单可多次提交，新增数据和修改数据后均可提交审批流程。
- **数据安全**，严格且合理的数据安全校验，流程敏感数据 (如合同附件) 不会被越权访问和下载。同时还支持审批中数据与最终审批完成数据的分离，避免造成对其他业务关联表的数据污染，审批完成后可自动执行业务数据的同步。

#### 静态表单
> 以下所有功能的前后端代码，均可通过橙单的低代码工具配置后生成。标准化接口和数据结构，可轻松实现正交化组合。

- 前后端支持单表增删改查、字典列表、分组聚合查询、部分更新、批量删除、导入导出、上传下载。
- 批量导入支持常量字典和数据表字典的反向翻译，比如文件存储的是“一年级”字符串，导入过程中会批量替换为对应的Id后插入数据表。
- 前后端支持主表与一对一、一对多、多对多关联表的关联查询，分组聚合查询、虚拟字段的聚合计算，数据导出、级联添加、级联更新、级联删除。
- 前后端支持主表与多对多中间表的批量插入、批量删除、单条删除、关联列表数据查询、未关联列表数据查询。
- 前后端支持主表与字典表数据、一对一、一对多和多对多与字典表数据的关联查询。
- 后台接口支持主表、一对一从表、一对多从表、多对多从表关联数据的单条和批量验证，会根据配置生成本地服务或跨服务远程调用的高性能数据验证。
- 以上所有功能，均支持分布式跨库跨服务操作，接口保持不变，需要分布式事务的场景，会自动生成 Seata 相关的代码注解。需要远程数据关联查询的，会生成 FeignClient 调用接口，并自动完成数据组装。

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

#### 多租户
- 多租户工程可同时创建三个应用，分别为租户运营管理后台应用 TenantAdmin，租户运营后台应用，以及面向租户前端 App 的 WebApi 应用。
- 对于多租户运营管理后台应用 TenantAdmin，不仅内置了自身的权限管理和租户运营管理等功能，同时也支持配置自定义的业务表单和租户统计表单。
- 支持全局公用字典和租户字典，前者由租户运营管理后台统一管理，租户字典数据可由租户管理员自行维护。为了保证整体运行时效率，两者均支持缓存 Redis。
- 租户数据支持逻辑隔离、物理隔离和混合隔离等多种方式，租户权限数据由租户运营管理服务统一管理，实时同步到多个租户运行系统数据库中。不仅非常有利于租户数据的迁移。同时也保证了运行时效率。业务逻辑代码与非多租户系统相比，差异也降至最低。
- 可配置定时任务 Job 服务，并将不同租户数据库中的业务行为数据，分组统计后刷新到租户运营管理数据库中，再由配置的统计表单进行显示。
- 可与现有的单点登录服务 (uaa) 无缝集成。

### 基础架构
欢迎加入我们的技术交流 QQ 群，如遇任何使用中的问题我们都将第一时间为您答疑。群3:[![加入QQ群3](https://img.shields.io/badge/392442075-red.svg)](https://qm.qq.com/cgi-bin/qm/qr?k=peNBWGDSQxgKzvZVDtPfjvTCD6MOqTMX&jump_from=webapi)
![QQ群3](orange-qq-group.png)

#### 开箱即用
项目信息如您所愿，工程名称、目录结构、基础包名、common模块、代码注释中的 @author 信息等，在创建工程时即已配置，不会留有橙单的任何信息。因此无需二次修改，前后端直接编译运行即可。如编译期和运行时出现问题，那一定是我们的 bug，在得到您反馈后，我们将及时修复。

#### 项目部署
- 单体环境。[http://www.orangeforms.com/development-doc/edu-single/](http://www.orangeforms.com/development-doc/edu-single/)
- 微服务。[http://www.orangeforms.com/development-doc/edu-multi/](http://www.orangeforms.com/development-doc/edu-multi/)
- 多租户。[http://www.orangeforms.com/development-doc/edu-tenant/](http://www.orangeforms.com/development-doc/edu-tenant/)

#### 技术选型
- 前端: Element (Vue) / Luckysheet / ECharts / AntV + Axios + Webpack。
- 后端: Spring Boot / Spring Cloud / Spring Cloud Alibaba + Spring Security OAuth2 + Mybatis + Jwt。
- 数据库：MySQL + PostgreSQL + Oracle。
- 工具库: Flowable + Hutool + Guava + Caffeine + Lombok + MapStruct + Mybatis Plus + Knife4j + x-easypdf。
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