package com.xdivo.orm.core;

import com.xdivo.orm.utils.SpringUtils;
import com.xdivo.orm.utils.ThreadUtils;
import org.apache.log4j.Logger;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.util.StringUtils;

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

    public Model findFirst(String sql, Object... params){
        sql = sql.concat(" LIMIT 1");
        Map<String, Object> resultMap = jdbcTemplate.queryForMap(sql, params);
        return mapping(resultMap);
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
     * 根据多个属性查询
     * @param params 参数
     * @param orderCol 排序列
     * @param direaction 排序方向
     * @param size 返回数量
     * @return 实体列表
     */
    public List<Model> findByMap(Map<String, Object> params, String orderCol, String direaction, int size){
        List<Object> paramList = new ArrayList<>();
        StringBuilder sqlBuilder = new StringBuilder("SELECT * FROM " + tableName + " Where 1 = 1");
        for(Map.Entry<String, Object> param : params.entrySet()){
            sqlBuilder.append(" AND " + param.getKey() + " = ? ");
            paramList.add(param.getValue());
        }
        sqlBuilder.append(" ORDER BY " + orderCol + " " + direaction);
        paramList.add(size);
        sqlBuilder.append(" LIMIT ? ");
        List<Map<String, Object>> results = jdbcTemplate.queryForList(sqlBuilder.toString(), paramList.toArray());
        return mappingList(results);
    }

    public Model findById(Object id) {
        String pkField = Register.PK_MAP.get(this.getClass());
        String pkColumn = Register.PROPERTY_MAP.get(pkField);
        String sql = "SELECT * FROM " + tableName + " WHERE " + pkColumn + " = ?";
        return mapping(jdbcTemplate.queryForMap(sql, id));
    }

    /**
     * 获取属性值
     * @param fieldName 属性名
     * @return Object
     */
    public Object getValue(String fieldName) {
        Method method = Register.GETTERS_MAP.get(fieldName);
        try {
            return method.invoke(this);
        } catch (IllegalAccessException | InvocationTargetException e) {
            log.error("执行" + fieldName + " 的getter失败");
            e.printStackTrace();
        }
        return null;
    }

    /**
     * list转换成modelList
     * @param mapList 数据库记录
     * @return ist<Model>
     */
    public List<Model> mappingList(List<Map<String, Object>> mapList) {
        List<Model> models = new ArrayList<>();
        for(Map<String, Object> map : mapList) {
            models.add(mapping(map));
        }
        return models;
    }

    /**
     * map转换成model
     * @param map 数据库记录
     * @return Model
     */
    public Model mapping(Map<String, Object> map) {
        for(Map.Entry<String, Object> entry : map.entrySet()) {
            //空值字段直接忽略
            if(null == entry.getValue()){
                continue;
            }
            String field = Register.DATA_MAP.get(entry.getKey());
            Method setter = Register.SETTERS_MAP.get(field);

            //没有在映射的字段也忽略
            if(StringUtils.isEmpty(field) || null == setter) {
                continue;
            }

            try {
                setter.invoke(this, entry.getValue());
            } catch (IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }
        }
        return this;
    }

}
