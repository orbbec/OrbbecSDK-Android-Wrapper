package com.orbbec.obsensor.types;

/**
 * \if English
 * Configuration Item for the filter
 * \else
 * Filter配置项
 * \endif
 */
public class FilterConfigSchemaItem {
    /**
     * \if English
     * Name of the configuration item
     * \else
     * 配置项名称
     * \endif
     */
    private String name;
    /**
     * \if English
     * Value type of the configuration item {@link FilterConfigValueType#value()}
     * \else
     * 配置项值类型 {@link FilterConfigValueType#value()}
     * \endif
     */
    private int type;
    /**
     * \if English
     * Minimum value casted to double
     * \else
     * 最小值转换为双精度值
     * \endif
     */
    private double min;
    /**
     * \if English
     * Maximum value casted to double
     * \else
     * 最大值转换为双精度值
     * \endif
     */
    private double max;
    /**
     * \if English
     * Step value casted to double
     * \else
     * 步进值转换为双精度值
     * \endif
     */
    private double step;
    /**
     * \if English
     * Default value casted to double
     * \else
     * 默认值转换为双精度值
     * \endif
     */
    private double def;
    /**
     * \if English
     * Description of the configuration item
     * \else
     * 配置项描述
     * \endif
     */
    private String desc;

    public FilterConfigSchemaItem() {
    }

    public String getName() {
        return name;
    }

    public FilterConfigValueType getType() {
        return FilterConfigValueType.get(type);
    }

    public double getMin() {
        return min;
    }

    public double getMax() {
        return max;
    }

    public double getStep() {
        return step;
    }

    public double getDef() {
        return def;
    }

    public String getDesc() {
        return desc;
    }
}
