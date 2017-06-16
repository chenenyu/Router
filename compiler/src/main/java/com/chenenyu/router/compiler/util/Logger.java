package com.chenenyu.router.compiler.util;

import javax.annotation.processing.Messager;
import javax.lang.model.element.Element;
import javax.tools.Diagnostic;

/**
 * {@link Messager} wrapper.
 * <p>
 * Created by Enyu Chen on 2017/6/13.
 */
public class Logger {
    private Messager messager;

    public Logger(Messager messager) {
        this.messager = messager;
    }

    public void info(CharSequence info) {
        messager.printMessage(Diagnostic.Kind.NOTE, info);
    }

    public void info(Element element, CharSequence info) {
        messager.printMessage(Diagnostic.Kind.NOTE, info, element);
    }

    public void warn(CharSequence info) {
        messager.printMessage(Diagnostic.Kind.WARNING, info);
    }

    public void warn(Element element, CharSequence info) {
        messager.printMessage(Diagnostic.Kind.WARNING, info, element);
    }

    public void error(CharSequence info) {
        messager.printMessage(Diagnostic.Kind.ERROR, info);
    }

    public void error(Element element, CharSequence info) {
        messager.printMessage(Diagnostic.Kind.ERROR, info, element);
    }
}
