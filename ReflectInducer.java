public class ReflectInducer {
  public static void main(String[] args) throws Exception {
    Class<?> c = Class.forName("org.valkyrienskies.core.api.ships.ShipForcesInducer");
    System.out.println("name=" + c.getName());
    System.out.println("interfaces:");
    for (Class<?> i : c.getInterfaces()) {
      System.out.println(" - " + i.getName());
      for (var m : i.getDeclaredMethods()) {
        System.out.println("   * " + m.toString());
      }
    }
    System.out.println("declared methods:");
    for (var m : c.getDeclaredMethods()) {
      System.out.println(" - " + m.toString());
    }
  }
}
