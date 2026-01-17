package fuzs.easyanvils.client.util;

import fuzs.easyanvils.util.FormattedStringDecomposer;
import fuzs.easyanvils.util.FormattedStringUtil;
import fuzs.easyanvils.util.FormattedTextCharSink;
import fuzs.easyanvils.util.StyleCombiningCharSink;
import net.minecraft.client.StringSplitter;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.util.StringDecomposer;
import org.apache.commons.lang3.mutable.MutableFloat;
import org.apache.commons.lang3.mutable.MutableInt;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public class FormattedStringSplitter {

    /**
     * @see Font#width(String)
     */
    public static int width(StringSplitter stringSplitter, String text) {
        return Mth.ceil(stringWidth(stringSplitter, text));
    }

    /**
     * @see StringSplitter#stringWidth(String)
     */
    public static float stringWidth(StringSplitter stringSplitter, String content) {
        MutableFloat mutableFloat = new MutableFloat();
        StyleCombiningCharSink styleCombiningCharSink = new FormattedTextCharSink(content, FormattedStringUtil.EMPTY);
        styleCombiningCharSink.iterateForwards((int position, Style style, int codePoint) -> {
            mutableFloat.add(stringSplitter.stringWidth(FormattedCharSequence.forward(Character.toString(codePoint),
                    style)));
            return true;
        });
        // TODO cleanup
        MutableFloat mutableFloat2 = new MutableFloat();
        FormattedStringDecomposer.iterateFormatted(content,
                FormattedStringUtil.EMPTY,
                (int position, Style style, int codePoint) -> {
                    mutableFloat2.add(stringSplitter.stringWidth(FormattedCharSequence.forward(Character.toString(
                            codePoint), style)));
                    return true;
                });
        return mutableFloat.floatValue();
    }

    /**
     * @see Font#plainSubstrByWidth(String, int)
     */
    public static String plainSubstrByWidth(StringSplitter stringSplitter, String text, int maxWidth, int skip) {
        return plainHeadByWidth(stringSplitter, text, maxWidth, FormattedStringUtil.EMPTY, skip);
    }

    /**
     * @see StringSplitter#plainHeadByWidth(String, int, Style)
     */
    public static String plainHeadByWidth(StringSplitter stringSplitter, String content, int maxWidth, Style style, int skip) {
        Objects.requireNonNull(content, "string is null");
        FormattedTextCharSink formattedTextCharSink = new FormattedTextCharSink(content, style);
        WidthLimitedCharSink widthLimitedCharSink = new WidthLimitedCharSink(stringSplitter, maxWidth, skip);
        formattedTextCharSink.iterateForwards(widthLimitedCharSink);
        return content.substring(skip, widthLimitedCharSink.getPosition());
    }

    /**
     * @see Font#plainSubstrByWidth(String, int, boolean)
     */
    public static String plainSubstrByWidth(StringSplitter stringSplitter, String text, int maxWidth, boolean tail) {
        return tail ? plainTailByWidth(stringSplitter, text, maxWidth, FormattedStringUtil.EMPTY) :
                plainHeadByWidth(stringSplitter, text, maxWidth, FormattedStringUtil.EMPTY, 0);
    }

    /**
     * @see StringSplitter#plainTailByWidth(String, int, Style)
     */
    public static String plainTailByWidth(StringSplitter stringSplitter, String content, int maxWidth, Style style) {
        Objects.requireNonNull(content, "string is null");
        FormattedTextCharSink formattedTextCharSink = new FormattedTextCharSink(content, style);
        WidthLimitedCharSink widthLimitedCharSink = new WidthLimitedCharSink(stringSplitter, maxWidth);
        formattedTextCharSink.iterateBackwards(widthLimitedCharSink);
        return content.substring(widthLimitedCharSink.getPosition());
        // TODO cleanup
//        Objects.requireNonNull(content, "string is null");
//        StyleCombiningCharSink styleCombiningCharSink = new StyleCombiningCharSink(content);
//        StringSplitter.WidthLimitedCharSink widthLimitedCharSink = stringSplitter.new WidthLimitedCharSink(maxWidth);
//        MutableInt mutableInt = new MutableInt(content.length());
//        for (Map.Entry<StringBuilder, Style> entry : styleCombiningCharSink.getFormattedStrings().reversed()) {
//            mutableInt.subtract(entry.getKey().length());
//            StringDecomposer.iterateBackwards(entry.getKey().toString(),
//                    entry.getValue(),
//                    (int position, Style currentStyle, int codePoint) -> {
//                        return widthLimitedCharSink.accept(mutableInt.intValue() + position, currentStyle, codePoint);
//                    });
//        }
//
//        return styleCombiningCharSink.getAsString().substring(widthLimitedCharSink.getPosition());
    }
}
