package neresources.compatibility.thaumcraft;

import cpw.mods.fml.common.Optional;
import neresources.api.distributions.DistributionCustom;
import neresources.api.distributions.DistributionSquare;
import neresources.api.messages.ModifyOreMessage;
import neresources.api.messages.RegisterOreMessage;
import neresources.api.utils.*;
import neresources.api.utils.conditionals.Conditional;
import neresources.compatibility.CompatBase;
import neresources.entries.MobEntry;
import neresources.registry.MessageRegistry;
import neresources.utils.ModList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import thaumcraft.common.config.Config;
import thaumcraft.common.config.ConfigBlocks;
import thaumcraft.common.config.ConfigItems;
import thaumcraft.common.entities.monster.*;

public class ThaumcraftCompat extends CompatBase
{

    @Override
    protected void init()
    {
        registerThaumcraftOres();
        registerThaumcraftMobs();
    }

    @Optional.Method(modid = ModList.Names.THAUMCRAFT)
    private void registerThaumcraftOres()
    {
        ItemStack amber = new ItemStack(ConfigBlocks.blockCustomOre, 1, 7);
        ItemStack amberDrop = new ItemStack(ConfigItems.itemResource, 1, 6);
        MessageRegistry.addMessage(new ModifyOreMessage(amber, Priority.FIRST, amberDrop));
        for (int i = 0; i < 6; i++)
        {
            ItemStack infusedStone = new ItemStack(ConfigBlocks.blockCustomOre, 1, i + 1);
            ItemStack infusedShard = new ItemStack(ConfigItems.itemShard, 2, i);
            MessageRegistry.addMessage(new ModifyOreMessage(infusedStone, Priority.FIRST, infusedShard));
        }
        if (Config.genCinnibar) genCinnibar();
        if (Config.genAmber) genAmber();
        if (Config.genInfusedStone) genInfused();
    }

