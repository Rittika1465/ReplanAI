package Replan.AI.controller;

import Replan.AI.model.UserProfile;
import Replan.AI.service.ProfileService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/profile")
public class ProfileController {
    private final ProfileService profileService;

    public ProfileController(ProfileService profileService) {
        this.profileService = profileService;
    }
     @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserProfile createProfile(@Valid @RequestBody UserProfile profile){
        return profileService.createProfile(profile);
    }

    @GetMapping("/{id}")
    public UserProfile getProfile(@PathVariable String id){
        return profileService.getProfileById(id);
    }

    @PutMapping("/{id}")
    public UserProfile updatedProfile(@PathVariable String id, @Valid @RequestBody UserProfile profile){
        return profileService.updateProfile(id, profile);
    }
}
