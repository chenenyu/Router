package com.chenenyu.router;

/**
 * Initialization.
 * <p>
 * Created by chenenyu on 2017/11/9.
 */
public final class Configuration {
    boolean debuggable;
    String[] modules;

    private Configuration() {
    }

    public static class Builder {
        private boolean debuggable;
        private String[] modules;

        public Builder setDebuggable(boolean debuggable) {
            this.debuggable = debuggable;
            return this;
        }

        public Builder registerModules(String... modules) {
            this.modules = modules;
            return this;
        }

        public Configuration build() {
            if (modules == null || modules.length == 0) {
                throw new RuntimeException("You must call registerModules() to initialize Router.");
            }
            Configuration configuration = new Configuration();
            configuration.debuggable = this.debuggable;
            configuration.modules = this.modules;
            return configuration;
        }
    }
}
