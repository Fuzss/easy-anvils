package fuzs.easyanvils.client.gui.components;

import fuzs.easyanvils.client.gui.screens.inventory.tooltip.LargeTooltipPositioner;
import fuzs.easyanvils.util.FormattedStringDecomposer;
import fuzs.puzzleslib.api.client.gui.v2.tooltip.TooltipBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.inventory.tooltip.BelowOrAboveWidgetTooltipPositioner;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipPositioner;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.*;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.FormattedCharSink;

import java.util.List;
import java.util.Optional;

public class FormattingGuideWidget extends AbstractWidget {
    private static final Component QUESTION_MARK_COMPONENT = Component.literal("?");

    private final Font font;
    private Component inactiveMessage = CommonComponents.EMPTY;

    public FormattingGuideWidget(int x, int y, Font font) {
        this(x, y, QUESTION_MARK_COMPONENT, font);
    }

    public FormattingGuideWidget(int x, int y, Component message, Font font) {
        super(x - font.width(message) * 2, y, font.width(message) * 2, font.lineHeight, message);
        this.font = font;
        this.active = true;
        this.setMessage(message);
        TooltipBuilder tooltipBuilder = TooltipBuilder.create()
                .setTooltipPositionerFactory((ClientTooltipPositioner clientTooltipPositioner, AbstractWidget abstractWidget) -> {
                    if (clientTooltipPositioner instanceof BelowOrAboveWidgetTooltipPositioner) {
                        return new LargeTooltipPositioner(abstractWidget.getRectangle());
                    } else {
                        return new LargeTooltipPositioner(null);
                    }
                })
                .setTooltipLineProcessor((List<? extends FormattedText> tooltipLines) -> {
                    return tooltipLines.stream().map(FormattingGuideWidget::getVisualOrder).toList();
                });
        for (ChatFormatting chatFormatting : ChatFormatting.values()) {
            MutableComponent component = Component.translatable("chat.formatting." + chatFormatting.getName());
            // black font and obfuscated text cannot be read on the tooltip
            if (chatFormatting != ChatFormatting.BLACK && chatFormatting != ChatFormatting.OBFUSCATED) {
                component.withStyle(chatFormatting);
            }

            tooltipBuilder.addLines(Component.literal("§" + chatFormatting.getChar()).append(" - ").append(component));
        }

        tooltipBuilder.build(this);
    }

    private static FormattedCharSequence getVisualOrder(FormattedText formattedText) {
        return (FormattedCharSink formattedCharSink) -> {
            return formattedText.visit((Style style, String string) -> {
                // This is the same iterate method we use for styling anvil & name tag edit box contents which will keep formatting codes intact.
                // It will apply them to ensuing characters, though, which is not an issue here.
                // As all components containing formatting codes consist of two characters representing the formatting code.
                return FormattedStringDecomposer.iterateFormatted(string, style, formattedCharSink) ? Optional.empty() :
                        FormattedText.STOP_ITERATION;
            }, Style.EMPTY).isPresent();
        };
    }

    @Override
    public Component getMessage() {
        return this.isHoveredOrFocused() ? this.message : this.inactiveMessage;
    }

    @Override
    public void setMessage(Component message) {
        this.message = ComponentUtils.mergeStyles(message, Style.EMPTY.withColor(ChatFormatting.YELLOW));
        this.inactiveMessage = ComponentUtils.mergeStyles(message, Style.EMPTY.withColor(0x404040));
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        Component component = this.getMessage();
        int posX = this.getX() + (this.getWidth() - this.font.width(component)) / 2;
        int posY = this.getY() + (this.getHeight() - 9) / 2;
        guiGraphics.drawString(this.font, component, posX, posY, -1, false);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent mouseButtonEvent, boolean doubleClick) {
        return false;
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
        // NO-OP
    }
}
