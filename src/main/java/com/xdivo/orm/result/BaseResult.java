package com.xdivo.orm.result;

import com.xdivo.orm.core.Model;

import java.util.List;

/**
 * 分页结果基类
 * Created by liujunjie on 16-7-21.
 */
public class BaseResult {

    private List<Model> data;

    private int pageSize;

    public List<Model> getData() {
        return data;
    }

    public BaseResult setData(List<Model> data) {
        this.data = data;
        return this;
    }

    public int getPageSize() {
        return pageSize;
    }

    public BaseResult setPageSize(int pageSize) {
        this.pageSize = pageSize;
        return this;
    }
}
