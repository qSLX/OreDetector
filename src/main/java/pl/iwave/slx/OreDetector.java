package pl.iwave.slx;

import com.google.inject.Inject;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.ConfigurationOptions;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.config.DefaultConfig;
import org.spongepowered.api.effect.sound.SoundTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GamePreInitializationEvent;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
import org.spongepowered.api.event.game.state.GameStartingServerEvent;
import org.spongepowered.api.event.game.state.GameStoppingServerEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static java.lang.Math.abs;

@Plugin(id = "oredetector", name = "OreDetector", version = "1.0")
public class OreDetector
	{
	static public ArrayList<Player> fPlayers = new ArrayList<Player>();

	@Inject
	private Logger fLogger;

	@Inject
	@DefaultConfig(sharedRoot = true)
	private Path fConfigFile;

	private ConfigurationLoader<CommentedConfigurationNode> fConfigLoader;
	private ConfigurationNode fConfig;

	int MinHorizontalRange;
	int MaxHorizontalRange;
	int MinVerticalRange;
	int MaxVerticalRange;
	List<BlockType> DetectedBlocks;
	double Volume;
	double Pitch;
	double FrequencyVerticalMultiplier;

	@Listener
	public void OnGamePreinitializationEvent(GamePreInitializationEvent aEvent)
		{
		fConfigLoader = HoconConfigurationLoader.builder().setPath(fConfigFile).build();
		try
			{
			final ConfigurationOptions ConfigOptions = ConfigurationOptions.defaults().setShouldCopyDefaults(true);
			fConfig = fConfigLoader.load(ConfigOptions);
			}
		catch (IOException e)
			{
			fLogger.error("Cannot load configuration from '" + fConfigFile + "'. " + e.getMessage());
			}
		}

	@Listener
	public void OnGameStartingEvent(GameStartingServerEvent aEvent)
		{
		HashMap<String, Boolean> lMap = new HashMap<String, Boolean>();
		lMap.put("off", false);
		lMap.put("on", true);

		CommandSpec lCommandSpec = CommandSpec.builder()
			.description(Text.of("<on|off> - Enable or disable ore detector"))
			.arguments(
				GenericArguments.onlyOne(GenericArguments.choices(Text.of("Detector"), lMap)))
			.executor(new OreDetectorCommand())
			.build();

		Sponge.getCommandManager().register(this, lCommandSpec, "oredetector", "detector");
		}

	@Listener
	public void onServerStart(GameStartedServerEvent aEvent)
		{
		// Read config
		MinHorizontalRange = GetConfigInt("MinHorizontalRange", 2, 1, 10);
		MaxHorizontalRange = GetConfigInt("MaxHorizontalRange", 3, 1, 10);
		MinVerticalRange = GetConfigInt("MinVerticalRange", 2, 1, 10);
		MaxVerticalRange = GetConfigInt("MaxVerticalRange", 2, 1, 10);
		String DetectedBlocksStr = fConfig.getNode("DetectedBlocks").getString("diamond_ore,emerald_ore,gold_ore,redstone_ore,lit_redstone_ore");
		DetectedBlocks = ParseBlockTypes(DetectedBlocksStr);

		if (MinHorizontalRange > MaxHorizontalRange)
			{
			fLogger.error("Config 'MinHorizontalRange' is higher than 'MaxHorizontalRange'. Swapping values.");
			int Temp = MinHorizontalRange;
			MinHorizontalRange = MaxHorizontalRange;
			MaxHorizontalRange = Temp;
			}

		if (MinVerticalRange > MaxVerticalRange)
			{
			fLogger.error("Config 'MinVerticalRange' is higher than 'MaxVerticalRange'. Swapping values.");
			int Temp = MinVerticalRange;
			MinVerticalRange = MaxVerticalRange;
			MaxVerticalRange = Temp;
			}

		Volume = (double) (GetConfigInt("Volume", 20, 0, 100)) / 100;
		Pitch = GetConfigDouble("Frequency", 440, 185, 740) / 370;
		FrequencyVerticalMultiplier = GetConfigDouble("FrequencyVerticalMultiplier", 1.1, 1, 4);


		// Scheduler
		Sponge.getScheduler().createTaskBuilder().interval(2, TimeUnit.SECONDS).execute(new Runnable()
			{
			@Override
			public void run()
				{
				for (Iterator<Player> it = fPlayers.iterator(); it.hasNext(); )
					{
					Player lPlayer = it.next();
					Location<World> lLocation = lPlayer.getLocation();
Searching:
					for (int x = -MaxHorizontalRange; x <= MaxHorizontalRange; ++x)
						for (int y = -MaxVerticalRange; y <= MaxVerticalRange + 1; ++y)
							for (int z = -MaxHorizontalRange; z <= MaxHorizontalRange; ++z)
								{
								if (abs(x) < MinHorizontalRange && (abs(y) < MinVerticalRange || y == MinVerticalRange) && abs(z) < MinHorizontalRange)
									continue;

								Location<World> lTempLocation = lLocation.add(x, y, z);
								BlockType lType = lTempLocation.getBlockType();
								if (DetectedBlocks.contains(lType))
									{
									double lPitch = Pitch;
									if (y < 0)
										lPitch /= FrequencyVerticalMultiplier;
									else if (y > 1)
										lPitch *= FrequencyVerticalMultiplier;

									if (lPitch > 2)
										lPitch = 2;

									lPlayer.playSound(SoundTypes.BLOCK_NOTE_PLING, lTempLocation.getPosition(), Volume, lPitch);
									break Searching;
									}
								}
					}
				}
			}).submit(this);
		}

	private List<BlockType> ParseBlockTypes(String aString)
		{
		ArrayList<BlockType> Result = new ArrayList<BlockType>();
		String[] SplittedString = aString.split(",");
		for (String S : SplittedString)
			{
			S = S.trim();
			if (S.equalsIgnoreCase("coal_ore"))
				Result.add(BlockTypes.COAL_ORE);
			else if (S.equalsIgnoreCase("diamond_ore"))
				Result.add(BlockTypes.DIAMOND_ORE);
			else if (S.equalsIgnoreCase("emerald_ore"))
				Result.add(BlockTypes.EMERALD_ORE);
			else if (S.equalsIgnoreCase("gold_ore"))
				Result.add(BlockTypes.GOLD_ORE);
			else if (S.equalsIgnoreCase("iron_ore"))
				Result.add(BlockTypes.IRON_ORE);
			else if (S.equalsIgnoreCase("lapis_ore"))
				Result.add(BlockTypes.LAPIS_ORE);
			else if (S.equalsIgnoreCase("lit_redstone_ore"))
				Result.add(BlockTypes.LIT_REDSTONE_ORE);
			else if (S.equalsIgnoreCase("quartz_ore"))
				Result.add(BlockTypes.QUARTZ_ORE);
			else if (S.equalsIgnoreCase("redstone_ore"))
				Result.add(BlockTypes.REDSTONE_ORE);
			else
				fLogger.error("Unrecognized value '" + S + "' in config entry 'DetectedBlocks'. Ignoring.");
			}

		return Result;
		}

	private int GetConfigInt(String aName, int aDefault, int aMin, int aMax)
		{
		int Result = fConfig.getNode(aName).getInt(aDefault);
		if (Result < aMin)
			{
			Result = aMin;
			}
		else if (Result > aMax)
			{
			Result = aMax;
			}
		else
			{
			return Result;
			}

		fLogger.error("Config entry '" + aName + "' should be in range [" + aMin + ":" + aMax + "]");
		fConfig.getNode(aName).setValue(Result);
		return Result;
		}

	private double GetConfigDouble(String aName, double aDefault, double aMin, double aMax)
		{
		double Result = fConfig.getNode(aName).getDouble(aDefault);
		if (Result < aMin)
			{
			Result = aMin;
			}
		else if (Result > aMax)
			{
			Result = aMax;
			}
		else
			{
			return Result;
			}

		fLogger.error("Config entry '" + aName + "' should be in range [" + aMin + ":" + aMax + "]");
		fConfig.getNode(aName).setValue(Result);
		return Result;
		}

	@Listener
	public void OnGameStoppingServerEvent(GameStoppingServerEvent aEvent)
		{
		try
			{
			fConfigLoader.save(fConfig);
			}
		catch (IOException e)
			{
			fLogger.error("Cannot save configuration to '" + fConfigFile + "'. " + e.getMessage());
			}
		}
	}
