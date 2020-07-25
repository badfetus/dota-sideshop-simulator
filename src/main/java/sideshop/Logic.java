package sideshop;

import static sideshop.Data.*;
import static sideshop.RandomUtil.*;

public class Logic {
    static int gold = STARTING_GOLD;
    static int gemCount = 0;
    static Hero[] playerGrid = new Hero[GRID_SIZE];
    static Hero[] shopGrid = new Hero[SHOP_SIZE];
    static int[][] shopHeroPool = new int[TIER_COUNT][UNIQUE_HERO_PER_TIER];
    static Hero[][][] heroes = new Hero[TIER_COUNT][UNIQUE_HERO_PER_TIER][MAX_STARS];
    static int[][] playerHeroPool = new int[TIER_COUNT][UNIQUE_HERO_PER_TIER];
    static int playerHeroCount = 0;
    static int shopHeroCount = 0;

    static int lastUpdate = STARTING_GOLD + 10;
    static int percentComplete = 0;

    public static void main(String[] args) {
        initializeHeroes();
        initializeShopHeroPool();
        fillShop();
        System.out.println("0% complete.");
        while (gold > 0) {
            if (lastUpdate - gold >= STARTING_GOLD / 100) {
                percentComplete++;
                System.out.println(percentComplete + "% complete");
                lastUpdate -= STARTING_GOLD / 100;
            }

            buyAndSellHeroes();
            if (gold >= REROLL_COST) {
                rerollShop();
            } else {
                break;
            }
        }
        sellRemainingHeroes();

        System.out.println(String.format("%,d %s gems obtained from %,d gold", gemCount, COLLECT_RED_GEMS ? "red" : "blue", STARTING_GOLD - gold));
    }


    private static void initializeHeroes() {
        for (int i = 0; i < heroes.length; i++) {
            for (int j = 0; j < heroes[i].length; j++) {
                for (int k = 0; k < heroes[i][j].length; k++) {
                    heroes[i][j][k] = new Hero(i, j, k);
                }
            }
        }
    }


    private static void initializeShopHeroPool() {
        for (int i = 0; i < shopHeroPool.length; i++) {
            for (int j = 0; j < shopHeroPool[i].length; j++) {
                shopHeroPool[i][j] = TOTAL_UNIT_AMOUNT_IN_POOL;
            }
        }
    }

    private static void fillShop() {
        emptyShop();
        for (int i = 0; i < shopGrid.length; i++) {
            fillShopSpot(i);
        }
    }

    private static void emptyShop() {
        for (int i = 0; i < shopGrid.length; i++) {
            if (shopGrid[i] != null) {
                shopHeroPool[shopGrid[i].tier][shopGrid[i].id]++;
                shopGrid[i] = null;
                shopHeroCount--;
            }
        }
    }

    private static void fillShopSpot(int shopSpot) {
        int tier = pickFromWeights(TIER_WEIGHTS);
        int id = pickFromWeights(shopHeroPool[tier]);
        shopHeroPool[tier][id]--;
        shopGrid[shopSpot] = heroes[tier][id][0];
        shopHeroCount++;
    }

    /*
        Worth isn't really the right word here, but it refers to how many 1 star versions of that hero we are withholding in player bench.
        e.g. if you have a 3 star hero (the maximum) it will have 9 worth whereas 1 2star + 2 1stars will be 5 worth (3 + 1 + 1).
    */

    private static void buyAndSellHeroes() {
        while (true) {
            boolean canBuyAHero = canBuyAHero();
            buyHeroesToCombine();
            if (playerHeroCount == GRID_SIZE) {
                sellMostWorthlessHero();
            } else {
                flipNonCollectHeroes();
                buyMostWorthlessHero();
            }

            if (!canBuyAHero) {
                break;
            }
        }
    }

