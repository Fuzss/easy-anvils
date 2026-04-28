package fuzs.easyanvils.common.util;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSink;
import net.minecraft.util.StringDecomposer;

/**
 * @see net.minecraft.util.StringDecomposer
 */
public class FormattedStringDecomposer {

    /**
     * @see net.minecraft.util.StringDecomposer#iterateFormatted(String, Style, FormattedCharSink)
     */
    public static boolean iterateFormatted(String text, Style style, FormattedCharSink sink) {
        return iterateFormatted(text, 0, style, sink);
    }

    /**
     * @see net.minecraft.util.StringDecomposer#iterateFormatted(String, int, Style, FormattedCharSink)
     */
    public static boolean iterateFormatted(String text, int skip, Style style, FormattedCharSink sink) {
        return iterateFormatted(text, skip, style, style, sink);
    }

    /**
     * The vanilla method normally removes all formatting codes (in the form of '§' + character code) and converts them
     * directly to the current style.
     * <p>
     * We don't want formatting codes to be removed, but do also want them to be applied to the current style to allow
     * for editing text with formatting codes in a text field.
     *
     * @see net.minecraft.util.StringDecomposer#iterateFormatted(String, int, Style, Style, FormattedCharSink)
     */
    public static boolean iterateFormatted(String text, int skip, Style currentStyle, Style defaultStyle, FormattedCharSink sink) {
        Style style = currentStyle;
        for (int position = skip; position < text.length(); position++) {
            char character = text.charAt(position);
            if (character == '§') {
                if (position + 1 < text.length()) {
                    char d = text.charAt(position + 1);
                    ChatFormatting chatFormatting = ChatFormatting.getByCode(d);
                    if (chatFormatting != null) {
                        style = chatFormatting == ChatFormatting.RESET ? defaultStyle :
                                style.applyLegacyFormat(chatFormatting);
                    }
                }
            }

            if (Character.isHighSurrogate(character)) {
                if (position + 1 >= text.length()) {
                    if (!sink.accept(position, style, 65533)) {
                        return false;
                    }
                    break;
                }

                char d = text.charAt(position + 1);
                if (Character.isLowSurrogate(d)) {
                    if (!sink.accept(position, style, Character.toCodePoint(character, d))) {
                        return false;
                    }

                    position++;
                } else if (!sink.accept(position, style, 65533)) {
                    return false;
                }
            } else if (!StringDecomposer.feedChar(
                    character == '§' || position - 1 >= 0 && text.charAt(position - 1) == '§' ? defaultStyle : style,
                    sink,
                    position,
                    character)) {
                return false;
            }
        }

        return true;
    }
}
