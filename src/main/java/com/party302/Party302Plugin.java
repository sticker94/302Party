package com.party302;

import com.google.inject.Provides;
import javax.inject.Inject;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.Player;
import net.runelite.api.events.GameStateChanged;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;

@Slf4j
@PluginDescriptor(
		name = "302 Party"
)
public class Party302Plugin extends Plugin
{
	@Inject
	private Client client;

	@Inject
	private Party302Config config;

	private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

	@Override
	protected void startUp() throws Exception
	{
		log.info("302 Party started!");
	}

	@Override
	protected void shutDown() throws Exception
	{
		log.info("302 Party stopped!");
		scheduler.shutdownNow();
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged gameStateChanged)
	{
		log.info("Current GameState: " + gameStateChanged.getGameState());
		if (gameStateChanged.getGameState() == GameState.LOGGED_IN)
		{
			// Schedule the task to run after a delay of 30 seconds
			scheduler.schedule(this::processCharacterVerification, 30, TimeUnit.SECONDS);
		}
	}

	private void processCharacterVerification()
	{
		String username = getPlayerName();
		log.info("Retrieved player name: " + (username != null ? username : "null"));
		if (username != null)
		{
			String token = config.token(); // Get the token from the config
			if (!token.isEmpty())
			{
				sendCharacterNameToServer(username, token);
			}
			else
			{
				log.warn("No token provided. Please enter a token in the plugin configuration.");
			}
		}
		else
		{
			log.warn("Failed to retrieve player name.");
		}
	}

	private String getPlayerName() {
		Player localPlayer = client.getLocalPlayer();
		return (localPlayer != null) ? localPlayer.getName() : null;
	}

	private void sendCharacterNameToServer(String characterName, String token)
	{
		try
		{
			URL url = new URL("https://302party.com/verify_character.php");
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod("POST");
			connection.setDoOutput(true);
			connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

			String postData = "character_name=" + characterName + "&verification_key=" + token;

			try (OutputStream os = connection.getOutputStream()) {
				byte[] input = postData.getBytes("utf-8");
				os.write(input, 0, input.length);
			}

			int code = connection.getResponseCode();
			log.info("POST Response Code :: " + code);
			if (code == HttpURLConnection.HTTP_OK) {
				log.info("Verification successful");
			} else {
				log.warn("Verification failed: " + code);
			}
		}
		catch (Exception e)
		{
			log.error("Error sending character name to server", e);
		}
	}

	@Provides
	Party302Config provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(Party302Config.class);
	}
}
