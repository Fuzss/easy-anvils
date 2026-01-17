package fuzs.easyanvils.util;

import com.google.common.collect.ImmutableList;
import fuzs.puzzleslib.api.util.v1.ComponentHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Style;
import org.apache.commons.lang3.mutable.MutableInt;

import java.util.List;
import java.util.Map;

public class FormattedTextCharSink extends StyleCombiningCharSink {

    public FormattedTextCharSink(String string, Style defaultStyle) {
        this(defaultStyle);
        FormattedStringDecomposer.iterateFormatted(string, defaultStyle, this);
    }

    public FormattedTextCharSink(Style defaultStyle) {
        super(defaultStyle);
    }

    // TODO cleanup

//    @Override
//    public List<Map.Entry<String, Style>> getStrings() {
//        ImmutableList.Builder<Map.Entry<String, Style>> builder = ImmutableList.builder();
//        for (Map.Entry<String, Style> entry : super.getStrings()) {
//            String styleString = ComponentHelper.getAsString(entry.getValue());
//            if (!styleString.isEmpty()) {
//                builder.add(Map.entry(styleString, Style.EMPTY));
//            }
//
//            if (!entry.getKey().isEmpty()) {
//                builder.add(entry);
//            }
//
//            if (!styleString.isEmpty()) {
//                builder.add(Map.entry(ChatFormatting.RESET.toString(), Style.EMPTY));
//            }
//        }
//
//        return builder.build();
//    }
//
//    @Override
//    public int getPosition() {
//        return super.getPosition() + this.getStyles().stream().mapToInt((Style style) -> {
//            MutableInt mutableInt = new MutableInt();
//            ComponentHelper.getLegacyFormat(style, (ChatFormatting chatFormatting) -> {
//                mutableInt.add(chatFormatting.toString().length());
//            });
//            return mutableInt.intValue() > 0 ? mutableInt.intValue() + ChatFormatting.RESET.toString().length() : 0;
//        }).sum();
//    }
}
