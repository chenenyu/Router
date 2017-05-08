package com.chenenyu.router;

import java.util.Map;

/**
 * Route table mapping.
 * <p>
 * Created by Cheney on 2016/12/22.
 */
public interface RouteTable {
    /**
     * Mapping between uri and target, such as {@link android.app.Activity} or {@link android.support.v4.app.Fragment}.
     *
     * @param map route table.
     */
    void handle(Map<String, Class<?>> map);
}
