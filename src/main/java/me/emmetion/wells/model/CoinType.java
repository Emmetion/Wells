package me.emmetion.wells.model;

public enum CoinType {
    BRONZE("BRONZE_COIN", 1),
    SILVER("SILVER_COIN", 3),
    GOLD("GOLD_COIN", 10);

    private String wells_id;
    private int experience;

    CoinType(String wells_id, int experience) {
        this.wells_id = wells_id;
        this.experience = experience;
    }

    public String getWellsId() {
        return this.wells_id;
    }

    public int getExperience() {
        return this.experience;
    }

    public static CoinType getCoinTypeFromWellsID(String wells_id) {
        for (CoinType ct : CoinType.values()) {
            if (ct.wells_id.equals(wells_id)) {
                return ct;
            }
        }

        return null;
    }

    @Override
    public String toString() {
        return String.format("CoinType{type=%s, experience=%d}", wells_id, experience);
    }

}
