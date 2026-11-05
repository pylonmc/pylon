package io.github.pylonmc.pylon.api.item;

public interface ChargeableRebarItem {

    double getCharge();

    void setCharge(double charge);

    double getCapacity();

    default boolean isRechargeable() {
        return true;
    }

    default void charge(double charge) {
        setCharge(Math.clamp(getCharge() + charge, 0, getCapacity()));
    }

    default void discharge(double charge) {
        setCharge(Math.clamp(getCharge() - charge, 0, getCapacity()));
    }
}
