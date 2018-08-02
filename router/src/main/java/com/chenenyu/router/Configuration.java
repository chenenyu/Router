package com.chenenyu.router;

/**
 * Initialization.
 * <p>
 * Created by chenenyu on 2017/11/9.
 */
public final class Configuration {
    boolean debuggable;
    @Deprecated
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

        /**
         * There is no need to call <code>registerModules</code> when using gradle plugin.
         */
        @Deprecated
        public Builder registerModules(String... modules) {
            this.modules = modules;
            return this;
        }

        public Configuration build() {
            Configuration configuration = new Configuration();
            configuration.debuggable = this.debuggable;
            configuration.modules = this.modules;
            return configuration;
        }
    }
}
