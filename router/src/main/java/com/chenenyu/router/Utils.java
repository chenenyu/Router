package com.chenenyu.router;

import java.util.Map;

/**
 * Created by zhangleilei on 04/01/2017.
 */

public class Utils {

    private Utils() {
        throw new UnsupportedClassVersionError("can not be instantiated!");
    }

    /**
     *
     * @param query key1=value1&key2=value2 example: id=9527&key=hello
     */
    public static void parseParams(Map<String, String> map, String query) {
        if (query != null && !query.isEmpty()) {
            String[] entries = query.split("&");
            for (String entry : entries) {
                if (entry.contains("=")) {
                    String[] kv = entry.split("=");
                    if (kv.length > 1) {
                        map.put(kv[0], kv[1]);
                    }
                }
            }
        }
    }

}
