package fuzs.easyanvils.common.client.util;

import net.minecraft.client.StringSplitter;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.FormattedCharSink;

/**
 * @see StringSplitter.WidthLimitedCharSink
 */
public class WidthClosestCharSink implements FormattedCharSink {
    private final StringSplitter splitter;
    private final int skip;
    private float maxWidth;
    private int position;

    public WidthClosestCharSink(StringSplitter splitter, float maxWidth) {
        this(splitter, maxWidth, 0);
    }

    public WidthClosestCharSink(StringSplitter splitter, float maxWidth, int skip) {
        this.splitter = splitter;
        this.skip = skip;
        this.maxWidth = maxWidth;
        this.position = skip;
    }

    @Override
    public boolean accept(int position, Style style, int codePoint) {
        if (position < this.skip) {
            return true;
        }

        float characterWidth = this.splitter.stringWidth(FormattedCharSequence.forward(Character.toString(codePoint),
                style));
        this.maxWidth -= characterWidth;

        if (this.maxWidth >= -characterWidth / 2.0F) {
            this.position = position + Character.charCount(codePoint);
            return true;
        } else {
            return false;
        }
    }

    public int getPosition() {
        return this.position;
    }
}
