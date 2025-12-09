package com.greencopper.core.services.manager

public interface ServiceManager {
    public fun enable(aspects: Set<ServiceAspect>, enabled: Boolean)
}