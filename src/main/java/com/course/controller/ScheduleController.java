package com.course.controller;

import com.course.entity.ClassSchedule;
import com.course.repository.ClassScheduleRepository;
import com.course.repository.RoomRepository;
import com.course.entity.Room;
import com.course.entity.CourseOffering;
import com.course.repository.CourseOfferingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/schedules")
public class ScheduleController {

    @Autowired
    private ClassScheduleRepository classScheduleRepository;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private CourseOfferingRepository courseOfferingRepository;

    // Get all schedules (optionally by offering)
    @GetMapping
    public List<ClassSchedule> getAll(@RequestParam(required = false) Long offeringId) {
        if (offeringId != null) {
            return classScheduleRepository.findByOfferingId(offeringId);
        }
        return classScheduleRepository.findAll();
    }

    // Get a single schedule
    @GetMapping("/{id}")
    public ResponseEntity<ClassSchedule> getById(@PathVariable Long id) {
        return classScheduleRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Create a new schedule
    @PostMapping
    public ResponseEntity<ClassSchedule> create(@RequestBody ClassSchedule schedule) {
        // Validate offering and room
        if (schedule.getOffering() == null || schedule.getRoom() == null) {
            return ResponseEntity.badRequest().build();
        }
        Optional<CourseOffering> offering = courseOfferingRepository.findById(schedule.getOffering().getId());
        Optional<Room> room = roomRepository.findById(schedule.getRoom().getId());
        if (offering.isEmpty() || room.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        schedule.setOffering(offering.get());
        schedule.setRoom(room.get());
        return ResponseEntity.ok(classScheduleRepository.save(schedule));
    }

    // Update a schedule
    @PutMapping("/{id}")
    public ResponseEntity<ClassSchedule> update(@PathVariable Long id, @RequestBody ClassSchedule schedule) {
        return classScheduleRepository.findById(id)
                .map(existing -> {
                    if (schedule.getOffering() != null) {
                        Optional<CourseOffering> offering = courseOfferingRepository
                                .findById(schedule.getOffering().getId());
                        offering.ifPresent(existing::setOffering);
                    }
                    if (schedule.getRoom() != null) {
                        Optional<Room> room = roomRepository.findById(schedule.getRoom().getId());
                        room.ifPresent(existing::setRoom);
                    }
                    if (schedule.getDayOfWeek() != null)
                        existing.setDayOfWeek(schedule.getDayOfWeek());
                    if (schedule.getStartTime() != null)
                        existing.setStartTime(schedule.getStartTime());
                    if (schedule.getEndTime() != null)
                        existing.setEndTime(schedule.getEndTime());
                    return ResponseEntity.ok(classScheduleRepository.save(existing));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // Delete a schedule
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (!classScheduleRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        classScheduleRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
