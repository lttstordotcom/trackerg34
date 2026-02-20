package com.example.trackerg;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WorkoutService {

    private final WorkoutFileRepository repo;
    private List<Workout> workouts = new ArrayList<>();
    private List<Interval> intervals = new ArrayList<>();

    public WorkoutService(WorkoutFileRepository repo) {
        this.repo = repo;
        reloadFromFiles();
    }

    private void reloadFromFiles() {
        this.workouts = repo.loadWorkouts();
        this.intervals = repo.loadIntervals();
    }

    private void saveToFiles() {
        repo.saveWorkouts(workouts);
        repo.saveIntervals(intervals);
    }

    public List<Workout> listWorkouts(String query, String sortKey, String sort) {
        return listWorkoutsForUser(null, query, sortKey, sort);
    }

    public void createFromForm(WorkoutForm form) {
        createFromFormForUser(null, form);

    }

    public Workout getWorkout(int id) {
        return getWorkoutForUser(null, id);
    }

    public List<Interval> getIntervalsForWorkout(int workoutId) {
        return getIntervalsForWorkoutForUser(null, workoutId);
    }

    public void updateFromForm(WorkoutForm form) {
        updateFromFormForUser(null, form);
    }

    public void deleteWorkout(int id) {
        deleteWorkoutForUser(null, id);
    }

    public Map<String, Workout> bestPRs() {
        return bestPRsForUser(null);
    }

    public List<Workout> listWorkoutsForUser(String username, String query, String sortKey, String sort) {
        reloadFromFiles();
        List<Workout> base = filterByUsername(workouts, username);
        List<Workout> filtered = filter(base, query);
        sort(filtered, sortKey);
        return filtered;
    }

    public void createFromFormForUser(String username, WorkoutForm form) {
        reloadFromFiles();

        Workout w = formToWorkout(form);
				w.setUsername("test");
        if (username != null && !username.isBlank()) {
            w.setUsername(username);
        }

        int nextId = 1;
        for (Workout existing : workouts) {
            if (existing.getId() >= nextId) nextId = existing.getId() + 1;
        }
        w.setId(nextId);

        workouts.add(w);

		if (form.getIntervals() != null) {
    		int idx = 0;
    		for (IntervalForm inf : form.getIntervals()) {
        		if (inf == null) continue; 

        			Interval in = intervalFromForm(inf);
        			in.setWorkoutId(nextId);
        			in.setIndex(idx++);

        			intervals.add(in);
			}		
		}

        saveToFiles();
    }

    public Workout getWorkoutForUser(String username, int id) {
        reloadFromFiles();
        for (Workout w : workouts) {
            if (w.getId() == id && owns(username, w)) {
                return w;
            }
        }
        return null;
    }

    public List<Interval> getIntervalsForWorkoutForUser(String username, int workoutId) {
        reloadFromFiles();

        Workout w = getWorkoutForUser(username, workoutId);
        if (w == null) return new ArrayList<>();

        List<Interval> out = new ArrayList<>();
        for (Interval in : intervals) {
            if (in.getWorkoutId() == workoutId) out.add(in);
        }
        return out;
    }

    public void updateFromFormForUser(String username, WorkoutForm form) {
        reloadFromFiles();

        int id = form.getId();
        Workout existing = getWorkoutForUser(username, id);
        if (existing == null) return;

        Workout updated = formToWorkout(form);
        updated.setId(id);
        updated.setUsername(existing.getUsername());

        for (int i = 0; i < workouts.size(); i++) {
            if (workouts.get(i).getId() == id) {
                workouts.set(i, updated);
                break;
            }
        }

        List<Interval> kept = new ArrayList<>();
        for (Interval in : intervals) {
            if (in.getWorkoutId() != id) kept.add(in);
        }
        intervals = kept;
		
		if (form.getIntervals() != null) {
    		int idx = 0;
    		for (IntervalForm inf : form.getIntervals()) {
        			if (inf == null) continue;

        				Interval in = intervalFromForm(inf);
        				in.setWorkoutId(id);
        				in.setIndex(idx++);

        				intervals.add(in);
    		}
		}
        saveToFiles();
    }

    public void deleteWorkoutForUser(String username, int id) {
        reloadFromFiles();

        Workout existing = getWorkoutForUser(username, id);
        if (existing == null) return;

        List<Workout> keptW = new ArrayList<>();
        for (Workout w : workouts) {
            if (w.getId() != id) keptW.add(w);
        }
        workouts = keptW;

        List<Interval> keptI = new ArrayList<>();
        for (Interval in : intervals) {
            if (in.getWorkoutId() != id) keptI.add(in);
        }
        intervals = keptI;

        saveToFiles();
    }

    public Map<String, Workout> bestPRsForUser(String username) {
        reloadFromFiles();

        List<Workout> base = filterByUsername(workouts, username);
        Map<String, Workout> best = new HashMap<>();

        for (Workout w : base) {
            String key;

            int d = w.getDistanceMeters();
            if (d == 2000) key = "2k";
            else if (d == 5000) key = "5k";
            else if (d == 6000) key = "6k";
            else key = "Other";

            Workout cur = best.get(key);
            if (cur == null || w.getWatts() > cur.getWatts()) {
                best.put(key, w);
            }
        }

        return best;
    }

    private boolean owns(String username, Workout w) {
        if (username == null || username.isBlank()) return true;
        if (w.getUsername() == null) return false;
        return username.equals(w.getUsername());
    }

    private List<Workout> filterByUsername(List<Workout> list, String username) {
        if (username == null || username.isBlank()) return list;

        List<Workout> out = new ArrayList<>();
        for (Workout w : list) {
            if (w.getUsername() != null && username.equals(w.getUsername())) {
                out.add(w);
            }
        }
        return out;
    }

    private List<Workout> filter(List<Workout> list, String query) {
        if (query == null || query.isBlank()) return list;

        String q = query.toLowerCase();
        List<Workout> out = new ArrayList<>();

        for (Workout w : list) {
            String notes = w.getNotes() == null ? "" : w.getNotes().toLowerCase();
            String date = w.getDate() == null ? "" : w.getDate().toString().toLowerCase();

            if (notes.contains(q) || date.contains(q)) {
                out.add(w);
            }
        }

        return out;
    }

    private void sort(List<Workout> list, String sortKey) {
        if (sortKey == null) return;

        if (sortKey.equals("watts")) {
            for (int i = 0; i < list.size(); i++) {
                int best = i;
                for (int j = i + 1; j < list.size(); j++) {
                    if (list.get(j).getWatts() > list.get(best).getWatts()) best = j;
                }
                Workout tmp = list.get(i);
                list.set(i, list.get(best));
                list.set(best, tmp);
            }
        }

        if (sortKey.equals("date")) {
            for (int i = 0; i < list.size(); i++) {
                int best = i;
                for (int j = i + 1; j < list.size(); j++) {
                    if (list.get(j).getDate().isAfter(list.get(best).getDate())) best = j;
                }
                Workout tmp = list.get(i);
                list.set(i, list.get(best));
                list.set(best, tmp);
            }
        }

        if (sortKey.equals("distance")) {
            for (int i = 0; i < list.size(); i++) {
                int best = i;
                for (int j = i + 1; j < list.size(); j++) {
                    if (list.get(j).getDistanceMeters() > list.get(best).getDistanceMeters()) best = j;
                }
                Workout tmp = list.get(i);
                list.set(i, list.get(best));
                list.set(best, tmp);
            }
        }
    }

    private Workout formToWorkout(WorkoutForm form) {
        Workout w = new Workout();

        w.setDate(form.getDate());
        w.setDistanceMeters(form.getDistanceMeters());

        int timeSeconds = form.getTimeMin() * 60 + form.getTimeSec();
        w.setTimeSeconds(timeSeconds);

        w.setStrokeRate(form.getStrokeRate());
        w.setNotes(form.getNotes());
        w.setFavorite(form.isFavorite());
        w.setInterval(form.isIntervalWorkout());

        if (w.getDistanceMeters() > 0 && w.getTimeSeconds() > 0) {
            double splitSeconds = (w.getTimeSeconds() * 500.0) / w.getDistanceMeters();
            w.setSplitSeconds(splitSeconds);

            double pacePerMeter = splitSeconds / 500.0;
            double watts = 2.8 / Math.pow(pacePerMeter, 3);
            w.setWatts(watts);
        }

        return w;
    }
private Interval intervalFromForm(IntervalForm inf) {
    Interval in = new Interval();

    in.setWorkDistanceMeters(inf.getWorkDistanceMeters());

    int workSeconds = inf.getWorkMin() * 60 + inf.getWorkSec();
    in.setWorkTimeSeconds(workSeconds);

    int restSeconds = inf.getRestMin() * 60 + inf.getRestSec();
    in.setRestTimeSeconds(restSeconds);

    return in;
}
}