package com.orbbec.obsensor;

/**
 * \if English
 * Characteristics used to describe each property
 * \else
 * 用于描述每一个属性的特性
 * \endif
 */
public class DevicePropertyInfo {
    int propertyID;
    String propertyName;
    int propertyTypeID;
    int permissionID;

    /** 
	 * \if English
	 * get property
     *
     * @return return property {@link DeviceProperty}
	 * \else
     * 获取属性
     *
     * @return 返回属性 {@link DeviceProperty}
	 * \endif
     */
    public DeviceProperty getProperty() {
        return DeviceProperty.get(propertyID);
    }

    /**
	 * \if English
	 * get property name
     *
     * @return return property name
	 * \else
     * 获取属性名称
     *
     * @return 返回属性名称
	 * \endif
     */
    public String getPropertyName() {
        return propertyName;
    }

    /**
	 * \if English
     * get property type
     *
     * @return returns property type {@link PropertyType}
	 * \else
     * 获取属性类型
     *
     * @return 返回属性类型 {@link PropertyType}
	 * \endif
     */
    public PropertyType getPropertyType() {
        return PropertyType.get(propertyTypeID);
    }

    /**
	 * \if English
     * Get the permission type of the property
     *
     * @return Returns the attribute's permission type {@link PermissionType}
	 * \else
     * 获取属性的权限类型
     *
     * @return 返回属性的权限类型 {@link PermissionType}
	 * \endif
     */
    public PermissionType getPermissionType() {
        return PermissionType.get(permissionID);
    }
}
