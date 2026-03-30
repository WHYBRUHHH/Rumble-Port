import java.lang.reflect.Modifier;
public class ReflectInducer2 {
  public static void main(String[] args) throws Exception {
    Class<?> c = Class.forName("org.valkyrienskies.core.api.ships.ShipForcesInducer");
    System.out.println("isInterface=" + c.isInterface());
    System.out.println("modifiers=" + Modifier.toString(c.getModifiers()));
    System.out.println("super=" + c.getSuperclass());
    System.out.println("methods incl inherited:");
    for (var m : c.getMethods()) {
      if (m.getDeclaringClass() != Object.class) System.out.println(" - " + m.toString() + " | decl=" + m.getDeclaringClass().getName());
    }
  }
}
