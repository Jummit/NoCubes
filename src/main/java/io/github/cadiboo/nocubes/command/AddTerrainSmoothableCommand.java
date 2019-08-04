package io.github.cadiboo.nocubes.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import io.github.cadiboo.nocubes.NoCubes;
import io.github.cadiboo.nocubes.config.ConfigHelper;
import io.github.cadiboo.nocubes.network.S2CAddTerrainSmoothable;
import io.github.cadiboo.nocubes.util.StateHolder;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.BlockStateArgument;
import net.minecraftforge.fml.network.PacketDistributor;

import static io.github.cadiboo.nocubes.util.ModUtil.COMMAND_PERMISSION_LEVEL;

public class AddTerrainSmoothableCommand {

	public static void register(CommandDispatcher<CommandSource> dispatcher) {
		dispatcher.register(Commands.literal("addTerrainSmoothable")
				.requires((source) -> source.hasPermissionLevel(COMMAND_PERMISSION_LEVEL))
				.then(Commands.argument("block", BlockStateArgument.blockState())
						.executes(AddTerrainSmoothableCommand::addBlockState)
				)
		);
	}

	private static int addBlockState(final CommandContext<CommandSource> ctx) {
		final BlockState blockState = BlockStateArgument.getBlockState(ctx, "block").getState();
		if (blockState == StateHolder.AIR_DEFAULT) {
			NoCubes.LOGGER.error("Trying to add invalid terrain smoothable blockstate: " + blockState);
			return 0;
		}
		ConfigHelper.addTerrainSmoothable(blockState);
		NoCubes.CHANNEL.send(PacketDistributor.ALL.noArg(), new S2CAddTerrainSmoothable(Block.getStateId(blockState)));
		return Command.SINGLE_SUCCESS;
	}

}
