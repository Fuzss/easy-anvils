package fuzs.easyanvils.common.client.gui.components;

import com.google.common.collect.Sets;
import fuzs.easyanvils.common.EasyAnvils;
import fuzs.easyanvils.common.client.gui.screens.inventory.tooltip.LargeTooltipPositioner;
import fuzs.easyanvils.common.util.FormattedStringDecomposer;
import fuzs.puzzleslib.common.api.client.gui.v2.tooltip.TooltipBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.inventory.tooltip.BelowOrAboveWidgetTooltipPositioner;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipPositioner;
import net.minecraft.network.chat.*;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.FormattedCharSink;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.StringJoiner;

public class FormattingGuideWidget extends AbstractWidget {
    private static final Component QUESTION_MARK_COMPONENT = Component.literal("?");
    private static final ChatFormatting[] CHAT_FORMATTING_VALUES = ChatFormatting.values();
    /**
     * Black font and obfuscated text cannot be read on the tooltip.
     */
    private static final Set<ChatFormatting> PLAIN_CHAT_FORMATTING = Sets.immutableEnumSet(ChatFormatting.BLACK,
            ChatFormatting.OBFUSCATED);
    public static final String CHAT_FORMATTING_FORMAT_KEY = EasyAnvils.id("chat.formatting")
            .toLanguageKey("gui", "format");

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
        for (ChatFormatting chatFormatting : CHAT_FORMATTING_VALUES) {
            String translationKey = getChatFormattingKey(chatFormatting);
            Component component;
            if (!PLAIN_CHAT_FORMATTING.contains(chatFormatting)) {
                component = Component.translatable(translationKey).withStyle(chatFormatting);
            } else {
                component = Component.translatable(translationKey);
            }

            tooltipBuilder.addLines(Component.translatable(CHAT_FORMATTING_FORMAT_KEY,
                    chatFormatting.getChar(),
                    component));
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

    public static String getChatFormattingKey(ChatFormatting chatFormatting) {
        return EasyAnvils.id("chat.formatting").toLanguageKey("gui", chatFormatting.getName());
    }

    public static String getChatFormattingName(ChatFormatting chatFormatting) {
        StringJoiner stringJoiner = new StringJoiner(" ");
        String[] strings = chatFormatting.getName().split("_");
        for (String string : strings) {
            stringJoiner.add(Character.toUpperCase(string.charAt(0)) + string.substring(1));
        }

        return stringJoiner.toString();
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
    public void extractWidgetRenderState(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
        Component component = this.getMessage();
        int posX = this.getX() + (this.getWidth() - this.font.width(component)) / 2;
        int posY = this.getY() + (this.getHeight() - 9) / 2;
        guiGraphics.text(this.font, component, posX, posY, -1, false);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
        // NO-OP
    }
}
