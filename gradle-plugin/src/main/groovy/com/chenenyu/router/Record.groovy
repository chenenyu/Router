package com.chenenyu.router

/**
 * Created by chenenyu on 2018/7/26.
 */
class Record {
    String templateName

    Set<String> aptClasses = []

    Record(String templateName) {
        this.templateName = templateName
    }
}
