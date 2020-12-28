/*
* Licensed to the Apache Software Foundation (ASF) under one or more
* contributor license agreements.  See the NOTICE file distributed with
* this work for additional information regarding copyright ownership.
* The ASF licenses this file to You under the Apache License, Version 2.0
* (the "License"); you may not use this file except in compliance with
* the License.  You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package javax.servlet;

import java.util.Collection;
import java.util.Set;

/**
 * 20201228
 * A. 可以通过其进一步配置Servlet的接口。
 */
/**
 * A.
 * Interface through which a Servlet may be further configured.
 *
 * @since Servlet 3.0
 */
// 20201228 可以通过其进一步配置Servlet的接口。
public interface ServletRegistration extends Registration {

    /**
     * 20201228
     * A. 为该ServletRegistration表示的Servlet添加具有给定URL模式的Servlet映射。 如果任何指定的URL模式已经映射到其他Servlet，则不会执行任何更新。
     * B. 如果多次调用此方法，则每个后续调用都会增加前者的效果。 返回的集不受ServletRegistration对象的支持，因此返回集的更改不会反映在ServletRegistration对象中，反之亦然。
     */
    /**
     * A.
     * Adds a servlet mapping with the given URL patterns for the Servlet
     * represented by this ServletRegistration. If any of the specified URL
     * patterns are already mapped to a different Servlet, no updates will
     * be performed.
     *
     * B.
     * If this method is called multiple times, each successive call adds to
     * the effects of the former. The returned set is not backed by the
     * ServletRegistration object, so changes in the returned set are not
     * reflected in the ServletRegistration object, and vice-versa.
     *
     * @param urlPatterns The URL patterns that this Servlet should be mapped to
     * @return the (possibly empty) Set of URL patterns that are already mapped
     * to a different Servlet
     * @throws IllegalArgumentException if urlPattern is null or empty
     * @throws IllegalStateException if the associated ServletContext has
     *                                  already been initialised
     */
    // 20201228 为该ServletRegistration表示的Servlet添加具有给定URL模式的Servlet映射。 如果任何指定的URL模式已经映射到其他Servlet，则不会执行任何更新
    public Set<String> addMapping(String... urlPatterns);

    /**
     * Gets the currently available mappings of the Servlet represented by this
     * ServletRegistration.
     *
     * If permitted, any changes to the returned Collection must not affect this
     * ServletRegistration.
     *
     * @return a (possibly empty) Collection of the currently available mappings
     * of the Servlet represented by this ServletRegistration
     */
    public Collection<String> getMappings();

    public String getRunAsRole();

    /**
     * Interface through which a Servlet registered via one of the addServlet
     * methods on ServletContext may be further configured.
     */
    // 20201228 可以通过接口进一步配置通过ServletContext上的addServlet方法之一注册的Servlet。
    public static interface Dynamic extends ServletRegistration, Registration.Dynamic {
        public void setLoadOnStartup(int loadOnStartup);
        public Set<String> setServletSecurity(ServletSecurityElement constraint);
        public void setMultipartConfig(MultipartConfigElement multipartConfig);
        public void setRunAsRole(String roleName);
    }
}
