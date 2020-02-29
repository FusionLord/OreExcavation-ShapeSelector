package net.fusionlord.mods.oreexcavation.shapeselector.screens.components;

import javax.annotation.Nullable;

public class ActionPressed {
    private Runnable action;

    public ActionPressed(@Nullable Runnable action) {
        this.action = action;
    }

    public boolean pressed(boolean pressed) {
        if (pressed && action != null)
            action.run();

        return pressed;
    }
}
