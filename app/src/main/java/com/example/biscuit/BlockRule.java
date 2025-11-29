package com.example.biscuit;

public class BlockRule {
    public boolean blocked;
    public int startHour;
    public int startMinute;
    public int endHour;
    public int endMinute;


    public BlockRule() {}

    public BlockRule(boolean blocked, int startHour, int startMinute, int endHour, int endMinute) {
        this.blocked = blocked;
        this.startHour = startHour;
        this.startMinute = startMinute;
        this.endHour = endHour;
        this.endMinute = endMinute;
    }
}
