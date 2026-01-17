package fuzs.easyanvils.util;

import fuzs.puzzleslib.api.util.v1.ComponentHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.Style;
import net.minecraft.util.StringUtil;
import net.minecraft.world.item.ItemStack;
import org.apache.commons.lang3.mutable.MutableInt;

import java.util.Objects;

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
    public static boolean isAllowedChatCharacter(CharacterEvent characterEvent) {
        return isAllowedChatCharacter(characterEvent.codepoint());
    }

    /**
     * @see net.minecraft.util.StringUtil#isAllowedChatCharacter(int)
     */
    public static boolean isAllowedChatCharacter(int codePoint) {
        return StringUtil.isAllowedChatCharacter(codePoint) || codePoint == '§';
    }

    /**
     * @see net.minecraft.util.StringUtil#filterText(String)
     */
    public static String filterText(String text) {
        return filterText(text, false);
    }

    /**
     * @see net.minecraft.util.StringUtil#filterText(String, boolean)
     */
    public static String filterText(String text, boolean keepLinesBreaks) {
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

    public static Component getAsComponent(String string) {
        Objects.requireNonNull(string, "string is null");
        return new StyleCombiningCharSink(string, EMPTY).getAsComponent();
    }

    public static int stringLength(String string) {
        Objects.requireNonNull(string, "string is null");
        return new StyleCombiningCharSink(string, Style.EMPTY).length();
    }

    public static String substring(String string, int startIndex) {
        return substring(string, startIndex, string.length());
    }

    public static String substring(String string, int startIndex, int endIndex) {
        Objects.requireNonNull(string, "string is null");
        return new StyleCombiningCharSink(string, Style.EMPTY) {
            @Override
            public boolean accept(int position, Style style, int codePoint) {
                return this.length() < startIndex || this.length() < endIndex && super.accept(position,
                        style,
                        codePoint);
            }
        }.getAsString();
    }

    public static String deleteLastCharacters(String string, int charCount) {
        Objects.requireNonNull(string, "string is null");
        // use this to properly convert legacy formatting codes that are part of the string value
        StyleCombiningCharSink styleCombiningCharSink = new StyleCombiningCharSink(string, Style.EMPTY);
        StyleCombiningCharSink styleCombiningCharSink2 = new StyleCombiningCharSink(Style.EMPTY);
        MutableInt mutableInt = new MutableInt(styleCombiningCharSink.length() - charCount);
        styleCombiningCharSink.iterateForwards((int position, Style style, int codePoint) -> {
            mutableInt.subtract(Character.charCount(codePoint));
            if (mutableInt.intValue() >= 0) {
                return styleCombiningCharSink2.accept(position, style, codePoint);
            } else {
                return false;
            }
        });
        return styleCombiningCharSink2.getAsString();
    }

    @Deprecated
    public static Component getAsComponent(String string, Style style) {
        return Component.literal(string).withStyle(style);
    }

    @Deprecated
    public static String getAsString(String string, Style style) {
        if (!style.isEmpty()) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(ComponentHelper.getAsString(style));
            stringBuilder.append(string);
            stringBuilder.append(ChatFormatting.RESET);
            return stringBuilder.toString();
        } else {
            return string;
        }
    }
}
