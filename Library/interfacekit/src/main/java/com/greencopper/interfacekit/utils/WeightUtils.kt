package com.greencopper.interfacekit.utils

import java.util.TreeMap
import kotlin.math.ceil

public interface Weighted {
    public val weight: Int
}

internal fun randomByWeight(weighted: List<Weighted>) : Weighted {
    var totalWeight = 0
    val navigableMap = weighted.associateByTo(TreeMap<Int, Weighted>()) {
        totalWeight += it.weight
        totalWeight
    }
    val randomIndex = ceil(Math.random() * totalWeight).toInt()
    return navigableMap.ceilingEntry(randomIndex)!!.value
}
