package com.syos.inventory.domain.value;

/**
 * Enumeration representing units of measurement for products
 */
public enum UnitOfMeasure {
    KG("kg", "Kilogram", "Weight"),
    G("g", "Gram", "Weight"),
    L("l", "Liter", "Volume"),
    ML("ml", "Milliliter", "Volume"),
    PCS("pcs", "Pieces", "Count"),
    PACK("pack", "Pack", "Count"),
    BOX("box", "Box", "Count"),
    BOTTLE("bottle", "Bottle", "Count");

    private final String code;
    private final String displayName;
    private final String type;

    UnitOfMeasure(String code, String displayName, String type) {
        this.code = code;
        this.displayName = displayName;
        this.type = type;
    }

    public String getCode() {
        return code;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getType() {
        return type;
    }

    public static UnitOfMeasure fromCode(String code) {
        for (UnitOfMeasure unit : values()) {
            if (unit.code.equalsIgnoreCase(code)) {
                return unit;
            }
        }
        throw new IllegalArgumentException("Invalid unit of measure code: " + code);
    }

    @Override
    public String toString() {
        return displayName;
    }
}