    @Optional.Method(modid = ModList.Names.THAUMCRAFT)
    private void registerThaumcraftMobs()
    {
        Conditional randomAspect = new Conditional("ner.randomAspect.text", Modifier.pink);
        String[] tainted = new String[]{"Tainted areas"};
        DropItem flesh = new DropItem(Items.rotten_flesh, 0, 2);
        DropItem brain = new DropItem(ConfigItems.itemZombieBrain, 0, 1);
        if (Config.spawnAngryZombie)
            registerMob(new MobEntry(new EntityBrainyZombie(world), LightLevel.hostile, flesh, brain));

        DropItem essence = new DropItem(new ItemStack(ConfigItems.itemWispEssence), 1, 1, randomAspect);
        if (Config.spawnWisp) registerMob(new MobEntry(new EntityWisp(world), LightLevel.hostile, essence));

        DropItem knowledge = new DropItem(new ItemStack(ConfigItems.itemResource, 1, 9), 1, 1, 0.025F, Conditional.playerKill, Conditional.rareDrop);
        DropItem bean = new DropItem(new ItemStack(ConfigItems.itemManaBean), 0, 1, randomAspect);
        if (Config.spawnPech) registerMob(new MobEntry(new EntityPech(world), LightLevel.any, bean, knowledge));

        DropItem taintSlime = new DropItem(new ItemStack(ConfigItems.itemResource, 1, 11), 0, 1);
        DropItem taintTendril = new DropItem(new ItemStack(ConfigItems.itemResource, 1, 12), 0, 1);
        if (Config.spawnTaintacle)
        {
            registerMob(new MobEntry(new EntityTaintacle(world), LightLevel.any, tainted, taintSlime, taintTendril));
            registerMob(new MobEntry(new EntityTaintacleSmall(world), LightLevel.any, tainted));
        }
        if (Config.spawnTaintSpore)
        {
            registerMob(new MobEntry(new EntityTaintSporeSwarmer(world), LightLevel.any, tainted, taintSlime, taintTendril));
        }
        taintSlime = new DropItem(new ItemStack(ConfigItems.itemResource, 1, 11), 0, 1, 0.166F);
        taintTendril = new DropItem(new ItemStack(ConfigItems.itemResource, 1, 12), 0, 1, 0.166F);
        EntityLivingBase[] taintedEntities = new EntityLivingBase[]{new EntityTaintChicken(world), new EntityTaintCow(world), new EntityTaintCreeper(world), new EntityTaintPig(world), new EntityTaintSheep(world),
                new EntityTaintSheep(world), new EntityTaintSpider(world), new EntityTaintVillager(world)};
        for (EntityLivingBase entity : taintedEntities)
            registerMob(new MobEntry(entity, LightLevel.any, tainted, taintSlime, taintTendril));

        DropItem string = new DropItem(Items.string, 0, 2);
        DropItem spider = new DropItem(Items.spider_eye, 1, 1, 0.33F, Conditional.playerKill);
        registerMob(new MobEntry(new EntityMindSpider(world), LightLevel.hostile, string, spider));

        knowledge = new DropItem(new ItemStack(ConfigItems.itemResource, 1, 9), 0, 1, 0.1F);
        DropItem voidSeed = new DropItem(new ItemStack(ConfigItems.itemResource, 1, 17), 0, 1, 0.2F);
        DropItem crimsonRites = new DropItem(new ItemStack(ConfigItems.itemEldritchObject, 1, 1), 1, 1, 0.025F, Conditional.playerKill, Conditional.rareDrop);
        DropItem cultHelmet = new DropItem(new ItemStack(ConfigItems.itemHelmetCultistPlate), 0, 1, 0.085F, Conditional.equipmentDrop);
        DropItem cultChest = new DropItem(new ItemStack(ConfigItems.itemChestCultistPlate), 0, 1, 0.085F, Conditional.equipmentDrop);
        DropItem cultLegs = new DropItem(new ItemStack(ConfigItems.itemLegsCultistPlate), 0, 1, 0.085F, Conditional.equipmentDrop);
        DropItem cultBoots = new DropItem(new ItemStack(ConfigItems.itemBootsCultist), 0, 1, 0.085F, Conditional.equipmentDrop);
        DropItem thaumSword = new DropItem(new ItemStack(ConfigItems.itemSwordThaumium), 0, 1, 0.085F, Conditional.equipmentDrop);
        DropItem cultRobeHelmet = new DropItem(new ItemStack(ConfigItems.itemHelmetCultistRobe), 0, 1, 0.085F, Conditional.equipmentDrop);
        DropItem voidSword = new DropItem(new ItemStack(ConfigItems.itemSwordVoid), 0, 1, 0.085F, Conditional.equipmentDrop);
        DropItem cultRobeChest = new DropItem(new ItemStack(ConfigItems.itemChestCultistRobe), 0, 1, 0.085F, Conditional.equipmentDrop);
        DropItem cultRobeLegs = new DropItem(new ItemStack(ConfigItems.itemLegsCultistRobe), 0, 1, 0.085F, Conditional.equipmentDrop);
        registerMob(new MobEntry(new EntityCultistKnight(world), LightLevel.hostile, voidSeed, knowledge, crimsonRites, cultHelmet, cultChest, cultLegs, cultBoots, thaumSword, cultRobeHelmet, voidSword));
        registerMob(new MobEntry(new EntityCultistCleric(world), LightLevel.hostile, voidSeed, knowledge, crimsonRites, cultRobeHelmet, cultRobeChest, cultRobeLegs, cultBoots));
    }

    @Optional.Method(modid = ModList.Names.THAUMCRAFT)
    private void genInfused()
    {
        int minY = 5;
        int maxY = 59;
        int veinSize = 6;
        int numVeins = 8;
        for (int i = 0; i < 6; i++)
        {
            ItemStack infusedStone = new ItemStack(ConfigBlocks.blockCustomOre, 1, i + 1);
            registerOre(new RegisterOreMessage(infusedStone, new DistributionSquare(numVeins, veinSize, minY, maxY)));
        }
    }

    @Optional.Method(modid = ModList.Names.THAUMCRAFT)
    private void genAmber()
    {
        int minY = 0;
        int maxY = 64;
        float maxYRange = 25;
        float numVeins = 20F;
        float chance = numVeins / ((maxY - maxYRange / 2 - minY + 1) * 256);
        float[] distribution = DistributionHelpers.getSquareDistribution(minY, maxY - (int) maxYRange, chance);
        DistributionHelpers.addDistribution(distribution, DistributionHelpers.getRampDistribution(maxY, (int) (maxY - maxYRange), chance), maxY - (int) maxYRange);
        ItemStack amber = new ItemStack(ConfigBlocks.blockCustomOre, 1, 7);
        registerOre(new RegisterOreMessage(amber, new DistributionCustom(distribution)));
    }

    @Optional.Method(modid = ModList.Names.THAUMCRAFT)
    private void genCinnibar()
    {
        int minY = 0;
        int maxY = 64 / 5;
        float numVeins = 18F;
        float chance = numVeins / (maxY * 256);
        ItemStack ore = new ItemStack(ConfigBlocks.blockCustomOre, 1, 0);
        registerOre(new RegisterOreMessage(ore, new DistributionSquare(minY, maxY, chance)));
    }
}
