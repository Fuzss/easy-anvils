package fuzs.easyanvils.config;

import fuzs.easyanvils.EasyAnvils;

import java.util.function.IntUnaryOperator;

public enum PriorWorkPenalty {
    NONE(itemRepairCost -> 0),
    VANILLA(IntUnaryOperator.identity()),
    LIMITED(itemRepairCost -> limitedRepairCost(repairCostToRepairs(itemRepairCost)));

    public final IntUnaryOperator operator;

    PriorWorkPenalty(IntUnaryOperator operator) {
        this.operator = operator;
    }

    static int repairCostToRepairs(int itemRepairCost) {
        itemRepairCost++;
        int priorRepairs = 0;
        while (itemRepairCost >= 2) {
            itemRepairCost /= 2;
            priorRepairs++;
        }
        return priorRepairs;
    }

    static int limitedRepairCost(int priorRepairs) {
        int itemRepairCost = 0;
        for (int i = 0; i < priorRepairs; i++) {
            itemRepairCost += Math.min(itemRepairCost + 1,
                    EasyAnvils.CONFIG.get(ServerConfig.class).priorWorkPenalty.maximumPriorWorkPenaltyIncrease
            );
        }
        return itemRepairCost;
    }
}
