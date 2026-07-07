package com.github.ejc5m;

import android.content.Context;

import androidx.annotation.NonNull;

import com.aliucord.annotations.AliucordPlugin;
import com.aliucord.entities.Plugin;
import com.aliucord.patcher.*;
import com.discord.panels.OverlappingPanelsLayout;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

@AliucordPlugin(
    requiresRestart = false
)

public class UnlockSwipePanels extends Plugin {
    private Field swipeDirectionField;

    @Override
    public void start(@NonNull Context context) throws Throwable {
        var layoutClass = OverlappingPanelsLayout.class;

        Method getNormalizedXMethod = layoutClass.getDeclaredMethod("getNormalizedX", float.class);
        getNormalizedXMethod.setAccessible(true);

        swipeDirectionField = layoutClass.getDeclaredField("swipeDirection");
        swipeDirectionField.setAccessible(true);

        patcher.patch(getNormalizedXMethod, new PreHook(cf -> {
            float targetedX = (float) cf.args[0];
            try {
                if (targetedX > 0.0f) {
                    // Force RIGHT to unlock the right panel
                    swipeDirectionField.set(cf.thisObject,  OverlappingPanelsLayout.SwipeDirection.RIGHT);
                } else if (targetedX < 0.0f) {
                    // Force LEFT to unlock the left panel
                    swipeDirectionField.set(cf.thisObject,  OverlappingPanelsLayout.SwipeDirection.LEFT);
                }
            } catch (Exception ignored) {

            }
        }));
    }

    @Override
    public void stop(@NonNull Context context) {
        patcher.unpatchAll();
    }
}
