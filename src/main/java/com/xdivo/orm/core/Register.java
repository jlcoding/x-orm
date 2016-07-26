package com.xdivo.orm.core;

import com.xdivo.orm.annotation.Column;
import com.xdivo.orm.annotation.Entity;
import com.xdivo.orm.annotation.Join;
import com.xdivo.orm.annotation.PK;
import com.xdivo.orm.mapping.ColumnMap;
import com.xdivo.orm.mapping.JoinMap;
import com.xdivo.orm.mapping.ModelMap;
import com.xdivo.orm.utils.Scanner;
import com.xdivo.orm.utils.SpringUtils;
import com.xdivo.orm.utils.ThreadUtils;
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

    //Model与其属性 数据库字段对应关系
    public static Map<Class<?>, ModelMap> modelMapping = new HashMap<>();

    //表名与主键映射
    public static Map<String, String> TABLE_PK_MAP = new HashMap<>();

    /**
     * 初始化线程池
     * @param coreSize coreSize
     * @param maxPoolSize maxPoolSize
     * @param queueSize queueSize
     */
    public static void initThreadPool (int coreSize, int maxPoolSize, int queueSize){
        ThreadUtils.init(coreSize, maxPoolSize, queueSize);
    }

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
                log.info("正在注册Model " + clazz + " => " + table);
                ModelMap modelMap = new ModelMap();

                //获取对应表名
                modelMap.setTable(table);

                List<ColumnMap> columnMaps = new ArrayList<>();

                List<JoinMap> joinMaps = new ArrayList<>();

                //获取model属性
                Field[] fields = clazz.getDeclaredFields();

                boolean hasPk = false;

                //遍历属性保存关系
                for(Field field : fields){
                    Column column = field.getAnnotation(Column.class);
                    if(null == column){
                        continue;
                    }

                    ColumnMap columnMap = new ColumnMap();

                    //数据库字段名
                    String fieldName = column.name();

                    //属性名
                    String propertyName = field.getName();

                    //保存属性与数据库字段关系
                    columnMap.setField(fieldName);
                    columnMap.setProperty(propertyName);

                    //添加属性与数据库字段映射
                    columnMaps.add(columnMap);

                    //扫描主键
                    if(field.isAnnotationPresent(PK.class) && !hasPk){
                        modelMap.setPrimaryKey(propertyName);
                        hasPk = true;
                    }

                    //扫描关联列
                    Join join = field.getAnnotation(Join.class);
                    if(null != join){
                        JoinMap joinMap = new JoinMap();
                        joinMap.setColumn(fieldName);
                        joinMap.setType(field.getType());
                        joinMap.setPropertyName(propertyName);
                        joinMap.setRefColumn(join.refColumn());
                        joinMaps.add(joinMap);
                    }

                    //获取getter/setter
                    String firstLetter = propertyName.substring(0, 1).toUpperCase();
                    String getter = "get" + firstLetter + propertyName.substring(1);
                    String setter = "set" + firstLetter + propertyName.substring(1);
                    try {
                        Method getterMethod = clazz.getDeclaredMethod(getter);
                        Method setterMethod = clazz.getDeclaredMethod(setter, field.getType());
                        columnMap.setGetter(getterMethod);
                        columnMap.setSetter(setterMethod);
                    } catch (NoSuchMethodException e) {
                        log.error(propertyName + "缺少getter方法");
                        e.printStackTrace();
                    }
                }
                modelMap.setJoinMaps(joinMaps);
                modelMap.setColumnMaps(columnMaps);
                modelMapping.put(clazz, modelMap);
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
                log.info("正在注册Record " + (String)tablePk.get("TABLE_NAME") + " => " + (String)tablePk.get("COLUMN_NAME"));
                TABLE_PK_MAP.put((String)tablePk.get("TABLE_NAME"), (String)tablePk.get("COLUMN_NAME"));
            }
        }
    }
}
