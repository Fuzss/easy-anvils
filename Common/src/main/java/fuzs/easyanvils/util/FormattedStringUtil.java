package fuzs.easyanvils.util;

import fuzs.easyanvils.common.EasyAnvils;
import fuzs.easyanvils.common.config.ServerConfig;
import fuzs.puzzleslib.api.util.v1.StyleCombiningCharSink;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;
import net.minecraft.util.StringDecomposer;
import net.minecraft.util.StringUtil;
import net.minecraft.world.item.ItemStack;

import java.util.Objects;

/**
 * @see net.minecraft.util.StringUtil
 */
public class FormattedStringUtil {
    /**
     * A custom style for merging via {@link ComponentUtils#mergeStyles(Component, Style)} that preserves the style set
     * by the player without any visual changes like italics being applied by vanilla.
     *
     * @see ItemStack#getStyledHoverName()
     */
    public static final Style EMPTY = Style.EMPTY.withBold(false)
            .withItalic(false)
            .withUnderlined(false)
            .withStrikethrough(false)
            .withObfuscated(false);

    /**
     * @see CharacterEvent#isAllowedChatCharacter()
     */
    public static boolean isAllowedChatCharacter(CharacterEvent event) {
        if (!EasyAnvils.CONFIG.get(ServerConfig.class).miscellaneous.renamingSupportsFormatting) {
            return event.isAllowedChatCharacter();
        }

        return isAllowedChatCharacter(event.codepoint());
    }

    /**
     * @see net.minecraft.util.StringUtil#isAllowedChatCharacter(int)
     */
    public static boolean isAllowedChatCharacter(int character) {
        if (!EasyAnvils.CONFIG.get(ServerConfig.class).miscellaneous.renamingSupportsFormatting) {
            return StringUtil.isAllowedChatCharacter(character);
        }

        return StringUtil.isAllowedChatCharacter(character) || character == '§';
    }

    /**
     * @see net.minecraft.util.StringUtil#filterText(String)
     */
    public static String filterText(String text) {
        if (!EasyAnvils.CONFIG.get(ServerConfig.class).miscellaneous.renamingSupportsFormatting) {
            return StringUtil.filterText(text);
        }

        return filterText(text, false);
    }

    /**
     * @see net.minecraft.util.StringUtil#filterText(String, boolean)
     */
    public static String filterText(String text, boolean keepLinesBreaks) {
        if (!EasyAnvils.CONFIG.get(ServerConfig.class).miscellaneous.renamingSupportsFormatting) {
            return StringUtil.filterText(text, keepLinesBreaks);
        }

        StringBuilder stringBuilder = new StringBuilder();
        for (char character : text.toCharArray()) {
            if (isAllowedChatCharacter(character)) {
                stringBuilder.append(character);
            } else if (keepLinesBreaks && character == '\n') {
                stringBuilder.append(character);
            }
        }

        return stringBuilder.toString();
    }

    /**
     * @see fuzs.puzzleslib.api.util.v1.ComponentHelper#getAsComponent(String)
     */
    public static Component getAsComponent(String text) {
        Objects.requireNonNull(text, "text is null");
        if (!EasyAnvils.CONFIG.get(ServerConfig.class).miscellaneous.renamingSupportsFormatting) {
            return Component.literal(text);
        }

        return StyleCombiningCharSink.of(text, EMPTY).getAsComponent();
    }

    /**
     * @see String#length()
     */
    public static int stringLength(String text) {
        Objects.requireNonNull(text, "text is null");
        if (!EasyAnvils.CONFIG.get(ServerConfig.class).miscellaneous.renamingSupportsFormatting) {
            return text.length();
        }

        return StyleCombiningCharSink.of(text, Style.EMPTY).length();
    }

    /**
     * @see String#substring(int)
     */
    public static String substring(String text, int startIndex) {
        Objects.requireNonNull(text, "text is null");
        if (!EasyAnvils.CONFIG.get(ServerConfig.class).miscellaneous.renamingSupportsFormatting) {
            return text.substring(startIndex);
        }

        return substring(text, startIndex, text.length());
    }

    /**
     * @see String#substring(int, int)
     */
    public static String substring(String text, int startIndex, int endIndex) {
        Objects.requireNonNull(text, "text is null");
        if (!EasyAnvils.CONFIG.get(ServerConfig.class).miscellaneous.renamingSupportsFormatting) {
            return text.substring(startIndex, endIndex);
        }

        StyleCombiningCharSink styleCombiningCharSink = new StyleCombiningCharSink(Style.EMPTY) {
            @Override
            public boolean accept(int position, Style style, int codePoint) {
                return this.length() < startIndex || this.length() < endIndex && super.accept(position,
                        style,
                        codePoint);
            }
        };
        StringDecomposer.iterateFormatted(FormattedText.of(text), Style.EMPTY, styleCombiningCharSink);
        return styleCombiningCharSink.getAsString();
    }
}
