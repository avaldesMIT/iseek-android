/**
 * Copyright (C) 2015 iSeek, Inc.
 * All Rights Reserved.
 *
 * This software is the confidential and proprietary information of iSeek, Inc.
 * You shall not disclose such confidential information and shall use it only
 * in accordance with the terms of the license agreement you entered into with
 * iSeek, Inc.
 */
package us.iseek.model.android;

import java.util.EnumSet;

/**
 * Enumerates the selected styles and their respective indexes
 *
 * @author Armando Valdes
 * @since 1.0
 */
public enum MenuItem {

    TOPICS_BUTTON("topicsButton", 0, 1),
    CHAT_BUTTON("chatButton", 2, 3),
    START_TOPIC_BUTTON("startTopicButton", 4, 5),
    SETTINGS_BUTTON("settingsButton", 6, 7);

    private String symbol;
    private int selectedIndex;
    private int unselectedIndex;

    /**
     * Creates a enum constant.
     *
     * @param symbol
     *            - The symbol for this instance
     * @param unselectedIndex
     *            - The unselected index for this instance
     * @param selectedIndex
     *            - The selected index for this instance
     */
    MenuItem(String symbol, int unselectedIndex, int selectedIndex) {
        this.symbol = symbol;
        this.selectedIndex = selectedIndex;
        this.unselectedIndex = unselectedIndex;
    }

    /**
     * Retrieves the symbolic representation of this enum.
     *
     * @return the symbol.
     */
    public String toSymbol() {
        return this.symbol;
    }

    /**
     * @return the selectedIndex
     */
    public int getSelectedIndex() {
        return this.selectedIndex;
    }

    /**
     * @return the unselectedIndex
     */
    public int getUnselectedIndex() {
        return this.unselectedIndex;
    }

    /**
     * Retrieves an enum constant based on the symbolic representation provided.
     *
     * @param symbol
     *            - The symbolic representation of the enum constant.
     * @return The enum constant corresponding to the symbolic representation of
     *         the constant, or null if no constant matches the symbol provided.
     */
    public static MenuItem fromSymbol(Object symbol) {
        // Iterate over all constants to see if any matches the symbol provided
        for (MenuItem constant : EnumSet.allOf(MenuItem.class)) {
            if (constant.toSymbol().equals(symbol)) {
                return constant;
            }
        }

        // No constant matched the symbol provided
        return null;
    }

    /**
     * Retrieves an enum constant based on the selectedIndex provided.
     *
     * @param selectedIndex
     *            - The selectedIndex of the enum constant.
     * @return The enum constant corresponding to the selectedIndex of the constant,
     *         or null if no constant matches the selectedIndex provided.
     */
    public static MenuItem fromSelectedIndex(int selectedIndex) {
        // Iterate over all constants to see if any matches the symbol provided
        for (MenuItem constant : EnumSet.allOf(MenuItem.class)) {
            if (constant.getSelectedIndex() == selectedIndex) {
                return constant;
            }
        }

        // No constant matched the symbol provided
        return null;
    }

    /**
     * Retrieves an enum constant based on the unSelectedIndex provided.
     *
     * @param unSelectedIndex
     *            - The unSelectedIndex of the enum constant.
     * @return The enum constant corresponding to the unSelectedIndex of the constant,
     *         or null if no constant matches the unSelectedIndex provided.
     */
    public static MenuItem fromUnselectedIndex(int unSelectedIndex) {
        // Iterate over all constants to see if any matches the symbol provided
        for (MenuItem constant : EnumSet.allOf(MenuItem.class)) {
            if (constant.getUnselectedIndex() == unSelectedIndex) {
                return constant;
            }
        }

        // No constant matched the symbol provided
        return null;
    }
}
