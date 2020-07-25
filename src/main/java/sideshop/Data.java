package sideshop;

public class Data {
    public static final int STARTING_GOLD = 100_000_000;

    public static final int[][] RED_GEM_REWARDS = {
            {1, 3, 18},
            {2, 8, 60},
            {3, 18, 185},
            {4, 30, 0},
            {7, 110, 0},
    };

    public static final int[][] BLUE_GEM_REWARDS = {
            {0,0,0},
            {0,0,0},
            {0,0,0},
            {0,0,1},
            {0,0,3},
    };

    public static final boolean COLLECT_RED_GEMS = true;
    public static final boolean[] COLLECT_TIER = {false, false, true, false, false};
    public static final boolean FLIP_NON_COLLECT_HEROES = false;

    public static final int GRID_SIZE = 9;
    public static final int SHOP_SIZE = 5;

    public static final int TIER_COUNT = 5;
    public static final int[] TIER_WEIGHTS = {72, 19, 5, 3, 1};
    public static final int TOTAL_UNIT_AMOUNT_IN_POOL = 18;
    public static final int UNIQUE_HERO_PER_TIER = 5;
    public static final int NUM_TO_UPGRADE = 3;
    public static final int MAX_STARS = 3;
    public static final int REROLL_COST= 2;
}
