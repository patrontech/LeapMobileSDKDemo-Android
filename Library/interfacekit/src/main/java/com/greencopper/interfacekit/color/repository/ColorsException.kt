package com.greencopper.interfacekit.color.repository

public class ColorsException {
    public class ColorsNotLoadedException : Throwable() {
        override val message: String
            get() = "Trying to access colors in repository before processing the recipe."
    }
}
