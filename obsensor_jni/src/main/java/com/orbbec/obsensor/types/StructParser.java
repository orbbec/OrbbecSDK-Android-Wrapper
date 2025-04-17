package com.orbbec.obsensor.types;

import android.util.Log;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.Comparator;

public class StructParser {
    private static final String TAG = "StructParser";

    static boolean parseBytes(Object obj, byte[] bytes) {
        try {
            Field[] fields = obj.getClass().getDeclaredFields();
            fields = Arrays.stream(fields)
                    .filter(f -> f.isAnnotationPresent(StructField.class))
                    .toArray(Field[]::new);
            Arrays.sort(fields, Comparator.comparingInt(Field::getModifiers));

            for (Field field : fields) {
                StructField annotation = field.getAnnotation(StructField.class);
                if (annotation == null) {
                    throw new IllegalArgumentException("Field " + field.getName() + " is missing @StructField annotation");
                }
                int offset = annotation.offset();
                int size = annotation.size();

                if (offset + size > bytes.length) {
                    throw new IllegalArgumentException("Byte array too small for field: " + field.getName());
                }

                field.setAccessible(true);

                if (field.getType().isArray()) {
                    Class<?> componentType = field.getType().getComponentType();
                    if (componentType == null) {
                        throw new IllegalArgumentException("Field is not an array but has arraySize set: " + field.getName());
                    }

                    int arraySize = annotation.arraySize();
                    int elementSize = size / arraySize;

                    if (componentType.isArray()) {
                        int rows = annotation.rows();
                        int cols = arraySize / rows;

                        Object array2D = field.get(obj);
                        if (array2D == null) {
                            array2D = Array.newInstance(componentType, rows);
                        }

                        Class<?> baseComponentType = componentType.getComponentType();
                        if (baseComponentType == null) {
                            throw new IllegalArgumentException("Component type cannot be null");
                        }

                        for (int row = 0; row < rows; row++) {
                            Object rowArray = Array.get(array2D, row);
                            if (rowArray == null) {
                                rowArray = Array.newInstance(baseComponentType, cols);
                            }

                            for (int col = 0; col < cols; col++) {
                                int elementOffset = offset + (row * cols + col) * elementSize;
                                byte[] elementBytes = Arrays.copyOfRange(bytes, elementOffset, elementOffset + elementSize);
                                Object element = baseComponentType.getDeclaredConstructor().newInstance();
                                parseBytes(element, elementBytes);
                                Array.set(rowArray, col, element);
                            }
                            Array.set(array2D, row, rowArray);
                        }
                        field.set(obj, array2D);
                    } else {
                        Object array = field.get(obj);
                        if (array == null) {
                            array = Array.newInstance(componentType, arraySize);
                        }

                        if (componentType.isPrimitive()) {
                            for (int i = 0; i < arraySize; i++) {
                                byte[] elementBytes = Arrays.copyOfRange(bytes, offset + i * elementSize, offset + (i + 1) * elementSize);
                                Object element = parseFieldValue(componentType, elementBytes, elementSize);
                                Array.set(array, i, element);
                            }
                        } else {
                            for (int i = 0; i < arraySize; i++) {
                                byte[] elementBytes = Arrays.copyOfRange(bytes, offset + i * elementSize, offset + (i + 1) * elementSize);
                                Object element = componentType.getDeclaredConstructor().newInstance();
                                parseBytes(element, elementBytes);
                                Array.set(array, i, element);
                            }
                        }

                        field.set(obj, array);
                    }
                } else {
                    byte[] fieldBytes = Arrays.copyOfRange(bytes, offset, offset + size);
                    if (field.getType().isPrimitive() || field.getType() == String.class
                            || field.getType().isEnum()) {
                        Object value = parseFieldValue(field.getType(), fieldBytes, size);
                        field.set(obj, value);
                    } else {
                        Object nestedObject = field.get(obj);
                        if (nestedObject == null) {
                            nestedObject = field.getType().getDeclaredConstructor().newInstance();
                        }
                        parseBytes(nestedObject, fieldBytes);
                        field.set(obj, nestedObject);
                    }
                }
            }
            return true;
        } catch (Exception e) {
            Log.e("StructParser", "Failed to parse bytes: " + e.getMessage(), e);
            return false;
        }
    }

