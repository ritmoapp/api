package com.wellness.ritmo.domain.service.strategy;

import com.wellness.ritmo.domain.model.Enum.SessionType;
import org.springframework.stereotype.Component;

@Component
public class EasyPaceStrategy implements PaceCalculationStrategy {

    @Override
    public SessionType supports() {
        return SessionType.EASY;
    }

    @Override
    public int calculatePace(int paceAvgSeg) {
        return (int) Math.round(paceAvgSeg * 1.2);
    }
}
