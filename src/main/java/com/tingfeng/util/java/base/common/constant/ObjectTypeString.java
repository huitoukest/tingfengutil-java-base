package com.tingfeng.util.java.base.common.constant;

/**
 * 常见的java Object的class的名称
 *
 * @deprecated 使用 {@link PrimitiveType} 替代，
 * 通过 PrimitiveType.fromPrimitive()/fromWrapper() 获取对应类型信息。
 * String 和 Date 类型请直接使用对应的 Class 字面量。
 * @author huitoukest
 */
@Deprecated
public class ObjectTypeString {
	/**
	 * 包装类型的类名,对应Type
	 */
	public final static String 
			clsNameBoolean ="java.lang.Boolean",
			clsNameDate ="java.util.Date",
			clsNameFloat ="java.lang.Float",
			clsNameDouble ="java.lang.Double",
			clsNameLong = "java.lang.Long",
			clsNameInteger ="java.lang.Integer",
			clsNameString ="java.lang.String",				
			clsNameShort ="java.lang.Short",
			clsNameByte ="java.lang.Byte";
	/**
	 * 基础数据类型的类名,对应Type
	 */
	public final static String 
		clsNameBaseBoolean ="boolean",
		clsNameBaseFloat ="float",
		clsNameBaseDouble ="double",
		clsNameBaseLong = "long",
		clsNameBaseInt ="int",				
		clsNameBaseShort ="short",
		clsNameBaseByte ="byte";

}
