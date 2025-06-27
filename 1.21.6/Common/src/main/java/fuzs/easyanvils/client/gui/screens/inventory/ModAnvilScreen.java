package fuzs.easyanvils.client.gui.screens.inventory;

import fuzs.easyanvils.EasyAnvils;
import fuzs.easyanvils.client.gui.components.FormattableEditBox;
import fuzs.easyanvils.client.gui.components.FormattingGuideWidget;
import fuzs.easyanvils.config.ServerConfig;
import fuzs.easyanvils.network.client.ServerboundRenameItemMessage;
import fuzs.easyanvils.util.ComponentDecomposer;
import fuzs.easyanvils.world.level.block.entity.AnvilBlockEntity;
import fuzs.puzzleslib.api.network.v4.MessageSender;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AnvilScreen;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class ModAnvilScreen extends AnvilScreen {
    private static final Component TOO_EXPENSIVE_TEXT = Component.translatable("container.repair.expensive");

    public ModAnvilScreen(AnvilMenu anvilMenu, Inventory inventory, Component component) {
        super(anvilMenu, inventory, component);
        this.titleLabelY = 8;
    }

    @Override
    protected void subInit() {
        int i = (this.width - this.imageWidth) / 2;
        int j = (this.height - this.imageHeight) / 2;
        if (EasyAnvils.CONFIG.get(ServerConfig.class).miscellaneous.renamingSupportsFormatting) {
            this.name = new FormattableEditBox(this.font, i + 62, j + 24, 103, 12, AnvilBlockEntity.REPAIR_COMPONENT);
        } else {
            this.name = new EditBox(this.font, i + 62, j + 24, 103, 12, AnvilBlockEntity.REPAIR_COMPONENT);
        }
        this.name.setCanLoseFocus(false);
        this.name.setTextColor(-1);
        this.name.setTextColorUneditable(-1);
        this.name.setBordered(false);
        this.name.setMaxLength(50);
        this.name.setResponder(this::onNameChanged);
        this.name.setValue("");
        this.addWidget(this.name);
        this.setInitialFocus(this.name);
        this.name.setEditable(false);
        this.name.setVisible(false);
        this.addRenderableWidget(new FormattingGuideWidget(this.leftPos + this.imageWidth - 7,
                this.topPos + this.titleLabelY,
                this.font));
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (this.getFocused() == this.name && this.isDragging() && button == 0) {
            return this.getFocused().mouseDragged(mouseX, mouseY, button, dragX, dragY);
        }

        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    private void onNameChanged(String input) {
        Slot slot = this.menu.getSlot(0);
        if (!slot.hasItem()) return;
        if (!slot.getItem().has(DataComponents.CUSTOM_NAME) &&
                input.equals(slot.getItem().getHoverName().getString())) {
            input = "";
        }

        if (this.menu.setItemName(input)) {
            MessageSender.broadcast(new ServerboundRenameItemMessage(input));
        }
    }

    @Override
    public void resize(Minecraft minecraft, int width, int height) {
        boolean visible = this.name.isVisible();
        super.resize(minecraft, width, height);
        this.name.setVisible(visible);
    }


    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        // copied from AbstractContainerScreen super
        guiGraphics.drawString(this.font, this.title, this.titleLabelX, this.titleLabelY, 0x404040, false);
        guiGraphics.drawString(this.font,
                this.playerInventoryTitle,
                this.inventoryLabelX,
                this.inventoryLabelY,
                4210752,
                false);
        int i = this.menu.getCost();
        if (i != 0) {
            int j = 8453920;
            Component component;
            // allow for custom max repair cost
            int maxAnvilRepairCost = EasyAnvils.CONFIG.get(ServerConfig.class).costs.tooExpensiveLimit;
            if ((maxAnvilRepairCost != -1 && i >= maxAnvilRepairCost || i == -1) &&
                    !this.minecraft.player.getAbilities().instabuild) {
                component = TOO_EXPENSIVE_TEXT;
                j = 16736352;
            } else if (!this.menu.getSlot(2).hasItem()) {
                component = null;
            } else {
                component = Component.translatable("container.repair.cost", i);
                if (!this.menu.getSlot(2).mayPickup(this.minecraft.player)) {
                    j = 16736352;
                }
            }
            if (component != null) {
                int k = this.imageWidth - 8 - this.font.width(component) - 2;
                guiGraphics.fill(k - 2, 67, this.imageWidth - 8, 79, 1325400064);
                guiGraphics.drawString(this.font, component, k, 69, j);
            }
        }
    }

    @Override
    public void slotChanged(AbstractContainerMenu containerToSend, int dataSlotIndex, ItemStack stack) {
        if (dataSlotIndex == 0) {
            this.name.setValue(stack.isEmpty() ? "" : ComponentDecomposer.toFormattedString(stack.getHoverName()));
            this.name.setEditable(!stack.isEmpty());
            this.setFocused(this.name);
            this.name.setVisible(!stack.isEmpty());
        }
    }
}