    static boolean wrapBytes(Object obj, byte[] bytes) {
        try {
            Field[] fields = obj.getClass().getDeclaredFields();

            fields = Arrays.stream(fields)
                    .filter(f -> f.isAnnotationPresent(StructField.class))
                    .toArray(Field[]::new);

            for (Field field : fields) {
                StructField annotation = field.getAnnotation(StructField.class);
                if (annotation == null) {
                    throw new IllegalArgumentException("Field " + field.getName() + " is missing @StructField annotation");
                }
                int offset = annotation.offset();
                int size = annotation.size();

                if (offset + size > bytes.length) {
                    throw new IllegalArgumentException("Byte array too small for field: " + field.getName());
                }

                field.setAccessible(true);

                if (field.getType().isArray()) {
                    Object array = field.get(obj);
                    if (array == null) {
                        throw new IllegalArgumentException("Field array is null: " + field.getName());
                    }

                    Class<?> componentType = field.getType().getComponentType();
                    if (componentType == null) {
                        throw new IllegalArgumentException("Field is not an array: " + field.getName());
                    }

                    int arraySize = annotation.arraySize();
                    int elementSize = size / arraySize;

                    if (componentType.isArray()) {
                        int rows = annotation.rows();
                        int cols = arraySize / rows;

                        for (int row = 0; row < rows; row++) {
                            Object rowArray = Array.get(array, row);
                            if (rowArray == null) {
                                throw new IllegalArgumentException("Row array is null at index: " + row);
                            }

                            for (int col = 0; col < cols; col++) {
                                Object element = Array.get(rowArray, col);
                                byte[] elementBytes = new byte[elementSize];
                                wrapBytes(element, elementBytes);
                                int elementOffset = offset + (row * cols + col) * elementSize;
                                System.arraycopy(elementBytes, 0, bytes, elementOffset, elementSize);
                            }
                        }
                    } else {
                        if (componentType.isPrimitive()) {
                            for (int i = 0; i < arraySize; i++) {
                                Object element = Array.get(array, i);
                                byte[] elementBytes = wrapFieldValue(componentType, element, elementSize);
                                System.arraycopy(elementBytes, 0, bytes, offset + i * elementSize, elementSize);
                            }
                        } else {
                            for (int i = 0; i < arraySize; i++) {
                                Object element = Array.get(array, i);
                                byte[] elementBytes = new byte[elementSize];
                                wrapBytes(element, elementBytes);
                                System.arraycopy(elementBytes, 0, bytes, offset + i * elementSize, elementSize);
                            }
                        }
                    }
                } else if (field.getType().isPrimitive() || field.getType() == String.class
                        || field.getType().isEnum()) {
                    Object value = field.get(obj);
                    byte[] fieldBytes = wrapFieldValue(field.getType(), value, size);
                    System.arraycopy(fieldBytes, 0, bytes, offset, size);
                } else {
                    Object nestedObject = field.get(obj);
                    byte[] nestedBytes = new byte[size];
                    wrapBytes(nestedObject, nestedBytes);
                    System.arraycopy(nestedBytes, 0, bytes, offset, size);
                }
            }
            return true;
        } catch (Exception e) {
            Log.e("StructParser", "Failed to wrap bytes: " + e.getMessage(), e);
            return false;
        }
    }

