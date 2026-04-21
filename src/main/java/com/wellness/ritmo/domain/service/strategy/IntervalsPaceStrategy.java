package com.wellness.ritmo.domain.service.strategy;

import com.wellness.ritmo.domain.model.Enum.SessionType;
import org.springframework.stereotype.Component;

@Component
public class IntervalsPaceStrategy implements PaceCalculationStrategy {

    @Override
    public SessionType supports() {
        return SessionType.INTERVALS;
    }

    @Override
    public int calculatePace(int paceAvgSeg) {
        return (int) Math.round(paceAvgSeg * 0.9);
    }
}
