package com.bylazar.configurables;

/**
 * Virtual Robot's approximation of Panels' PanelsConfigurables singleton.
 *
 * The real library re-reads a class's @Configurable fields from the web dashboard.
 * The simulator has no dashboard, so refreshing is a no-op and fields keep whatever
 * values the source code gives them.
 */
public class PanelsConfigurables {

    public static final PanelsConfigurables INSTANCE = new PanelsConfigurables();

    private PanelsConfigurables() { }

    /** No-op: nothing to pull from, so the object's current field values stand. */
    public void refreshClass(Object instance) { }

    /** No-op: see {@link #refreshClass(Object)}. */
    public void refreshClass(Class<?> clazz) { }
}
