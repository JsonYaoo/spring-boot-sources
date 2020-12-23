/*
 * Copyright 2002-2018 the original author or authors.
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

package org.springframework.web.method.support;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.springframework.http.HttpStatus;
import org.springframework.lang.Nullable;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.validation.support.BindingAwareModelMap;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.bind.support.SimpleSessionStatus;

/**
 * 20201222
 * A. 记录模型并查看由{@link HandlerMethodArgumentResolver HandlerMethodArgumentResolvers}和{@link HandlerMethodReturnValueHandler HandlerMethodReturnValueHandlers}做出的相关决策。
 * B. {@link #setRequestHandled}标志可用于指示请求已直接处理，并且不需要视图分辨率。
 * C. 实例化时会自动创建一个默认的{@link Model}。 可以通过{@link #setRedirectModel}提供备用模型实例，以用于重定向方案。 当{@link #setRedirectModelScenario}设置为{@code true}表示
 *     重定向方案时，{@link #getModel（）}返回重定向模型，而不是默认模型。
 */
/**
 * A.
 * Records model and view related decisions made by
 * {@link HandlerMethodArgumentResolver HandlerMethodArgumentResolvers} and
 * {@link HandlerMethodReturnValueHandler HandlerMethodReturnValueHandlers} during the course of invocation of
 * a controller method.
 *
 * B.
 * <p>The {@link #setRequestHandled} flag can be used to indicate the request
 * has been handled directly and view resolution is not required.
 *
 * C.
 * <p>A default {@link Model} is automatically created at instantiation.
 * An alternate model instance may be provided via {@link #setRedirectModel}
 * for use in a redirect scenario. When {@link #setRedirectModelScenario} is set
 * to {@code true} signalling a redirect scenario, the {@link #getModel()}
 * returns the redirect model instead of the default model.
 *
 * @author Rossen Stoyanchev
 * @author Juergen Hoeller
 * @since 3.1
 */
// 20201222 记录模型并查看由{@link HandlerMethodArgumentResolver HandlerMethodArgumentResolvers}和{@link HandlerMethodReturnValueHandler HandlerMethodReturnValueHandlers}做出的相关决策。
public class ModelAndViewContainer {

	// 20201222 是否忽略默认的重定向模型, 默认为false
	private boolean ignoreDefaultModelOnRedirect = false;

	@Nullable
	private Object view;

	// 20201222 默认模型
	private final ModelMap defaultModel = new BindingAwareModelMap();

	// 20201222 重定向模型
	@Nullable
	private ModelMap redirectModel;

	// 20201222 重定向场景, 默认为false
	private boolean redirectModelScenario = false;

	@Nullable
	private HttpStatus status;

	private final Set<String> noBinding = new HashSet<>(4);

	private final Set<String> bindingDisabled = new HashSet<>(4);

	private final SessionStatus sessionStatus = new SimpleSessionStatus();

	private boolean requestHandled = false;

	/**
	 * 20201223
	 * A. 默认情况下，在渲染和重定向方案中都使用“默认”模型的内容。 另外，控制器方法可以声明{@code RedirectAttributes}类型的参数，并使用它提供属性以准备重定向URL。
	 * B. 将此标志设置为{@code true}可以确保即使未声明RedirectAttributes参数，也不会在重定向方案中使用“默认”模型。 将其设置为{@code false}意味着如果控制器方法未声明
	 *    RedirectAttributes参数，则可以在重定向中使用“默认”模型。
	 * C. 默认设置为{@code false}。
	 */
	/**
	 * A.
	 * By default the content of the "default" model is used both during
	 * rendering and redirect scenarios. Alternatively controller methods
	 * can declare an argument of type {@code RedirectAttributes} and use
	 * it to provide attributes to prepare the redirect URL.
	 *
	 * B.
	 * <p>Setting this flag to {@code true} guarantees the "default" model is
	 * never used in a redirect scenario even if a RedirectAttributes argument
	 * is not declared. Setting it to {@code false} means the "default" model
	 * may be used in a redirect if the controller method doesn't declare a
	 * RedirectAttributes argument.
	 *
	 * C.
	 * <p>The default setting is {@code false}.
	 */
	// 20201223 默认情况下，在渲染和重定向方案中都使用“默认”模型的内容。 另外，控制器方法可以声明{@code RedirectAttributes}类型的参数，并使用它提供属性以准备重定向URL
	public void setIgnoreDefaultModelOnRedirect(boolean ignoreDefaultModelOnRedirect) {
		this.ignoreDefaultModelOnRedirect = ignoreDefaultModelOnRedirect;
	}

