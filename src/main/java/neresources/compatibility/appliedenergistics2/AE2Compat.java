package neresources.compatibility.appliedenergistics2;

import appeng.api.AEApi;
import appeng.core.AEConfig;
import appeng.core.features.AEFeature;
import neresources.api.distributions.DistributionSquare;
import neresources.api.messages.ModifyOreMessage;
import neresources.api.messages.RegisterOreMessage;
import neresources.api.utils.Priority;
import neresources.compatibility.CompatBase;
import neresources.registry.MessageRegistry;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;

public class AE2Compat extends CompatBase
{

    @Override
    protected void init()
    {
        ItemStack quartzOre = AEApi.instance().blocks().blockQuartzOre.stack(1);
        ItemStack chargedQuartz = AEApi.instance().blocks().blockQuartzOreCharged.stack(1);
        ItemStack itemQuartz = AEApi.instance().materials().materialCertusQuartzCrystal.stack(1);
        ItemStack itemCharged = AEApi.instance().materials().materialCertusQuartzCrystalCharged.stack(1);

        OreDictionary.registerOre("oreChargedCertusQuartz", chargedQuartz);
        OreDictionary.registerOre("crystalChargedCertusQuartz", itemCharged);

        MessageRegistry.addMessage(new ModifyOreMessage(quartzOre, Priority.FIRST, itemQuartz));
        MessageRegistry.addMessage(new ModifyOreMessage(chargedQuartz, Priority.FIRST, itemCharged));

        boolean spawn = AEConfig.instance.featureFlags.contains(AEFeature.CertusQuartzWorldGen);
        if (!spawn) return;
        int numVeins = AEConfig.instance.quartzOresClusterAmount;
        int veinSize = AEConfig.instance.quartzOresPerCluster;
        float spawnChargedChance = AEConfig.instance.spawnChargedChance;
        int minY = 52;
        int maxY = 74;
        float chance = (float) (numVeins * veinSize) / ((maxY - minY + 1) * 256) * 1.5F;

        registerOre(new RegisterOreMessage(quartzOre, new DistributionSquare(Math.max(0, minY - veinSize / 2), minY, maxY, Math.min(maxY + veinSize / 2, 255), chance * spawnChargedChance)));
        registerOre(new RegisterOreMessage(chargedQuartz, new DistributionSquare(Math.max(0, minY - veinSize / 2), minY, maxY, Math.min(maxY + veinSize / 2, 255), chance * (1F - spawnChargedChance))));
    }
}
