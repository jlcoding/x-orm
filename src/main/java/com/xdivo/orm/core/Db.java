package com.xdivo.orm.core;

import com.xdivo.orm.utils.SpringUtils;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Record 操作类
 * Created by liujunjie on 16-7-20.
 */
public class Db {

    private static JdbcTemplate jdbcTemplate;

    static {
        Db.jdbcTemplate = SpringUtils.getBean(JdbcTemplate.class);
    }

    /**
     * 根据id查找
     * @param tableName 表名
     * @param id 主键
     * @return Record
     */
    public static Record findById(String tableName, String id) {
        Record record = new Record();
        String sql = "SELECT * FROM " + tableName + " WHERE " + Register.TABLE_PK_MAP.get(tableName) + " = ?";
        Map<String, Object> resultMap = jdbcTemplate.queryForMap(sql, new Object[]{id});
        if(null != resultMap && !resultMap.isEmpty()) {
            for(Map.Entry<String, Object> entry : resultMap.entrySet()) {
                record.set(entry.getKey(), entry.getValue());
            }
            return record;
        }else {
            return null;
        }
    }

    /**
     * 根据sql语句查找
     * @param sql sql语句
     * @param param 参数
     * @return List<Record>
     */
    public static List<Record> find(String sql, Object... param) {
        List<Record> records = new ArrayList<>();
        List<Map<String, Object>> results = jdbcTemplate.queryForList(sql);
        if(null != results && !results.isEmpty()) {
            for(Map<String, Object> result : results) {
                Record record = new Record();
                for(Map.Entry<String, Object> entry : result.entrySet()) {
                    record.set(entry.getKey(), entry.getValue());
                }
                records.add(record);
            }
            return records;
        }
        return null;
    }

    /**
     * 保存record
     * @param tableName 表名
     * @param record record对象
     * @return long
     */
    public static long save(String tableName, Record record) {
        StringBuilder sqlBuilder = new StringBuilder("INSERT INTO ")
                .append(tableName)
                .append("(");
        List<Object> params = new ArrayList<>();
        StringBuilder values = new StringBuilder("");
        boolean isPk = false;
        for(Map.Entry<String, Object> entry : record.getColumns().entrySet()) {
            sqlBuilder.append(entry.getKey())
                    .append(",");

            values.append("?")
                    .append(",");

            //获取属性值
            params.add(entry.getValue());
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
     * 更新record
     * @param tableName 表名
     * @param record 对象
     * @return long
     */
    public long update(String tableName, Record record) {

        List<Object> params = new ArrayList<>();
        String pkName = Register.TABLE_PK_MAP.get(tableName);
        StringBuilder sqlBuilder = new StringBuilder("UPDATE ")
                .append(tableName)
                .append(" SET ");
        for (Map.Entry<String, Object> column : record.getColumns().entrySet()) {
            sqlBuilder.append(column.getKey())
                    .append(" = ?, ");
            //获取属性值
            params.add(column.getValue());
        }
        sqlBuilder.deleteCharAt(sqlBuilder.length() - 1);
        sqlBuilder.append(" WHERE ")
                .append(pkName)
                .append(" = ?");
        params.add(record.get(pkName));
        String sql = sqlBuilder.toString();
        return jdbcTemplate.update(sql, params.toArray());
    }
}
