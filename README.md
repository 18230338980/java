# 原文
https://github.com/baomidou/dynamic-datasource-spring-boot-starter


<p align="center">
<img src="https://images.gitee.com/uploads/images/2019/0626/231046_f44892b9_709883.png" border="0" />

</p>

<p align="center">
	<strong>一个基于springboot的快速集成多数据源的启动器</strong>
</p>

# 简介

dynamic-datasource 是一个基于springboot的快速集成多数据源的启动器。

其支持 **Jdk 1.8+,    SpringBoot 1.4.x  1.5.x   2.0.x**。

# 特性

1. 数据源分组，适用于多种场景 纯粹多库  读写分离  一主多从  混合模式。
2. 内置敏感参数加密和启动初始化表结构schema数据库database。
3. 提供对Druid，Mybatis-Plus，P6sy，Jndi的快速集成。
4. 简化Druid和HikariCp配置，提供全局参数配置。
5. 提供自定义数据源来源接口(默认使用yml或properties配置)。
6. 提供项目启动后增减数据源方案。
7. 提供Mybatis环境下的  **纯读写分离** 方案。
9. 提供多层数据源嵌套切换。（ServiceA >>>  ServiceB >>> ServiceC，每个Service都是不同的数据源）（有问题）
11. **基于seata的分布式事务支持。**

# 约定

1. 本框架只做 **切换数据源** 这件核心的事情，并**不限制你的具体操作**，切换了数据源可以做任何CRUD。
2. 配置文件所有以下划线 `_` 分割的数据源 **首部** 即为组的名称，相同组名称的数据源会放在一个组下。
3. 切换数据源可以是组名，也可以是具体数据源名称。组名则切换时采用负载均衡算法切换。
4. 默认的数据源名称为  **master** ，你可以通过 `spring.datasource.dynamic.primary` 修改。
5. 方法上的注解优先于类上注解。

# 使用方法

1. 使用  **@DS**  切换数据源。

**@DS** 可以注解在方法上和类上，**同时存在方法注解优先于类上注解**。

强烈建议只注解在service实现上。

|     注解      |                   结果                   |
| :-----------: | :--------------------------------------: |
|    没有@DS    |                默认数据源                |
| @DS("dsName") | dsName可以为组名也可以为具体某个库的名称 |

```java
@Service
@DS("slave")
public class UserServiceImpl implements UserService {

  @Autowired
  private JdbcTemplate jdbcTemplate;

  public List<Map<String, Object>> selectAll() {
    return  jdbcTemplate.queryForList("select * from user");
  }
  
  @Override
  @DS("slave_1")
  public List<Map<String, Object>> selectByCondition() {
    return  jdbcTemplate.queryForList("select * from user where age >10");
  }
}
```