/**
 * Copyright © 2018 organization baomidou
 * <pre>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * <pre/>
 */
package com.steam.datasource.spring.boot.autoconfigure;

import com.steam.datasource.rout.DynamicRoutingDataSource;
import com.steam.datasource.aop.DynamicDataSourceAnnotationAdvisor;
import com.steam.datasource.aop.DynamicDataSourceAnnotationInterceptor;
import com.steam.datasource.provider.DynamicDataSourceProvider;
import com.steam.datasource.provider.YmlDynamicDataSourceProvider;
import com.steam.datasource.spring.boot.autoconfigure.druid.DruidDynamicDataSourceConfiguration;
import com.steam.datasource.strategy.DynamicDataSourceStrategy;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import javax.sql.DataSource;
import java.util.Map;

/**
 * 动态数据源核心自动配置类
 *
 * @author TaoYu Kanyuxia
 * @see DynamicDataSourceProvider
 * @see DynamicDataSourceStrategy
 * @see DynamicRoutingDataSource
 * @since 1.0.0
 */
@Slf4j
@Configuration
@AllArgsConstructor
@EnableConfigurationProperties(DynamicDataSourceProperties.class)
// 在spring-jdbc的自动配置之前先定义好datasource
@AutoConfigureBefore(DataSourceAutoConfiguration.class)
// 引入druid的自动配置类
@Import(value = {DruidDynamicDataSourceConfiguration.class, DynamicDataSourceCreatorAutoConfiguration.class})
@ConditionalOnProperty(prefix = DynamicDataSourceProperties.PREFIX, name = "enabled", havingValue = "true", matchIfMissing = true)
public class DynamicDataSourceAutoConfiguration {

  private final DynamicDataSourceProperties properties;

  /**
   * 用于生成一个“库名->数据源”的map
   * @return
   */
  @Bean
  @ConditionalOnMissingBean
  public DynamicDataSourceProvider dynamicDataSourceProvider() {
    Map<String, DataSourceProperty> datasourceMap = properties.getDatasource();
    return new YmlDynamicDataSourceProvider(datasourceMap);
  }

  @Bean
  @ConditionalOnMissingBean
  public DynamicDataSourceStrategy dynamicDataSourceStrategy() throws Exception {
    return properties.getStrategy().newInstance();
  }

  /**
   * 定义DataSource的Bean，DynamicRoutingDataSource利用DynamicDataSourceProvider生成了“库名->数据源”的map，DynamicDataSourceStrategy默认为LoadBalanceDynamicDataSourceStrategy
   * 在获取真实数据源的时候，再根据ThreadLocal里的变量，决定选取map中的那个datasource
   * 注意properties.getPrimary()，这个默认是master，即默认走主库
   *
   * @param dynamicDataSourceProvider
   * @param dynamicDataSourceStrategy
   * @return
   */
  @Bean
  @ConditionalOnMissingBean
  public DataSource dataSource(DynamicDataSourceProvider dynamicDataSourceProvider, DynamicDataSourceStrategy dynamicDataSourceStrategy) {
    DynamicRoutingDataSource dataSource = new DynamicRoutingDataSource();
    dataSource.setPrimary(properties.getPrimary());
    dataSource.setStrict(properties.getStrict());
    dataSource.setStrategy(dynamicDataSourceStrategy);
    dataSource.setProvider(dynamicDataSourceProvider);
    dataSource.setP6spy(properties.getP6spy());
    dataSource.setSeata(properties.getSeata());
    return dataSource;
  }

  /**
   * @Ds 注解的advisor，只要在类或者方法上，增加了@Ds注解，就会被拦截：
   * 在方法执行前根据@Ds的value，往ThreadLocal设置要访问的数据源；
   * 在方法执行结束后，清除ThreadLocal中的值。
   * @return
   */
  @Bean
  @ConditionalOnMissingBean
  public DynamicDataSourceAnnotationAdvisor dynamicDatasourceAnnotationAdvisor() {
    DynamicDataSourceAnnotationInterceptor interceptor = new DynamicDataSourceAnnotationInterceptor();
    DynamicDataSourceAnnotationAdvisor advisor = new DynamicDataSourceAnnotationAdvisor(interceptor);
    advisor.setOrder(properties.getOrder());
    return advisor;
  }
}
