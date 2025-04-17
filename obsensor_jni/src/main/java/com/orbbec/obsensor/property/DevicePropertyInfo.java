package com.orbbec.obsensor.property;

import com.orbbec.obsensor.types.PermissionType;

/**
 * \if English
 * Used to describe the characteristics of each property
 * \else
 * 用来描述每个属性的特性
 * \endif
 */
public class DevicePropertyInfo {
    /**
     * \if English
     * Property ID
     * \else
     * 属性ID
     * \endif
     */
    private int propertyID;
    /**
     * \if English
     * Property name
     * \else
     * 属性名称
     * \endif
     */
    private String propertyName;
    /**
     * \if English
     * Property type
     * \else
     * 属性类型
     * \endif
     */
    private int propertyTypeID;
    /**
     * \if English
     * Property permission
     * \else
     * 属性权限
     * \endif
     */
    private int permissionID;

    /**
     * \if English
     * get property
     *
     * @return return property {@link DeviceProperty}
     * \else
     * 获取属性
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
     * @return 返回属性的权限类型 {@link PermissionType}
     * \endif
     */
    public PermissionType getPermissionType() {
        return PermissionType.get(permissionID);
    }
}
