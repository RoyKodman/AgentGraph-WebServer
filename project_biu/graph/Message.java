package graph;

import java.util.Date;

public class Message {
    public final byte[] data;
    public final String asText;
    public final double asDouble; // We assume that String only represent numbers.
    public final Date date; // The date when the message was created.

    public Message(String asText){
        this.data = asText.getBytes();
        double valueDouble;
        try {
            valueDouble = Double.parseDouble(asText);
        }catch (NumberFormatException e) {
            valueDouble = Double.NaN;
        }
        this.asDouble = valueDouble;
        this.asText = asText;
        this.date = new Date();
    }

    public Message(byte[] dataAsBytes){
        this(dataAsBytes.toString());
    }

    public Message(double dataAsDouble){
        this(Double.toString(dataAsDouble));
    }

}
