/*
 * Copyright 2012-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.boot.autoconfigure.condition;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.boot.autoconfigure.AutoConfigurationImportEvent;
import org.springframework.boot.autoconfigure.AutoConfigurationImportListener;

/**
 * {@link AutoConfigurationImportListener} to record results with the
 * {@link ConditionEvaluationReport}.
 *
 * @author Phillip Webb
 */
// 20201215 {@link AutoConfigurationImportListener}可以使用{@link ConditionEvaluationReport}记录结果。
class ConditionEvaluationReportAutoConfigurationImportListener implements AutoConfigurationImportListener, BeanFactoryAware {

	private ConfigurableListableBeanFactory beanFactory;

	// 20201215 处理自动配置导入事件 -> 记录候选者和排除项的类的名称
	@Override
	public void onAutoConfigurationImportEvent(AutoConfigurationImportEvent event) {
		if (this.beanFactory != null) {
			ConditionEvaluationReport report = ConditionEvaluationReport.get(this.beanFactory);

			// 20201215 记录作为条件评估候选者的类的名称。
			report.recordEvaluationCandidates(event.getCandidateConfigurations());

			// 20201215 记录已从条件评估中排除的类的名称。
			report.recordExclusions(event.getExclusions());
		}
	}

	@Override
	public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
		this.beanFactory = (beanFactory instanceof ConfigurableListableBeanFactory)
				? (ConfigurableListableBeanFactory) beanFactory : null;
	}

}
