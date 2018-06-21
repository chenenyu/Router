/**
 * {@link com.chenenyu.router.matcher.AbsExplicitMatcher}:
 * <ul>
 * <li>{@link com.chenenyu.router.matcher.DirectMatcher}</li>
 * <li>{@link com.chenenyu.router.matcher.SchemeMatcher}</li>
 * </ul>
 * {@link com.chenenyu.router.matcher.AbsImplicitMatcher}:
 * <ul>
 * <li>{@link com.chenenyu.router.matcher.ImplicitMatcher}</li>
 * <li>{@link com.chenenyu.router.matcher.BrowserMatcher}</li>
 * </ul>
 * <p>
 * Default matcher priority in router:<p>
 * {@link com.chenenyu.router.matcher.DirectMatcher} ->
 * {@link com.chenenyu.router.matcher.SchemeMatcher} ->
 * {@link com.chenenyu.router.matcher.ImplicitMatcher} ->
 * {@link com.chenenyu.router.matcher.BrowserMatcher}
 * <p>
 * See {@link com.chenenyu.router.MatcherRegistry} for more info.
 */
package com.chenenyu.router.matcher;