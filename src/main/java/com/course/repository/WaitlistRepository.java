package com.course.repository;

import com.course.entity.Waitlist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WaitlistRepository extends JpaRepository<Waitlist, Long> {
    List<Waitlist> findByStudentIdOrderByPositionAsc(Long studentId);

    List<Waitlist> findByOfferingIdOrderByPositionAsc(Long offeringId);
}
