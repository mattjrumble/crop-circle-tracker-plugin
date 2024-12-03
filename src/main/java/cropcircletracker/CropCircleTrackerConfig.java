package cropcircletracker;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;

@ConfigGroup(CropCircleTrackerConfig.GROUP)
public interface CropCircleTrackerConfig extends Config {
    String GROUP = "cropcircletracker";
    String HIDE_TOOLBAR_BUTTON_NAME = "hideToolbarButton";

    @ConfigSection(position = 10, name = "World Types", description = "Settings for displaying different world types.")
    String worldTypeSection = "World Types";

    @ConfigSection(position = 20, name = "Endpoints", description = "Settings for external endpoints.")
    String endpointsSection = "Endpoints";

    @ConfigItem(position = 1, keyName = "defaultLocation", name = "Default", description = "The default crop circle location used when first opening the plugin.")
    default CropCircle defaultLocation()
    {
        return CropCircle.BRIMHAVEN;
    }

    @ConfigItem(position = 2, keyName = "minimumLikelihood", name = "Minimum likelihood", description = "Minimum likelihood percentage to show in the table. Increase this to hide low percentage sightings.")
    default int minimumLikelihood()
    {
        return 0;
    }

    @ConfigItem(position = 3, keyName = HIDE_TOOLBAR_BUTTON_NAME, name = "Hide toolbar button", description = "Hide the toolbar button for this plugin. Use this if you want to contribute to crowdsourcing but don't care about the sightings yourself.")
    default boolean hideToolbarButton()
    {
        return false;
    }

    @ConfigItem(position = 1, keyName = "showPVPWorlds", name = "Show PVP worlds", description = "Show PvP worlds in the table.", section = worldTypeSection)
    default boolean showPVPWorlds()
    {
        return false;
    }

    @ConfigItem(position = 2, keyName = "showHighRiskWorlds", name = "Show high risk worlds", description = "Show high risk worlds in the table.", section = worldTypeSection)
    default boolean showHighRiskWorlds()
    {
        return true;
    }


    @ConfigItem(position = 3, keyName = "show1250TotalWorlds", name = "Show 1250 total worlds", description = "Show 1250 total worlds in the table.", section = worldTypeSection)
    default boolean show1250TotalWorlds()
    {
        return true;
    }

    @ConfigItem(position = 4, keyName = "show1500TotalWorlds", name = "Show 1500 total worlds", description = "Show 1500 total worlds in the table.", section = worldTypeSection)
    default boolean show1500TotalWorlds()
    {
        return true;
    }

    @ConfigItem(position = 5, keyName = "show1750TotalWorlds", name = "Show 1750 total worlds", description = "Show 1750 total worlds in the table.", section = worldTypeSection)
    default boolean show1750TotalWorlds()
    {
        return true;
    }

    @ConfigItem(position = 6, keyName = "show2000TotalWorlds", name = "Show 2000 total worlds", description = "Show 2000 total worlds in the table.", section = worldTypeSection)
    default boolean show2000TotalWorlds()
    {
        return true;
    }

    @ConfigItem(position = 7, keyName = "show2200TotalWorlds", name = "Show 2200 total worlds", description = "Show 2200 total worlds in the table.", section = worldTypeSection)
    default boolean show2200TotalWorlds()
    {
        return true;
    }

    @ConfigItem(position = 8, keyName = "showSeasonalWorlds", name = "Show Seasonal/League worlds", description = "Show Seasonal/League worlds in the table.", section = worldTypeSection)
    default boolean showSeasonalWorlds()
    {
        return true;
    }

    @ConfigItem(position = 1, keyName = "getEndpoint", name = "GET endpoint", description = "HTTP endpoint to get sighting information from.", section = endpointsSection)
    default String getEndpoint()
    {
        return "https://cropcircletracker.com/get/";
    }

    @ConfigItem(position = 2, keyName = "postEndpoint", name = "POST endpoint", description = "HTTP endpoint to post sightings to.", section = endpointsSection)
    default String postEndpoint()
    {
        return "https://cropcircletracker.com/post/";
    }

    @ConfigItem(position = 3, keyName = "sharedKey", name = "Shared key", description = "A shared key used to authenticate with the server.", section = endpointsSection)
    default String sharedKey()
    {
        return "gnomechild123";
    }
}
