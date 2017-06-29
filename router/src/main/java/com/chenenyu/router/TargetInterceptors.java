package com.chenenyu.router;

import java.util.Map;

/**
 * Target interceptors mapping.
 * <p>
 * Created by Enyu Chen on 2017/6/29.
 */
public interface TargetInterceptors {
    /**
     * Mapping between target and interceptors, the target class may be an {@link android.app.Activity},
     * {@link android.app.Fragment} or {@link android.support.v4.app.Fragment}.
     *
     * @param map target -> interceptors.
     */
    void handle(Map<Class<?>, String[]> map);
}
