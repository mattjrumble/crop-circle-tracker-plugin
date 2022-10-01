package cropcircletracker;

import com.google.gson.Gson;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import javax.inject.Inject;
import javax.swing.*;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.GameObject;
import net.runelite.api.GameState;
import net.runelite.api.Tile;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.GameObjectSpawned;
import net.runelite.api.events.GameStateChanged;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.game.WorldService;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.task.Schedule;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.util.ImageUtil;
import net.runelite.http.api.worlds.World;
import net.runelite.http.api.worlds.WorldResult;
import okhttp3.*;

import static net.runelite.api.ObjectID.CENTRE_OF_CROP_CIRCLE;

@Slf4j
@PluginDescriptor(name = "Crop Circle Tracker")
public class CropCircleTrackerPlugin extends Plugin
{
	private static final int CROP_CIRCLE_OBJECT = CENTRE_OF_CROP_CIRCLE;
	private static final int GET_LIKELIHOODS_PERIOD_SECONDS = 5;
	private static final int CROP_CIRCLE_RECHECK_PERIOD_SECONDS = 10;
	private static final int PANEL_REFRESH_PERIOD_SECONDS = 3;
	private static final String GET_URL = "http://127.0.0.1:8000/";
	private static final String POST_URL = "http://127.0.0.1:8000/";
	private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
	private static final Map<WorldPoint, CropCircle> MAPPING = CropCircle.mapping();

	@Inject
	private Client client;

	@Inject
	private ClientToolbar clientToolbar;

	@Inject
	private WorldService worldService;

	@Inject
	private OkHttpClient okHttpClient;

	@Inject
	private Gson gson;

	public Map<Integer, World> worldMapping = new HashMap<>();

	private CropCircleTrackerPanel panel;

	private CropCircle lastCropCircle = null;

	private int currentWorld = -1;

	@Override
	public void startUp()
	{
		panel = injector.getInstance(CropCircleTrackerPanel.class);
		final BufferedImage icon = ImageUtil.loadImageResource(getClass(), "/icon.png");
		NavigationButton navButton = NavigationButton.builder()
			.tooltip("Crop Circle Tracker")
			.icon(icon)
			.priority(7)
			.panel(panel)
			.build();
		clientToolbar.addNavigation(navButton);
		setWorldMapping();
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged event)
	{
		if (event.getGameState() == GameState.LOGGED_IN)
		{
			lastCropCircle = null;
			currentWorld = client.getWorld();
		}
	}

	/* Send an HTTP GET request for crop circle likelihoods and update the table. */
	@Schedule(period = GET_LIKELIHOODS_PERIOD_SECONDS, unit = ChronoUnit.SECONDS, asynchronous = true)
	public void getLikelihoods()
	{
		// Don't make constant GET requests unless the panel is open.
		if (panel.open)
		{
			log.debug("Getting likelihoods");
			Request request = new Request.Builder().url(GET_URL).get().build();
			okHttpClient.newCall(request).enqueue(new Callback()
			{
				@Override
				public void onFailure(Call call, IOException e)
				{
					log.error("GET failed: {}", e.getMessage());
				}
				@Override
				public void onResponse(Call call, Response response)
				{
					if (response.isSuccessful())
					{
						try
						{
							JsonObject likelihoods = gson.fromJson(response.body().string(), JsonObject.class);
							SwingUtilities.invokeLater(() -> {
								panel.likelihoods = likelihoods;
								panel.updateTable();
							});
						}
						catch (IOException | JsonSyntaxException e)
						{
							log.error("GET failed: {}", e.getMessage());
						}
					}
					else
					{
						log.error("GET unsuccessful");
					}
					response.close();
				}
			});
		}
	}

	/* Store a mapping of world ID to world. */
	private void setWorldMapping()
	{
		WorldResult worldResult = worldService.getWorlds();
		if (worldResult != null)
		{
			for (World world :  worldResult.getWorlds())
			{
				worldMapping.put(world.getId(), world);
			}
		}
	}

	/* Send an HTTP POST request for a crop circle sighting. */
	private void postSighting(CropCircle cropCircle)
	{
		int world = currentWorld;

		// Check we actually have a current world recorded.
		if (world == -1)
		{
			return;
		}

		// Check that the crop circle is still visible.
		if (!cropCircleVisible(cropCircle.getWorldPoint()))
		{
			lastCropCircle = null;
			return;
		}

		// Check that the world has not changed since determining the crop circle is still visible.
		if (world != currentWorld)
		{
			return;
		}

		Map<String, Object> data = new HashMap<>();
		data.put("world", world);
		data.put("location", cropCircle.getIndex());
		String json = gson.toJson(data);
		log.debug("Posting sighting: {}", json);
		Request request = new Request.Builder().url(POST_URL).post(RequestBody.create(JSON, json)).build();
		okHttpClient.newCall(request).enqueue(new Callback()
		{
			@Override
			public void onFailure(Call call, IOException e)
			{
				log.error("POST failed: {}", e.getMessage());
			}
			@Override
			public void onResponse(Call call, Response response)
			{
				if (!response.isSuccessful())
				{
					log.error("POST unsuccessful");
				}
				response.close();
			}
		});
		lastCropCircle = cropCircle;
	}

	/* Determine if a crop circle is visible on the given WorldPoint. */
	private boolean cropCircleVisible(WorldPoint worldPoint)
	{
		LocalPoint localPoint = LocalPoint.fromWorld(client, worldPoint);
		if (localPoint != null) {
			Tile[][][] tiles = client.getScene().getTiles();
			Tile tile = tiles[worldPoint.getPlane()][localPoint.getSceneX()][localPoint.getSceneY()];
			for (GameObject object : tile.getGameObjects())
			{
				if (object != null && object.getId() == CROP_CIRCLE_OBJECT)
				{
					return true;
				}
			}
		}
		return false;
	}

	/* Periodically check if the last seen crop circle is still visible. */
	@Schedule(period = CROP_CIRCLE_RECHECK_PERIOD_SECONDS, unit = ChronoUnit.SECONDS, asynchronous = true)
	public void poll()
	{
		if (lastCropCircle != null)
		{
			postSighting(lastCropCircle);
		}
	}

	@Subscribe
	public void onGameObjectSpawned(GameObjectSpawned event)
	{
		if (event.getGameObject().getId() == CROP_CIRCLE_OBJECT)
		{
			CropCircle cropCircle = MAPPING.get(event.getTile().getWorldLocation());
			if (cropCircle != null)
			{
				postSighting(cropCircle);
			}
		}
	}
}
