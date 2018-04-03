package com.chenenyu.router.template;

/**
 * Interface that help to generate param class.
 * <p>
 * Created by chenenyu on 2017/6/15.
 */
public interface ParamInjector {
    /**
     * Inject params.
     *
     * @param obj Activity or fragment instance.
     */
    void inject(Object obj);
}