	/**
	 * Set a view name to be resolved by the DispatcherServlet via a ViewResolver.
	 * Will override any pre-existing view name or View.
	 */
	public void setViewName(@Nullable String viewName) {
		this.view = viewName;
	}

	/**
	 * Return the view name to be resolved by the DispatcherServlet via a
	 * ViewResolver, or {@code null} if a View object is set.
	 */
	@Nullable
	public String getViewName() {
		return (this.view instanceof String ? (String) this.view : null);
	}

	/**
	 * Set a View object to be used by the DispatcherServlet.
	 * Will override any pre-existing view name or View.
	 */
	public void setView(@Nullable Object view) {
		this.view = view;
	}

	/**
	 * Return the View object, or {@code null} if we using a view name
	 * to be resolved by the DispatcherServlet via a ViewResolver.
	 */
	@Nullable
	public Object getView() {
		return this.view;
	}

	/**
	 * Whether the view is a view reference specified via a name to be
	 * resolved by the DispatcherServlet via a ViewResolver.
	 */
	public boolean isViewReference() {
		return (this.view instanceof String);
	}

	/**
	 * 20201222
	 * 返回要使用的模型-“默认”模型或“重定向”模型。 如果{@code redirectModelScenario = false}或没有重定向模型（即未将RedirectAttributes声明为方法参数）和
	 * {@code ignoreDefaultModelOnRedirect = false}，则使用默认模型。
	 */
	/**
	 * Return the model to use -- either the "default" or the "redirect" model.
	 * The default model is used if {@code redirectModelScenario=false} or
	 * there is no redirect model (i.e. RedirectAttributes was not declared as
	 * a method argument) and {@code ignoreDefaultModelOnRedirect=false}.
	 */
	// 20201222 返回要使用的模型-“默认”模型或“重定向”模型。
	public ModelMap getModel() {
		// 20201222 eg: true, 使用“默认”模型
		if (useDefaultModel()) {
			return this.defaultModel;
		}
		else {
			if (this.redirectModel == null) {
				this.redirectModel = new ModelMap();
			}
			return this.redirectModel;
		}
	}

	/**
	 * Whether to use the default model or the redirect model.
	 */
	// 20201222 是使用默认模型还是重定向模型。
	private boolean useDefaultModel() {
		// 20201222 eg: false || (null == null && !false) => true
		return (!this.redirectModelScenario || (this.redirectModel == null && !this.ignoreDefaultModelOnRedirect));
	}

	/**
	 * Return the "default" model created at instantiation.
	 * <p>In general it is recommended to use {@link #getModel()} instead which
	 * returns either the "default" model (template rendering) or the "redirect"
	 * model (redirect URL preparation). Use of this method may be needed for
	 * advanced cases when access to the "default" model is needed regardless,
	 * e.g. to save model attributes specified via {@code @SessionAttributes}.
	 * @return the default model (never {@code null})
	 * @since 4.1.4
	 */
	public ModelMap getDefaultModel() {
		return this.defaultModel;
	}

	/**
	 * Provide a separate model instance to use in a redirect scenario.
	 * <p>The provided additional model however is not used unless
	 * {@link #setRedirectModelScenario} gets set to {@code true}
	 * to signal an actual redirect scenario.
	 */
	public void setRedirectModel(ModelMap redirectModel) {
		this.redirectModel = redirectModel;
	}

	/**
	 * Whether the controller has returned a redirect instruction, e.g. a
	 * "redirect:" prefixed view name, a RedirectView instance, etc.
	 */
	public void setRedirectModelScenario(boolean redirectModelScenario) {
		this.redirectModelScenario = redirectModelScenario;
	}

	/**
	 * Provide an HTTP status that will be passed on to with the
	 * {@code ModelAndView} used for view rendering purposes.
	 * @since 4.3
	 */
	public void setStatus(@Nullable HttpStatus status) {
		this.status = status;
	}

	/**
	 * Return the configured HTTP status, if any.
	 * @since 4.3
	 */
	@Nullable
	public HttpStatus getStatus() {
		return this.status;
	}

	/**
	 * Programmatically register an attribute for which data binding should not occur,
	 * not even for a subsequent {@code @ModelAttribute} declaration.
	 * @param attributeName the name of the attribute
	 * @since 4.3
	 */
	public void setBindingDisabled(String attributeName) {
		this.bindingDisabled.add(attributeName);
	}

	/**
	 * Whether binding is disabled for the given model attribute.
	 * @since 4.3
	 */
	public boolean isBindingDisabled(String name) {
		return (this.bindingDisabled.contains(name) || this.noBinding.contains(name));
	}

