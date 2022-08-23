package cropcircletracker;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class CropCirclePluginTest
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(CropCircleTrackerPlugin.class);
		RuneLite.main(args);
	}
}