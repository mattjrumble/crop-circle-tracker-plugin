package cropcircletracker;

import com.google.gson.Gson;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import javax.inject.Inject;
import javax.swing.*;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.google.inject.Provides;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.GameObjectSpawned;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.WorldChanged;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
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
	private static final int GET_LIKELIHOODS_PERIOD_SECONDS = 10;
	private static final int CROP_CIRCLE_RECHECK_PERIOD_SECONDS = 10;
	private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
	private static final Map<WorldPoint, CropCircle> MAPPING = CropCircle.mapping();

	@Inject
	@Getter
	public CropCircleTrackerConfig config;

	@Inject
	private ConfigManager configManager;

	@Inject
	public Client client;

	@Inject
	public ClientThread clientThread;

	@Inject
	private ClientToolbar clientToolbar;

	@Inject
	private WorldService worldService;

	@Inject
	private OkHttpClient okHttpClient;

	@Inject
	private Gson gson;

	public Map<Integer, World> worldMapping = new HashMap<>();

	public WorldHopper worldHopper = new WorldHopper(this);

	private NavigationButton navButton = null;

	private CropCircleTrackerPanel panel;

	private CropCircle lastCropCircle = null;

	private int currentWorld = -1;

	@Provides
	CropCircleTrackerConfig getConfig(ConfigManager configManager)
	{
		return configManager.getConfig(CropCircleTrackerConfig.class);
	}

	@Override
	protected void startUp() throws Exception
	{
		panel = injector.getInstance(CropCircleTrackerPanel.class);
		final BufferedImage icon = ImageUtil.loadImageResource(getClass(), "/icon.png");
		navButton = NavigationButton.builder()
			.tooltip("Crop Circle Tracker")
			.icon(icon)
			.priority(7)
			.panel(panel)
			.build();
		clientToolbar.addNavigation(navButton);
		setWorldMapping();
	}
	
	@Override
	protected void shutDown() throws Exception
	{
		clientToolbar.removeNavigation(navButton);
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

	@Subscribe
	public void onGameTick(GameTick gameTick)
	{
		worldHopper.handleHop();
	}

	/* Send an HTTP GET request for crop circle likelihoods and update the table. */
	@Schedule(period = GET_LIKELIHOODS_PERIOD_SECONDS, unit = ChronoUnit.SECONDS, asynchronous = true)
	public void getLikelihoods()
	{
		// Don't make constant GET requests unless the panel is open.
		if (panel.open)
		{
			makeRequest(
					"GET",
					config.getEndpoint(),
					null,
					(call, response) -> {
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
					},
					(call, response) -> {
						String errorMessage;
						if (response.code() == 401)
						{
							errorMessage = "Server authentication error";
						}
						else if (response.code() == 503)
						{
							errorMessage = "Server temporarily unavailable";
						}
						else
						{
							errorMessage = "Server error";
						}
						SwingUtilities.invokeLater(() -> panel.displayError(errorMessage));
					},
					(call, e) -> SwingUtilities.invokeLater(() -> panel.displayError("Server unavailable"))
			);
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

		// Check we're actually logged in.
		if (client.getGameState() != GameState.LOGGED_IN)
		{
			lastCropCircle = null;
			return;
		}

		// Check we actually have a current world recorded.
		if (world == -1)
		{
			lastCropCircle = null;
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
			lastCropCircle = null;
			return;
		}

		Map<String, Object> data = new HashMap<>();
		data.put("world", world);
		data.put("location", cropCircle.getIndex());
		makeRequest(
				"POST",
				config.postEndpoint(),
				RequestBody.create(JSON, gson.toJson(data)),
				(call, response) -> {},
				(call, response) -> {},
				(call, e) -> {}
		);
		lastCropCircle = cropCircle;
	}

	private void makeRequest(
			String method, String url, RequestBody body, BiConsumer<Call, Response> onSuccessfulResponse,
			BiConsumer<Call, Response> onUnsuccessfulResponse, BiConsumer<Call, Exception> onFailure
	)
	{
		log.debug("Making {} request to {} with body {}", method, url, body);
		Request request = null;
		try
		{
			request = new Request.Builder()
					.url(url)
					.addHeader("Authorization", "Bearer " + config.sharedKey())
					.method(method, body)
					.build();
		}
		catch (IllegalArgumentException e)
		{
			log.error("Invalid URL: {}", e.getMessage());
			SwingUtilities.invokeLater(() -> panel.displayError("Invalid " + method + " endpoint"));
		}
		if (request != null)
		{
			okHttpClient.newCall(request).enqueue(new Callback()
				{
					@Override
					public void onResponse(Call call, Response response)
					{
						if (response.isSuccessful())
						{
							onSuccessfulResponse.accept(call, response);
						}
						else
						{
							log.error("{} unsuccessful: {}", method, response.message());
							onUnsuccessfulResponse.accept(call, response);
						}
						response.close();
					}
					@Override
					public void onFailure(Call call, IOException e)
					{
						log.error("{} failed: {}", method, e.getMessage());
						onFailure.accept(call, e);
					}
				}
			);
		}
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

	@Subscribe
	public void onWorldChanged(WorldChanged event)
	{
		SwingUtilities.invokeLater(() -> panel.updateTable());
	}
}
