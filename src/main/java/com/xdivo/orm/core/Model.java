package com.xdivo.orm.core;

import com.xdivo.orm.mapping.JoinMap;
import com.xdivo.orm.result.ScrollResult;
import com.xdivo.orm.utils.SpringUtils;
import com.xdivo.orm.utils.ThreadUtils;
import org.apache.log4j.Logger;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.util.StringUtils;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 数据库操作模型类
 * Created by jaleel on 16-7-17.
 */
public class Model<T> extends BaseModel implements Serializable {

    private static final long serialVersionUID = -1453641993600063553L;

    public static class Direction {
        public final static String ASC = "asc";

        public final static String DESC = "desc";
    }

    private final static Logger log = Logger.getLogger(Model.class);

    private JdbcTemplate jdbcTemplate;

    private String tableName;

    private String pk;

    public Model() {
        this.jdbcTemplate = SpringUtils.getBean(JdbcTemplate.class);
        this.tableName = TABLE_MAP.get(this.getClass());
        this.pk = PK_MAP.get(this.getClass());
    }

    public T findFirst(String sql, Object... params) {
        sql = sql.concat(" LIMIT 1");
        Map<String, Object> resultMap = jdbcTemplate.queryForMap(sql, params);
        return mapping(resultMap);
    }

    /**
     * 保存model
     *
     * @return long
     */
    public long save() {
        StringBuilder sqlBuilder = new StringBuilder("INSERT INTO ")
                .append(tableName)
                .append("(");
        List<String> fieldNames = FIELDS_MAP.get(this.getClass());
        List<Object> params = new ArrayList<>();
        StringBuilder values = new StringBuilder("");
        for (String fieldName : fieldNames) {
            sqlBuilder.append(PROPERTY_MAP.get(fieldName))
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
        String sql = sqlBuilder.toString();
        return jdbcTemplate.update(sql, params.toArray());
    }

    /**
     * 更新model
     *
     * @return long
     */
    public long update() {
        List<String> fieldNames = FIELDS_MAP.get(this.getClass());
        List<Object> params = new ArrayList<>();
        StringBuilder sqlBuilder = new StringBuilder("UPDATE ")
                .append(tableName)
                .append(" SET ");
        for (String fieldName : fieldNames) {
            if (pk.equals(fieldName)) {
                continue;
            }
            sqlBuilder.append(PROPERTY_MAP.get(fieldName))
                    .append(" = ?, ");
            //获取属性值
            params.add(getValue(fieldName));
        }
        sqlBuilder.deleteCharAt(sqlBuilder.length() - 1);
        sqlBuilder.append(" WHERE ")
                .append(PROPERTY_MAP.get(pk))
                .append(" = ?");
        params.add(getValue(pk));
        String sql = sqlBuilder.toString();
        return jdbcTemplate.update(sql, params.toArray());
    }

    /**
     * 异步保持model
     */
    public void asyncSave() {
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
    public void asyncUpdate() {
        ThreadUtils.execute(new Runnable() {
            @Override
            public void run() {
                update();
            }
        });
    }

    /**
     * 根据多个属性查询
     *
     * @param params    参数
     * @param orderCol  排序列
     * @param direction 排序方向
     * @param size      返回数量
     * @return 实体列表
     */
    public List<T> findByMap(Map<String, Object> params, String orderCol, String direction, Integer size) {
        List<Object> paramList = new ArrayList<>();
        StringBuilder sqlBuilder = new StringBuilder("SELECT * FROM " + tableName + " Where 1 = 1");
        for (Map.Entry<String, Object> param : params.entrySet()) {
            sqlBuilder.append(" AND " + PROPERTY_MAP.get(param.getKey()) + " = ? ");
            paramList.add(param.getValue());
        }

        if(!StringUtils.isEmpty(orderCol) && !StringUtils.isEmpty(direction)) {
            sqlBuilder.append(" ORDER BY " + PROPERTY_MAP.get(orderCol) + " " + direction);
        }

        if(null != size) {
            sqlBuilder.append(" LIMIT ? ");
            paramList.add(size);
        }

        List<Map<String, Object>> results = jdbcTemplate.queryForList(sqlBuilder.toString(), paramList.toArray());
        return mappingList(results);
    }


    /**
     * 瀑布流分页 (暂时只支持Number类型的列)
     *
     * @param orderColName  排序列名
     * @param orderColValue 排序列值
     * @param direction     方向
     * @param params        参数
     * @param pageSize      每页数量
     * @return ScrollResult
     */
    public ScrollResult scroll(String orderColName, Number orderColValue, String direction, Map<String, Object> params, int pageSize) {
        ScrollResult result = new ScrollResult();
        String operator = null;
        List<Object> paramList = new ArrayList<>();
        List<T> dataList = new ArrayList<>();
        String dataColName = PROPERTY_MAP.get(orderColName);
        //升序? 降序?
        if (direction.equals(Direction.ASC)) {
            operator = ">";
        } else {
            operator = "<";
        }

        //拼接语句
        StringBuilder sqlBuilder = new StringBuilder("SELECT * FROM " + tableName + " WHERE " + dataColName + " " + operator + " ? ");
        paramList.add(orderColValue);
        if (null != params && !params.isEmpty()) {
            for (Map.Entry<String, Object> param : params.entrySet()) {
                sqlBuilder.append(" AND " + PROPERTY_MAP.get(param.getKey()) + " = ? ");
                paramList.add(param.getValue());
            }
        }
        sqlBuilder.append(" ORDER BY " + dataColName + " " + direction + " LIMIT " + (pageSize + 1));
        List<Map<String, Object>> results = jdbcTemplate.queryForList(sqlBuilder.toString(), paramList.toArray());

        //处理结果
        int i = 0;
        boolean hasMore = false;
        Object lastValue = null;
        for (Map<String, Object> map : results) {
            i++;
            if (i >= (pageSize + 1)) {
                hasMore = true;
                break;
            }
            dataList.add(mapping(map));
            lastValue = map.get(dataColName);

        }
        result.setHasMore(hasMore);
        result.setData(dataList);
        result.setLastValue(lastValue);
        result.setPageSize(pageSize);
        return result;
    }


    public T findById(Object id) {
        String pkField = PK_MAP.get(this.getClass());
        String pkColumn = PROPERTY_MAP.get(pkField);
        String sql = "SELECT * FROM " + tableName + " WHERE " + pkColumn + " = ?";
        return mapping(jdbcTemplate.queryForMap(sql, id));
    }

    /**
     * 获取属性值
     *
     * @param propertyName 属性名
     * @return Object
     */
    public Object getValue(String propertyName) {
        Method method = GETTERS_MAP.get(propertyName);
        try {
            return method.invoke(this);
        } catch (IllegalAccessException | InvocationTargetException e) {
            log.error("执行" + propertyName + " 的getter失败");
            e.printStackTrace();
        }
        return null;
    }

    public void setValue(T model, String propertyName, Object value) {
        Method method = SETTERS_MAP.get(propertyName);
        try {
            method.invoke(model, value);
        } catch (InvocationTargetException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    /**
     * list转换成modelList
     *
     * @param mapList 数据库记录
     * @return ist<Model>
     */
    public List<T> mappingList(List<Map<String, Object>> mapList) {
        List<T> models = new ArrayList<>();
        for (Map<String, Object> map : mapList) {
            models.add(mapping(map));
        }
        return models;
    }

    /**
     * map转换成model
     *
     * @param map 数据库记录
     * @return Model
     */
    public T mapping(Map<String, Object> map) {
        T model = null;
        try {
            model = (T)this.getClass().newInstance();

            List<String> joinColumns = new ArrayList<>();

            //检查是否有外键关联
            List<JoinMap> joinMaps = modelMap.getJoinMaps();
            for(JoinMap joinMap : joinMaps) {
                if(null != joinMap) {
                    Model refModel = (Model) joinMap.getType().newInstance();
                    Object columnVal = map.get(joinMap.getColumn());
                    String refColumn = refModel.PROPERTY_MAP.get(joinMap.getRefColumn());
                    Map<String, Object> resultMap = jdbcTemplate.queryForMap("SELECT * FROM " + refModel.tableName + " WHERE " + refColumn + " = ?", columnVal);
                    setValue(model, joinMap.getPropertyName(), refModel.mapping(resultMap));
                    joinColumns.add(joinMap.getColumn());
                }
            }
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                //空值字段直接忽略
                if (null == entry.getValue()) {
                    continue;
                }

                //如果是关联属性忽略
                if (joinColumns.contains(entry.getKey())) {
                    continue;
                }

                String field = FIELD_MAP.get(entry.getKey());
                Method setter = SETTERS_MAP.get(field);

                //没有在映射的字段也忽略
                if (StringUtils.isEmpty(field) || null == setter) {
                    continue;
                }
                setter.invoke(model, entry.getValue());
            }

        } catch (InvocationTargetException | InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return model;
    }

}
