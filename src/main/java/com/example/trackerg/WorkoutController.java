package com.example.trackerg;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Controller
public class WorkoutController {

    private final WorkoutService service;

    public WorkoutController(WorkoutService service) {
        this.service = service;
    }

    @GetMapping("/")
    public String home() {
        return "redirect:/workouts";
    }

    @GetMapping("/workouts")
    public String workouts(@RequestParam(value="query", required=false) String query,
                           @RequestParam(value="sort", required=false) String sort,
                           Model model,
                           HttpSession session) {

        String username = (String) session.getAttribute("username");
        if (username == null) return "redirect:/login";

        model.addAttribute("workouts", service.listWorkouts(query, sort, null, null));
        model.addAttribute("query", query == null ? "" : query);
        model.addAttribute("sort", sort == null ? "" : sort);
        model.addAttribute("username", username);
        return "workouts";
    }

    @GetMapping("/workouts/new")
    public String newWorkout(Model model) {
        WorkoutForm form = new WorkoutForm();
        model.addAttribute("form", form);
        model.addAttribute("mode", "create");
        return "workout_form";
    }

    @PostMapping("/workouts")
    public String create(@ModelAttribute("form") WorkoutForm form, HttpSession session) {
        String username = (String) session.getAttribute("username");
        if (username == null) return "redirect:/login";

        service.createFromForm(username, form);
        return "redirect:/workouts";
    }

    @GetMapping("/workouts/edit/{id}")
    public String edit(@PathVariable int id, Model model, HttpSession session) {
        String username = (String) session.getAttribute("username");
        if (username == null) return "redirect:/login";

        Workout w = service.getWorkout(id);
        List<Interval> intervalRows = service.getIntervalsForWorkout(id);

        WorkoutForm form = new WorkoutForm();
        form.setId(w.getId());
        form.setDate(w.getDate());
        form.setInterval(w.isInterval());
        form.setFavorite(w.isFavorite());
        form.setDistanceMeters(w.getDistanceMeters());
        form.setStrokeRate(w.getStrokeRate());
        form.setNotes(w.getNotes());

        int total = w.getTimeSeconds();
        form.setTimeMinutes(total / 60);
        form.setTimeSeconds(total % 60);

        model.addAttribute("intervalData", intervalRows);
        model.addAttribute("form", form);
        model.addAttribute("mode", "edit");
        return "workout_form";
    }

    @PostMapping("/workouts/update")
    public String update(@ModelAttribute("form") WorkoutForm form, HttpSession session) {
        String username = (String) session.getAttribute("username");
        if (username == null) return "redirect:/login";

        service.updateFromForm(username, form);
        return "redirect:/workouts";
    }

    @GetMapping("/workouts/delete/{id}")
    public String delete(@PathVariable int id, HttpSession session) {
        String username = (String) session.getAttribute("username");
        if (username == null) return "redirect:/login";

        service.deleteWorkout(username, id);
        return "redirect:/workouts";
    }

    @GetMapping("/prs")
    public String prs(Model model, HttpSession session) {
        String username = (String) session.getAttribute("username");
        if (username == null) return "redirect:/login";

        Map<String, Workout> best = service.bestPRs(username);
        model.addAttribute("best", best);
        return "prs";
    }
}