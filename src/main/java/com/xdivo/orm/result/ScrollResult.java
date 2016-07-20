package com.xdivo.orm.result;

import java.io.Serializable;

/**
 * 瀑布流分页结果
 * Created by liujunjie on 16-7-21.
 */
public class ScrollResult extends BaseResult implements Serializable{

    private static final long serialVersionUID = -8063828692900632543L;

    private boolean hasMore;

    private Object lastValue;

    public boolean isHasMore() {
        return hasMore;
    }

    public ScrollResult setHasMore(boolean hasMore) {
        this.hasMore = hasMore;
        return this;
    }

    public Object getLastValue() {
        return lastValue;
    }

    public ScrollResult setLastValue(Object lastValue) {
        this.lastValue = lastValue;
        return this;
    }
}
