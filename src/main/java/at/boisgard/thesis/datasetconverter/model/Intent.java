/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package at.boisgard.thesis.datasetconverter.model;

/**
 *
 * @author BUERO
 */
public enum Intent {

    SCHEDULE("schedule"),
    STANDINGS("standings"),
    RESULT("result"),
    SQUAD("squad"),
    PLAYER_INFO("player_info"),
    PLAYER_STATS("player_stats");

    private final String value;

    Intent(String value) {
        this.value = value;
    }

    public String getValue() {
        return this.value;
    }

    public static Intent getFittingIntent(String intentString) {

        switch (intentString) {

            case "schedule":
                return Intent.SCHEDULE;

            case "standings":
                return Intent.STANDINGS;

            case "squad":
                return Intent.SQUAD;

            case "result":
                return Intent.RESULT;

            case "player_info":
                return Intent.PLAYER_INFO;

            case "player_stats":
                return Intent.PLAYER_STATS;

            default:
                return Intent.SCHEDULE;
        }
    }
}
