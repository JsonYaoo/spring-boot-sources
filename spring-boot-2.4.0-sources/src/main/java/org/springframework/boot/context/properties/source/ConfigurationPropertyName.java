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

package org.springframework.boot.context.properties.source;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.springframework.util.Assert;

/**
 * 20201202
 * A. 由点分隔的元素组成的配置属性名。用户创建的名称可以包含字符“{@code a-z}”“{@code 0-9}”）和“{@code-}”，它们必须是小写的，并且必须以字母数字字符开头。
 *    “{@code-}”纯粹用于格式化，即“{@code foo-bar}”和“{@code foobar}”被认为是等效的。
 * B. “{@code[}”和“{@code]}”字符可用于指示关联索引（即{@link Map}键或{@link Collection}索引）。索引名称不受限制，并且视为区分大小写。
 * C. 以下是一些典型的例子：
 * 		a. {@code spring.main.banner-mode}
 * 		b. {@code server.hosts[0].name}
 * 		c. {@code log[org.springboot].level}
 */
/**
 * A.
 * A configuration property name composed of elements separated by dots. User created
 * names may contain the characters "{@code a-z}" "{@code 0-9}") and "{@code -}", they
 * must be lower-case and must start with an alpha-numeric character. The "{@code -}" is
 * used purely for formatting, i.e. "{@code foo-bar}" and "{@code foobar}" are considered
 * equivalent.
 *
 * B.
 * <p>
 * The "{@code [}" and "{@code ]}" characters may be used to indicate an associative
 * index(i.e. a {@link Map} key or a {@link Collection} index. Indexes names are not
 * restricted and are considered case-sensitive.
 *
 * C.
 * <p>
 * Here are some typical examples:
 * <ul>
 * <li>{@code spring.main.banner-mode}</li>
 * <li>{@code server.hosts[0].name}</li>
 * <li>{@code log[org.springboot].level}</li>
 * </ul>
 *
 * @author Phillip Webb
 * @author Madhura Bhave
 * @since 2.0.0
 * @see #of(CharSequence)
 * @see ConfigurationPropertySource
 */
// 20201202 由点分隔的元素组成的配置属性名
public final class ConfigurationPropertyName implements Comparable<ConfigurationPropertyName> {

	private static final String EMPTY_STRING = "";

	/**
	 * An empty {@link ConfigurationPropertyName}.
	 */
	public static final ConfigurationPropertyName EMPTY = new ConfigurationPropertyName(Elements.EMPTY);

	private Elements elements;

	private final CharSequence[] uniformElements;

	private String string;

	private int hashCode;

	// 20201202 构造方法
	private ConfigurationPropertyName(Elements elements) {
		// 20201202 设置属性解析结果
		this.elements = elements;

		// 20201202 属性实际字符个数
		this.uniformElements = new CharSequence[elements.getSize()];
	}

	/**
	 * Returns {@code true} if this {@link ConfigurationPropertyName} is empty.
	 * @return {@code true} if the name is empty
	 */
	public boolean isEmpty() {
		return this.elements.getSize() == 0;
	}

	/**
	 * Return if the last element in the name is indexed.
	 * @return {@code true} if the last element is indexed
	 */
	public boolean isLastElementIndexed() {
		int size = getNumberOfElements();
		return (size > 0 && isIndexed(size - 1));
	}

