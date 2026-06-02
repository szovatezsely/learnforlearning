package com.learnforlearning.config;

import com.learnforlearning.domain.Grade;
import com.learnforlearning.domain.Specialization;
import com.learnforlearning.domain.Subject;
import com.learnforlearning.domain.Teacher;
import com.learnforlearning.domain.TeacherAssignment;
import com.learnforlearning.domain.User;
import com.learnforlearning.repository.SubjectRepository;
import com.learnforlearning.repository.TeacherRepository;
import com.learnforlearning.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * Seeds a small, deterministic data set on first start so the app — and especially
 * the recommender — has something to work with out of the box. Disabled in the
 * {@code prod} profile and skipped if any subjects already exist.
 *
 * <p>Default admin login: {@code admin@lfl.hu} / {@code password}.
 */
@Component
@ConditionalOnProperty(name = "app.seed-on-startup", havingValue = "true")
public class DataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final UserRepository userRepository;
    private final SubjectRepository subjectRepository;
    private final TeacherRepository teacherRepository;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(UserRepository userRepository,
                      SubjectRepository subjectRepository,
                      TeacherRepository teacherRepository,
                      PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.subjectRepository = subjectRepository;
        this.teacherRepository = teacherRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (subjectRepository.count() > 0) {
            return;
        }
        log.info("Seeding sample data (subjects, teachers, students)...");

        List<Subject> subjects = seedSubjects();
        seedTeachers(subjects);
        seedAdmin();
        seedStudents(subjects);

        log.info("Seeding complete: {} subjects, {} teachers, {} users.",
                subjectRepository.count(), teacherRepository.count(), userRepository.count());
    }

    private List<Subject> seedSubjects() {
        List<Subject> subjects = new ArrayList<>();
        // name, code, credits, evenSemester, existsA/B/C, optionalA/B/C, url
        subjects.add(subject("Adatbázisok 1 Ea", "IP-18AB1E", 2, true,
                true, true, true, false, false, false));
        subjects.add(subject("Programozás", "IP-18PROGEG", 5, false,
                true, true, true, false, false, false));
        subjects.add(subject("Diszkrét matematika", "IP-18DMTEG", 4, false,
                true, true, true, false, false, false));
        // Track A electives
        subjects.add(subject("Mesterséges intelligencia", "IP-18MIEG", 3, true,
                true, false, false, true, false, false));
        subjects.add(subject("Számítógépes grafika", "IP-18SGEG", 3, false,
                true, false, false, true, false, false));
        subjects.add(subject("Mobilfejlesztés", "IP-18MOBEG", 3, true,
                true, false, false, true, false, false));
        // Track B electives
        subjects.add(subject("Webfejlesztés", "IP-18WEBEG", 3, true,
                false, true, false, false, true, false));
        subjects.add(subject("Hálózati architektúrák", "IP-18HALEG", 3, false,
                false, true, false, false, true, false));
        subjects.add(subject("Operációs rendszerek 2", "IP-18OR2EG", 3, true,
                false, true, false, false, true, false));
        // Track C electives
        subjects.add(subject("Adattudomány", "IP-18ADTEG", 3, true,
                false, false, true, false, false, true));
        subjects.add(subject("Kriptográfia", "IP-18KRIPTEG", 3, false,
                false, false, true, false, false, true));
        subjects.add(subject("Funkcionális programozás", "IP-18FUNKEG", 3, true,
                false, false, true, false, false, true));

        return subjectRepository.saveAll(subjects);
    }

    private Subject subject(String name, String code, int credits, boolean even,
                            boolean a, boolean b, boolean c,
                            boolean optA, boolean optB, boolean optC) {
        return new Subject(name, code, credits, even, a, b, c, optA, optB, optC,
                "https://www.inf.elte.hu/", true);
    }

    private void seedTeachers(List<Subject> subjects) {
        String[] names = {
                "Vincellér Zoltán", "Szalai-Gindl János", "Brányi László", "Hajas Csilla",
                "Vörös Péter", "Nikovits Tibor", "Bokros Ferenc", "Lehotay-Kéry Péter"
        };
        for (int i = 0; i < names.length; i++) {
            Teacher teacher = new Teacher(names[i], true);
            // Assign each teacher to two subjects, round-robin.
            Subject first = subjects.get(i % subjects.size());
            Subject second = subjects.get((i + 3) % subjects.size());
            teacher.getAssignments().add(new TeacherAssignment(first, teacher, true));
            if (second != first) {
                teacher.getAssignments().add(new TeacherAssignment(second, teacher, true));
            }
            teacherRepository.save(teacher);
        }
    }

    private void seedAdmin() {
        User admin = new User("Adminisztrátor", "admin@lfl.hu",
                passwordEncoder.encode("password"), Specialization.A, true);
        userRepository.save(admin);
    }

    /**
     * Creates students whose elective grades carry a deliberate signal: "good selectors"
     * score well in electives, "bad selectors" poorly, so the booster has structure to find.
     */
    private void seedStudents(List<Subject> subjects) {
        Specialization[] tracks = {Specialization.A, Specialization.B, Specialization.C};
        List<Subject> mandatory = subjects.stream().filter(s -> !anyOptional(s)).toList();

        int studentIndex = 0;
        for (Specialization track : tracks) {
            List<Subject> electives = subjects.stream().filter(s -> s.isOptionalOn(track)).toList();
            for (int i = 0; i < 6; i++, studentIndex++) {
                boolean goodSelector = i % 2 == 0;
                User student = new User(
                        "Hallgató " + (studentIndex + 1),
                        "hallgato" + (studentIndex + 1) + "@lfl.hu",
                        passwordEncoder.encode("password"),
                        track, false);

                // Mandatory grades hover around 3 (the overall-average anchor).
                for (Subject m : mandatory) {
                    student.getGrades().add(new Grade(student, m, 3));
                }
                // Electives: good selectors take the first electives and ace them; bad ones fail.
                int taken = 0;
                for (Subject e : electives) {
                    if (taken++ >= 2) {
                        break;
                    }
                    int grade = goodSelector ? 5 : 2;
                    student.getGrades().add(new Grade(student, e, grade));
                }
                userRepository.save(student);
            }
        }
    }

    private static boolean anyOptional(Subject s) {
        return s.isOptionalOnA() || s.isOptionalOnB() || s.isOptionalOnC();
    }
}
