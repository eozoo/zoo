package com.cowave.commons.framework.util;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.text.NumberFormat;
import java.util.Set;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author shanhuiming
 *
 */
public class Converts {

	/**
	 * 获取参数不为空值
	 */
	public static <T> T nvl(T value, T defaultValue){
		return value != null ? value : defaultValue;
	}

	/**
	 * 类型强转
	 */
	@SuppressWarnings("unchecked")
	public static <T> T cast(Object obj){
		return (T) obj;
	}

	/**
	 * 转换为字符串
	 */
	public static String toStr(Object value, String defaultValue){
		if (null == value){
			return defaultValue;
		}

		if (value instanceof String){
			return (String) value;
		}
		return value.toString();
	}

	/**
	 * 转换为字符串，可能返回null
	 */
	public static String toStr(Object value){
		return toStr(value, null);
	}

	/**
	 * 转换为字符
	 */
	public static Character toChar(Object value, Character defaultValue){
		if (null == value){
			return defaultValue;
		}

		if (value instanceof Character){
			return (Character) value;
		}

		final String valueStr = toStr(value, null);
		return StringUtils.isEmpty(valueStr) ? defaultValue : valueStr.charAt(0);
	}

	/**
	 * 转换为字符
	 */
	public static Character toChar(Object value){
		return toChar(value, null);
	}

	/**
	 * 转换为byte
	 */
	public static Byte toByte(Object value, Byte defaultValue){
		if (value == null){
			return defaultValue;
		}
		if (value instanceof Byte){
			return (Byte) value;
		}
		if (value instanceof Number){
			return ((Number) value).byteValue();
		}

		final String valueStr = toStr(value, null);
		if (StringUtils.isEmpty(valueStr)){
			return defaultValue;
		}

		try{
			return Byte.parseByte(valueStr);
		}catch (Exception e){
			return defaultValue;
		}
	}

	/**
	 * 转换为byte，出错返回null
	 */
	public static Byte toByte(Object value){
		return toByte(value, null);
	}

	/**
	 * 转换为Short
	 */
	public static Short toShort(Object value, Short defaultValue){
		if (value == null){
			return defaultValue;
		}
		if (value instanceof Short){
			return (Short) value;
		}
		if (value instanceof Number){
			return ((Number) value).shortValue();
		}

		final String valueStr = toStr(value, null);
		if (StringUtils.isEmpty(valueStr)){
			return defaultValue;
		}

		try{
			return Short.parseShort(valueStr.trim());
		}catch (Exception e){
			return defaultValue;
		}
	}

	/**
	 * 转换为Short，出错返回null
	 */
	public static Short toShort(Object value){
		return toShort(value, null);
	}

	/**
	 * 转换为Number
	 */
	public static Number toNumber(Object value, Number defaultValue){
		if (value == null){
			return defaultValue;
		}
		if (value instanceof Number){
			return (Number) value;
		}

		final String valueStr = toStr(value, null);
		if (StringUtils.isEmpty(valueStr)){
			return defaultValue;
		}

		try{
			return NumberFormat.getInstance().parse(valueStr);
		}catch (Exception e){
			return defaultValue;
		}
	}

	/**
	 * 转换为Number，出错返回null
	 */
	public static Number toNumber(Object value){
		return toNumber(value, null);
	}

	/**
	 * 转换为int
	 */
	public static Integer toInt(Object value, Integer defaultValue){
		if (value == null){
			return defaultValue;
		}
		if (value instanceof Integer){
			return (Integer) value;
		}
		if (value instanceof Number){
			return ((Number) value).intValue();
		}

		final String valueStr = toStr(value, null);
		if (StringUtils.isEmpty(valueStr)){
			return defaultValue;
		}

		try{
			return Integer.parseInt(valueStr.trim());
		}catch (Exception e){
			return defaultValue;
		}
	}

	/**
	 * 转换为int，出错返回null
	 */
	public static Integer toInt(Object value){
		return toInt(value, null);
	}

	/**
	 * 转换为Integer数组，默认分隔符","
	 */
	public static Integer[] toIntArray(String str){
		return toIntArray(",", str);
	}

	/**
	 * 转换为Long数组，默认分隔符","
	 */
	public static Long[] toLongArray(String str){
		return toLongArray(",", str);
	}

	/**
	 * 转换为String数组，默认分隔符","
	 */
	public static String[] toStrArray(String str){
		return toStrArray(",", str);
	}

	/**
	 * 转换为Integer数组
	 */
	public static Integer[] toIntArray(String split, String str){
		if (StringUtils.isEmpty(str)){
			return new Integer[] {};
		}

		String[] arr = str.split(split);
		final Integer[] ints = new Integer[arr.length];
		for (int i = 0; i < arr.length; i++){
			final Integer v = toInt(arr[i], 0);
			ints[i] = v;
		}
		return ints;
	}

