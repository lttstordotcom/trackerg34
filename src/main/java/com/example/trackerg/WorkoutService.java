package com.example.trackerg.service;

import com.example.trackerg.model.Workout;
import com.example.trackerg.repository.WorkoutFileRepository;

import java.util.ArrayList;
import java.util.List;

public class WorkoutService {

    private final WorkoutFileRepository repository;

    public WorkoutService(WorkoutFileRepository repository) {
        this.repository = repository;
    }

    // LIST WORKOUTS (MULTI-USER)
    public List<Workout> listWorkouts(String username, String query, String sortKey) {

        // 1. Load ALL workouts from CSV
        List<Workout> all = repository.loadAll();

        // 2. Filter by username
        List<Workout> userWorkouts = new ArrayList<>();
        for (Workout w : all) {
            if (username.equals(w.getUsername())) {
                userWorkouts.add(w);
            }
        }

        // 3. Apply search filter
        List<Workout> filtered = filter(userWorkouts, query);

        // 4. Apply sorting
        sort(filtered, sortKey);

        return filtered;
    }
    // CREATE WORKOUT
    public void addWorkout(String username, Workout workout) {

        workout.setUsername(username);

        List<Workout> all = repository.loadAll();
        all.add(workout);

        repository.saveAll(all);
    }

    // DELETE WORKOUT
    public void deleteWorkout(String username, int id) {

        List<Workout> all = repository.loadAll();
        List<Workout> updated = new ArrayList<>();

        for (Workout w : all) {
            if (w.getId() == id && username.equals(w.getUsername())) {
                continue; // skip (delete)
            }
            updated.add(w);
        }

        repository.saveAll(updated);
    }

    // GET WORKOUT 
    public Workout getWorkoutForUser(String username, int id) {

        List<Workout> all = repository.loadAll();

        for (Workout w : all) {
            if (w.getId() == id && username.equals(w.getUsername())) {
                return w;
            }
        }

        return null;
    }
    // SEARCH FILTER
    private List<Workout> filter(List<Workout> workouts, String query) {

        if (query == null || query.isBlank()) {
            return workouts;
        }

        List<Workout> result = new ArrayList<>();
        String lower = query.toLowerCase();

        for (Workout w : workouts) {
            if (w.getNotes() != null &&
                w.getNotes().toLowerCase().contains(lower)) {
                result.add(w);
            }
        }

        return result;
    }

    // SORTING
    private void sort(List<Workout> workouts, String sortKey) {

        if (sortKey == null) return;

        // Sort by watts (descending)
        if (sortKey.equals("watts")) {

            for (int i = 0; i < workouts.size(); i++) {
                int best = i;
                for (int j = i + 1; j < workouts.size(); j++) {
                    if (workouts.get(j).getWatts() >
                        workouts.get(best).getWatts()) {
                        best = j;
                    }
                }

                Workout temp = workouts.get(i);
                workouts.set(i, workouts.get(best));
                workouts.set(best, temp);
            }
        }

        // Sort by date (newest first)
        if (sortKey.equals("date")) {

            for (int i = 0; i < workouts.size(); i++) {
                int best = i;
                for (int j = i + 1; j < workouts.size(); j++) {
                    if (workouts.get(j).getDate()
                        .isAfter(workouts.get(best).getDate())) {
                        best = j;
                    }
                }

                Workout temp = workouts.get(i);
                workouts.set(i, workouts.get(best));
                workouts.set(best, temp);
            }
        }
    }
}