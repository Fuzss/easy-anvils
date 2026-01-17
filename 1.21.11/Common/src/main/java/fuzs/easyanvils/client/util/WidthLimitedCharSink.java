package fuzs.easyanvils.client.util;

import net.minecraft.client.StringSplitter;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.FormattedCharSink;

/**
 * @see StringSplitter.WidthLimitedCharSink
 */
public class WidthLimitedCharSink implements FormattedCharSink {
    private final StringSplitter stringSplitter;
    private final int skip;
    private float maxWidth;
    private int position;

    public WidthLimitedCharSink(StringSplitter stringSplitter, float maxWidth) {
        this(stringSplitter, maxWidth, 0);
    }

    public WidthLimitedCharSink(StringSplitter stringSplitter, float maxWidth, int skip) {
        this.stringSplitter = stringSplitter;
        this.skip = skip;
        this.maxWidth = maxWidth;
    }

    @Override
    public boolean accept(int position, Style style, int codePoint) {
        if (position >= this.skip) {
            this.maxWidth -= this.stringSplitter.stringWidth(FormattedCharSequence.forward(Character.toString(codePoint),
                    style));
        }

        if (this.maxWidth >= 0.0F) {
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
