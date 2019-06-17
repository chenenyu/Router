package com.chenenyu.router.template;

import java.util.Map;

/**
 * Route table mapping.
 * <p>
 * Created by chenenyu on 2016/12/22.
 */
public interface RouteTable {
    /**
     * Mapping between uri and target, the target class may be an {@link android.app.Activity},
     * or {@link androidx.fragment.app.Fragment}.
     *
     * @param map uri -> target.
     */
    void handle(Map<String, Class<?>> map);
}
