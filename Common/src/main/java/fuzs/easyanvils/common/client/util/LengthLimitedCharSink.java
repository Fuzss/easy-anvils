package fuzs.easyanvils.common.client.util;

import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSink;

public class LengthLimitedCharSink implements FormattedCharSink {
    private final int skip;
    private int maxLength;

    public LengthLimitedCharSink(int maxLength, int skip) {
        this.maxLength = maxLength;
        this.skip = skip;
    }

    @Override
    public boolean accept(int position, Style style, int codePoint) {
        if (position >= this.skip) {
            this.maxLength -= Character.charCount(codePoint);
            return this.maxLength >= 0;
        } else {
            return false;
        }
    }
}
