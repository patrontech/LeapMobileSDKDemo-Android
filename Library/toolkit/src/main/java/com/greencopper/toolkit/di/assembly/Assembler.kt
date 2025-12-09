package com.greencopper.toolkit.di.assembly

public interface Assembler {
    public fun assemble(assemblies: List<Assembly> = emptyList())
}

public fun Assembler.assemble(vararg assemblies: Assembly) {
    assemble(assemblies.toList())
}