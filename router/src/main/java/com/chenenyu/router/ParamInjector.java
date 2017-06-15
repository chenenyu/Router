package com.chenenyu.router;

/**
 * Interface that help to generate param class.
 * <p>
 * Created by Enyu Chen on 2017/6/15.
 */
public interface ParamInjector {
    /**
     * Inject params.
     *
     * @param obj Activity or fragment instance.
     */
    void inject(Object obj);
}
