package com.wellness.ritmo.domain.service.strategy;

import com.wellness.ritmo.domain.model.Enum.SessionType;

public interface PaceCalculationStrategy {

    SessionType supports();

    int calculatePace(int paceAvgSeg);
}