	/**
	 * 转换为Long数组
	 */
	public static Long[] toLongArray(String split, String str){
		if (StringUtils.isEmpty(str)){
			return new Long[] {};
		}

		String[] arr = str.split(split);
		final Long[] longs = new Long[arr.length];
		for (int i = 0; i < arr.length; i++){
			final Long v = toLong(arr[i], null);
			longs[i] = v;
		}
		return longs;
	}

	/**
	 * 转换为String数组
	 */
	public static String[] toStrArray(String split, String str){
		return str.split(split);
	}

	/**
	 * 转换为long
	 */
	public static Long toLong(Object value, Long defaultValue){
		if (value == null){
			return defaultValue;
		}
		if (value instanceof Long){
			return (Long) value;
		}
		if (value instanceof Number){
			return ((Number) value).longValue();
		}

		final String valueStr = toStr(value, null);
		if (StringUtils.isEmpty(valueStr)){
			return defaultValue;
		}

		try{
			return new BigDecimal(valueStr.trim()).longValue();
		}catch (Exception e){
			return defaultValue;
		}
	}

	/**
	 * 转换为long，出错返回null
	 */
	public static Long toLong(Object value){
		return toLong(value, null);
	}

	/**
	 * 转换为double
	 */
	public static Double toDouble(Object value, Double defaultValue){
		if (value == null){
			return defaultValue;
		}
		if (value instanceof Double){
			return (Double) value;
		}
		if (value instanceof Number){
			return ((Number) value).doubleValue();
		}

		final String valueStr = toStr(value, null);
		if (StringUtils.isEmpty(valueStr)){
			return defaultValue;
		}

		try{
			return new BigDecimal(valueStr.trim()).doubleValue();
		}catch (Exception e){
			return defaultValue;
		}
	}

	/**
	 * 转换为double，出错返回null
	 */
	public static Double toDouble(Object value){
		return toDouble(value, null);
	}

	/**
	 * 转换为Float
	 */
	public static Float toFloat(Object value, Float defaultValue){
		if (value == null){
			return defaultValue;
		}
		if (value instanceof Float){
			return (Float) value;
		}
		if (value instanceof Number){
			return ((Number) value).floatValue();
		}

		final String valueStr = toStr(value, null);
		if (StringUtils.isEmpty(valueStr)){
			return defaultValue;
		}

		try{
			return Float.parseFloat(valueStr.trim());
		}catch (Exception e){
			return defaultValue;
		}
	}

	/**
	 * 转换为Float，出错返回null
	 */
	public static Float toFloat(Object value){
		return toFloat(value, null);
	}

	/**
	 * 转换为boolean，支持：true、false、yes、ok、no，1,0
	 */
	public static Boolean toBool(Object value, Boolean defaultValue){
		if (value == null){
			return defaultValue;
		}
		if (value instanceof Boolean){
			return (Boolean) value;
		}

		String valueStr = toStr(value, null);
		if (StringUtils.isEmpty(valueStr)){
			return defaultValue;
		}

		valueStr = valueStr.trim().toLowerCase();
		return switch (valueStr) {
			case "true" -> true;
			case "false" -> false;
			case "yes" -> true;
			case "ok" -> true;
			case "no" -> false;
			case "1" -> true;
			case "0" -> false;
			default -> defaultValue;
		};
	}

	/**
	 * 转换为boolean，出错返回null
	 */
	public static Boolean toBool(Object value){
		return toBool(value, null);
	}

	/**
	 * 转换为Enum
	 */
	public static <E extends Enum<E>> E toEnum(Class<E> clazz, Object value, E defaultValue){
		if (value == null){
			return defaultValue;
		}

		if (clazz.isAssignableFrom(value.getClass())){
			@SuppressWarnings("unchecked")
			E e = (E) value;
			return e;
		}

		final String valueStr = toStr(value, null);
		if (StringUtils.isEmpty(valueStr)){
			return defaultValue;
		}

		try{
			return Enum.valueOf(clazz, valueStr);
		}catch (Exception e){
			return defaultValue;
		}
	}

	/**
	 * 转换为Enum，出错返回null
	 */
	public static <E extends Enum<E>> E toEnum(Class<E> clazz, Object value){
		return toEnum(clazz, value, null);
	}

	/**
	 * 转换为BigInteger
	 */
	public static BigInteger toBigInteger(Object value, BigInteger defaultValue){
		if (value == null){
			return defaultValue;
		}
		if (value instanceof BigInteger){
			return (BigInteger) value;
		}
		if (value instanceof Long){
			return BigInteger.valueOf((Long) value);
		}

		final String valueStr = toStr(value, null);
		if (StringUtils.isEmpty(valueStr)){
			return defaultValue;
		}

		try{
			return new BigInteger(valueStr);
		}catch (Exception e){
			return defaultValue;
		}
	}

