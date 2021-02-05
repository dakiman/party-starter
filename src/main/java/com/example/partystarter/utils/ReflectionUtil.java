package com.example.partystarter.utils;

import lombok.extern.slf4j.Slf4j;
import java.lang.reflect.Field;

import static org.springframework.util.ReflectionUtils.findField;

@Slf4j
public class ReflectionUtil {

    public static String getFieldValue(Object object, String fieldName){
        Class<?> clazz = object.getClass();
        Field field = findField(clazz, fieldName);

        try {
            return field.get(object).toString();
        } catch (IllegalAccessException | NullPointerException e) {
            return null;
        }
    }

}