	/**
	 * Register whether data binding should occur for a corresponding model attribute,
	 * corresponding to an {@code @ModelAttribute(binding=true/false)} declaration.
	 * <p>Note: While this flag will be taken into account by {@link #isBindingDisabled},
	 * a hard {@link #setBindingDisabled} declaration will always override it.
	 * @param attributeName the name of the attribute
	 * @since 4.3.13
	 */
	public void setBinding(String attributeName, boolean enabled) {
		if (!enabled) {
			this.noBinding.add(attributeName);
		}
		else {
			this.noBinding.remove(attributeName);
		}
	}

	/**
	 * Return the {@link SessionStatus} instance to use that can be used to
	 * signal that session processing is complete.
	 */
	public SessionStatus getSessionStatus() {
		return this.sessionStatus;
	}

	/**
	 * 20201223
	 * A. 该请求是否已在处理程序中完全处理，例如 {@code @ResponseBody}方法，因此不需要视图分辨率。 当控制器方法声明类型为{@code ServletResponse}或
	 *    {@code OutputStream}的参数时，也可以设置此标志。
	 * B. 默认值为{@code false}。
	 */
	/**
	 * A.
	 * Whether the request has been handled fully within the handler, e.g.
	 * {@code @ResponseBody} method, and therefore view resolution is not
	 * necessary. This flag can also be set when controller methods declare an
	 * argument of type {@code ServletResponse} or {@code OutputStream}).
	 *
	 * B.
	 * <p>The default value is {@code false}.
	 */
	// 20201223 该请求是否已在处理程序中完全处理
	public void setRequestHandled(boolean requestHandled) {
		this.requestHandled = requestHandled;
	}

	/**
	 * Whether the request has been handled fully within the handler.
	 */
	public boolean isRequestHandled() {
		return this.requestHandled;
	}

	/**
	 * Add the supplied attribute to the underlying model.
	 * A shortcut for {@code getModel().addAttribute(String, Object)}.
	 */
	public ModelAndViewContainer addAttribute(String name, @Nullable Object value) {
		getModel().addAttribute(name, value);
		return this;
	}

	/**
	 * Add the supplied attribute to the underlying model.
	 * A shortcut for {@code getModel().addAttribute(Object)}.
	 */
	public ModelAndViewContainer addAttribute(Object value) {
		// 20201222 eg: BindingAwareModelMap@xxxx, eg: BindingAwareModelMap@xxxx
		getModel().addAttribute(value);
		return this;
	}

	/**
	 * Copy all attributes to the underlying model.
	 * A shortcut for {@code getModel().addAllAttributes(Map)}.
	 */
	// 20201222 将所有属性复制到基础模型。 {@code getModel（）。addAllAttributes（Map）}的快捷方式。
	public ModelAndViewContainer addAllAttributes(@Nullable Map<String, ?> attributes) {
		getModel().addAllAttributes(attributes);
		return this;
	}

	/**
	 * Copy attributes in the supplied {@code Map} with existing objects of
	 * the same name taking precedence (i.e. not getting replaced).
	 * A shortcut for {@code getModel().mergeAttributes(Map<String, ?>)}.
	 */
	// 20201222 复制提供的{@code Map}中的属性，并使用具有相同名称的现有对象优先（即不被替换）。 {@code getModel（）。mergeAttributes（Map <String，？>）}的快捷方式。
	public ModelAndViewContainer mergeAttributes(@Nullable Map<String, ?> attributes) {
		// 20201222
		getModel().mergeAttributes(attributes);
		return this;
	}

	/**
	 * Remove the given attributes from the model.
	 */
	public ModelAndViewContainer removeAttributes(@Nullable Map<String, ?> attributes) {
		if (attributes != null) {
			for (String key : attributes.keySet()) {
				getModel().remove(key);
			}
		}
		return this;
	}

	/**
	 * Whether the underlying model contains the given attribute name.
	 * A shortcut for {@code getModel().containsAttribute(String)}.
	 */
	public boolean containsAttribute(String name) {
		return getModel().containsAttribute(name);
	}


	/**
	 * Return diagnostic information.
	 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("ModelAndViewContainer: ");
		if (!isRequestHandled()) {
			if (isViewReference()) {
				sb.append("reference to view with name '").append(this.view).append("'");
			}
			else {
				sb.append("View is [").append(this.view).append(']');
			}
			if (useDefaultModel()) {
				sb.append("; default model ");
			}
			else {
				sb.append("; redirect model ");
			}
			sb.append(getModel());
		}
		else {
			sb.append("Request handled directly");
		}
		return sb.toString();
	}

}
