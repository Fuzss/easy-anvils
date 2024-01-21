package fuzs.easyanvils.client.gui.screens.inventory;

import com.mojang.blaze3d.systems.RenderSystem;
import fuzs.easyanvils.EasyAnvils;
import fuzs.easyanvils.client.gui.components.OpenEditBox;
import fuzs.easyanvils.config.ServerConfig;
import fuzs.easyanvils.network.client.C2SNameTagUpdateMessage;
import fuzs.easyanvils.util.ComponentDecomposer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class NameTagEditScreen extends Screen {
    private static final ResourceLocation EDIT_NAME_TAG_LOCATION = new ResourceLocation(EasyAnvils.MOD_ID, "textures/gui/edit_name_tag.png");

    private final int imageWidth = 176;
    private final int imageHeight = 48;
    private int leftPos;
    private int topPos;
    private final int titleLabelX = 60;
    private final int titleLabelY = 8;
    private final InteractionHand hand;
    private String itemName;
    private EditBox name;

    public NameTagEditScreen(InteractionHand hand, Component title) {
        super(Component.translatable("easyanvils.name_tag.edit", Items.NAME_TAG.getDescription()));
        this.hand = hand;
        this.itemName = ComponentDecomposer.toFormattedString(title);
    }

    @Override
    protected void init() {
        this.leftPos = (this.width - this.imageWidth) / 2;
        this.topPos = this.height / 4;
        this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, (button) -> {
            EasyAnvils.NETWORK.sendToServer(new C2SNameTagUpdateMessage(this.hand, this.itemName));
            this.onClose();
        }).bounds(this.width / 2 - 100, this.height / 4 + 120, 200, 20).build());
        if (EasyAnvils.CONFIG.get(ServerConfig.class).renamingSupportsFormatting) {
            this.name = new OpenEditBox(this.font, this.leftPos + 62, this.topPos + 26, 103, 12, Component.translatable("container.repair"));
        } else {
            this.name = new EditBox(this.font, this.leftPos + 62, this.topPos + 26, 103, 12, Component.translatable("container.repair"));
        }
        this.name.setCanLoseFocus(false);
        this.name.setTextColor(-1);
        this.name.setTextColorUneditable(-1);
        this.name.setBordered(false);
        this.name.setMaxLength(50);
        this.name.setResponder(s -> this.itemName = s);
        this.name.setValue(this.itemName);
        this.addWidget(this.name);
        this.setInitialFocus(this.name);
    }

    @Override
    public void tick() {
        this.name.tick();
    }

    @Override
    public void resize(Minecraft minecraft, int width, int height) {
        String s = this.name.getValue();
        this.init(minecraft, width, height);
        this.name.setValue(s);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics);
        this.renderBg(guiGraphics, partialTick, mouseX, mouseY);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        guiGraphics.drawString(this.font, this.title, this.leftPos + this.titleLabelX, this.topPos + this.titleLabelY, 4210752, false);
        this.name.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        guiGraphics.blit(EDIT_NAME_TAG_LOCATION, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);
        guiGraphics.pose().pushPose();
        guiGraphics.pose().scale(2.0F, 2.0F, 2.0F);
        guiGraphics.renderItem(new ItemStack(Items.NAME_TAG), (this.leftPos + 17) / 2, (this.topPos + 8) / 2);
        guiGraphics.pose().popPose();
    }
}
