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
package com.steam.datasource.aop;

import com.steam.datasource.annotation.DS;
import com.steam.datasource.support.DataSourceClassResolver;
import com.steam.datasource.toolkit.DynamicDataSourceContextHolder;
import lombok.Setter;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.core.annotation.AnnotationUtils;

import java.lang.reflect.Method;

/**
 * Core Interceptor of Dynamic Datasource
 *
 * @author TaoYu
 * @since 1.2.0
 */
public class DynamicDataSourceAnnotationInterceptor implements MethodInterceptor {

  private static final DataSourceClassResolver RESOLVER = new DataSourceClassResolver();

  @Override
  public Object invoke(MethodInvocation invocation) throws Throwable {
    try {
      DynamicDataSourceContextHolder.push(determineDatasource(invocation));
      return invocation.proceed();
    } finally {
      DynamicDataSourceContextHolder.poll();
    }
  }

  private String determineDatasource(MethodInvocation invocation) throws Throwable {
    Method method = invocation.getMethod();
    DS ds = method.isAnnotationPresent(DS.class) ? method.getAnnotation(DS.class)
        : AnnotationUtils.findAnnotation(RESOLVER.targetClass(invocation), DS.class);
    return ds.value();
  }
}