    private static Object parseFieldValue(Class<?> fieldType, byte[] bytes, int size) {
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        buffer.order(ByteOrder.nativeOrder());
        if (fieldType == byte.class || fieldType == Byte.class) {
            return bytes[0];
        } else if (fieldType == short.class || fieldType == Short.class) {
//            if (size == 2)
//                return buffer.getShort();
//            else if (size == 1)
//                return (short) (bytes[0] & 0xFF);
            return size == 2 ? buffer.getShort() : (short) (bytes[0] & 0xFF);
        } else if (fieldType == int.class || fieldType == Integer.class) {
//            if (size == 4)
//                return buffer.getInt();
//            else if (size == 2)
//                return buffer.getShort() & 0xFFFF;
            return size == 4 ? buffer.getInt() : (buffer.getShort() & 0xFFFF);
        } else if (fieldType == long.class || fieldType == Long.class) {
//            if (size == 8)
//                return buffer.getLong();
//            else if (size == 4)
//                return buffer.getInt() & 0xFFFFFFFFL;
            return size == 8 ? buffer.getLong() : (buffer.getInt() & 0xFFFFFFFFL);
        } else if (fieldType == float.class || fieldType == Float.class) {
            return buffer.getFloat();
        } else if (fieldType == double.class || fieldType == Double.class) {
            return buffer.getDouble();
        } else if (fieldType == String.class) {
            return new String(bytes).trim();
        } else if (fieldType.isEnum()) {
            int value = buffer.getInt();
            try {
                Method method = fieldType.getMethod("get", int.class);
                return method.invoke(null, value);
            } catch (Exception e) {
                throw new UnsupportedOperationException(
                        "Failed to parse enum value for type: " + fieldType.getName(), e);
            }
        }
        throw new UnsupportedOperationException("Unsupported field type: " + fieldType.getName());
    }

    private static byte[] wrapFieldValue(Class<?> fieldType, Object value, int size) {
        ByteBuffer buffer = ByteBuffer.allocate(size);
        buffer.order(ByteOrder.nativeOrder());
        if (fieldType == byte.class || fieldType == Byte.class) {
            buffer.put((byte) value);
        } else if (fieldType == short.class || fieldType == Short.class) {
            if (size == 2)
                buffer.putShort((short) value);
            else if (size == 1)
                buffer.put((byte) ((short) value & 0xFF));
        } else if (fieldType == int.class || fieldType == Integer.class) {
            if (size == 4)
                buffer.putInt((int) value);
            else if (size == 2) {
                buffer.putShort((short) ((int) value & 0xFFFF));
//                buffer.put((byte) (((int) value >> 8) & 0xFF));
//                buffer.put((byte) (((int) value & 0xFF)));
            }
        } else if (fieldType == long.class || fieldType == Long.class) {
            if (size == 8)
                buffer.putLong((long) value);
            else if (size == 4) {
                buffer.putInt((int) ((long) value & 0xFFFFFFFFL));
//                for (int i = 3; i >= 0; i--) {
//                    buffer.put((byte) (((long) value >> (8 * i)) & 0xFF));
//                }
            }
        } else if (fieldType == float.class || fieldType == Float.class) {
            buffer.putFloat((float) value);
        } else if (fieldType == double.class || fieldType == Double.class) {
            buffer.putDouble((double) value);
        } else if (fieldType == String.class) {
            byte[] strBytes = ((String) value).getBytes();
            if (strBytes.length > size) {
                throw new IllegalArgumentException("String value is too large for the field");
            }
            buffer.put(strBytes);
        } else if (fieldType.isEnum()) {
            try {
                Method method = fieldType.getMethod("value");
                Object result = method.invoke(value);
                if (result == null) {
                    throw new IllegalArgumentException("Enum value() method returned null for type: " + fieldType.getName());
                }
                int enumValue = (int) result;
                buffer.putInt(enumValue);
            } catch (Exception e) {
                throw new UnsupportedOperationException(
                        "Failed to parse enum value for type: " + fieldType.getName(), e);
            }
        }
        return buffer.array();
    }
}
