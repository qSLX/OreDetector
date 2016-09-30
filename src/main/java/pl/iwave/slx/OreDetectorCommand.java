package pl.iwave.slx;

import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.command.source.CommandBlockSource;
import org.spongepowered.api.command.source.ConsoleSource;

import java.util.Optional;

/**
 * Created by slx on 28.09.16.
 */
public class OreDetectorCommand implements CommandExecutor
	{
	@Override
	public CommandResult execute(CommandSource src, CommandContext args) throws CommandException
		{
		if (src instanceof Player)
			{
			Player player = (Player) src;
			Optional<Boolean> State = args.<Boolean>getOne("Detector");
			if (State.get())
				{
				OreDetector.fPlayers.add(player);
				player.sendMessage(Text.of("Detector on."));
				}
			else
				{
				OreDetector.fPlayers.remove(player);
				player.sendMessage(Text.of("Detector off."));
				}

			return CommandResult.success();
			}
		else
			{
			src.sendMessage(Text.of("Only player can use it."));
			return CommandResult.empty();
			}
		}
	}

