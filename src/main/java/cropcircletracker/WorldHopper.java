package cropcircletracker;

import net.runelite.api.GameState;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.util.WorldUtil;
import net.runelite.http.api.worlds.World;

/* Class for handling all the world hopping logic. Most of the logic was copied from the world hopper plugin. */
public class WorldHopper
{
    private CropCircleTrackerPlugin plugin;
    private net.runelite.api.World targetWorld;
    private static final int DISPLAY_SWITCHER_MAX_ATTEMPTS = 3;
    private int displaySwitcherAttempts = 0;

    WorldHopper(CropCircleTrackerPlugin plugin)
    {
        this.plugin = plugin;
    }

    /*
    Schedule a world hop. This won't immediately world hop, it'll set the target world so that handleHop can perform the
    hop in the next game ticks.
    */
    public void scheduleHop(int worldID)
    {
        plugin.clientThread.invoke(() -> {
            World world = plugin.worldMapping.get(worldID);
            if (world == null)
            {
                return;
            }
            final net.runelite.api.World rsWorld = plugin.client.createWorld();
            rsWorld.setActivity(world.getActivity());
            rsWorld.setAddress(world.getAddress());
            rsWorld.setId(world.getId());
            rsWorld.setPlayerCount(world.getPlayers());
            rsWorld.setLocation(world.getLocation());
            rsWorld.setTypes(WorldUtil.toWorldTypes(world.getTypes()));
            if (plugin.client.getGameState() == GameState.LOGIN_SCREEN)
            {
                plugin.client.changeWorld(rsWorld);
                return;
            }
            targetWorld = rsWorld;
        });
    }

    /*
    Actually perform the world hop if a target world has been set. This method should be called on each game tick. It
    will take multiple ticks to perform the hop (at least one tick to open the world hopper, and another to make the
    hop).
    */
    public void handleHop()
    {
        if (targetWorld == null)
        {
            return;
        }
        if (plugin.client.getWidget(WidgetInfo.WORLD_SWITCHER_LIST) == null)
        {
            plugin.client.openWorldHopper();

            if (++displaySwitcherAttempts >= DISPLAY_SWITCHER_MAX_ATTEMPTS)
            {
                reset();
            }
        }
        else
        {
            plugin.client.hopToWorld(targetWorld);
            reset();
        }
    }

    private void reset()
    {
        targetWorld = null;
        displaySwitcherAttempts = 0;
    }
}
