package cropcircletracker;

import com.google.gson.Gson;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import javax.inject.Inject;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.GameObject;
import net.runelite.api.Tile;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.GameObjectSpawned;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.task.Schedule;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.util.ImageUtil;
import okhttp3.*;

import static net.runelite.api.ObjectID.CENTRE_OF_CROP_CIRCLE;

@Slf4j
@PluginDescriptor(name = "Crop Circle Tracker")
public class CropCircleTrackerPlugin extends Plugin
{
	private static final int CROP_CIRCLE_OBJECT = CENTRE_OF_CROP_CIRCLE;
	private static final int GET_LIKELIHOODS_POLLING_PERIOD_SECONDS = 10;
	private static final int CROP_CIRCLE_POLLING_PERIOD_SECONDS = 30;
	private static final String GET_URL = "http://127.0.0.1:8000/";
	private static final String POST_URL = "http://127.0.0.1:8000/";
	private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
	private static final Map<WorldPoint, CropCircle> MAPPING = CropCircle.mapping();

	@Inject
	private Client client;

	@Inject
	private ClientToolbar clientToolbar;

	@Inject
	private OkHttpClient okHttpClient;

	@Inject
	private Gson gson;

	private CropCircleTrackerPanel panel;

	private CropCircle lastCropCircle = null;

	private JsonObject likelihoods = null;

	@Override
	protected void startUp()
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
	}

	/* Send an HTTP GET request for crop circle likelihoods across worlds. */
	@Schedule(period = GET_LIKELIHOODS_POLLING_PERIOD_SECONDS, unit = ChronoUnit.SECONDS, asynchronous = true)
	public void getLikelihoods()
	{
		log.debug("Getting likelihoods");
		Request request = new Request.Builder().url(GET_URL).get().build();
		okHttpClient.newCall(request).enqueue(new Callback()
		{
			@Override
			public void onFailure(Call call, IOException e)
			{
				likelihoods = null;
				log.error("GET failed: {}", e.getMessage());
			}
			@Override
			public void onResponse(Call call, Response response)
			{
				if (response.isSuccessful())
				{
					try
					{
						likelihoods = gson.fromJson(response.body().string(), JsonObject.class);
					}
					catch (IOException | JsonSyntaxException e)
					{
						likelihoods = null;
						log.error("GET failed: {}", e.getMessage());
					}
				}
				else
				{
					likelihoods = null;
					log.error("GET unsuccessful");
				}
				response.close();
			}
		});
	}

	/* Send an HTTP POST request for a crop circle sighting. */
	private void postSighting(CropCircle cropCircle, int world)
	{
		if (client.getWorld() != world)
		{
			// If the current world is not the given world, don't post anything. This protects against edge-cases
			// where a crop circle is detected but then the player immediately hops world, which could cause a sighting
			// to be posted using the location from the old world along with the newly-hopped-to world.
			return;
		}
		Map<String, Object> data = new HashMap<>();
		data.put("world", world);
		data.put("location", cropCircle.getExternalId());
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
	@Schedule(period = CROP_CIRCLE_POLLING_PERIOD_SECONDS, unit = ChronoUnit.SECONDS, asynchronous = true)
	public void poll()
	{
		if (lastCropCircle != null)
		{
			int world = client.getWorld();
			if (cropCircleVisible(lastCropCircle.getWorldPoint()))
			{
				postSighting(lastCropCircle, world);
			}
			else
			{
				log.debug("Sighting removed: {}", lastCropCircle.getName());
				lastCropCircle = null;
			}
		}
	}

	@Subscribe
	public void onGameObjectSpawned(GameObjectSpawned event)
	{
		if (event.getGameObject().getId() == CROP_CIRCLE_OBJECT)
		{
			int world = client.getWorld();
			CropCircle cropCircle = MAPPING.get(event.getTile().getWorldLocation());
			if (cropCircle != null)
			{
				postSighting(cropCircle, world);
				lastCropCircle = cropCircle;
			}
		}
	}

	@Schedule(period = 5, unit = ChronoUnit.SECONDS, asynchronous = false)
	public void printLikelihoodsMessage()
	{
		if (likelihoods == null)
		{
			client.addChatMessage(ChatMessageType.CONSOLE, "", "No likelihoods yet", "");
		}
		else
		{
			// Convert all likelihoods to integer percentages so that they fit better in a chat message.
			JsonObject likelihoodsCopy = likelihoods.deepCopy();
			Iterator<String> keys = likelihoodsCopy.keySet().iterator();
			while (keys.hasNext())
			{
				String key = keys.next();
				JsonObject innerLikelihoods = likelihoodsCopy.get(key).getAsJsonObject();
				Iterator<String> innerKeys = innerLikelihoods.keySet().iterator();
				while (innerKeys.hasNext())
				{
					String innerKey = innerKeys.next();
					float innerValue = innerLikelihoods.get(innerKey).getAsFloat();
					innerLikelihoods.addProperty(innerKey, Math.round(innerValue * 100) + "%");
				}
			}
			client.addChatMessage(ChatMessageType.CONSOLE, "", "Likelihoods: " + likelihoodsCopy.toString(), "");
		}
	}
}
