package com.example.BuildTwin._0.domain.wbs.service;

import com.example.BuildTwin._0.domain.wbs.model.Activity;
import com.example.BuildTwin._0.domain.wbs.model.ActivityDependency;
import com.example.BuildTwin._0.domain.wbs.repository.ActivityDependencyRepository;
import com.example.BuildTwin._0.domain.wbs.repository.ActivityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class WbsServiceImpl implements WbsService {

    private final ActivityRepository activityRepository;
    private final ActivityDependencyRepository dependencyRepository;

    @Override
    public Activity createActivity(Activity activity) {
        if (activity.getStatus() == null) {
            activity.setStatus("PLANNED");
        }
        return activityRepository.save(activity);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Activity> getActivitiesByProject(Long projectId) {
        return activityRepository.findByProjectId(projectId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Activity> getLookaheadActivities(Long projectId, int days) {
        LocalDate today = LocalDate.now();
        LocalDate target = today.plusDays(days);
        return activityRepository.findLookaheadActivities(projectId, today, target);
    }

    @Override
    public ActivityDependency addDependency(ActivityDependency dependency) {
        return dependencyRepository.save(dependency);
    }
}
