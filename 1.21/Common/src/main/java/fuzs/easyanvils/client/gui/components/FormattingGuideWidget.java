package fuzs.easyanvils.client.gui.components;

import com.google.common.collect.Lists;
import fuzs.easyanvils.client.gui.screens.inventory.tooltip.LargeTooltipPositioner;
import fuzs.easyanvils.util.FormattedStringDecomposer;
import fuzs.puzzleslib.api.client.gui.v2.components.ScreenTooltipFactory;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractStringWidget;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.FormattedCharSink;

import java.util.List;
import java.util.Optional;

public class FormattingGuideWidget extends AbstractStringWidget {
    private static final Component QUESTION_MARK_COMPONENT = Component.literal("?");

    public FormattingGuideWidget(int x, int y, Font font) {
        super(x - font.width(QUESTION_MARK_COMPONENT) * 2, y, font.width(QUESTION_MARK_COMPONENT) * 2, font.lineHeight, QUESTION_MARK_COMPONENT, font);
        this.active = true;
        List<MutableComponent> formattingCodes = Lists.newArrayList();
        for (ChatFormatting chatFormatting : ChatFormatting.values()) {
            MutableComponent component = Component.translatable("chat.formatting." + chatFormatting.getName());
            // black font cannot be read on the tooltip
            if (chatFormatting != ChatFormatting.BLACK) component.withStyle(chatFormatting);
            formattingCodes.add(Component.literal("§" + chatFormatting.getChar()).append(" - ").append(component));
        }
        List<FormattedCharSequence> lines = formattingCodes.stream().map(FormattingGuideWidget::getVisualOrder).toList();
        ScreenTooltipFactory.setWidgetTooltipFromCharSequence(this, lines, (ScreenRectangle screenRectangle, Boolean forKeyboard) -> {
            return new LargeTooltipPositioner(forKeyboard ? screenRectangle : null);
        });
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.setColor(this.isHoveredOrFocused() ? ChatFormatting.YELLOW.getColor() : 4210752);
        int posX = this.getX() + (this.getWidth() - this.getFont().width(this.getMessage())) / 2;
        int posY = this.getY() + (this.getHeight() - 9) / 2;
        guiGraphics.drawString(this.getFont(), this.getMessage(), posX, posY, this.getColor(), false);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return false;
    }

    private static FormattedCharSequence getVisualOrder(FormattedText text) {
        return (FormattedCharSink formattedCharSink) -> {
            return text.visit((Style style, String string) -> {
                // this is the same iterate method we use for styling anvil & name tag edit box contents which will keep formatting codes intact
                // it will apply them to ensuing characters though, which is not an issue here
                // as all components containing formatting codes consist of two characters representing the formatting code
                return FormattedStringDecomposer.iterateFormatted(string, style, formattedCharSink) ? Optional.empty() : FormattedText.STOP_ITERATION;
            }, Style.EMPTY).isPresent();
        };
    }
}
