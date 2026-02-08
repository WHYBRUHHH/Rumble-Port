package org.valkyrienskies.rumbleport.server;

import com.mojang.brigadier.CommandDispatcher;
import dev.architectury.event.events.common.CommandRegistrationEvent;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaterniond;
import org.joml.Vector3d;
import org.joml.Vector3dc;
import org.valkyrienskies.core.api.ships.ServerShip;
import org.valkyrienskies.core.api.ships.Ship;
import org.valkyrienskies.core.internal.ShipTeleportData;
import org.valkyrienskies.core.util.datastructures.DenseBlockPosSet;
import org.valkyrienskies.mod.common.VSGameUtilsKt;
import org.valkyrienskies.mod.common.assembly.ShipAssemblyKt;

public final class RumblePortCommands {
    private static final double SHIP_SEARCH_RADIUS = 256.0D;
    private static final double KICK_UPWARD_VELOCITY = 5.0D;
    private static ServerShip closestShip = null;

    private RumblePortCommands() {
    }

    public static void register() {
        CommandRegistrationEvent.EVENT.register((dispatcher, registry, selection) -> registerCommands(dispatcher));
    }

    private static void registerCommands(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
            Commands.literal("ss")
                .requires(source -> true)
                .then(
                    Commands.literal("test")
                        .executes(context -> {
                            ServerPlayer player = context.getSource().getPlayerOrException();
                            createSingleBlockShip(player);
                            return 1;
                        })
                )
        );

        dispatcher.register(
            Commands.literal("ms")
                .requires(source -> true)
                .then(
                    Commands.literal("kick")
                        .executes(context -> {
                            ServerPlayer player = context.getSource().getPlayerOrException();
                            return straight(player) ? 1 : 0;
                        })
                )
        );
    }

    private static void createSingleBlockShip(ServerPlayer player) {
        ServerLevel world = player.serverLevel();
        BlockPos base = player.blockPosition().above();
        Vec3 look = player.getLookAngle().normalize();
        Vec3 offset = look.scale(2.0D);
        BlockPos origin = BlockPos.containing(base.getX() + offset.x, (double) base.getY(), base.getZ() + offset.z);

        world.setBlock(origin, Blocks.STONE.defaultBlockState(), 3);

        DenseBlockPosSet blocks = new DenseBlockPosSet();
        blocks.add(origin.getX(), origin.getY(), origin.getZ());

        ServerShip ship = ShipAssemblyKt.createNewShipWithBlocks(origin, blocks, world);

        HitResult hitResult = player.pick(5.0D, 1.0F, false);
        Vec3 lookTarget = hitResult.getLocation();

        Vector3d targetPosition = new Vector3d(lookTarget.x, lookTarget.y, lookTarget.z);
        Vector3d facingDirection = new Vector3d(look.x, look.y, look.z);
        if (facingDirection.lengthSquared() < 1.0E-6D) {
            facingDirection.set(0.0D, 0.0D, 1.0D);
        } else {
            facingDirection.normalize();
        }
        Quaterniond shipRotation = new Quaterniond().rotateTo(new Vector3d(0.0D, 0.0D, 1.0D), facingDirection);

        ShipTeleportData teleportData = VSGameUtilsKt.getVsCore().newShipTeleportData(
            targetPosition,
            shipRotation,
            new Vector3d(),
            new Vector3d(),
            VSGameUtilsKt.getDimensionId(world),
            null,
            null
        );
        VSGameUtilsKt.getShipObjectWorld(world).teleportShip(ship, teleportData);
    }

    private static boolean kick(ServerPlayer player) {
        ServerLevel world = player.serverLevel();
        Vec3 playerPos = player.position();
        AABB searchBox = new AABB(
            playerPos.x - SHIP_SEARCH_RADIUS,
            playerPos.y - SHIP_SEARCH_RADIUS,
            playerPos.z - SHIP_SEARCH_RADIUS,
            playerPos.x + SHIP_SEARCH_RADIUS,
            playerPos.y + SHIP_SEARCH_RADIUS,
            playerPos.z + SHIP_SEARCH_RADIUS
        );

        ServerShip closestShip = null;
        double closestDistanceSq = Double.MAX_VALUE;

        for (Ship ship : VSGameUtilsKt.getShipsIntersecting(world, searchBox)) {
            if (!(ship instanceof ServerShip)) {
                continue;
            }
            Vector3dc shipPos = ship.getTransform().getPositionInWorld();
            double dx = shipPos.x() - playerPos.x;
            double dy = shipPos.y() - playerPos.y;
            double dz = shipPos.z() - playerPos.z;
            double distSq = (dx * dx) + (dy * dy) + (dz * dz);
            if (distSq < closestDistanceSq) {
                closestDistanceSq = distSq;
                closestShip = (ServerShip) ship;
            }
        }

        if (closestShip == null) {
            return false;
        }

        Vector3d newVelocity = new Vector3d(closestShip.getVelocity())
            .add(0.0D, KICK_UPWARD_VELOCITY, 0.0D);
        Vector3d angularVelocity = new Vector3d(closestShip.getAngularVelocity());

        ShipTeleportData teleportData = VSGameUtilsKt.getVsCore().newShipTeleportData(
            closestShip.getTransform().getPositionInWorld(),
            closestShip.getTransform().getShipToWorldRotation(),
            newVelocity,
            angularVelocity,
            VSGameUtilsKt.getDimensionId(world),
            null,
            null
        );
        VSGameUtilsKt.getShipObjectWorld(world).teleportShip(closestShip, teleportData);
        return true;
    }

    private static boolean straight(ServerPlayer player) {
        findStructure(player);
        ServerLevel world = player.serverLevel();

        //Velocity
        Vector3d newVelocity = new Vector3d(closestShip.getVelocity())
                .add(0.0D, KICK_UPWARD_VELOCITY, 0.0D);
        Vector3d angularVelocity = new Vector3d(closestShip.getAngularVelocity());

        ShipTeleportData teleportData = VSGameUtilsKt.getVsCore().newShipTeleportData(
                closestShip.getTransform().getPositionInWorld(),
                closestShip.getTransform().getShipToWorldRotation(),
                newVelocity,
                angularVelocity,
                VSGameUtilsKt.getDimensionId(world),
                null,
                null
        );
        VSGameUtilsKt.getShipObjectWorld(world).teleportShip(closestShip, teleportData);
        return true;
    }

    private static void findStructure(ServerPlayer player) {
        ServerLevel world = player.serverLevel();
        Vec3 playerPos = player.position();
        AABB searchBox = new AABB(
                playerPos.x - SHIP_SEARCH_RADIUS,
                playerPos.y - SHIP_SEARCH_RADIUS,
                playerPos.z - SHIP_SEARCH_RADIUS,
                playerPos.x + SHIP_SEARCH_RADIUS,
                playerPos.y + SHIP_SEARCH_RADIUS,
                playerPos.z + SHIP_SEARCH_RADIUS
        );

        double closestDistanceSq = Double.MAX_VALUE;

        for (Ship ship : VSGameUtilsKt.getShipsIntersecting(world, searchBox)) {
            if (!(ship instanceof ServerShip)) {
                continue;
            }
            Vector3dc shipPos = ship.getTransform().getPositionInWorld();
            double dx = shipPos.x() - playerPos.x;
            double dy = shipPos.y() - playerPos.y;
            double dz = shipPos.z() - playerPos.z;
            double distSq = (dx * dx) + (dy * dy) + (dz * dz);
            if (distSq < closestDistanceSq) {
                closestDistanceSq = distSq;
                closestShip = (ServerShip) ship;
            }
        }
    }
}
