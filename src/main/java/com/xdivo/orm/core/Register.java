package com.xdivo.orm.core;

import com.xdivo.orm.annotation.Column;
import com.xdivo.orm.annotation.Entity;
import com.xdivo.orm.annotation.PK;
import com.xdivo.orm.utils.Scanner;
import com.xdivo.orm.utils.SpringUtils;
import org.apache.log4j.Logger;
import org.springframework.jdbc.core.JdbcTemplate;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Model注册类
 * Created by jaleel on 16-7-19.
 */

public class Register {

    private final static Logger log = Logger.getLogger(Register.class);

    //实体与表映射
    public static Map<Class<?>, String> TABLE_MAP = new HashMap<>();

    //属性与字段映射
    public static Map<String, String> PROPERTY_MAP = new HashMap<>();

    //数据库字段与属性名映射
    public static Map<String, String> DATA_MAP = new HashMap<>();

    //实体主键映射
    public static Map<Class<?>, String> PK_MAP = new HashMap<>();

    //表名与主键映射
    public static Map<String, String> TABLE_PK_MAP = new HashMap<>();

    //数据库字段列表映射
    public static Map<Class<?>, List<String>> FIELDS_MAP = new HashMap<>();

    //getter映射
    public static Map<String, Method> GETTERS_MAP = new HashMap<>();

    //setter映射
    public static Map<String, Method> SETTERS_MAP = new HashMap<>();

    /**
     * 注册model
     * @param basePackage 扫描的包
     * @throws IOException
     */
    public static void registerModel(String basePackage) throws IOException {
        //扫描包里类
        List<Class<?>> clazzs = Scanner.getClasses(basePackage);

        for(Class<? > clazz : clazzs){

            //检查属性注解
            Entity entity = clazz.getAnnotation(Entity.class);
            if(null != entity){
                String table = entity.table();
                TABLE_MAP.put(clazz, table.toLowerCase());

                Field[] fields = clazz.getDeclaredFields();

                boolean hasPk = false;
                List<String> fieldNames = new ArrayList<>();
                for(Field field : fields){
                    Column column = field.getAnnotation(Column.class);
                    if(null == column){
                        continue;
                    }
                    String dataName = column.name();
                    String propertyName = field.getName();
                    fieldNames.add(propertyName);
                    //添加数据映射
                    PROPERTY_MAP.put(propertyName, dataName);

                    //添加数据库字段与属性名映射
                    DATA_MAP.put(dataName, propertyName);

                    //扫描主键
                    if(field.isAnnotationPresent(PK.class) && !hasPk){
                        PK_MAP.put(clazz, propertyName);
                        hasPk = true;
                    }

                    //获取getter/setter
                    String firstLetter = propertyName.substring(0, 1).toUpperCase();
                    String getter = "get" + firstLetter + propertyName.substring(1);
                    String setter = "set" + firstLetter + propertyName.substring(1);
                    try {
                        Method getterMethod = clazz.getDeclaredMethod(getter);
                        Method setterMethod = clazz.getDeclaredMethod(setter, field.getType());
                        GETTERS_MAP.put(propertyName, getterMethod);
                        SETTERS_MAP.put(propertyName, setterMethod);
                    } catch (NoSuchMethodException e) {
                        log.error(propertyName + "缺少getter方法");
                        e.printStackTrace();
                    }
                }
                FIELDS_MAP.put(clazz, fieldNames);
            }
        }
    }

    /**
     * 注册record
     * @param dbName 数据库名
     */
    public static void registerRecord(String dbName) {
        JdbcTemplate jdbcTemplate = SpringUtils.getBean(JdbcTemplate.class);
        List<Map<String, Object>> tablePks = jdbcTemplate.queryForList("SELECT t.TABLE_NAME, c.COLUMN_NAME FROM INFORMATION_SCHEMA.TABLE_CONSTRAINTS AS t, INFORMATION_SCHEMA.KEY_COLUMN_USAGE AS c WHERE t.TABLE_NAME = c.TABLE_NAME  AND t.TABLE_SCHEMA = '" + dbName + "' AND t.CONSTRAINT_TYPE = 'PRIMARY KEY';");

        for(Map<String, Object> tablePk : tablePks) {
            if(tablePk.containsKey("COLUMN_NAME")){
                TABLE_PK_MAP.put((String)tablePk.get("TABLE_NAME"), (String)tablePk.get("COLUMN_NAME"));
            }
        }
    }
}
