package models;

 //Registro de una calle entre dos puntos, usado por la persistencia y por la
 //vista para dibujar las aristas y es la fuente de verdad de la direccionalidad
 //(bidireccional o de un solo sentido), ya que esa informacion no puede
 //derivarse de forma inequivoca desde la lista de adyacencia de Graph una vez
 //que se editan conexiones
 
public class EdgeRecord {

    private final String from;            //Todo final porque una vez creada una arista
    private final String to;              //con ciertos extremos y cierta direccionalidad,
    private final boolean bidirectional;  //no tiene sentido "editarla" si cambia algo,
                                          //se borra y se crea una nueva
    public EdgeRecord(String from, String to, boolean bidirectional) {
        this.from = from;
        this.to = to;
        this.bidirectional = bidirectional;
    }

    public String getFrom() {
        return from;
    }

    public String getTo() {
        return to;
    }

    public boolean isBidirectional() {
        return bidirectional;
    }
}
