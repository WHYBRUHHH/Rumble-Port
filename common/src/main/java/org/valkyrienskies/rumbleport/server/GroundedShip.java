package org.valkyrienskies.rumbleport.server;

import dev.architectury.event.events.common.TickEvent;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import org.valkyrienskies.core.api.ships.LoadedServerShip;
import org.valkyrienskies.core.api.ships.ServerShip;
import org.valkyrienskies.core.internal.joints.VSFixedJoint;
import org.valkyrienskies.core.internal.joints.VSJointPose;
import org.joml.Quaterniond;
import org.joml.Vector3d;
import org.valkyrienskies.mod.common.VSGameUtilsKt;

public final class GroundedShip {
    private static final Map<Long, ShipStateData> SHIP_STATES = new ConcurrentHashMap<>();

    private GroundedShip() {
    }

    public static void register() {
        TickEvent.SERVER_POST.register(GroundedShip::tick);
    }

    public static void ground(ServerShip ship) {
        if (ship == null) {
            return;
        }
        ShipStateData existingState = SHIP_STATES.get(ship.getId());
        if (existingState != null) {
            existingState.anchorX = ship.getTransform().getPositionInWorld().x();
            existingState.anchorY = ship.getTransform().getPositionInWorld().y();
            existingState.anchorZ = ship.getTransform().getPositionInWorld().z();
            existingState.constraintsDirty = true;
        } else {
            SHIP_STATES.put(ship.getId(), new ShipStateData(
                ShipState.GROUNDED,
                ship.getChunkClaimDimension(),
                ship.getTransform().getPositionInWorld().x(),
                ship.getTransform().getPositionInWorld().y(),
                ship.getTransform().getPositionInWorld().z()
            ));
        }
        if (ship instanceof LoadedServerShip loadedShip) {
            applyGroundedState(loadedShip);
        }
    }

    public static void unground(ServerShip ship, MinecraftServer server) {
        if (ship == null) {
            return;
        }
        ShipStateData state = SHIP_STATES.remove(ship.getId());
        ship.setStatic(false);
        if (state != null) {
            Object shipWorld = VSGameUtilsKt.getShipObjectWorld(server);
            removeConstraints(shipWorld, state);
        }
    }

    public static boolean isGrounded(ServerShip ship) {
        return ship != null
            && SHIP_STATES.containsKey(ship.getId())
            && SHIP_STATES.get(ship.getId()).state == ShipState.GROUNDED;
    }

