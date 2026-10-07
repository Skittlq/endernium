package com.skittlq.endernium.item;

import java.math.BigDecimal;

public final class EnderniumTooltipNumbers {
    private EnderniumTooltipNumbers() {
    }

    public static String compact(double value) {
        return BigDecimal.valueOf(value).stripTrailingZeros().toPlainString();
    }
}
