public class ReflectVS {
  public static void main(String[] args) throws Exception {
    String[] names = {
      "org.valkyrienskies.core.api.ships.ShipPhysicsListener",
      "org.valkyrienskies.core.api.ships.ShipForcesInducer",
      "org.valkyrienskies.core.api.attachment.AttachmentHolder",
      "org.valkyrienskies.core.api.ships.ServerShip",
      "org.valkyrienskies.core.api.ships.LoadedServerShip",
      "org.valkyrienskies.core.api.ships.PhysShip"
    };
    for (String n : names) {
      Class<?> c = Class.forName(n);
      System.out.println("===== " + n + " =====");
      for (var m : c.getDeclaredMethods()) {
        System.out.println(m.toString());
      }
    }
  }
}