	/**
	 * 转换为BigInteger，出错返回null
	 */
	public static BigInteger toBigInteger(Object value){
		return toBigInteger(value, null);
	}

	/**
	 * 转换为BigDecimal
	 */
	public static BigDecimal toBigDecimal(Object value, BigDecimal defaultValue){
		if (value == null){
			return defaultValue;
		}
		if (value instanceof BigDecimal){
			return (BigDecimal) value;
		}
		if (value instanceof Long){
			return new BigDecimal((Long) value);
		}
		if (value instanceof Double){
			return BigDecimal.valueOf((Double)value);
		}
		if (value instanceof Integer){
			return new BigDecimal((Integer) value);
		}

		final String valueStr = toStr(value, null);
		if (StringUtils.isEmpty(valueStr)){
			return defaultValue;
		}

		try{
			return new BigDecimal(valueStr);
		}catch (Exception e){
			return defaultValue;
		}
	}

	/**
	 * 转换为BigDecimal，出错返回null
	 */
	public static BigDecimal toBigDecimal(Object value){
		return toBigDecimal(value, null);
	}

	/**
	 * 将对象转为字符串
	 * <p>Byte数组和ByteBuffer会被转换为对应字符串的数组； 对象数组会调用Arrays.toString方法
	 */
	public static String utf8Str(Object obj){
		return str(obj, "UTF-8");
	}

	/**
	 * 将对象转为字符串
	 * <p>Byte数组和ByteBuffer会被转换为对应字符串的数组； 对象数组会调用Arrays.toString方法
	 */
	public static String str(Object obj, String charsetName){
		return str(obj, Charset.forName(charsetName));
	}

	/**
	 * 将对象转为字符串
	 * <p>Byte数组和ByteBuffer会被转换为对应字符串的数组； 对象数组会调用Arrays.toString方法
	 */
	public static String str(Object obj, Charset charset){
		if (null == obj){
			return null;
		}

		if (obj instanceof String){
			return (String) obj;
		}else if (obj instanceof byte[]){
			return str((byte[]) obj, charset);
		}else if (obj instanceof Byte[]){
			byte[] bytes = ArrayUtils.toPrimitive((Byte[]) obj);
			return str(bytes, charset);
		}else if (obj instanceof ByteBuffer){
			return str((ByteBuffer) obj, charset);
		}
		return obj.toString();
	}

	/**
	 * 将byte数组转为字符串
	 */
	public static String str(byte[] bytes, String charset){
		return str(bytes, StringUtils.isEmpty(charset) ? Charset.defaultCharset() : Charset.forName(charset));
	}

	/**
	 * 将byte数组转为字符串
	 */
	public static String str(byte[] data, Charset charset){
		if (data == null){
			return null;
		}

		if (null == charset){
			return new String(data);
		}
		return new String(data, charset);
	}

	/**
	 * 将byteBuffer数组转为字符串
	 */
	public static String str(ByteBuffer data, String charset){
		if (data == null){
			return null;
		}

		return str(data, Charset.forName(charset));
	}

	/**
	 * 将byteBuffer数组转为字符串
	 */
	public static String str(ByteBuffer data, Charset charset){
		if (null == charset){
			charset = Charset.defaultCharset();
		}
		return charset.decode(data).toString();
	}

	/**
	 * 半角转全角
	 */
	public static String toFullWidth(String input){
		return toFullWidth(input, null);
	}

	/**
	 * 半角转全角
	 */
	public static String toFullWidth(String input, Set<Character> notConvertSet){
		char[] c = input.toCharArray();
		for (int i = 0; i < c.length; i++){
			if (null != notConvertSet && notConvertSet.contains(c[i])){
				continue; // 跳过不替换的字符
			}

			if (c[i] == ' '){
				c[i] = '\u3000';
			}else if (c[i] < '\177'){
				c[i] = (char) (c[i] + 65248);
			}
		}
		return new String(c);
	}

	/**
	 * 全角转半角
	 */
	public static String toHalfWidth(String input){
		return toHalfWidth(input, null);
	}

	/**
	 * 全角转半角
	 */
	public static String toHalfWidth(String text, Set<Character> notConvertSet){
		char[] c = text.toCharArray();
		for (int i = 0; i < c.length; i++){
			if (null != notConvertSet && notConvertSet.contains(c[i])){
				continue; // 跳过不替换的字符
			}

			if (c[i] == '\u3000'){
				c[i] = ' ';
			}else if (c[i] > '\uFF00' && c[i] < '\uFF5F'){
				c[i] = (char) (c[i] - 65248);
			}
		}
		return new String(c);
	}
}
