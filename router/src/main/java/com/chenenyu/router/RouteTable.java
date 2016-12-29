package com.chenenyu.router;

import android.app.Activity;

import java.util.Map;

/**
 * Route table mapping.
 * <p>
 * Created by Cheney on 2016/12/22.
 */
public interface RouteTable {
    /**
     * Mapping between uri and {@link android.app.Activity}.
     *
     * @param map Activity map.
     */
    void handleActivityTable(Map<String, Class<? extends Activity>> map);
}
