package com.clab.larvakey;

enum CHARACTER_TYPE {
    ALPHABET_LOWERCASE, ALPHABET_UPPERCASE, NUMBER, SYMBOL
}

enum GESTURE_DIRECTION {
    UP(0), RIGHTUP(1), RIGHT(2), RIGHTDOWN(3),
    DOWN(4), LEFTDOWN(5), LEFT(6), LEFTUP(7),
    STARTING_POINT(8), SHOULD_COME_BACK(9);
    private final int index;
    private GESTURE_DIRECTION(int index) {
        this.index = index;
    }
    public int toInt() {
        return index;
    }
}
