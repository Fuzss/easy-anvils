package fuzs.easyanvils.client.gui.screens.inventory;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import fuzs.easyanvils.EasyAnvils;
import fuzs.easyanvils.client.gui.components.OpenEditBox;
import fuzs.easyanvils.client.gui.components.TypeActionManager;
import fuzs.easyanvils.config.ServerConfig;
import fuzs.easyanvils.mixin.client.accessor.AnvilScreenAccessor;
import fuzs.easyanvils.network.client.C2SRenameItemMessage;
import fuzs.easyanvils.util.ComponentDecomposer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AnvilScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class ModAnvilScreen extends AnvilScreen {
    private static final Component TOO_EXPENSIVE_TEXT = new TranslatableComponent("container.repair.expensive");

    private EditBox name;

    public ModAnvilScreen(AnvilMenu anvilMenu, Inventory inventory, Component component) {
        super(anvilMenu, inventory, component);
    }

    @Override
    protected void subInit() {
        int i = (this.width - this.imageWidth) / 2;
        int j = (this.height - this.imageHeight) / 2;
        if (EasyAnvils.CONFIG.get(ServerConfig.class).renamingSupportsFormatting) {
            this.name = new OpenEditBox(this.font, i + 62, j + 24, 103, 12, new TranslatableComponent("container.repair"));
        } else {
            this.name = new EditBox(this.font, i + 62, j + 24, 103, 12, new TranslatableComponent("container.repair"));
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
        ((AnvilScreenAccessor) this).setName(this.name);
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
        if (!slot.getItem().hasCustomHoverName() && input.equals(slot.getItem().getHoverName().getString())) {
            input = "";
        }

        this.menu.setItemName(input);
        EasyAnvils.NETWORK.sendToServer(new C2SRenameItemMessage(input));
    }

    @Override
    public void resize(Minecraft minecraft, int width, int height) {
        TypeActionManager typeActionManager = this.name instanceof OpenEditBox openEditBox ? openEditBox.typeActionManager : null;
        boolean visible = this.name.isVisible();
        super.resize(minecraft, width, height);
        if (typeActionManager != null) ((OpenEditBox) this.name).typeActionManager = typeActionManager;
        this.name.setVisible(visible);
    }


    @Override
    protected void renderLabels(PoseStack poseStack, int mouseX, int mouseY) {
        RenderSystem.disableBlend();
        // copied from AbstractContainerScreen super
        this.font.draw(poseStack, this.title, this.titleLabelX, this.titleLabelY, 4210752);
        this.font.draw(poseStack, this.playerInventoryTitle, this.inventoryLabelX, this.inventoryLabelY, 4210752);
        int i = this.menu.getCost();
        if (i != 0) {
            int j = 8453920;
            Component component;
            // allow for custom max repair cost
            int maxAnvilRepairCost = EasyAnvils.CONFIG.get(ServerConfig.class).tooExpensiveLimit;
            if ((maxAnvilRepairCost != -1 && i >= maxAnvilRepairCost || i == -1) && !this.minecraft.player.getAbilities().instabuild) {
                component = TOO_EXPENSIVE_TEXT;
                j = 16736352;
            } else if (!this.menu.getSlot(2).hasItem()) {
                component = null;
            } else {
                component = new TranslatableComponent("container.repair.cost", i);
                if (!this.menu.getSlot(2).mayPickup(this.minecraft.player)) {
                    j = 16736352;
                }
            }
            if (component != null) {
                int k = this.imageWidth - 8 - this.font.width(component) - 2;
                GuiComponent.fill(poseStack, k - 2, 67, this.imageWidth - 8, 79, 1325400064);
                this.font.drawShadow(poseStack, component, k, 69, j);
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
