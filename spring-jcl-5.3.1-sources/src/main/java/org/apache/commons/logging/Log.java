/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.commons.logging;

/**
 * 20201203
 * A. 一个简单的日志接口抽象日志API。为了成功地被{@link logfactory}实例化，实现这个接口的类必须有一个构造函数，该构造函数接受一个表示该日志的“name”的字符串参数。
 * B. Log使用的六个日志级别是（按顺序）：
 * 		a. trace (the least serious)
 * 		b. debug
 * 		c. info
 * 		d. warn
 * 		e. error
 * 		f. fatal (the most serious)
 * C. 这些日志级别到底层日志系统使用的概念的映射依赖于实现。不过，实现应该确保这种排序按预期的方式进行。
 * D. 性能通常是一个日志问题。通过检查适当的属性，组件可以避免昂贵的操作（生成要记录的信息）, 例如：
 *    	if (log.isDebugEnabled()) {
 *        	... do something expensive ...
 *        	log.debug(theResult);
 *    	}
 * E. 底层日志系统的配置通常是在日志API外部完成的，通过该系统支持的任何机制。
 */
/**
 * A.
 * A simple logging interface abstracting logging APIs.  In order to be
 * instantiated successfully by {@link LogFactory}, classes that implement
 * this interface must have a constructor that takes a single String
 * parameter representing the "name" of this Log.
 *
 * B.
 * <p>The six logging levels used by <code>Log</code> are (in order):
 * <ol>
 * <li>trace (the least serious)</li>
 * <li>debug</li>
 * <li>info</li>
 * <li>warn</li>
 * <li>error</li>
 * <li>fatal (the most serious)</li>
 * </ol>
 *
 * C.
 * The mapping of these log levels to the concepts used by the underlying
 * logging system is implementation dependent.
 * The implementation should ensure, though, that this ordering behaves
 * as expected.
 *
 * D.
 * <p>Performance is often a logging concern.
 * By examining the appropriate property,
 * a component can avoid expensive operations (producing information
 * to be logged).
 *
 * <p>For example,
 * <pre>
 *    if (log.isDebugEnabled()) {
 *        ... do something expensive ...
 *        log.debug(theResult);
 *    }
 * </pre>
 *
 * E.
 * <p>Configuration of the underlying logging system will generally be done
 * external to the Logging APIs, through whatever mechanism is supported by
 * that system.
 *
 * @author Juergen Hoeller (for the {@code spring-jcl} variant)
 * @since 5.0
 */
// 20201203 一个简单的日志接口抽象日志API
public interface Log {

	/**
	 * Is fatal logging currently enabled?
	 * <p>Call this method to prevent having to perform expensive operations
	 * (for example, <code>String</code> concatenation)
	 * when the log level is more than fatal.
	 * @return true if fatal is enabled in the underlying logger.
	 */
	boolean isFatalEnabled();

	/**
	 * Is error logging currently enabled?
	 * <p>Call this method to prevent having to perform expensive operations
	 * (for example, <code>String</code> concatenation)
	 * when the log level is more than error.
	 * @return true if error is enabled in the underlying logger.
	 */
	boolean isErrorEnabled();

	/**
	 * Is warn logging currently enabled?
	 * <p>Call this method to prevent having to perform expensive operations
	 * (for example, <code>String</code> concatenation)
	 * when the log level is more than warn.
	 * @return true if warn is enabled in the underlying logger.
	 */
	boolean isWarnEnabled();

	/**
	 * Is info logging currently enabled?
	 * <p>Call this method to prevent having to perform expensive operations
	 * (for example, <code>String</code> concatenation)
	 * when the log level is more than info.
	 * @return true if info is enabled in the underlying logger.
	 */
	boolean isInfoEnabled();

	/**
	 * Is debug logging currently enabled?
	 * <p>Call this method to prevent having to perform expensive operations
	 * (for example, <code>String</code> concatenation)
	 * when the log level is more than debug.
	 * @return true if debug is enabled in the underlying logger. // 20201203 如果在基础记录器中启用了调试，则为true。
	 */
	// 20201203 当前是否启用调试日志记录？
	// 20201203 调用此方法可防止在日志级别高于debug时必须执行昂贵的操作（例如，字符串连接）。
	boolean isDebugEnabled();

	/**
	 * Is trace logging currently enabled?
	 * <p>Call this method to prevent having to perform expensive operations
	 * (for example, <code>String</code> concatenation)
	 * when the log level is more than trace.
	 * @return true if trace is enabled in the underlying logger.
	 */
	boolean isTraceEnabled();


	/**
	 * Logs a message with fatal log level.
	 * @param message log this message
	 */
	void fatal(Object message);

	/**
	 * Logs an error with fatal log level.
	 * @param message log this message
	 * @param t log this cause
	 */
	void fatal(Object message, Throwable t);

	/**
	 * Logs a message with error log level.
	 * @param message log this message
	 */
	void error(Object message);

	/**
	 * Logs an error with error log level.
	 * @param message log this message
	 * @param t log this cause
	 */
	void error(Object message, Throwable t);

	/**
	 * Logs a message with warn log level.
	 * @param message log this message
	 */
	void warn(Object message);

	/**
	 * Logs an error with warn log level.
	 * @param message log this message
	 * @param t log this cause
	 */
	void warn(Object message, Throwable t);

	/**
	 * Logs a message with info log level.
	 * @param message log this message
	 */
	void info(Object message);

	/**
	 * Logs an error with info log level.
	 * @param message log this message
	 * @param t log this cause
	 */
	void info(Object message, Throwable t);

	/**
	 * Logs a message with debug log level.
	 * @param message log this message
	 */
	void debug(Object message);

	/**
	 * Logs an error with debug log level.
	 * @param message log this message
	 * @param t log this cause
	 */
	void debug(Object message, Throwable t);

	/**
	 * Logs a message with trace log level.
	 * @param message log this message
	 */
	void trace(Object message);

	/**
	 * Logs an error with trace log level.
	 * @param message log this message
	 * @param t log this cause
	 */
	void trace(Object message, Throwable t);

}
