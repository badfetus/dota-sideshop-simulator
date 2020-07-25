package sideshop;

import static sideshop.Data.*;

public class Hero {
    public final int tier;
    public final int id;
    public final int stars;

    public Hero(int tier, int id, int stars) {
        this.tier = tier;
        this.id = id;
        this.stars = stars;
    }

    public int gemSellValue() {
        if(COLLECT_RED_GEMS) {
            return RED_GEM_REWARDS[tier][stars];
        }else{
            return BLUE_GEM_REWARDS[tier][stars];
        }
    }
}
