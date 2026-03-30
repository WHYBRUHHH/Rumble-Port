public class ReflectKinematics {
  public static void main(String[] args) throws Exception {
    Class<?> c = Class.forName("org.valkyrienskies.core.api.bodies.properties.BodyKinematics");
    System.out.println("===== " + c.getName() + " =====");
    for (var m : c.getDeclaredMethods()) System.out.println(m.toString());
  }
}
