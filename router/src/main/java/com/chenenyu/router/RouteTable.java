package com.chenenyu.router;

import java.util.Map;

/**
 * Route table mapping.
 * <p>
 * Created by Cheney on 2016/12/22.
 */
public interface RouteTable {
    /**
     * Mapping between uri and target, the target class may be an {@link android.app.Activity},
     * {@link android.app.Fragment} or {@link android.support.v4.app.Fragment}.
     *
     * @param map uri -> target.
     */
    void handle(Map<String, Class<?>> map);
}
