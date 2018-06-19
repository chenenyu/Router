package com.chenenyu.router.template;

import java.util.Map;

/**
 * Target interceptor mapping.
 * <p>
 * Created by chenenyu on 2017/6/29.
 */
public interface TargetInterceptorsTable {
    /**
     * Mapping between target and interceptors, the target class may be an {@link android.app.Activity},
     * {@link android.app.Fragment} or {@link android.support.v4.app.Fragment}.
     *
     * @param map target -> interceptors array.
     */
    void handle(Map<Class<?>, String[]> map);
}
