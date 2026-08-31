package fuzs.easyanvils.common.client.util;

import fuzs.easyanvils.common.util.FormattedStringDecomposer;
import fuzs.easyanvils.common.util.FormattedStringUtil;
import fuzs.puzzleslib.common.api.util.v1.StyleCombiningCharSink;
import net.minecraft.client.StringSplitter;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import org.apache.commons.lang3.mutable.MutableFloat;

import java.util.Objects;

/**
 * @see StringSplitter
 */
public class FormattedStringSplitter {

    /**
     * @see Font#width(String)
     */
    public static int width(StringSplitter splitter, String text) {
        return Mth.ceil(stringWidth(splitter, text));
    }

    /**
     * @see Font#width(String)
     */
    public static int width(StringSplitter splitter, String text, int start, int end) {
        return Mth.ceil(stringWidth(splitter, text, start, end));
    }

    /**
     * @see StringSplitter#stringWidth(String)
     */
    public static float stringWidth(StringSplitter splitter, String content) {
        return stringWidth(splitter, content, 0, content.length());
    }

    /**
     * @see StringSplitter#stringWidth(String)
     */
    public static float stringWidth(StringSplitter splitter, String content, int start, int end) {
        Objects.requireNonNull(content, "content is null");
        MutableFloat width = new MutableFloat();
        StyleCombiningCharSink sink = new StyleCombiningCharSink(FormattedStringUtil.EMPTY);
        FormattedStringDecomposer.iterateFormatted(content, FormattedStringUtil.EMPTY, sink);
        sink.iterateForwards((int position, Style style, int codePoint) -> {
            if (position >= start && position < end) {
                width.add(splitter.stringWidth(FormattedCharSequence.forward(Character.toString(codePoint), style)));
            }

            return position < end;
        });
        return width.floatValue();
    }

    /**
     * @see Font#plainSubstrByWidth(String, int)
     */
    public static String plainSubstrByWidth(StringSplitter splitter, String text, int maxWidth, int skip) {
        return plainHeadByWidth(splitter, text, maxWidth, FormattedStringUtil.EMPTY, skip);
    }

    /**
     * @see StringSplitter#plainHeadByWidth(String, int, Style)
     */
    public static String plainHeadByWidth(StringSplitter splitter, String content, int maxWidth, Style style, int skip) {
        Objects.requireNonNull(content, "content is null");
        StyleCombiningCharSink sink = new StyleCombiningCharSink(FormattedStringUtil.EMPTY);
        FormattedStringDecomposer.iterateFormatted(content, FormattedStringUtil.EMPTY, sink);
        WidthLimitedCharSink widthSink = new WidthLimitedCharSink(splitter, maxWidth, skip);
        sink.iterateForwards(widthSink);
        return content.substring(skip, widthSink.getPosition());
    }

    /**
     * @see Font#plainSubstrByWidth(String, int, boolean)
     */
    public static String plainSubstrByWidth(StringSplitter splitter, String text, int maxWidth, boolean tail) {
        return tail ? plainTailByWidth(splitter, text, maxWidth, FormattedStringUtil.EMPTY) :
                plainSubstrByWidth(splitter, text, maxWidth, 0);
    }

    /**
     * @see StringSplitter#plainTailByWidth(String, int, Style)
     */
    public static String plainTailByWidth(StringSplitter splitter, String content, int maxWidth, Style style) {
        Objects.requireNonNull(content, "content is null");
        StyleCombiningCharSink sink = new StyleCombiningCharSink(FormattedStringUtil.EMPTY);
        FormattedStringDecomposer.iterateFormatted(content, FormattedStringUtil.EMPTY, sink);
        WidthLimitedCharSink widthSink = new WidthLimitedCharSink(splitter, maxWidth);
        sink.iterateBackwards(widthSink);
        return content.substring(widthSink.getPosition());
    }

    /**
     * @see StringSplitter#plainIndexAtWidth(String, int, Style)
     */
    public static int plainIndexAtWidth(StringSplitter stringSplitter, String content, int width, int skip) {
        Objects.requireNonNull(content, "content is null");
        StyleCombiningCharSink sink = new StyleCombiningCharSink(FormattedStringUtil.EMPTY);
        FormattedStringDecomposer.iterateFormatted(content, FormattedStringUtil.EMPTY, sink);
        WidthClosestCharSink widthSink = new WidthClosestCharSink(stringSplitter, width, skip);
        sink.iterateForwards(widthSink);
        return widthSink.getPosition();
    }
}
