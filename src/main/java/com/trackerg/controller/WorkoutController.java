package com.trackerg.controller;

import com.trackerg.model.Workout;
import com.trackerg.service.WorkoutService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class WorkoutController {

    private final WorkoutService workoutService;

    public WorkoutController(WorkoutService workoutService) {
        this.workoutService = workoutService;
    }

    @GetMapping("/")
    public String index() {
        // this is just the main page
        return "index";
    }

    @GetMapping("/workouts")
    public String workouts(Model model) {
        // show all saved workouts
        model.addAttribute("workouts", workoutService.getAllWorkouts());
        return "workouts";
    }

    @GetMapping("/workouts/new")
    public String addWorkoutPage(Model model) {
        // render the add workout form
        model.addAttribute("workout", new Workout());
        return "addWorkout";
    }

    @PostMapping("/workouts")
    public String addWorkout(@ModelAttribute Workout workout) {
        workoutService.saveWorkout(workout);
        return "redirect:/workouts";
    }
}