	/**
	 * Return {@code true} if any element in the name is indexed.
	 * @return if the element has one or more indexed elements
	 * @since 2.2.10
	 */
	public boolean hasIndexedElement() {
		for (int i = 0; i < getNumberOfElements(); i++) {
			if (isIndexed(i)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Return if the element in the name is indexed.
	 * @param elementIndex the index of the element
	 * @return {@code true} if the element is indexed
	 */
	boolean isIndexed(int elementIndex) {
		return this.elements.getType(elementIndex).isIndexed();
	}

	/**
	 * Return if the element in the name is indexed and numeric.
	 * @param elementIndex the index of the element
	 * @return {@code true} if the element is indexed and numeric
	 */
	public boolean isNumericIndex(int elementIndex) {
		return this.elements.getType(elementIndex) == ElementType.NUMERICALLY_INDEXED;
	}

	/**
	 * Return the last element in the name in the given form.
	 * @param form the form to return
	 * @return the last element
	 */
	public String getLastElement(Form form) {
		int size = getNumberOfElements();
		return (size != 0) ? getElement(size - 1, form) : EMPTY_STRING;
	}

	/**
	 * Return an element in the name in the given form.
	 * @param elementIndex the element index
	 * @param form the form to return
	 * @return the last element
	 */
	public String getElement(int elementIndex, Form form) {
		CharSequence element = this.elements.get(elementIndex);
		ElementType type = this.elements.getType(elementIndex);
		if (type.isIndexed()) {
			return element.toString();
		}
		if (form == Form.ORIGINAL) {
			if (type != ElementType.NON_UNIFORM) {
				return element.toString();
			}
			return convertToOriginalForm(element).toString();
		}
		if (form == Form.DASHED) {
			if (type == ElementType.UNIFORM || type == ElementType.DASHED) {
				return element.toString();
			}
			return convertToDashedElement(element).toString();
		}
		CharSequence uniformElement = this.uniformElements[elementIndex];
		if (uniformElement == null) {
			uniformElement = (type != ElementType.UNIFORM) ? convertToUniformElement(element) : element;
			this.uniformElements[elementIndex] = uniformElement.toString();
		}
		return uniformElement.toString();
	}

	private CharSequence convertToOriginalForm(CharSequence element) {
		return convertElement(element, false,
				(ch, i) -> ch == '_' || ElementsParser.isValidChar(Character.toLowerCase(ch), i));
	}

	private CharSequence convertToDashedElement(CharSequence element) {
		return convertElement(element, true, ElementsParser::isValidChar);
	}

	private CharSequence convertToUniformElement(CharSequence element) {
		return convertElement(element, true, (ch, i) -> ElementsParser.isAlphaNumeric(ch));
	}

	private CharSequence convertElement(CharSequence element, boolean lowercase, ElementCharPredicate filter) {
		StringBuilder result = new StringBuilder(element.length());
		for (int i = 0; i < element.length(); i++) {
			char ch = lowercase ? Character.toLowerCase(element.charAt(i)) : element.charAt(i);
			if (filter.test(ch, i)) {
				result.append(ch);
			}
		}
		return result;
	}

	/**
	 * Return the total number of elements in the name.
	 * @return the number of elements
	 */
	public int getNumberOfElements() {
		return this.elements.getSize();
	}

	/**
	 * Create a new {@link ConfigurationPropertyName} by appending the given elements.
	 * @param elements the elements to append
	 * @return a new {@link ConfigurationPropertyName}
	 * @throws InvalidConfigurationPropertyNameException if the result is not valid
	 */
	public ConfigurationPropertyName append(String elements) {
		if (elements == null) {
			return this;
		}
		Elements additionalElements = probablySingleElementOf(elements);
		return new ConfigurationPropertyName(this.elements.append(additionalElements));
	}

	/**
	 * Return the parent of this {@link ConfigurationPropertyName} or
	 * {@link ConfigurationPropertyName#EMPTY} if there is no parent.
	 * @return the parent name
	 */
	public ConfigurationPropertyName getParent() {
		int numberOfElements = getNumberOfElements();
		return (numberOfElements <= 1) ? EMPTY : chop(numberOfElements - 1);
	}

	/**
	 * Return a new {@link ConfigurationPropertyName} by chopping this name to the given
	 * {@code size}. For example, {@code chop(1)} on the name {@code foo.bar} will return
	 * {@code foo}.
	 * @param size the size to chop
	 * @return the chopped name
	 */
	public ConfigurationPropertyName chop(int size) {
		if (size >= getNumberOfElements()) {
			return this;
		}
		return new ConfigurationPropertyName(this.elements.chop(size));
	}

	/**
	 * Returns {@code true} if this element is an immediate parent of the specified name.
	 * @param name the name to check
	 * @return {@code true} if this name is an ancestor
	 */
	public boolean isParentOf(ConfigurationPropertyName name) {
		Assert.notNull(name, "Name must not be null");
		if (getNumberOfElements() != name.getNumberOfElements() - 1) {
			return false;
		}
		return isAncestorOf(name);
	}

	/**
	 * Returns {@code true} if this element is an ancestor (immediate or nested parent) of
	 * the specified name.
	 * @param name the name to check
	 * @return {@code true} if this name is an ancestor
	 */
	public boolean isAncestorOf(ConfigurationPropertyName name) {
		Assert.notNull(name, "Name must not be null");
		if (getNumberOfElements() >= name.getNumberOfElements()) {
			return false;
		}
		return elementsEqual(name);
	}

	@Override
	public int compareTo(ConfigurationPropertyName other) {
		return compare(this, other);
	}

	private int compare(ConfigurationPropertyName n1, ConfigurationPropertyName n2) {
		int l1 = n1.getNumberOfElements();
		int l2 = n2.getNumberOfElements();
		int i1 = 0;
		int i2 = 0;
		while (i1 < l1 || i2 < l2) {
			try {
				ElementType type1 = (i1 < l1) ? n1.elements.getType(i1) : null;
				ElementType type2 = (i2 < l2) ? n2.elements.getType(i2) : null;
				String e1 = (i1 < l1) ? n1.getElement(i1++, Form.UNIFORM) : null;
				String e2 = (i2 < l2) ? n2.getElement(i2++, Form.UNIFORM) : null;
				int result = compare(e1, type1, e2, type2);
				if (result != 0) {
					return result;
				}
			}
			catch (ArrayIndexOutOfBoundsException ex) {
				throw new RuntimeException(ex);
			}
		}
		return 0;
	}

	private int compare(String e1, ElementType type1, String e2, ElementType type2) {
		if (e1 == null) {
			return -1;
		}
		if (e2 == null) {
			return 1;
		}
		int result = Boolean.compare(type2.isIndexed(), type1.isIndexed());
		if (result != 0) {
			return result;
		}
		if (type1 == ElementType.NUMERICALLY_INDEXED && type2 == ElementType.NUMERICALLY_INDEXED) {
			long v1 = Long.parseLong(e1);
			long v2 = Long.parseLong(e2);
			return Long.compare(v1, v2);
		}
		return e1.compareTo(e2);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		if (obj == null || obj.getClass() != getClass()) {
			return false;
		}
		ConfigurationPropertyName other = (ConfigurationPropertyName) obj;
		if (getNumberOfElements() != other.getNumberOfElements()) {
			return false;
		}
		if (this.elements.canShortcutWithSource(ElementType.UNIFORM)
				&& other.elements.canShortcutWithSource(ElementType.UNIFORM)) {
			return toString().equals(other.toString());
		}
		return elementsEqual(other);
	}

	private boolean elementsEqual(ConfigurationPropertyName name) {
		for (int i = this.elements.getSize() - 1; i >= 0; i--) {
			if (elementDiffers(this.elements, name.elements, i)) {
				return false;
			}
		}
		return true;
	}

	private boolean elementDiffers(Elements e1, Elements e2, int i) {
		ElementType type1 = e1.getType(i);
		ElementType type2 = e2.getType(i);
		if (type1.allowsFastEqualityCheck() && type2.allowsFastEqualityCheck()) {
			return !fastElementEquals(e1, e2, i);
		}
		if (type1.allowsDashIgnoringEqualityCheck() && type2.allowsDashIgnoringEqualityCheck()) {
			return !dashIgnoringElementEquals(e1, e2, i);
		}
		return !defaultElementEquals(e1, e2, i);
	}

	private boolean fastElementEquals(Elements e1, Elements e2, int i) {
		int length1 = e1.getLength(i);
		int length2 = e2.getLength(i);
		if (length1 == length2) {
			int i1 = 0;
			while (length1-- != 0) {
				char ch1 = e1.charAt(i, i1);
				char ch2 = e2.charAt(i, i1);
				if (ch1 != ch2) {
					return false;
				}
				i1++;
			}
			return true;
		}
		return false;
	}

	private boolean dashIgnoringElementEquals(Elements e1, Elements e2, int i) {
		int l1 = e1.getLength(i);
		int l2 = e2.getLength(i);
		int i1 = 0;
		int i2 = 0;
		while (i1 < l1) {
			if (i2 >= l2) {
				return false;
			}
			char ch1 = e1.charAt(i, i1);
			char ch2 = e2.charAt(i, i2);
			if (ch1 == '-') {
				i1++;
			}
			else if (ch2 == '-') {
				i2++;
			}
			else if (ch1 != ch2) {
				return false;
			}
			else {
				i1++;
				i2++;
			}
		}
		if (i2 < l2) {
			if (e2.getType(i).isIndexed()) {
				return false;
			}
			do {
				char ch2 = e2.charAt(i, i2++);
				if (ch2 != '-') {
					return false;
				}
			}
			while (i2 < l2);
		}
		return true;
	}

	private boolean defaultElementEquals(Elements e1, Elements e2, int i) {
		int l1 = e1.getLength(i);
		int l2 = e2.getLength(i);
		boolean indexed1 = e1.getType(i).isIndexed();
		boolean indexed2 = e2.getType(i).isIndexed();
		int i1 = 0;
		int i2 = 0;
		while (i1 < l1) {
			if (i2 >= l2) {
				return false;
			}
			char ch1 = indexed1 ? e1.charAt(i, i1) : Character.toLowerCase(e1.charAt(i, i1));
			char ch2 = indexed2 ? e2.charAt(i, i2) : Character.toLowerCase(e2.charAt(i, i2));
			if (!indexed1 && !ElementsParser.isAlphaNumeric(ch1)) {
				i1++;
			}
			else if (!indexed2 && !ElementsParser.isAlphaNumeric(ch2)) {
				i2++;
			}
			else if (ch1 != ch2) {
				return false;
			}
			else {
				i1++;
				i2++;
			}
		}
		if (i2 < l2) {
			if (indexed2) {
				return false;
			}
			do {
				char ch2 = Character.toLowerCase(e2.charAt(i, i2++));
				if (ElementsParser.isAlphaNumeric(ch2)) {
					return false;
				}
			}
			while (i2 < l2);
		}
		return true;
	}

	@Override
	public int hashCode() {
		int hashCode = this.hashCode;
		Elements elements = this.elements;
		if (hashCode == 0 && elements.getSize() != 0) {
			for (int elementIndex = 0; elementIndex < elements.getSize(); elementIndex++) {
				int elementHashCode = 0;
				boolean indexed = elements.getType(elementIndex).isIndexed();
				int length = elements.getLength(elementIndex);
				for (int i = 0; i < length; i++) {
					char ch = elements.charAt(elementIndex, i);
					if (!indexed) {
						ch = Character.toLowerCase(ch);
					}
					if (ElementsParser.isAlphaNumeric(ch)) {
						elementHashCode = 31 * elementHashCode + ch;
					}
				}
				hashCode = 31 * hashCode + elementHashCode;
			}
			this.hashCode = hashCode;
		}
		return hashCode;
	}

	@Override
	public String toString() {
		if (this.string == null) {
			this.string = buildToString();
		}
		return this.string;
	}

	private String buildToString() {
		if (this.elements.canShortcutWithSource(ElementType.UNIFORM, ElementType.DASHED)) {
			return this.elements.getSource().toString();
		}
		int elements = getNumberOfElements();
		StringBuilder result = new StringBuilder(elements * 8);
		for (int i = 0; i < elements; i++) {
			boolean indexed = isIndexed(i);
			if (result.length() > 0 && !indexed) {
				result.append('.');
			}
			if (indexed) {
				result.append('[');
				result.append(getElement(i, Form.ORIGINAL));
				result.append(']');
			}
			else {
				result.append(getElement(i, Form.DASHED));
			}
		}
		return result.toString();
	}

	/**
	 * Returns if the given name is valid. If this method returns {@code true} then the
	 * name may be used with {@link #of(CharSequence)} without throwing an exception.
	 * @param name the name to test
	 * @return {@code true} if the name is valid
	 */
	public static boolean isValid(CharSequence name) {
		return of(name, true) != null;
	}

	/**
	 * Return a {@link ConfigurationPropertyName} for the specified string.
	 * @param name the source name
	 * @return a {@link ConfigurationPropertyName} instance
	 * @throws InvalidConfigurationPropertyNameException if the name is not valid
	 */
	// 20201202 为指定的字符串返回{@link ConfigurationPropertyName}。name => "spring.main"
	public static ConfigurationPropertyName of(CharSequence name) {
		return of(name, false);
	}

	/**
	 * Return a {@link ConfigurationPropertyName} for the specified string or {@code null}
	 * if the name is not valid.
	 * @param name the source name
	 * @return a {@link ConfigurationPropertyName} instance
	 * @since 2.3.1
	 */
	public static ConfigurationPropertyName ofIfValid(CharSequence name) {
		return of(name, true);
	}

	/**
	 * Return a {@link ConfigurationPropertyName} for the specified string.
	 * @param name the source name
	 * @param returnNullIfInvalid if null should be returned if the name is not valid // 20201202 如果名称无效，则应返回if null
	 * @return a {@link ConfigurationPropertyName} instance
	 * @throws InvalidConfigurationPropertyNameException if the name is not valid and
	 * {@code returnNullIfInvalid} is {@code false}
	 */
	// 20201202 为指定的字符串返回{@link ConfigurationPropertyName}。 name => "spring.main"
	static ConfigurationPropertyName of(CharSequence name, boolean returnNullIfInvalid) {
		// 20201202 转换未统一的配置字符: 小写字母 | 数字 | -, 且含有每个字串开始结束索引
		Elements elements = elementsOf(name, returnNullIfInvalid);

		// 20201202 如果解析结果不空, 则构建 由点分隔的元素组成的配置属性名 对象
		return (elements != null) ? new ConfigurationPropertyName(elements) : null;
	}

	private static Elements probablySingleElementOf(CharSequence name) {
		return elementsOf(name, false, 1);
	}

	// 20201202 解析name的每个元素 name => "spring.main"
	private static Elements elementsOf(CharSequence name, boolean returnNullIfInvalid) {
		// 20201202 ElementsParser解析器初始化容量为6
		return elementsOf(name, returnNullIfInvalid, ElementsParser.DEFAULT_CAPACITY);
	}

	// 20201202 解析name的每个元素 -> 指定解析器初始化容量 name => "spring.main"
	private static Elements elementsOf(CharSequence name, boolean returnNullIfInvalid, int parserCapacity) {
		// 20201202 如果属性名成为空
		if (name == null) {
			// 20201202 默认是不提示报错
			Assert.isTrue(returnNullIfInvalid, "Name must not be null");
			return null;
		}

		// 20201202 如果属性名为长度为0
		if (name.length() == 0) {
			// 20201202 则返回一个空列表
			return Elements.EMPTY;
		}

		// 20201202 如果第一个元素或者最后一个元素为.
		if (name.charAt(0) == '.' || name.charAt(name.length() - 1) == '.') {
			if (returnNullIfInvalid) {
				return null;
			}

			// 20201202 则默认抛出name名称非法异常
			throw new InvalidConfigurationPropertyNameException(name, Collections.singletonList('.'));
		}

		// 20201202 构造ElementsParser解析器 name => "spring.main", 这里解析的结果是: start[]: 0, 7; end[]: 6, 10; type: UNIFORM: 表示字符已统一, 无需转换
		Elements elements = new ElementsParser(name, '.', parserCapacity).parse();

		// 20201202 遍历解析结果
		for (int i = 0; i < elements.getSize(); i++) {
			// 20201202 如果有元素为NON_UNIFORM, 即表示字符未统一, 需要转换
			if (elements.getType(i) == ElementType.NON_UNIFORM) {
				// 20201202 如果忽略null & 非法字符, 则无需抛出异常
				if (returnNullIfInvalid) {
					return null;
				}

				// 20201202 否则抛出非法配置属性名称异常
				throw new InvalidConfigurationPropertyNameException(name, getInvalidChars(elements, i));
			}
		}
		return elements;
	}

	// 20201202 获取非法字符
	private static List<Character> getInvalidChars(Elements elements, int index) {
		List<Character> invalidChars = new ArrayList<>();
		for (int charIndex = 0; charIndex < elements.getLength(index); charIndex++) {
			char ch = elements.charAt(index, charIndex);
			if (!ElementsParser.isValidChar(ch, charIndex)) {
				invalidChars.add(ch);
			}
		}
		return invalidChars;
	}

	/**
	 * Create a {@link ConfigurationPropertyName} by adapting the given source. See
	 * {@link #adapt(CharSequence, char, Function)} for details.
	 * @param name the name to parse
	 * @param separator the separator used to split the name
	 * @return a {@link ConfigurationPropertyName}
	 */
	public static ConfigurationPropertyName adapt(CharSequence name, char separator) {
		return adapt(name, separator, null);
	}

	/**
	 * Create a {@link ConfigurationPropertyName} by adapting the given source. The name
	 * is split into elements around the given {@code separator}. This method is more
	 * lenient than {@link #of} in that it allows mixed case names and '{@code _}'
	 * characters. Other invalid characters are stripped out during parsing.
	 * <p>
	 * The {@code elementValueProcessor} function may be used if additional processing is
	 * required on the extracted element values.
	 * @param name the name to parse
	 * @param separator the separator used to split the name
	 * @param elementValueProcessor a function to process element values
	 * @return a {@link ConfigurationPropertyName}
	 */
	static ConfigurationPropertyName adapt(CharSequence name, char separator,
			Function<CharSequence, CharSequence> elementValueProcessor) {
		Assert.notNull(name, "Name must not be null");
		if (name.length() == 0) {
			return EMPTY;
		}
		Elements elements = new ElementsParser(name, separator).parse(elementValueProcessor);
		if (elements.getSize() == 0) {
			return EMPTY;
		}
		return new ConfigurationPropertyName(elements);
	}

	/**
	 * The various forms that a non-indexed element value can take.
	 */
	public enum Form {

		/**
		 * The original form as specified when the name was created or adapted. For
		 * example:
		 * <ul>
		 * <li>"{@code foo-bar}" = "{@code foo-bar}"</li>
		 * <li>"{@code fooBar}" = "{@code fooBar}"</li>
		 * <li>"{@code foo_bar}" = "{@code foo_bar}"</li>
		 * <li>"{@code [Foo.bar]}" = "{@code Foo.bar}"</li>
		 * </ul>
		 */
		ORIGINAL,

		/**
		 * The dashed configuration form (used for toString; lower-case with only
		 * alphanumeric characters and dashes).
		 * <ul>
		 * <li>"{@code foo-bar}" = "{@code foo-bar}"</li>
		 * <li>"{@code fooBar}" = "{@code foobar}"</li>
		 * <li>"{@code foo_bar}" = "{@code foobar}"</li>
		 * <li>"{@code [Foo.bar]}" = "{@code Foo.bar}"</li>
		 * </ul>
		 */
		DASHED,

		/**
		 * The uniform configuration form (used for equals/hashCode; lower-case with only
		 * alphanumeric characters).
		 * <ul>
		 * <li>"{@code foo-bar}" = "{@code foobar}"</li>
		 * <li>"{@code fooBar}" = "{@code foobar}"</li>
		 * <li>"{@code foo_bar}" = "{@code foobar}"</li>
		 * <li>"{@code [Foo.bar]}" = "{@code Foo.bar}"</li>
		 * </ul>
		 */
		UNIFORM

	}

	/**
	 * Allows access to the individual elements that make up the name. We store the
	 * indexes in arrays rather than a list of object in order to conserve memory.
	 */
	// 20201202 允许访问组成名称的各个元素。为了节省内存，我们将索引存储在数组中而不是对象列表中。
	private static class Elements {

		private static final int[] NO_POSITION = {};

		private static final ElementType[] NO_TYPE = {};

		public static final Elements EMPTY = new Elements("", 0, NO_POSITION, NO_POSITION, NO_TYPE, null);

		private final CharSequence source;

		private final int size;

		private final int[] start;

		private final int[] end;

		private final ElementType[] type;

		/**
		 * Contains any resolved elements or can be {@code null} if there aren't any.
		 * Resolved elements allow us to modify the element values in some way (or example
		 * when adapting with a mapping function, or when append has been called). Note
		 * that this array is not used as a cache, in fact, when it's not null then
		 * {@link #canShortcutWithSource} will always return false which may hurt
		 * performance.
		 */
		private final CharSequence[] resolved;

		Elements(CharSequence source, int size, int[] start, int[] end, ElementType[] type, CharSequence[] resolved) {
			super();
			this.source = source;
			this.size = size;
			this.start = start;
			this.end = end;
			this.type = type;
			this.resolved = resolved;
		}

		Elements append(Elements additional) {
			int size = this.size + additional.size;
			ElementType[] type = new ElementType[size];
			System.arraycopy(this.type, 0, type, 0, this.size);
			System.arraycopy(additional.type, 0, type, this.size, additional.size);
			CharSequence[] resolved = newResolved(size);
			for (int i = 0; i < additional.size; i++) {
				resolved[this.size + i] = additional.get(i);
			}
			return new Elements(this.source, size, this.start, this.end, type, resolved);
		}

		Elements chop(int size) {
			CharSequence[] resolved = newResolved(size);
			return new Elements(this.source, size, this.start, this.end, this.type, resolved);
		}

		private CharSequence[] newResolved(int size) {
			CharSequence[] resolved = new CharSequence[size];
			if (this.resolved != null) {
				System.arraycopy(this.resolved, 0, resolved, 0, Math.min(size, this.size));
			}
			return resolved;
		}

		int getSize() {
			return this.size;
		}

		CharSequence get(int index) {
			if (this.resolved != null && this.resolved[index] != null) {
				return this.resolved[index];
			}
			int start = this.start[index];
			int end = this.end[index];
			return this.source.subSequence(start, end);
		}

		int getLength(int index) {
			if (this.resolved != null && this.resolved[index] != null) {
				return this.resolved[index].length();
			}
			int start = this.start[index];
			int end = this.end[index];
			return end - start;
		}

		char charAt(int index, int charIndex) {
			if (this.resolved != null && this.resolved[index] != null) {
				return this.resolved[index].charAt(charIndex);
			}
			int start = this.start[index];
			return this.source.charAt(start + charIndex);
		}

		ElementType getType(int index) {
			return this.type[index];
		}

		CharSequence getSource() {
			return this.source;
		}

		/**
		 * Returns if the element source can be used as a shortcut for an operation such
		 * as {@code equals} or {@code toString}.
		 * @param requiredType the required type
		 * @return {@code true} if all elements match at least one of the types
		 */
		boolean canShortcutWithSource(ElementType requiredType) {
			return canShortcutWithSource(requiredType, requiredType);
		}

		/**
		 * Returns if the element source can be used as a shortcut for an operation such
		 * as {@code equals} or {@code toString}.
		 * @param requiredType the required type
		 * @param alternativeType and alternative required type
		 * @return {@code true} if all elements match at least one of the types
		 */
		boolean canShortcutWithSource(ElementType requiredType, ElementType alternativeType) {
			if (this.resolved != null) {
				return false;
			}
			for (int i = 0; i < this.size; i++) {
				ElementType type = this.type[i];
				if (type != requiredType && type != alternativeType) {
					return false;
				}
				if (i > 0 && this.end[i - 1] + 1 != this.start[i]) {
					return false;
				}
			}
			return true;
		}

	}

	/**
	 * Main parsing logic used to convert a {@link CharSequence} to {@link Elements}.
	 */
	// 20201202 用于将{@link CharSequence}转换为{@link Elements}的主解析逻辑。
	private static class ElementsParser {
		// 20201202 ElementsParser解析器初始化容量
		private static final int DEFAULT_CAPACITY = 6;

		// 20201202 初始字符串
		private final CharSequence source;

		// 20201202 字符分隔符
		private final char separator;

		// 20201202 实际容量大小
		private int size;

		// 20201202 开始数组
		private int[] start;

		// 20201202 结束数组
		private int[] end;

		// 20201202 元素类型数组
		private ElementType[] type;

		// 20201202 已解析后的字符数组
		private CharSequence[] resolved;

		ElementsParser(CharSequence source, char separator) {
			this(source, separator, DEFAULT_CAPACITY);
		}

		// 20201202 构造ElementsParser解析器
		ElementsParser(CharSequence source, char separator, int capacity) {
			// 20201202 注册属性源 name => "spring.main"
			this.source = source;

			// 20201202 注册字符分隔符
			this.separator = separator;

			// 20201202 注册开始capacity长数组
			this.start = new int[capacity];

			// 20201202 注册结束capacity长数组
			this.end = new int[capacity];

			// 20201202 注册capacity长ElementType数组
			this.type = new ElementType[capacity];
		}

		// 20201202 解析器解析元素
		Elements parse() {
			return parse(null);
		}

		// 20201202 解析器解析元素, 带值操作参数 name => "spring.main", 这里不适用操作器，这里解析的结果是: start[]: 0, 7; end[]: 6, 10; type: UNIFORM: 表示字符已统一, 无需转换
		Elements parse(Function<CharSequence, CharSequence> valueProcessor) {
			// 20201202 获取解析源的长度
			int length = this.source.length();
			int openBracketCount = 0;
			int start = 0;

			// 20201202 初始化元素类型为 元素在逻辑上为空
			ElementType type = ElementType.EMPTY;

			// 20201202 遍历解析源
			for (int i = 0; i < length; i++) {
				// 20201202 获取i位置的char
				char ch = this.source.charAt(i);

				// 20201202 如果为'['
				if (ch == '[') {
					// 20201202 如果支架个数为0
					if (openBracketCount == 0) {
						// 20201202 记录start~i-1位置元素类型为当前元素类型
						add(start, i, type, valueProcessor);

						// 20201202 start指针移动到下一个位置
						start = i + 1;

						// 20201202 标识元素类型为 元素被数字索引
						type = ElementType.NUMERICALLY_INDEXED;
					}

					// 20201202 支架元素个数+1
					openBracketCount++;
				}

				// 20201202 如果为']'
				else if (ch == ']') {
					// 20201202 支架个数-1
					openBracketCount--;

					// 20201202 如果支架个数为0
					if (openBracketCount == 0) {
						// 20201202 记录start~i-1位置元素类型为当前元素类型
						add(start, i, type, valueProcessor);

						// 20201202 start指针移动到下一个位置
						start = i + 1;

						// 20201202 标识元素类型为 元素在逻辑上为空
						type = ElementType.EMPTY;
					}
				}

				// 20201202 如果既不是'['也不是']', 如果当前元素类型为INDEXED | NUMERICALLY_INDEXED(即开始时), 且如果当前char为分割符.时
				else if (!type.isIndexed() && ch == this.separator) {
					// 20201202 记录start~i-1位置元素类型为当前元素类型
					add(start, i, type, valueProcessor);

					// 20201202 start指针移动到下一个位置
					start = i + 1;

					// 20201202 标识元素类型为 元素在逻辑上为空
					type = ElementType.EMPTY;
				}

				// 20201202 否则如果为普通char
				else {
					// 20201202 则更新元素类型
					type = updateType(type, ch, i - start);
				}
			}

			// 20201202 如果支架个数不为0
			if (openBracketCount != 0) {
				// 20201202 则标记元素类型为 元素包含非统一字符，需要转换
				type = ElementType.NON_UNIFORM;
			}

			// 20201202 添加整个字符串到元素类型中
			add(start, length, type, valueProcessor);

			// 20201202 重新构建Elements对象
			return new Elements(this.source, this.size, this.start, this.end, this.type, this.resolved);
		}

		private ElementType updateType(ElementType existingType, char ch, int index) {
		    // 20201202 如果当前元素类型为INDEXED | NUMERICALLY_INDEXED(即开始时)
			if (existingType.isIndexed()) {
			    // 20201202 如果当前元素围殴NUMERICALLY_INDEXED(即开始时), 且当前char不是数字
				if (existingType == ElementType.NUMERICALLY_INDEXED && !isNumeric(ch)) {
				    // 20201202 则设置当前元素类型为INDEXED
					return ElementType.INDEXED;
				}

				// 20201202 返回当前元素类型
				return existingType;
			}

			// 20201202 如果当前元素类型为EMPTY()(即为整个字符串结束 或者 一小串字符 结束后), 且当前元素是否合法: 小写字母, 数字, -
			if (existingType == ElementType.EMPTY && isValidChar(ch, index)) {
			    // 20201202 如果是新的字串开始, 且合法, 如果为第一个char则设置为UNIFORM, 否则设置为NON_UNIFORM
				return (index == 0) ? ElementType.UNIFORM : ElementType.NON_UNIFORM;
			}

			// 20201202 如果为字串连续出现--
			if (existingType == ElementType.UNIFORM && ch == '-') {
			    // 20201202 则设置元素类型为DASHED
				return ElementType.DASHED;
			}

			// 20201202 如果当前char不合法
			if (!isValidChar(ch, index)) {
			    // 20201202 如果当前元素类型为EMPTY(即为整个字符串结束 或者 一小串字符 结束后), 且小写转换后的字符还不合法
				if (existingType == ElementType.EMPTY && !isValidChar(Character.toLowerCase(ch), index)) {
				    // 20201202 则当前元素类型设置为EMPTY, 表示忽略该字符
					return ElementType.EMPTY;
				}

				// 20201202 否则标识它为NON_UNIFORM, 需要进行转换
				return ElementType.NON_UNIFORM;
			}

			// 20201202 返回更新后的元素类型
			return existingType;
		}

		// 20201202 添加start、end索引, 记录start~end-1位置元素类型为当前元素类型
		private void add(int start, int end, ElementType type, Function<CharSequence, CharSequence> valueProcessor) {
			// 20201202 如果长度<1, 或者该元素类型为 元素在逻辑上为空（不包含有效字符）
			if ((end - start) < 1 || type == ElementType.EMPTY) {
				// 20201202 则直接返回
				return;
			}

			// 20201202 如果开始数组大小为实际元素个数, 达到临界, 进行扩容
			if (this.start.length == this.size) {
				// 20201202 对开始数组扩容+6
				this.start = expand(this.start);

				// 20201202 对结束数组扩容+6
				this.end = expand(this.end);

				// 20201202 对元素类型数组扩容
				this.type = expand(this.type);

				// 20201202 对目标结果数组扩容
				this.resolved = expand(this.resolved);
			}

			// 20201202 如果存在值操作器
			if (valueProcessor != null) {
				// 20201202 如果结果数组为空
				if (this.resolved == null) {
					// 20201202 则初始化开始数组为结果数组
					this.resolved = new CharSequence[this.start.length];
				}

				// 20201202 操作器处理子序列 start~end-1
				CharSequence resolved = valueProcessor.apply(this.source.subSequence(start, end));

				// 20201202 不适用操作器重新构造resolvedElements
				Elements resolvedElements = new ElementsParser(resolved, '.').parse();

				// 20201202 这种情况下长度必须大于1
				Assert.state(resolvedElements.getSize() == 1, "Resolved element must not contain multiple elements");

				// 20201202 获取第一个元素, 添加到结果数组末尾
				this.resolved[this.size] = resolvedElements.get(0);

				// 20201202 获取第一个元素类型作为元素类型
				type = resolvedElements.getType(0);
			}

			// 20201202 添加开始索引到开始数组
			this.start[this.size] = start;

			// 20201202 添加结束索引到结束数组
			this.end[this.size] = end;

			// 20201202 添加元素类型到元素类型数组
			this.type[this.size] = type;

			// 20201202 实际元素个数+1
			this.size++;
		}

		// 20201202 扩充数组
		private int[] expand(int[] src) {
			// 20201202 目标数组长度 = 当前数组长度 + 默认容量6
			int[] dest = new int[src.length + DEFAULT_CAPACITY];

			// 20201202 复制srcPos~src.length-1到destPos~dest.length-1
			System.arraycopy(src, 0, dest, 0, src.length);
			return dest;
		}

		// 20201202 对元素类型数组扩容
		private ElementType[] expand(ElementType[] src) {
			// 20201202 目标数组长度 = 当前数组长度 + 默认容量6
			ElementType[] dest = new ElementType[src.length + DEFAULT_CAPACITY];

			// 20201202 复制srcPos~src.length-1到destPos~dest.length-1
			System.arraycopy(src, 0, dest, 0, src.length);
			return dest;
		}

		// 20201202 对目标结果数组扩容
		private CharSequence[] expand(CharSequence[] src) {
			// 20201202 如果结果数组为空, 则不做处理
			if (src == null) {
				return null;
			}

			// 20201202 目标数组长度 = 当前数组长度 + 默认容量6
			CharSequence[] dest = new CharSequence[src.length + DEFAULT_CAPACITY];

			// 20201202 复制srcPos~src.length-1到destPos~dest.length-1
			System.arraycopy(src, 0, dest, 0, src.length);
			return dest;
		}

		// 20201202 判断当前索引的char是否合法
		static boolean isValidChar(char ch, int index) {
		    // 20201202 如果为小写字母, 数字, 或者-分隔符, 则都是合法的
			return isAlpha(ch) || isNumeric(ch) || (index != 0 && ch == '-');
		}

		static boolean isAlphaNumeric(char ch) {
			return isAlpha(ch) || isNumeric(ch);
		}

		// 20201202 判断当前char是否为小写字母: a~z
		private static boolean isAlpha(char ch) {
			return ch >= 'a' && ch <= 'z';
		}

		// 20201202 判断当前char是否数字: 0~9
		private static boolean isNumeric(char ch) {
			return ch >= '0' && ch <= '9';
		}

	}

	/**
	 * The various types of element that we can detect.
	 */
	// 20201202 我们能探测到的各种元素。
	private enum ElementType {

		/**
		 * The element is logically empty (contains no valid chars).
		 */
		// 20201202 元素在逻辑上为空（不包含有效字符）。
		EMPTY(false),

		/**
		 * The element is a uniform name (a-z, 0-9, no dashes, lowercase).
		 */
		// 20201202 元素是一个统一的名称（a-z，0-9，无虚线，小写）。
		UNIFORM(false),

		/**
		 * The element is almost uniform, but it contains (but does not start with) at
		 * least one dash.
		 */
		// 20201202 元素几乎是一致的，但它至少包含一个破折号（但不以破折号开头）。
		DASHED(false),

		/**
		 * The element contains non uniform characters and will need to be converted.
		 */
		// 20201202 元素包含非统一字符，需要转换。
		NON_UNIFORM(false),

		/**
		 * The element is non-numerically indexed.
		 */
		// 20201202 元素没有数字索引。
		INDEXED(true),

		/**
		 * The element is numerically indexed.
		 */
		// 20201202 元素被数字索引。
		NUMERICALLY_INDEXED(true);

		private final boolean indexed;

		ElementType(boolean indexed) {
			this.indexed = indexed;
		}

		public boolean isIndexed() {
			return this.indexed;
		}

		public boolean allowsFastEqualityCheck() {
			return this == UNIFORM || this == NUMERICALLY_INDEXED;
		}

		public boolean allowsDashIgnoringEqualityCheck() {
			return allowsFastEqualityCheck() || this == DASHED;
		}

	}

	/**
	 * Predicate used to filter element chars.
	 */
	private interface ElementCharPredicate {

		boolean test(char ch, int index);

	}

}
