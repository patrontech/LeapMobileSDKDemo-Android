package com.greencopper.core.services.manager

public interface ServiceSwitch {
    /**
     * An "aspect" is an identifier for a particular
     * service we want to manage through [ServiceManager],
     * such as Firebase or Gimbal. These are in camelCase.
     */
    public val aspect: ServiceAspect

    /**
     * Sets the switch to enabled or disabled.
     *
     * This is a method and not a property because
     * we can't always know what the state of the
     * underlying aspect is.
     */
    public fun enable(enabled: Boolean)
}