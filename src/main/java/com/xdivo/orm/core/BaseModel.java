package com.xdivo.orm.core;

import com.xdivo.orm.mapping.ColumnMap;
import com.xdivo.orm.mapping.ModelMap;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Model基类
 * Created by liujunjie on 16-7-23.
 */
public class BaseModel {

    protected ModelMap modelMap;

    //实体与表映射
    protected Map<Class<?>, String> TABLE_MAP = new HashMap<>();

    //属性与字段映射
    protected Map<String, String> PROPERTY_MAP = new HashMap<>();

    //数据库字段与属性名映射
    protected Map<String, String> FIELD_MAP = new HashMap<>();

    //实体主键映射
    protected Map<Class<?>, String> PK_MAP = new HashMap<>();

    //数据库字段列表映射
    protected Map<Class<?>, List<String>> FIELDS_MAP = new HashMap<>();

    //getter映射
    protected Map<String, Method> GETTERS_MAP = new HashMap<>();

    //setter映射
    protected Map<String, Method> SETTERS_MAP = new HashMap<>();

    public BaseModel() {
        Class<?> modelClass = this.getClass();
        this.modelMap = Register.modelMapping.get(modelClass);

        List<String> fields = new ArrayList<>();

        TABLE_MAP.put(modelClass, modelMap.getTable());

        PK_MAP.put(modelClass, modelMap.getPrimaryKey());

        for (ColumnMap columnMap : modelMap.getColumnMaps()) {
            PROPERTY_MAP.put(columnMap.getProperty(), columnMap.getField());

            FIELD_MAP.put(columnMap.getField(), columnMap.getProperty());

            GETTERS_MAP.put(columnMap.getProperty(), columnMap.getGetter());

            SETTERS_MAP.put(columnMap.getProperty(), columnMap.getSetter());

            fields.add(columnMap.getField());
        }

        PK_MAP.put(modelClass, modelMap.getPrimaryKey());

        FIELDS_MAP.put(modelClass, fields);

    }
}
