package io.github.pylonmc.pylon.content.machines.diesel;

public interface DieselRefuelable {
    double getDiesel();
    void setDiesel(double amount);
    double getDieselCapacity();

    default double getDieselFluidSpace() {
        return getDieselCapacity() - getDiesel();
    }
}