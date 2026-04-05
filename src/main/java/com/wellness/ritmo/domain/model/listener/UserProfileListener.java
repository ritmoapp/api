package com.wellness.ritmo.domain.model.listener;

import com.wellness.ritmo.domain.model.UserProfile;
import com.wellness.ritmo.domain.model.UserProfileHistory;
import com.wellness.ritmo.domain.repository.UserProfileHistoryRepository;
import com.wellness.ritmo.infrastructure.context.ApplicationContextProvider;
import jakarta.persistence.PostUpdate;
import org.springframework.context.ApplicationContext;

public class UserProfileListener {

    @PostUpdate
    public void onPostUpdate(UserProfile profile) {
        ApplicationContext context = ApplicationContextProvider.getContext();
        UserProfileHistoryRepository repository =
                context.getBean(UserProfileHistoryRepository.class);

        UserProfileHistory snapshot = new UserProfileHistory();
        snapshot.setUser(profile.getUser());
        snapshot.setWeightKg(profile.getWeightKg());
        snapshot.setPaceAvgSeg(profile.getPaceAvgSeg());
        snapshot.setConditioningLevel(profile.getConditioningLevel());

        repository.save(snapshot);
    }
}
