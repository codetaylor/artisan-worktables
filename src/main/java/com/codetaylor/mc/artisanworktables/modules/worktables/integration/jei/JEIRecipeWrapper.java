package com.codetaylor.mc.artisanworktables.modules.worktables.integration.jei;

import com.codetaylor.mc.artisanworktables.api.internal.recipe.IArtisanIngredient;
import com.codetaylor.mc.artisanworktables.api.internal.recipe.OutputWeightPair;
import com.codetaylor.mc.artisanworktables.api.internal.reference.EnumTier;
import com.codetaylor.mc.artisanworktables.api.recipe.ArtisanRecipe;
import com.codetaylor.mc.artisanworktables.modules.worktables.ModuleWorktables;
import com.codetaylor.mc.athenaeum.gui.GuiHelper;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.IRecipeWrapper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.client.config.GuiUtils;

import java.util.ArrayList;
import java.util.List;

public class JEIRecipeWrapper
    implements IRecipeWrapper {

  private static final ResourceLocation RECIPE_BACKGROUND = new ResourceLocation(
      ModuleWorktables.MOD_ID,
      "textures/gui/recipe_background.png"
  );

  public static EnumTier CATEGORY_TIER = EnumTier.WORKTABLE;

  private final ArtisanRecipe artisanRecipe;

  public JEIRecipeWrapper(
      ArtisanRecipe artisanRecipe
  ) {

    this.artisanRecipe = artisanRecipe;
  }

  public ResourceLocation getRegistryName() {

    return new ResourceLocation(this.artisanRecipe.getName());
  }

  public List<List<ItemStack>> getInputs() {

    List<List<ItemStack>> result = new ArrayList<>();

    for (IArtisanIngredient input : this.artisanRecipe.getIngredientList()) {
      ItemStack[] matchingStacks = input.toIngredient().getMatchingStacks();
      List<ItemStack> list = new ArrayList<>(matchingStacks.length);

      for (ItemStack matchingStack : matchingStacks) {
        list.add(matchingStack.copy());
      }
      result.add(list);
    }

    return result;
  }

  public FluidStack getFluidStack() {

    return this.artisanRecipe.getFluidIngredient();
  }

  public List<OutputWeightPair> getWeightedOutput() {

    return this.artisanRecipe.getOutputWeightPairList();
  }

  public List<ItemStack> getOutput() {

    List<OutputWeightPair> output = this.artisanRecipe.getOutputWeightPairList();
    List<ItemStack> result = new ArrayList<>(output.size());

    for (OutputWeightPair pair : output) {
      result.add(pair.getOutput().toItemStack().copy());
    }

    return result;
  }

  public boolean isShaped() {

    return this.artisanRecipe.isShaped();
  }

  public int getWidth() {

    return this.artisanRecipe.getWidth();
  }

  public int getHeight() {

    return this.artisanRecipe.getHeight();
  }

  public List<List<ItemStack>> getTools() {

    List<List<ItemStack>> result = new ArrayList<>();

    for (int i = 0; i < artisanRecipe.getToolCount(); i++) {
      ItemStack[] tools = this.artisanRecipe.getTools(i);
      List<ItemStack> itemStackList = new ArrayList<>(tools.length);

      for (ItemStack tool : tools) {
        itemStackList.add(tool.copy());
      }

      result.add(itemStackList);
    }

    return result;
  }

  public List<List<ItemStack>> getSecondaryInputs() {

    List<List<ItemStack>> result = new ArrayList<>();

    for (IArtisanIngredient ingredient : this.artisanRecipe.getSecondaryIngredients()) {
      ItemStack[] matchingStacks = ingredient.toIngredient().getMatchingStacks();
      List<ItemStack> list = new ArrayList<>(matchingStacks.length);

      for (ItemStack matchingStack : matchingStacks) {
        list.add(matchingStack.copy());
      }
      result.add(list);
    }

    return result;
  }

  public ItemStack getSecondaryOutput() {

    return this.artisanRecipe.getSecondaryOutput().toItemStack();
  }

  public ItemStack getTertiaryOutput() {

    return this.artisanRecipe.getTertiaryOutput().toItemStack();
  }

  public ItemStack getQuaternaryOutput() {

    return this.artisanRecipe.getQuaternaryOutput().toItemStack();
  }

  @Override
  public void getIngredients(IIngredients ingredients) {

    List<List<ItemStack>> inputs = new ArrayList<>();
    inputs.addAll(this.getInputs());
    inputs.addAll(this.getTools());
    inputs.addAll(this.getSecondaryInputs());
    ingredients.setInputLists(ItemStack.class, inputs);

    FluidStack fluidIngredient = this.artisanRecipe.getFluidIngredient();

    if (fluidIngredient != null) {
      ingredients.setInput(FluidStack.class, fluidIngredient);
    }

    List<ItemStack> output = new ArrayList<>();
    output.addAll(this.getOutput());
    output.add(this.getSecondaryOutput());
    output.add(this.getTertiaryOutput());
    output.add(this.getQuaternaryOutput());
    ingredients.setOutputs(ItemStack.class, output);
  }

  @Override
  public void drawInfo(
      Minecraft minecraft, int recipeWidth, int recipeHeight, int mouseX, int mouseY
  ) {

    GlStateManager.pushMatrix();
    GlStateManager.translate(0, 0, 1000);

    String experienceString = null;

    if (this.artisanRecipe.getExperienceRequired() > 0) {

      if (this.artisanRecipe.consumeExperience()) {
        experienceString = I18n.format(ModuleWorktables.Lang.JEI_XP_COST, this.artisanRecipe.getExperienceRequired());

      } else {
        experienceString = I18n.format(
            ModuleWorktables.Lang.JEI_XP_REQUIRED,
            this.artisanRecipe.getExperienceRequired()
        );
      }

    } else if (this.artisanRecipe.getLevelRequired() > 0) {

      if (this.artisanRecipe.consumeExperience()) {
        experienceString = I18n.format(ModuleWorktables.Lang.JEI_LEVEL_COST, this.artisanRecipe.getLevelRequired());

      } else {
        experienceString = I18n.format(ModuleWorktables.Lang.JEI_LEVEL_REQUIRED, this.artisanRecipe.getLevelRequired());
      }
    }

    if (experienceString != null) {
      this.drawExperienceString(minecraft, recipeHeight, experienceString);
    }

    if (CATEGORY_TIER == EnumTier.WORKTABLE) {
      this.drawToolDamageString(minecraft, 83, 52);

    } else if (CATEGORY_TIER == EnumTier.WORKSTATION) {
      this.drawToolDamageString(minecraft, 83, 33);

    } else if (CATEGORY_TIER == EnumTier.WORKSHOP) {
      this.drawToolDamageString(minecraft, 119, 39);
    }
    GlStateManager.popMatrix();

    GlStateManager.pushMatrix();
    GlStateManager.scale(0.5, 0.5, 1);
    GlStateManager.translate(0, 0, 1000);
    GlStateManager.enableDepth();
    GlStateManager.pushMatrix();

    if (CATEGORY_TIER == EnumTier.WORKSHOP) {

      int xPos = 256;
      int yPos = 12;

      if (!this.artisanRecipe.getSecondaryOutput().isEmpty()) {
        float chance = this.artisanRecipe.getSecondaryOutputChance();
        this.drawSecondaryOutputChanceString(minecraft, chance, xPos, yPos);
      }

      if (!this.artisanRecipe.getTertiaryOutput().isEmpty()) {
        float chance = this.artisanRecipe.getTertiaryOutputChance();
        this.drawSecondaryOutputChanceString(minecraft, chance, (xPos + 36), yPos);
      }

      if (!this.artisanRecipe.getQuaternaryOutput().isEmpty()) {
        float chance = this.artisanRecipe.getQuaternaryOutputChance();
        this.drawSecondaryOutputChanceString(minecraft, chance, (xPos + 72), yPos);
      }

    } else {

      int xPos = 331;
      int yPos = 32;

      if (!this.artisanRecipe.getSecondaryOutput().isEmpty()) {
        float chance = this.artisanRecipe.getSecondaryOutputChance();
        this.drawSecondaryOutputChanceString(minecraft, chance, xPos, yPos);
      }

      if (!this.artisanRecipe.getTertiaryOutput().isEmpty()) {
        float chance = this.artisanRecipe.getTertiaryOutputChance();
        this.drawSecondaryOutputChanceString(minecraft, chance, xPos, (yPos + 36));
      }

      if (!this.artisanRecipe.getQuaternaryOutput().isEmpty()) {
        float chance = this.artisanRecipe.getQuaternaryOutputChance();
        this.drawSecondaryOutputChanceString(minecraft, chance, xPos, (yPos + 72));
      }
    }

    GlStateManager.popMatrix();

    if (!this.artisanRecipe.isShaped()) {

      if (CATEGORY_TIER == EnumTier.WORKSHOP) {
        GuiHelper.drawTexturedRect(minecraft, RECIPE_BACKGROUND, 288, 58, 18, 17, 0, 0, 0, 1, 1);

      } else {
        GuiHelper.drawTexturedRect(minecraft, RECIPE_BACKGROUND, 234, 8, 18, 17, 0, 0, 0, 1, 1);
      }
    }

    GlStateManager.popMatrix();

    // TODO: attempt to move the following tooltip to IRecipeCategory#getTooltipStrings
    GlStateManager.pushMatrix();
    GlStateManager.translate(0, -8, 0);

    if (!this.artisanRecipe.isShaped()) {

      int x = 117;
      int y = 4;

      if (CATEGORY_TIER == EnumTier.WORKSHOP) {
        x = 144;
        y = 29;
      }

      if (mouseX >= x && mouseX <= x + 9 && mouseY >= y && mouseY <= y + 9) {
        List<String> tooltip = new ArrayList<>();
        tooltip.add(I18n.format(ModuleWorktables.Lang.JEI_TOOLTIP_SHAPELESS_RECIPE));
        GuiUtils.drawHoveringText(
            tooltip,
            mouseX,
            mouseY,
            minecraft.displayWidth,
            minecraft.displayHeight,
            200,
            minecraft.fontRenderer
        );
      }
    }
    GlStateManager.popMatrix();
  }

  private void drawSecondaryOutputChanceString(
      Minecraft minecraft,
      float secondaryOutputChance,
      int positionX,
      int positionY
  ) {

    String label = (int) (secondaryOutputChance * 100) + "%";
    minecraft.fontRenderer.drawString(
        label,
        positionX - minecraft.fontRenderer.getStringWidth(label) * 0.5f,
        positionY,
        0xFFFFFFFF,
        true
    );
  }

  private void drawExperienceString(Minecraft minecraft, int recipeHeight, String experienceString) {

    minecraft.fontRenderer.drawString(
        experienceString,
        5,
        recipeHeight - 10,
        0xFF80FF20,
        true
    );
  }

  private void drawToolDamageString(Minecraft minecraft, int offsetX, int offsetY) {

    for (int i = 0; i < this.artisanRecipe.getToolCount(); i++) {
      String label = "-" + this.artisanRecipe.getToolDamage(i);
      minecraft.fontRenderer.drawString(
          label,
          offsetX - minecraft.fontRenderer.getStringWidth(label) * 0.5f,
          offsetY + (22 * i),
          0xFFFFFFFF,
          true
      );
    }
  }
}
