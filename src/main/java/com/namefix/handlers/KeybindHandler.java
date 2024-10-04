package com.namefix.handlers;

import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public class KeybindHandler {
    private KeybindHandler() {}

    public static KeyBinding keyDeadeyeToggle;
    public static KeyBinding keyDeadeyeMark;
    public static KeyBinding keyDeadeyeShootTargets;

    private static KeyBinding registerKeybind(String translationKey, int key) {
        return KeyBindingHelper.registerKeyBinding(new KeyBinding(
                translationKey,
                InputUtil.Type.KEYSYM,
                key,
                "category.deadeye-mod"
        ));
    }

    private static KeyBinding registerKeybindMouse(String translationKey, int key) {
        return KeyBindingHelper.registerKeyBinding(new KeyBinding(
                translationKey,
                InputUtil.Type.MOUSE,
                key,
                "category.deadeye-mod"
        ));
    }

    public static void initialize() {
        keyDeadeyeToggle = registerKeybind("key.deadeye-mod.toggle", GLFW.GLFW_KEY_CAPS_LOCK);
        keyDeadeyeMark = registerKeybind("key.deadeye-mod.mark", GLFW.GLFW_KEY_X);
        keyDeadeyeShootTargets = registerKeybindMouse("key.deadeye-mod.shoot_targets", GLFW.GLFW_MOUSE_BUTTON_LEFT);
    }
}
