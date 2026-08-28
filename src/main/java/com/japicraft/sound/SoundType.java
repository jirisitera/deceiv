package com.japicraft.sound;

import com.japicraft.item.UniqueItem;
import org.intellij.lang.annotations.Subst;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public enum SoundType {
    DAGGER_THROW(UniqueItem.DAGGER.getModel(), "throw"),
    DAGGER_STAB(UniqueItem.DAGGER.getModel(), "stab"),
    REVOLVER_SHOOT(UniqueItem.REVOLVER.getModel(), "shoot"),
    ITEM_ACTION_PREPARE(null, "prepare"),
    ITEM_ACTION_READY(null, "ready");
    private final String prefix;
    private final String name;

    SoundType(@Nullable String prefix, @NotNull String name) {
        this.prefix = prefix;
        this.name = name;
    }

    @NotNull
    @Subst("default")
    public String getPath() {
        if (prefix == null) {
            return "." + name;
        } else {
            return prefix + "." + name;
        }
    }
}
