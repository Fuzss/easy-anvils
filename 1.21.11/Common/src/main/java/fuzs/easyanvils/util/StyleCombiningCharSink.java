package fuzs.easyanvils.util;

import fuzs.puzzleslib.api.util.v1.ComponentHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSink;
import net.minecraft.util.StringDecomposer;
import org.apache.commons.lang3.mutable.MutableInt;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;

@Deprecated
public class StyleCombiningCharSink implements FormattedCharSink {
    private final List<Map.Entry<StringBuilder, Style>> strings;
    private final Style defaultStyle;
    private int length;

    public StyleCombiningCharSink(String string, Style defaultStyle) {
        this(FormattedText.of(string), defaultStyle);
    }

    public StyleCombiningCharSink(FormattedText formattedText, Style defaultStyle) {
        this(defaultStyle);
        // Use this to properly convert legacy formatting codes that are part of the string value.
        StringDecomposer.iterateFormatted(formattedText, defaultStyle, this);
    }

    public StyleCombiningCharSink(Style defaultStyle) {
        this.strings = new ArrayList<>(List.of(Map.entry(new StringBuilder(), Style.EMPTY)));
        this.defaultStyle = defaultStyle;
    }

    public int length() {
        return this.length;
    }

    @Override
    public boolean accept(int position, Style style, int codePoint) {
        Map.Entry<StringBuilder, Style> entry = this.strings.getLast();
        StringBuilder stringBuilder;
        if (Objects.equals(entry.getValue(), style)) {
            stringBuilder = entry.getKey();
        } else {
            stringBuilder = new StringBuilder();
            this.strings.add(Map.entry(stringBuilder, style));
        }

        stringBuilder.appendCodePoint(codePoint);
        this.length += Character.charCount(codePoint);
        return true;
    }

    public Component getAsComponent() {
        MutableComponent mutableComponent = Component.empty().withStyle(this.defaultStyle);
        this.iterateForwards((String string, Style style) -> {
            Component component = FormattedStringUtil.getAsComponent(string, style);
            mutableComponent.append(component);
        });
        return mutableComponent;
    }

    public String getAsString() {
        StringBuilder mainStringBuilder = new StringBuilder();
        this.iterateForwards((String string, Style style) -> {
            mainStringBuilder.append(FormattedStringUtil.getAsString(string, style));
        });
        return mainStringBuilder.toString();
    }

    public void iterateForwards(FormattedCharSink formattedCharSink) {
        MutableInt mutableInt = new MutableInt();
        this.iterate(this.strings, (String string, Style style) -> {
            if (StringDecomposer.iterate(string, style, (int position, Style currentStyle, int codePoint) -> {
                return formattedCharSink.accept(mutableInt.intValue() + position, currentStyle, codePoint);
            })) {
                mutableInt.add(string.length());
                return true;
            } else {
                return false;
            }
        });
    }

    public void iterateForwards(BiConsumer<String, Style> componentConsumer) {
        this.iterate(this.strings, (String string, Style style) -> {
            componentConsumer.accept(string, style);
            return true;
        });
    }

    public void iterateBackwards(FormattedCharSink formattedCharSink) {
        MutableInt mutableInt = new MutableInt(this.length());
        this.iterate(this.strings.reversed(), (String string, Style style) -> {
            mutableInt.subtract(string.length());
            return StringDecomposer.iterateBackwards(string,
                    style,
                    (int position, Style currentStyle, int codePoint) -> {
                        return formattedCharSink.accept(mutableInt.intValue() + position, currentStyle, codePoint);
                    });
        });
    }

    public void iterateBackwards(BiConsumer<String, Style> componentConsumer) {
        this.iterate(this.strings.reversed(), (String string, Style style) -> {
            componentConsumer.accept(string, style);
            return true;
        });
    }

    private void iterate(List<Map.Entry<StringBuilder, Style>> strings, BiPredicate<String, Style> componentConsumer) {
        for (Map.Entry<StringBuilder, Style> entry : strings) {
            if (!entry.getKey().isEmpty() || !entry.getValue().isEmpty()) {
                Style style = this.defaultStyle.isEmpty() ? ComponentHelper.sanitizeLegacyFormat(entry.getValue()) :
                        entry.getValue();
                if (!componentConsumer.test(entry.getKey().toString(), style)) {
                    break;
                }
            }
        }
    }
}
