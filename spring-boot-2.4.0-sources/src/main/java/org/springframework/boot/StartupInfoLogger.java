/*
 * Copyright 2012-2020 the original author or authors.
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

package org.springframework.boot;

import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.util.concurrent.Callable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.boot.system.ApplicationHome;
import org.springframework.boot.system.ApplicationPid;
import org.springframework.context.ApplicationContext;
import org.springframework.core.log.LogMessage;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.StopWatch;
import org.springframework.util.StringUtils;

/**
 * Logs application information on startup.
 *
 * @author Phillip Webb
 * @author Dave Syer
 */
// 20201208 在启动时记录应用程序信息。
class StartupInfoLogger {

	private static final Log logger = LogFactory.getLog(StartupInfoLogger.class);

	private static final long HOST_NAME_RESOLVE_THRESHOLD = 200;

	// 20201208 记录源Class
	private final Class<?> sourceClass;

	// 20201208 构建启动时应用程序信息记录者。
	StartupInfoLogger(Class<?> sourceClass) {
		// 20201208 注册记录源Class
		this.sourceClass = sourceClass;
	}

	// 20201208 开始应用程序信息日志打印
	void logStarting(Log applicationLog) {
		// 20201208
		Assert.notNull(applicationLog, "Log must not be null");
		applicationLog.info(LogMessage.of(this::getStartingMessage));
		applicationLog.debug(LogMessage.of(this::getRunningMessage));
	}

	// 20201210 应用程序信息启动完毕日志打印 -> 打印计时记录ms
	void logStarted(Log applicationLog, StopWatch stopWatch) {
		// 20201210 如果在基础记录器中启用了信息，则为true。
		if (applicationLog.isInfoEnabled()) {
			// 20201210 记录具有信息日志级别的消息。
			applicationLog.info(
					// 20201210 打印计时记录ms
					getStartedMessage(stopWatch)
			);
		}
	}

	private CharSequence getStartingMessage() {
		StringBuilder message = new StringBuilder();
		message.append("Starting ");
		appendApplicationName(message);
		appendVersion(message, this.sourceClass);
		appendJavaVersion(message);
		appendOn(message);
		appendPid(message);
		appendContext(message);
		return message;
	}

	private CharSequence getRunningMessage() {
		StringBuilder message = new StringBuilder();
		message.append("Running with Spring Boot");
		appendVersion(message, getClass());
		message.append(", Spring");
		appendVersion(message, ApplicationContext.class);
		return message;
	}

	// 20201210 打印计时记录ms
	private CharSequence getStartedMessage(StopWatch stopWatch) {
		// 20201210 初始化结果字符串
		StringBuilder message = new StringBuilder();

		// 20201210 拼凑打印信息
		message.append("Started ");
		appendApplicationName(message);
		message.append(" in ");
		message.append(stopWatch.getTotalTimeMillis() / 1000.0);
		message.append(" seconds");
		try {
			double uptime = ManagementFactory.getRuntimeMXBean().getUptime() / 1000.0;
			message.append(" (JVM running for ").append(uptime).append(")");
		}
		catch (Throwable ex) {
			// No JVM time available
			// 20201210 没有可用的JVM时间
		}
		return message;
	}

	private void appendApplicationName(StringBuilder message) {
		String name = (this.sourceClass != null) ? ClassUtils.getShortName(this.sourceClass) : "application";
		message.append(name);
	}

	private void appendVersion(StringBuilder message, Class<?> source) {
		append(message, "v", () -> source.getPackage().getImplementationVersion());
	}

	private void appendOn(StringBuilder message) {
		long startTime = System.currentTimeMillis();
		append(message, "on ", () -> InetAddress.getLocalHost().getHostName());
		long resolveTime = System.currentTimeMillis() - startTime;
		if (resolveTime > HOST_NAME_RESOLVE_THRESHOLD) {
			logger.warn(LogMessage.of(() -> {
				StringBuilder warning = new StringBuilder();
				warning.append("InetAddress.getLocalHost().getHostName() took ");
				warning.append(resolveTime);
				warning.append(" milliseconds to respond.");
				warning.append(" Please verify your network configuration");
				if (System.getProperty("os.name").toLowerCase().contains("mac")) {
					warning.append(" (macOS machines may need to add entries to /etc/hosts)");
				}
				warning.append(".");
				return warning;
			}));
		}
	}

	private void appendPid(StringBuilder message) {
		append(message, "with PID ", ApplicationPid::new);
	}

	private void appendContext(StringBuilder message) {
		StringBuilder context = new StringBuilder();
		ApplicationHome home = new ApplicationHome(this.sourceClass);
		if (home.getSource() != null) {
			context.append(home.getSource().getAbsolutePath());
		}
		append(context, "started by ", () -> System.getProperty("user.name"));
		append(context, "in ", () -> System.getProperty("user.dir"));
		if (context.length() > 0) {
			message.append(" (");
			message.append(context);
			message.append(")");
		}
	}

	private void appendJavaVersion(StringBuilder message) {
		append(message, "using Java ", () -> System.getProperty("java.version"));
	}

	private void append(StringBuilder message, String prefix, Callable<Object> call) {
		append(message, prefix, call, "");
	}

	private void append(StringBuilder message, String prefix, Callable<Object> call, String defaultValue) {
		Object result = callIfPossible(call);
		String value = (result != null) ? result.toString() : null;
		if (!StringUtils.hasLength(value)) {
			value = defaultValue;
		}
		if (StringUtils.hasLength(value)) {
			message.append((message.length() > 0) ? " " : "");
			message.append(prefix);
			message.append(value);
		}
	}

	private Object callIfPossible(Callable<Object> call) {
		try {
			return call.call();
		}
		catch (Exception ex) {
			return null;
		}
	}

}
