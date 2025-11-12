package casey.lcbdev.model.game;

public class AttackResult {
    public enum Type { HIT, MISS, SUNK, ALREADY, INVALID }

    public final Type type;
    public final String shipKey; 

    public AttackResult(Type type, String shipKey) {
        this.type = type;
        this.shipKey = shipKey;
    }

    public static AttackResult hit(String key) { return new AttackResult(Type.HIT, key); }
    public static AttackResult miss() { return new AttackResult(Type.MISS, null); }
    public static AttackResult sunk(String key) { return new AttackResult(Type.SUNK, key); }
    public static AttackResult already() { return new AttackResult(Type.ALREADY, null); }
    public static AttackResult invalid() { return new AttackResult(Type.INVALID, null); }
}
