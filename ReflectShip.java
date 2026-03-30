public class ReflectShip {
  public static void main(String[] args) throws Exception {
    String[] names = {
      "org.valkyrienskies.core.api.ships.Ship",
      "org.valkyrienskies.core.api.ships.LoadedShip",
      "org.valkyrienskies.core.api.ships.properties.ShipTransform",
      "org.valkyrienskies.core.api.ships.properties.ShipTransformProvider"
    };
    for (String n : names) {
      Class<?> c = Class.forName(n);
      System.out.println("===== " + n + " =====");
      for (var m : c.getDeclaredMethods()) System.out.println(m.toString());
    }
  }
}