    private static void tick(MinecraftServer server) {
        if (SHIP_STATES.isEmpty()) {
            return;
        }
        Object shipWorld = VSGameUtilsKt.getShipObjectWorld(server);
        Iterator<Map.Entry<Long, ShipStateData>> iterator = SHIP_STATES.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Long, ShipStateData> entry = iterator.next();
            ShipStateData state = entry.getValue();
            LoadedServerShip ship = VSGameUtilsKt.getShipObjectWorld(server).getLoadedShips().getById(entry.getKey());
            if (ship == null) {
                removeConstraints(shipWorld, state);
                iterator.remove();
                continue;
            }
            ServerLevel level = VSGameUtilsKt.getLevelFromDimensionId(server, state.dimensionId);
            if (level == null) {
                removeConstraints(shipWorld, state);
                iterator.remove();
                continue;
            }
            if (!applyState(shipWorld, ship, state)) {
                removeConstraints(shipWorld, state);
                iterator.remove();
            }
        }
    }

    private static boolean applyState(Object shipWorld, LoadedServerShip ship, ShipStateData state) {
        if (state.state == ShipState.GROUNDED) {
            applyGroundedState(ship);
            ensureConstraints(shipWorld, ship, state);
            return true;
        }
        return false;
    }

    private static void applyGroundedState(LoadedServerShip ship) {
        ship.setStatic(false);
    }

    private static void ensureConstraints(Object shipWorld, LoadedServerShip ship, ShipStateData state) {
        if (!state.constraintsDirty && state.fixedJointId != null) {
            return;
        }

        if (state.constraintsDirty) {
            removeConstraints(shipWorld, state);
        }

        Long groundBodyId = getGroundBodyId(shipWorld, state.dimensionId);
        if (groundBodyId == null) {
            return;
        }
        ServerShip groundBody = getShipById(shipWorld, groundBodyId);
        if (groundBody == null) {
            return;
        }

        Vector3d anchorPosInWorld = new Vector3d(state.anchorX, state.anchorY, state.anchorZ);
        Vector3d groundLocalPos = new Vector3d(anchorPosInWorld);
        groundBody.getWorldToShip().transformPosition(groundLocalPos);
        Vector3d shipLocalPos = new Vector3d(anchorPosInWorld);
        ship.getWorldToShip().transformPosition(shipLocalPos);
        Quaterniond groundRot = new Quaterniond(groundBody.getTransform().getShipToWorldRotation());
        Quaterniond shipRot = new Quaterniond(ship.getTransform().getShipToWorldRotation());
        Quaterniond localRot0 = groundRot.invert(new Quaterniond()).mul(shipRot);

        VSFixedJoint fixedJoint = new VSFixedJoint(
            groundBodyId,
            new VSJointPose(groundLocalPos, localRot0),
            ship.getId(),
            new VSJointPose(shipLocalPos, new Quaterniond()),
            null
        );

        state.fixedJointId = createJointOrConstraint(shipWorld, fixedJoint);
        state.constraintsDirty = false;
    }

    private static void removeConstraints(Object shipWorld, ShipStateData state) {
        if (state.fixedJointId != null) {
            removeJointOrConstraint(shipWorld, state.fixedJointId);
            state.fixedJointId = null;
        }
    }

    private static Integer createJointOrConstraint(Object shipWorld, Object joint) {
        String[] createNames = {"createNewConstraint", "createConstraint", "createNewJoint", "createJoint", "addConstraint", "addJoint"};
        for (String methodName : createNames) {
            for (Method method : shipWorld.getClass().getMethods()) {
                if (!method.getName().equals(methodName) || method.getParameterCount() != 1) {
                    continue;
                }
                Class<?> parameterType = method.getParameterTypes()[0];
                if (!parameterType.isAssignableFrom(joint.getClass())) {
                    continue;
                }
                try {
                    Object result = method.invoke(shipWorld, joint);
                    if (result instanceof Number number) {
                        return number.intValue();
                    }
                    if (result != null) {
                        try {
                            Method getJointIdMethod = result.getClass().getMethod("getJointId");
                            Object id = getJointIdMethod.invoke(result);
                            if (id instanceof Number number) {
                                return number.intValue();
                            }
                        } catch (NoSuchMethodException ignored) {
                            // No id getter on this result type.
                        }
                    }
                } catch (ReflectiveOperationException ignored) {
                    // Try the next candidate method.
                }
            }
        }
        return null;
    }

    private static void removeJointOrConstraint(Object shipWorld, int id) {
        String[] removeNames = {"removeConstraint", "removeJoint", "deleteConstraint", "deleteJoint"};
        for (String methodName : removeNames) {
            for (Method method : shipWorld.getClass().getMethods()) {
                if (!method.getName().equals(methodName) || method.getParameterCount() != 1) {
                    continue;
                }
                try {
                    method.invoke(shipWorld, id);
                    return;
                } catch (ReflectiveOperationException ignored) {
                    // Try the next candidate method.
                }
            }
        }
    }

    private static Long getGroundBodyId(Object shipWorld, String dimensionId) {
        try {
            Method method = shipWorld.getClass().getMethod("getDimensionToGroundBodyIdImmutable");
            Object mapObject = method.invoke(shipWorld);
            if (mapObject instanceof Map<?, ?> map) {
                Object idObject = map.get(dimensionId);
                if (idObject instanceof Number number) {
                    return number.longValue();
                }
            }
        } catch (ReflectiveOperationException ignored) {
            // Ground body map unavailable.
        }
        return null;
    }

    private static ServerShip getShipById(Object shipWorld, long shipId) {
        try {
            Method getAllShipsMethod = shipWorld.getClass().getMethod("getAllShips");
            Object allShips = getAllShipsMethod.invoke(shipWorld);
            if (allShips == null) {
                return null;
            }
            Method getByIdMethod = allShips.getClass().getMethod("getById", long.class);
            Object shipObject = getByIdMethod.invoke(allShips, shipId);
            if (shipObject instanceof ServerShip serverShip) {
                return serverShip;
            }
        } catch (ReflectiveOperationException ignored) {
            // Ship lookup unavailable.
        }
        return null;
    }

    private enum ShipState {
        GROUNDED
    }

    private static final class ShipStateData {
        private final ShipState state;
        private final String dimensionId;
        private double anchorX;
        private double anchorY;
        private double anchorZ;
        private Integer fixedJointId;
        private boolean constraintsDirty;

        private ShipStateData(ShipState state, String dimensionId, double anchorX, double anchorY, double anchorZ) {
            this.state = state;
            this.dimensionId = dimensionId;
            this.anchorX = anchorX;
            this.anchorY = anchorY;
            this.anchorZ = anchorZ;
            this.constraintsDirty = true;
        }
    }

}
