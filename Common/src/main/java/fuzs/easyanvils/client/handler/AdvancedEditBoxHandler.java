package fuzs.easyanvils.client.handler;

import com.mojang.blaze3d.platform.InputConstants;
import fuzs.puzzleslib.api.event.v1.core.EventResult;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;

public class AdvancedEditBoxHandler {

    public static EventResult onBeforeMouseClick(Screen screen, double mouseX, double mouseY, int button) {
        for (GuiEventListener guiEventListener : screen.children()) {
            if (guiEventListener instanceof EditBox && guiEventListener.mouseClicked(mouseX, mouseY, button)) {
                screen.setFocused(guiEventListener);
                if (button == InputConstants.MOUSE_BUTTON_LEFT) {
                    screen.setDragging(true);
                }

                return EventResult.INTERRUPT;
            }
        }

        return EventResult.PASS;
    }

    public static EventResult onBeforeMouseRelease(Screen screen, double mouseX, double mouseY, int button) {
        screen.setDragging(false);
        return screen.getChildAt(mouseX, mouseY).filter(EditBox.class::isInstance).filter((GuiEventListener guiEventListener) -> {
            return guiEventListener.mouseReleased(mouseX, mouseY, button);
        }).isPresent() ? EventResult.INTERRUPT : EventResult.PASS;
    }

    public static EventResult onBeforeMouseDrag(Screen screen, double mouseX, double mouseY, int button, double dragX, double dragY) {
        return screen.getFocused() instanceof EditBox && screen.isDragging() && button == InputConstants.MOUSE_BUTTON_LEFT && screen.getFocused().mouseDragged(mouseX, mouseY, button, dragX, dragY) ? EventResult.INTERRUPT : EventResult.PASS;
    }
}
