package com.chenenyu.router.compiler;

import javax.lang.model.element.Element;

/**
 * Exception when compiling.
 * <p>
 * Created by Cheney on 2016/12/21.
 */
public class RouteException extends Exception {

    private Element element;

    public RouteException(String s, Element element) {
        super(s);
        this.element = element;
    }

    public RouteException(String s, Throwable throwable, Element element) {
        super(s, throwable);
        this.element = element;
    }

    public Element getElement() {
        return element;
    }

}
