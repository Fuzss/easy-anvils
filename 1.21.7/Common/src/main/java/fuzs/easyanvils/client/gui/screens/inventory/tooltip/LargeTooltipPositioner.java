package fuzs.easyanvils.client.gui.screens.inventory.tooltip;

import net.minecraft.client.gui.navigation.ScreenAxis;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipPositioner;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2i;
import org.joml.Vector2ic;

public class LargeTooltipPositioner implements ClientTooltipPositioner {
    @Nullable
    private final ScreenRectangle screenRectangle;

    public LargeTooltipPositioner(@Nullable ScreenRectangle screenRectangle) {
        this.screenRectangle = screenRectangle;
    }

    @Override
    public Vector2ic positionTooltip(int screenWidth, int screenHeight, int mouseX, int mouseY, int tooltipWidth, int tooltipHeight) {

        if (this.screenRectangle != null) {

            // Adapted from BelowOrAboveWidgetTooltipPositioner
            Vector2i vector2i = new Vector2i();
            vector2i.x = this.screenRectangle.right() + 5;
            vector2i.y = this.screenRectangle.getCenterInAxis(ScreenAxis.VERTICAL) - tooltipHeight / 2;

            if (vector2i.x + tooltipWidth > screenWidth) {
                vector2i.x = this.screenRectangle.left() - 5 - tooltipWidth;
            }

            if (vector2i.y + tooltipHeight + 7 > screenHeight) {
                vector2i.y = screenHeight - tooltipHeight - 7;
            } else if (vector2i.y - 7 < 0) {
                vector2i.y = 7;
            }

            return vector2i;
        } else {

            // Adapted from DefaultTooltipPositioner
            Vector2i vector2i = new Vector2i(mouseX, mouseY).add(12, -Math.max(12, tooltipHeight / 3));
            if (vector2i.x + tooltipWidth > screenWidth) {
                vector2i.x = Math.max(vector2i.x - 24 - tooltipWidth, 5);
            }

            if (vector2i.y - 7 < 0) {
                vector2i.y = 7;
            } else if (vector2i.y + tooltipHeight + 7 > screenHeight) {
                vector2i.y = screenHeight - tooltipHeight - 7;
            }

            return vector2i;
        }
    }
}
