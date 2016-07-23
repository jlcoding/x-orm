package com.xdivo.orm.mapping;

import java.util.List;

/**
 * Model映射
 * Created by liujunjie on 16-7-23.
 */
public class ModelMap {

    //对应表名
    private String table;

    private String primaryKey;

    //属性与数据库字段
    private List<ColumnMap> columnMaps;

    //关联Models
    private List<JoinMap> joinMaps;

    public String getTable() {
        return table;
    }

    public ModelMap setTable(String table) {
        this.table = table;
        return this;
    }

    public List<ColumnMap> getColumnMaps() {
        return columnMaps;
    }

    public ModelMap setColumnMaps(List<ColumnMap> columnMaps) {
        this.columnMaps = columnMaps;
        return this;
    }

    public String getPrimaryKey() {
        return primaryKey;
    }

    public ModelMap setPrimaryKey(String primaryKey) {
        this.primaryKey = primaryKey;
        return this;
    }

    public List<JoinMap> getJoinMaps() {
        return joinMaps;
    }

    public ModelMap setJoinMaps(List<JoinMap> joinMaps) {
        this.joinMaps = joinMaps;
        return this;
    }
}
