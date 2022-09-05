package cropcircletracker;

import com.google.gson.Gson;

import java.io.IOException;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import javax.inject.Inject;

import lombok.extern.slf4j.Slf4j;
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
import okhttp3.*;

import static net.runelite.api.ObjectID.CENTRE_OF_CROP_CIRCLE;

@Slf4j
@PluginDescriptor(name = "Crop Circle Tracker")
public class CropCircleTrackerPlugin extends Plugin
{
	private static final int CROP_CIRCLE_OBJECT = CENTRE_OF_CROP_CIRCLE;
	private static final int POLLING_PERIOD_SECONDS = 30;
	private static final String POST_URL = "http://127.0.0.1:8000/";
	private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
	private static final Map<WorldPoint, CropCircle> MAPPING = CropCircle.mapping();

	@Inject
	private Client client;

	@Inject
	private OkHttpClient okHttpClient;

	@Inject
	private Gson gson;

	private CropCircle lastCropCircle = null;

	/* Send an HTTP POST request for a crop circle sighting. */
	private void postSighting(CropCircle cropCircle, int world)
	{
		Map<String, Object> data = new HashMap<>();
		data.put("world", world);
		data.put("location", cropCircle.getExternalId());
		data.put("datetime", "...");
		String json = gson.toJson(data);
		log.debug("Posting sighting: {}", json);
		Request request = new Request.Builder().url(POST_URL).post(RequestBody.create(JSON, json)).build();
		okHttpClient.newCall(request).enqueue(new Callback()
		{
			@Override
			public void onFailure(Call call, IOException e)
			{
				log.debug("Post failed: {}", e.getMessage());
			}
			@Override
			public void onResponse(Call call, Response response)
			{
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
	@Schedule(period = POLLING_PERIOD_SECONDS, unit = ChronoUnit.SECONDS, asynchronous = true)
	public void poll()
	{
		if (lastCropCircle != null)
		{
			if (cropCircleVisible(lastCropCircle.getWorldPoint()))
			{
				postSighting(lastCropCircle, client.getWorld());
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
			CropCircle cropCircle = MAPPING.get(event.getTile().getWorldLocation());
			if (cropCircle != null)
			{
				postSighting(cropCircle, client.getWorld());
				lastCropCircle = cropCircle;
			}
		}
	}
}
