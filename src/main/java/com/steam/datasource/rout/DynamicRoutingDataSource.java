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
package com.steam.datasource.rout;

import com.p6spy.engine.spy.P6DataSource;
import com.steam.datasource.provider.DynamicDataSourceProvider;
import com.steam.datasource.strategy.DynamicDataSourceStrategy;
import com.steam.datasource.toolkit.DynamicDataSourceContextHolder;
import io.seata.rm.datasource.DataSourceProxy;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.StringUtils;

import javax.sql.DataSource;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 核心动态数据源组件
 *
 * @author TaoYu Kanyuxia
 * @since 1.0.0
 */
@Slf4j
public class DynamicRoutingDataSource extends AbstractRoutingDataSource implements InitializingBean, DisposableBean {
    /**
     * 数据源名称包含下划线，则会把下划线分割的第一部分作为数据源组名
     */
    private static final String UNDERLINE = "_";
    /**
     * 用于生成真实数据源的map
     */
    @Setter
    private DynamicDataSourceProvider provider;
    /**
     * 当同一个分组有多个数据源时，采用的负载均衡算法，目前支持轮询和随机访问两种，分别是LoadBalanceDynamicDataSourceStrategy和RandomDynamicDataSourceStrategy类
     */
    @Setter
    private DynamicDataSourceStrategy strategy;
    /**
     * 默认数据源的名称或分组名称
     */
    @Setter
    private String primary;
    /**
     * 是否保持粘性，即访问了某个数据源，接下来就一直访问那个数据源
     */
    @Setter
    private boolean strict;
    /**
     * 监控的Spring boot数据库操作
     */
    private boolean p6spy;
    /**
     * 分布式事务服务
     */
    private boolean seata;
    /**
     * 所有数据库
     */
    private Map<String, DataSource> dataSourceMap = new LinkedHashMap<>();
    /**
     * 分组数据库
     */
    private Map<String, DynamicGroupDataSource> groupDataSources = new ConcurrentHashMap<>();

    /**
     * 该类继承了spring的AbstractRoutingDataSource类，所以需要实现它的抽象方法，选择数据源。这里需要关注的是DynamicDataSourceContextHolder类
     *
     * @return
     */
    @Override
    public DataSource determineDataSource() {
        return getDataSource(DynamicDataSourceContextHolder.peek());
    }

    /**
     * 没有指定数据源的时候，就使用默认数据源
     *
     * @return
     */
    private DataSource determinePrimaryDataSource() {
        log.debug("dynamic-datasource switch to the primary datasource");
        return groupDataSources.containsKey(primary) ? groupDataSources.get(primary).determineDataSource() : dataSourceMap.get(primary);
    }

    /**
     * 获取当前所有的数据源
     *
     * @return 当前所有数据源
     */
    public Map<String, DataSource> getCurrentDataSources() {
        return dataSourceMap;
    }

    /**
     * 获取的当前所有的分组数据源
     *
     * @return 当前所有的分组数据源
     */
    public Map<String, DynamicGroupDataSource> getCurrentGroupDataSources() {
        return groupDataSources;
    }

    /**
     * 获取数据源
     *
     * @param ds 数据源名称
     * @return 数据源
     */
    public DataSource getDataSource(String ds) {
        if (StringUtils.isEmpty(ds)) {
            return determinePrimaryDataSource();
        } else if (!groupDataSources.isEmpty() && groupDataSources.containsKey(ds)) {
            log.debug("dynamic-datasource switch to the datasource named [{}]", ds);
            return groupDataSources.get(ds).determineDataSource();
        } else if (dataSourceMap.containsKey(ds)) {
            log.debug("dynamic-datasource switch to the datasource named [{}]", ds);
            return dataSourceMap.get(ds);
        }
        if (strict) {
            throw new RuntimeException("dynamic-datasource could not find a datasource named" + ds);
        }
        return determinePrimaryDataSource();
    }

    /**
     * 添加数据源
     *
     * @param ds         数据源名称
     * @param dataSource 数据源
     */
    public synchronized void addDataSource(String ds, DataSource dataSource) {
        if (!dataSourceMap.containsKey(ds)) {
            dataSource = wrapDataSource(ds, dataSource);
            dataSourceMap.put(ds, dataSource);
            this.addGroupDataSource(ds, dataSource);
            log.info("dynamic-datasource - load a datasource named [{}] success", ds);
        } else {
            log.warn("dynamic-datasource - load a datasource named [{}] failed, because it already exist", ds);
        }
    }

    private DataSource wrapDataSource(String ds, DataSource dataSource) {
        if (p6spy) {
            dataSource = new P6DataSource(dataSource);
            log.info("dynamic-datasource [{}] wrap p6spy plugin", ds);
        }
        if (seata) {
            dataSource = new DataSourceProxy(dataSource);
            log.info("dynamic-datasource [{}] wrap seata plugin", ds);
        }
        return dataSource;
    }

