package com.chenenyu.router;

import com.chenenyu.router.matcher.AbsExplicitMatcher;
import com.chenenyu.router.matcher.AbsImplicitMatcher;
import com.chenenyu.router.matcher.AbsMatcher;
import com.chenenyu.router.matcher.BrowserMatcher;
import com.chenenyu.router.matcher.DirectMatcher;
import com.chenenyu.router.matcher.ImplicitMatcher;
import com.chenenyu.router.matcher.SchemeMatcher;
import com.chenenyu.router.util.RLog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Matcher registry.
 * <br>
 * Created by chenenyu on 2017/1/5.
 */
public class MatcherRegistry {

    private static final List<AbsMatcher> ALL = new ArrayList<>();
    private static final List<AbsExplicitMatcher> explicitMatcher = new ArrayList<>();
    private static final List<AbsImplicitMatcher> implicitMatcher = new ArrayList<>();

    static {
        ALL.add(new DirectMatcher(0x1000));
        ALL.add(new SchemeMatcher(0x0100));
        ALL.add(new ImplicitMatcher(0x0010));
        ALL.add(new BrowserMatcher(0x0000));
        Collections.sort(ALL);
        classifyMatcher();
    }

    public static void register(AbsMatcher matcher) {
        if (matcher instanceof AbsExplicitMatcher || matcher instanceof AbsImplicitMatcher) {
            ALL.add(matcher);
            Collections.sort(ALL);
            classifyMatcher();
        } else {
            RLog.e(String.format("%s must be a subclass of AbsExplicitMatcher or AbsImplicitMatcher",
                    matcher.getClass().getSimpleName()));
        }
    }

    public static List<AbsMatcher> getMatcher() {
        return ALL;
    }

    public static List<AbsExplicitMatcher> getExplicitMatcher() {
        return explicitMatcher;
    }

    public static List<AbsImplicitMatcher> getImplicitMatcher() {
        return implicitMatcher;
    }

    public static void clear() {
        ALL.clear();
        explicitMatcher.clear();
        implicitMatcher.clear();
    }

    private static void classifyMatcher() {
        explicitMatcher.clear();
        implicitMatcher.clear();
        for (AbsMatcher absMatcher : ALL) {
            if (absMatcher instanceof AbsExplicitMatcher) {
                explicitMatcher.add((AbsExplicitMatcher) absMatcher);
            } else if (absMatcher instanceof AbsImplicitMatcher) {
                implicitMatcher.add((AbsImplicitMatcher) absMatcher);
            }
        }
    }
}
