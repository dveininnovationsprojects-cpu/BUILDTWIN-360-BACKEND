package com.example.BuildTwin._0.model;

/**
 * Categorization of activities relative to the Look-Ahead planning time window.
 * Supports standard weekly work plans as well as 14-Day (Two-Week / Sprint) Lookahead schedules.
 */
public enum LookaheadCategory {
    STARTING_THIS_WEEK,  // Activity scheduled to commence within the look-ahead window
    FINISHING_THIS_WEEK, // Activity scheduled to complete within the look-ahead window
    ONGOING,             // Activity started before or at the start and active across the window
    OVERDUE,             // Activity was scheduled earlier, still not completed (backlog/slippage)
    UPCOMING,            // Scheduled to start in the later portion of the window
    STARTING_WEEK_1,     // Commencing in Week 1 of 14-day lookahead
    STARTING_WEEK_2,     // Commencing in Week 2 of 14-day lookahead
    FINISHING_WEEK_1,    // Finishing in Week 1 of 14-day lookahead
    FINISHING_WEEK_2,    // Finishing in Week 2 of 14-day lookahead
    SPANNING_BOTH_WEEKS  // Active across both Week 1 and Week 2 of 14-day lookahead
}