    private void addGroupDataSource(String ds, DataSource dataSource) {
        /**
         * 数据源名称包含下划线，就获取分组名称，设置到DynamicGroupDataSource中去
         */
        if (ds.contains(UNDERLINE)) {
            String group = ds.split(UNDERLINE)[0];
            if (groupDataSources.containsKey(group)) {
                groupDataSources.get(group).addDatasource(dataSource);
            } else {
                try {
                    DynamicGroupDataSource groupDatasource = new DynamicGroupDataSource(group, strategy);
                    groupDatasource.addDatasource(dataSource);
                    groupDataSources.put(group, groupDatasource);
                } catch (Exception e) {
                    log.error("dynamic-datasource - add the datasource named [{}] error", ds, e);
                    dataSourceMap.remove(ds);
                }
            }
        }
    }

    /**
     * 删除数据源
     *
     * @param ds 数据源名称
     */
    public synchronized void removeDataSource(String ds) {
        if (!StringUtils.hasText(ds)) {
            throw new RuntimeException("remove parameter could not be empty");
        }
        if (primary.equals(ds)) {
            throw new RuntimeException("could not remove primary datasource");
        }
        if (dataSourceMap.containsKey(ds)) {
            DataSource dataSource = dataSourceMap.get(ds);
            try {
                closeDataSource(ds, dataSource);
            } catch (Exception e) {
                throw new RuntimeException("dynamic-datasource - remove the database named " + ds + " failed", e);
            }
            dataSourceMap.remove(ds);
            if (ds.contains(UNDERLINE)) {
                String group = ds.split(UNDERLINE)[0];
                if (groupDataSources.containsKey(group)) {
                    groupDataSources.get(group).removeDatasource(dataSource);
                }
            }
            log.info("dynamic-datasource - remove the database named [{}] success", ds);
        } else {
            log.warn("dynamic-datasource - could not find a database named [{}]", ds);
        }
    }

    public void setP6spy(boolean p6spy) {
        if (p6spy) {
            try {
                Class.forName("com.p6spy.engine.spy.P6DataSource");
                log.info("dynamic-datasource detect P6SPY plugin and enabled it");
                this.p6spy = true;
            } catch (Exception e) {
                log.warn("dynamic-datasource enabled P6SPY ,however without p6spy dependency");
            }
        } else {
            this.p6spy = false;
        }
    }

    public void setSeata(boolean seata) {
        if (seata) {
            try {
                Class.forName("io.seata.rm.datasource.DataSourceProxy");
                this.seata = true;
                log.info("dynamic-datasource detect ALIBABA SEATA and enabled it");
            } catch (Exception e) {
                this.seata = false;
                log.warn("dynamic-datasource enabled ALIBABA SEATA  ,however without seata dependency");
            }
        }
    }

    /**
     * 销毁bean的时候，需要调用所有真实数据源的close方法，关闭数据源
     *
     * @throws Exception
     */
    @Override
    public void destroy() throws Exception {
        log.info("dynamic-datasource start closing ....");
        for (Map.Entry<String, DataSource> item : dataSourceMap.entrySet()) {
            closeDataSource(item.getKey(), item.getValue());
        }
        log.info("dynamic-datasource all closed success,bye");
    }

    private void closeDataSource(String name, DataSource dataSource)
            throws NoSuchFieldException, IllegalAccessException, InvocationTargetException {
        if (seata) {
            DataSourceProxy dataSourceProxy = (DataSourceProxy) dataSource;
            dataSource = dataSourceProxy.getTargetDataSource();
        }
        if (p6spy) {
            Field realDataSourceField = P6DataSource.class.getDeclaredField("realDataSource");
            realDataSourceField.setAccessible(true);
            dataSource = (DataSource) realDataSourceField.get(dataSource);
        }
        Class<? extends DataSource> clazz = dataSource.getClass();
        try {
            Method closeMethod = clazz.getDeclaredMethod("close");
            closeMethod.invoke(dataSource);
        } catch (NoSuchMethodException e) {
            log.warn("dynamic-datasource close the datasource named [{}] failed,", name);
        }
    }

    /**
     * 完成初始化之后，需要对默认数据源做一个校验，如果不包含默认数据源，则直接报错
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        Map<String, DataSource> dataSources = provider.loadDataSources();
        // 添加并分组数据源
        for (Map.Entry<String, DataSource> dsItem : dataSources.entrySet()) {
            addDataSource(dsItem.getKey(), dsItem.getValue());
        }
        // 检测默认数据源设置
        if (groupDataSources.containsKey(primary)) {
            log.info("dynamic-datasource initial loaded [{}] datasource,primary group datasource named [{}]", dataSources.size(), primary);
        } else if (dataSourceMap.containsKey(primary)) {
            log.info("dynamic-datasource initial loaded [{}] datasource,primary datasource named [{}]", dataSources.size(), primary);
        } else {
            throw new RuntimeException("dynamic-datasource Please check the setting of primary");
        }
    }

}