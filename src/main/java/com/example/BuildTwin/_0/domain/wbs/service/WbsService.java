package com.example.BuildTwin._0.domain.wbs.service;

import com.example.BuildTwin._0.domain.wbs.model.Activity;
import com.example.BuildTwin._0.domain.wbs.model.ActivityDependency;

import java.util.List;

public interface WbsService {
    Activity createActivity(Activity activity);
    List<Activity> getActivitiesByProject(Long projectId);
    List<Activity> getLookaheadActivities(Long projectId, int days);
    ActivityDependency addDependency(ActivityDependency dependency);
}
