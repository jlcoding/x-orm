package com.xdivo.orm.core;

import com.xdivo.orm.utils.SpringUtils;
import com.xdivo.orm.utils.ThreadUtils;
import org.apache.log4j.Logger;
import org.springframework.jdbc.core.JdbcTemplate;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 数据库操作模型类
 * Created by jaleel on 16-7-17.
 */
public class Model<T> {

    private final static Logger log = Logger.getLogger(Model.class);

    private Map<String, Object> attributes;

    private JdbcTemplate jdbcTemplate;

    private String tableName;

    private String pk;

    public Model(){
        this.jdbcTemplate = SpringUtils.getBean(JdbcTemplate.class);
        this.tableName = Register.TABLE_MAP.get(this.getClass());
        this.pk = Register.PK_MAP.get(this.getClass());
    }

    public T findFirst(String sql, Object... params){
        jdbcTemplate.queryForMap(sql, params);
        return null;
    }

    public Map<String, Object> getAttributes() {
        return attributes;
    }

    public Model setAttributes(Map<String, Object> attributes) {
        this.attributes = attributes;
        return this;
    }

    /**
     * 保存model
     * @return long
     */
    public long save(){
        StringBuilder sqlBuilder = new StringBuilder("INSERT INTO ")
                .append(tableName)
                .append("(");
        List<String> fieldNames = Register.FIELDS_MAP.get(this.getClass());
        List<Object> params = new ArrayList<>();
        StringBuilder values = new StringBuilder("");
        boolean isPk = false;
        for(String fieldName : fieldNames) {
            if(pk.equals(fieldName)) {
                isPk = true;
            }
            sqlBuilder.append(Register.PROPERTY_MAP.get(fieldName))
                    .append(",");

            values.append("?")
                    .append(",");

            //获取属性值
            params.add(getValue(fieldName));
        }
        sqlBuilder.deleteCharAt(sqlBuilder.length() - 1)
                .append(")")
                .append(" VALUES (")
                .append(values).deleteCharAt(sqlBuilder.length() - 1)
                .append(")");
        String sql =sqlBuilder.toString();
        return jdbcTemplate.update(sql, params.toArray());
    }

    /**
     * 更新model
     * @return long
     */
    public long update() {
        List<String> fieldNames = Register.FIELDS_MAP.get(this.getClass());
        List<Object> params = new ArrayList<>();
        StringBuilder sqlBuilder = new StringBuilder("UPDATE ")
                .append(tableName)
                .append(" SET ");
        for(String fieldName : fieldNames){
            if(pk.equals(fieldName)) {
                continue;
            }
            sqlBuilder.append(Register.PROPERTY_MAP.get(fieldName))
                    .append(" = ?, " );
            //获取属性值
            params.add(getValue(fieldName));
        }
        sqlBuilder.deleteCharAt(sqlBuilder.length() - 1);
        sqlBuilder.append(" WHERE ")
                .append(Register.PROPERTY_MAP.get(pk))
                .append(" = ?");
        params.add(getValue(pk));
        String sql = sqlBuilder.toString();
        return jdbcTemplate.update(sql, params.toArray());
    }

    /**
     * 异步保持model
     */
    public void asyncSave(){
        ThreadUtils.execute(new Runnable() {
            @Override
            public void run() {
                save();
            }
        });
    }

    /**
     * 异步更新model
     */
    public void asyncUpdate(){
        ThreadUtils.execute(new Runnable() {
            @Override
            public void run() {
                update();
            }
        });
    }

    /**
     * 获取属性值
     * @param fieldName 属性名
     * @return Object
     */
    private Object getValue(String fieldName) {
        Method method = Register.GETTERS_MAP.get(fieldName);
        try {
            return method.invoke(this);
        } catch (IllegalAccessException | InvocationTargetException e) {
            log.error("执行" + fieldName + " 的getter失败");
            e.printStackTrace();
        }
        return null;
    }

}
