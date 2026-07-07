package com.github.ejc5m;

import android.content.Context;

import androidx.annotation.NonNull;

import com.aliucord.annotations.AliucordPlugin;
import com.aliucord.entities.Plugin;
import com.aliucord.patcher.*;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

@AliucordPlugin(
    requiresRestart = false
)

public class UnlockSwipePanels extends Plugin {
    private Field swipeDirectionField;
    private Object swipeDirectionLeft;
    private Object swipeDirectionRight;


    @Override
    public void start(@NonNull Context context) throws Throwable {
        Class<?> layoutClass = Class.forName("com.discord.panels.OverlappingPanelsLayout");

        Method getNormalizedXMethod = layoutClass.getDeclaredMethod("getNormalizedX", float.class);
        getNormalizedXMethod.setAccessible(true);

        swipeDirectionField = layoutClass.getDeclaredField("swipeDirection");
        swipeDirectionField.setAccessible(true);

        Class<?> swipeDirectionEnum = Class.forName("com.discord.panels.OverlappingPanelsLayout$SwipeDirection");
        swipeDirectionLeft = Enum.valueOf((Class<Enum>) swipeDirectionEnum, "LEFT");
        swipeDirectionRight = Enum.valueOf((Class<Enum>) swipeDirectionEnum, "RIGHT");

        patcher.patch(getNormalizedXMethod, new PreHook(cf -> {
            float targetedX = (float) cf.args[0];
            try {
                if (targetedX > 0.0f) {
                    // Force RIGHT to unlock the right panel
                    swipeDirectionField.set(cf.thisObject, swipeDirectionRight);
                } else if (targetedX < 0.0f) {
                    // Force LEFT to unlock the left panel
                    swipeDirectionField.set(cf.thisObject, swipeDirectionLeft);
                }
            } catch (Exception e) {

            }
        }));
    }

    @Override
    public void stop(@NonNull Context context) {
        // Remove all patches
        patcher.unpatchAll();
    }
}
