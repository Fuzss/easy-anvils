package fuzs.easyanvils.util;

import com.google.common.collect.Lists;
import net.minecraft.ChatFormatting;
import net.minecraft.client.StringSplitter;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.FormattedCharSink;
import net.minecraft.util.Mth;
import org.apache.commons.lang3.mutable.MutableFloat;
import org.apache.commons.lang3.mutable.MutableInt;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * custom string decomposer for iterating formatted strings without removing formatting codes, intended for text fields
 */
public class FormattedStringDecomposer {

    public static int stringWidth(Font font, @Nullable String content, int skip) {
        if (content == null) return 0;
        MutableFloat mutableFloat = new MutableFloat();
        FormattedStringDecomposer.iterateFormatted(content, Style.EMPTY, (index, style, j) -> {
            if (index >= skip) {
                mutableFloat.add(font.getSplitter().stringWidth(FormattedCharSequence.forward(Character.toString(j), style)));
            }
            return true;
        });
        return Mth.ceil(mutableFloat.floatValue());
    }

    public static boolean isAllowedChatCharacter(char character) {
        return character >= ' ' && character != 127;
    }

    public static String filterText(String input) {
        return filterText(input, false);
    }

    public static String filterText(String string, boolean keepLinesBreaks) {
        StringBuilder stringBuilder = new StringBuilder();
        char[] var3 = string.toCharArray();
        for (char c : var3) {
            if (isAllowedChatCharacter(c)) {
                stringBuilder.append(c);
            } else if (keepLinesBreaks && c == '\n') {
                stringBuilder.append(c);
            }
        }
        return stringBuilder.toString();
    }

    public static int plainIndexAtWidth(Font font, String content, int skip, int maxWidth, Style style) {
        WidthLimitedCharSink widthLimitedCharSink = new WidthLimitedCharSink(font.getSplitter(), (float) maxWidth, skip);
        iterateFormatted(content, style, widthLimitedCharSink);
        return widthLimitedCharSink.getPosition();
    }

    public static String plainHeadByWidth(Font font, String content, int skip, int maxWidth, Style style) {
        return content.substring(skip, plainIndexAtWidth(font, content, skip, maxWidth, style));
    }

    public static String plainTailByWidth(Font font, String content, int maxWidth, Style style) {
        MutableFloat mutableFloat = new MutableFloat();
        MutableInt mutableInt = new MutableInt(content.length());
        iterateFormattedBackwards(content, style, (j, stylex, k) -> {
            float stringWidth = font.getSplitter().stringWidth(FormattedCharSequence.forward(Character.toString(k), stylex));
            float f = mutableFloat.addAndGet(stringWidth);
            if (f > maxWidth) {
                return false;
            } else {
                mutableInt.setValue(j);
                return true;
            }
        });
        return content.substring(mutableInt.intValue());
    }

    public static boolean iterateFormatted(String text, Style defaultStyle, FormattedCharSink sink) {
        int textLength = text.length();
        Style currentStyle = defaultStyle;

        for (int position = 0; position < textLength; ++position) {
            char character = text.charAt(position);
            if (character == '§') {
                if (position + 1 < textLength) {
                    char d = text.charAt(position + 1);
                    ChatFormatting chatFormatting = ChatFormatting.getByCode(d);
                    if (chatFormatting != null) {
                        currentStyle = chatFormatting == ChatFormatting.RESET ? defaultStyle : currentStyle.applyLegacyFormat(chatFormatting);
                    }

                    // also feed those chars so they show up in the anvil text box
                    if (feedChar(text, defaultStyle, sink, position, character, textLength) == -1) {
                        return false;
                    }
                    position += 1;
                    if (feedChar(text, defaultStyle, sink, position, text.charAt(position), textLength) == -1) {
                        return false;
                    }
                } else {
                    return feedChar(text, defaultStyle, sink, position, character, textLength) != -1;
                }

            } else {
                // weird syntax to allow splitting this into separate method so it can be reused above
                position = feedChar(text, currentStyle, sink, position, character, textLength);
                if (position == -1) {
                    return false;
                }
            }
        }

        return true;
    }

    public static boolean iterateFormattedBackwards(String text, Style defaultStyle, FormattedCharSink sink) {
        List<FormattedCharSequence> list = Lists.newArrayList();
        iterateFormatted(text, defaultStyle, (index, style, j) -> {
            list.add(FormattedCharSequence.forward(Character.toString(j), style));
            return true;
        });
        for (int i = list.size() - 1; i >= 0; i--) {
            if (!list.get(i).accept(sink)) {
                return false;
            }
        }
        return true;
    }

    private static int feedChar(String text, Style style, FormattedCharSink sink, int position, char character, int textLength) {
        if (Character.isHighSurrogate(character)) {
            if (position + 1 >= textLength) {
                if (!sink.accept(position, style, 65533)) {
                    return -1;
                }
                return textLength;
            }

            char d = text.charAt(position + (1));
            if (Character.isLowSurrogate(d)) {
                if (!sink.accept(position, style, Character.toCodePoint(character, d))) {
                    return -1;
                }

                position += 1;
            } else if (!sink.accept(position, style, 65533)) {
                return -1;
            }
        } else if (!feedChar(style, sink, position, character)) {
            return -1;
        }
        return position;
    }

    private static boolean feedChar(Style style, FormattedCharSink sink, int position, char character) {
        return Character.isSurrogate(character) ? sink.accept(position, style, 65533) : sink.accept(position, style, character);
    }

    private static class WidthLimitedCharSink implements FormattedCharSink {
        private final StringSplitter splitter;
        private float maxWidth;
        private final int skip;
        private int position;

        public WidthLimitedCharSink(StringSplitter splitter, float f, int skip) {
            this.splitter = splitter;
            this.maxWidth = f;
            this.skip = skip;
        }

        @Override
        public boolean accept(int i, Style style, int j) {
            if (i >= this.skip) {
                this.maxWidth -= this.splitter.stringWidth(FormattedCharSequence.forward(Character.toString(j), style));
            }
            if (this.maxWidth >= 0.0F) {
                this.position = i + Character.charCount(j);
                return true;
            } else {
                return false;
            }
        }

        public int getPosition() {
            return this.position;
        }

        public void resetPosition() {
            this.position = 0;
        }
    }

    public static class LengthLimitedCharSink implements FormattedCharSink {
        private final int skip;
        private int maxLength;

        public LengthLimitedCharSink(int maxLength, int skip) {
            this.maxLength = maxLength;
            this.skip = skip;
        }

        @Override
        public boolean accept(int i, Style style, int j) {
            if (i >= this.skip) {
                this.maxLength -= Character.charCount(j);
            } else {
                return false;
            }
            return this.maxLength >= 0;
        }
    }
}
