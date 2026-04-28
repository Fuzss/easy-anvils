package fuzs.easyanvils.common.config;

import fuzs.easyanvils.common.EasyAnvils;

import java.util.function.IntUnaryOperator;

public enum PriorWorkPenalty {
    NONE((int itemRepairCost) -> 0),
    VANILLA(IntUnaryOperator.identity()),
    LIMITED((int itemRepairCost) -> limitedRepairCost(repairCostToRepairs(itemRepairCost)));

    public final IntUnaryOperator operator;

    PriorWorkPenalty(IntUnaryOperator operator) {
        this.operator = operator;
    }

    private static int repairCostToRepairs(int itemRepairCost) {
        itemRepairCost++;
        int priorRepairs = 0;
        while (itemRepairCost >= 2) {
            itemRepairCost /= 2;
            priorRepairs++;
        }

        return priorRepairs;
    }

    private static int limitedRepairCost(int priorRepairs) {
        int maximumPriorWorkPenaltyIncrease = EasyAnvils.CONFIG.get(ServerConfig.class).priorWorkPenalty.maximumPriorWorkPenaltyIncrease;
        int itemRepairCost = 0;
        for (int i = 0; i < priorRepairs; i++) {
            itemRepairCost += Math.min(itemRepairCost + 1, maximumPriorWorkPenaltyIncrease);
        }

        return itemRepairCost;
    }
}
