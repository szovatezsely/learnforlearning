package com.learnforlearning.service;

import com.learnforlearning.web.view.CommentView;
import java.util.List;

/** Aggregated figures for the landing page. */
public record HomeStats(long userCount, long gradeCount, List<CommentView> recentComments, String bestTeacher) {
}
