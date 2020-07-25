package sideshop;

import java.util.concurrent.ThreadLocalRandom;

public class RandomUtil {
    public static int pickFromWeights(int[] weights){
        int sum = 0;
        for(int weight: weights){
            sum += weight;
        }

        if(sum == 0){
            throw new IllegalArgumentException("Weights must sum above 0.");
        }

        int randVal = ThreadLocalRandom.current().nextInt(sum);
        int currWeight = 0;
        for(int i = 0; i<weights.length; i++){
            currWeight += weights[i];
            if(currWeight > randVal){
                return i;
            }
        }

        throw new RuntimeException("This should be unreachable");
    }
}
