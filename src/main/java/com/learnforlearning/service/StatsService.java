package com.learnforlearning.service;

import com.learnforlearning.domain.Teacher;
import com.learnforlearning.domain.TeacherVote;
import com.learnforlearning.repository.GradeRepository;
import com.learnforlearning.repository.TeacherRepository;
import com.learnforlearning.repository.TeacherVoteRepository;
import com.learnforlearning.repository.UserRepository;
import com.learnforlearning.web.view.CommentView;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
public class StatsService {

    private static final int RECENT_COMMENT_LIMIT = 5;

    private final UserRepository userRepository;
    private final GradeRepository gradeRepository;
    private final TeacherRepository teacherRepository;
    private final TeacherVoteRepository voteRepository;

    public StatsService(UserRepository userRepository,
                        GradeRepository gradeRepository,
                        TeacherRepository teacherRepository,
                        TeacherVoteRepository voteRepository) {
        this.userRepository = userRepository;
        this.gradeRepository = gradeRepository;
        this.teacherRepository = teacherRepository;
        this.voteRepository = voteRepository;
    }

    @Transactional(readOnly = true)
    public HomeStats forHomePage() {
        long userCount = userRepository.count();
        long gradeCount = gradeRepository.count();

        List<CommentView> recent = voteRepository.findRecentComments().stream()
                .limit(RECENT_COMMENT_LIMIT)
                .map(v -> new CommentView(v.getUser().getName(), v.getComment(), v.getPositiveVote()))
                .toList();

        String bestTeacher = teacherRepository.findByAcceptedTrue().stream()
                .max(Comparator.comparingInt(Teacher::score))
                .filter(t -> t.score() > 0)
                .map(Teacher::getName)
                .orElse("—");

        return new HomeStats(userCount, gradeCount, recent, bestTeacher);
    }
}