    private static boolean canBuyAHero() {
        for (Hero hero : shopGrid) {
            if (hero != null) {
                if (gold >= hero.tier + 1 && (COLLECT_TIER[hero.tier] || FLIP_NON_COLLECT_HEROES)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static void buyHeroesToCombine() {
        for (int i = 0; i < shopGrid.length; i++) {
            if (shopGrid[i] != null) {
                if (playerHeroPool[shopGrid[i].tier][shopGrid[i].id] % NUM_TO_UPGRADE == NUM_TO_UPGRADE - 1) {
                    buyHero(i);
                }
            }
        }
    }

    private static void flipNonCollectHeroes() {
        if (FLIP_NON_COLLECT_HEROES) {
            for (int i = 0; i < shopGrid.length; i++) {
                if (shopGrid[i] != null) {
                    if (!COLLECT_TIER[shopGrid[i].tier] && gold >= shopGrid[i].tier + 1) { //Flip the hero
                        gold -= shopGrid[i].tier + 1;
                        gemCount += RED_GEM_REWARDS[shopGrid[i].tier][0];
                        shopHeroPool[shopGrid[i].tier][shopGrid[i].id]++;
                        shopGrid[i] = null;
                        shopHeroCount--;
                    }
                }
            }
        }
    }

    private static void buyHero(int shopIndex) {
        Hero hero = shopGrid[shopIndex];
        if (gold >= hero.tier + 1) {
            shopGrid[shopIndex] = null;
            playerHeroPool[hero.tier][hero.id]++;
            gold -= hero.tier + 1;
            if (shouldCombine(hero)) {
                combineHero(hero);
            } else {
                addHero(hero);
            }
        }
    }

    private static boolean shouldCombine(Hero hero) {
        return playerHeroPool[hero.tier][hero.id] % Math.pow(NUM_TO_UPGRADE, hero.stars + 1) == 0;
    }

    private static void combineHero(Hero hero) {
        for (int i = 0; i < playerGrid.length; i++) {
            if (playerGrid[i] != null) {
                if (playerGrid[i].equals(hero)) {
                    removeHero(i);
                }
            }
        }

        Hero combinedHero = heroes[hero.tier][hero.id][hero.stars + 1];
        addHero(combinedHero);
        if (shouldCombine(combinedHero)) {
            combineHero(combinedHero);
        } else if (combinedHero.stars == MAX_STARS - 1) {
            sellHero(combinedHero);
        }
    }

    private static void removeHero(int playerIndex) {
        playerGrid[playerIndex] = null;
        playerHeroCount--;
    }

    private static void addHero(Hero hero) {
        for (int i = 0; i < playerGrid.length; i++) {
            if (playerGrid[i] == null) {
                playerGrid[i] = hero;
                playerHeroCount++;
                return;
            }
        }
        throw new RuntimeException("Failed to add hero - no empty spots found.");
    }

    private static void sellHero(Hero hero) {
        for (int i = 0; i < playerGrid.length; i++) {
            if (playerGrid[i] == hero) {
                sellHero(i);
                return;
            }
        }
        throw new RuntimeException("Failed to sell hero - hero not found on player bench");
    }

    private static void sellHero(int playerIndex) {
        Hero hero = playerGrid[playerIndex];
        gemCount += hero.gemSellValue();
        removeHero(playerIndex);
        playerHeroPool[hero.tier][hero.id] -= Math.pow(NUM_TO_UPGRADE, hero.stars);
        shopHeroPool[hero.tier][hero.id] += Math.pow(NUM_TO_UPGRADE, hero.stars);
    }

    private static void sellMostWorthlessHero() {
        int mostWorthlessHeroValue = 99999999;
        int mostWorthlessHeroPosition = -1;
        int mostWorthlessHeroStars = 99999999;

        for (int i = 0; i < playerGrid.length; i++) {
            if (playerGrid[i] != null) {
                int heroValue = playerHeroPool[playerGrid[i].tier][playerGrid[i].id];
                if (heroValue < mostWorthlessHeroValue || (heroValue == mostWorthlessHeroValue && playerGrid[i].stars < mostWorthlessHeroStars)) {
                    mostWorthlessHeroValue = heroValue;
                    mostWorthlessHeroPosition = i;
                    mostWorthlessHeroStars = playerGrid[i].stars;
                }
            }
        }

        sellHero(mostWorthlessHeroPosition);
    }

    private static void buyMostWorthlessHero() {
        int mostWorthlessHeroValue = 99999999;
        int mostWorthlessHeroPosition = -1;

        for (int i = 0; i < shopGrid.length; i++) {
            if (shopGrid[i] != null) {
                int heroValue = playerHeroPool[shopGrid[i].tier][shopGrid[i].id];
                if (heroValue < mostWorthlessHeroValue && gold >= shopGrid[i].tier + 1) {
                    mostWorthlessHeroValue = heroValue;
                    mostWorthlessHeroPosition = i;
                }
            }
        }

        if (mostWorthlessHeroPosition == -1) { //nothing to buy
            return;
        }
        buyHero(mostWorthlessHeroPosition);
    }


    private static void rerollShop() {
        gold -= REROLL_COST;
        fillShop();
    }

    private static void sellRemainingHeroes() {
        for (int i = 0; i < playerGrid.length; i++) {
            if (playerGrid[i] != null) {
                sellHero(i);
            }
        }
    }
}
