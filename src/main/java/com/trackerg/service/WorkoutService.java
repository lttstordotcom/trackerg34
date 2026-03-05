package com.trackerg.service;

import com.trackerg.model.Workout;
import com.trackerg.repository.WorkoutRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class WorkoutService {

    private final WorkoutRepository workoutRepository;

    public WorkoutService(WorkoutRepository workoutRepository) {
        this.workoutRepository = workoutRepository;
    }

    public List<Workout> getAllWorkouts() {
        return workoutRepository.findAll();
    }

    public void saveWorkout(Workout workout) {
        // save a workout to the database
        workoutRepository.save(workout);
    }
}
