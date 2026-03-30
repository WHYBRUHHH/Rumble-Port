public class ReflectKt {
  public static void main(String[] args) throws Exception {
    String[] names = {
      "org.valkyrienskies.core.api.ships.ServerShipKt",
      "org.valkyrienskies.core.api.ships.LoadedServerShipKt"
    };
    for(String n : names){
      Class<?> c = Class.forName(n);
      System.out.println("===== "+n+" =====");
      for(var m : c.getDeclaredMethods()) System.out.println(m.toString());
    }
  }
}
