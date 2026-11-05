package io.github.pylonmc.pylon.api.item;

public interface ChargeableRebarItem {

    double getCharge();

    void setCharge(double charge);

    double getCapacity();

    boolean isRechargeable();

    default boolean isFull() {
        return getCharge() == getCapacity();
    }

    /**
     * @return the amount of charge that was actually used
     */
    default double charge(double charge) {
        double oldCharge = getCharge();
        double newCharge = Math.clamp(oldCharge + charge, 0, getCapacity());
        setCharge(newCharge);
        return newCharge - oldCharge;
    }

    default void discharge(double charge) {
        setCharge(Math.clamp(getCharge() - charge, 0, getCapacity()));
    }
}
