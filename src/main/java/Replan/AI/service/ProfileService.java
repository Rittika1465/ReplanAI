package Replan.AI.service;

import Replan.AI.model.UserProfile;
import Replan.AI.repository.UserProfileRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ProfileService {
    private final UserProfileRepository profileRepository;

    public ProfileService(UserProfileRepository profileRepository){
        this.profileRepository = profileRepository;
    }

    public UserProfile createProfile(UserProfile profile){
        validatePreferredTime(profile);

        profile.setId(null);
        profile.prepareForCreate();

        return profileRepository.save(profile);
    }

    public UserProfile getProfileById(String id){
        return profileRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Profile not found"
                ));
    }

    public UserProfile updateProfile(String id, UserProfile updatedProfile){
        validatePreferredTime(updatedProfile);

        UserProfile existingProfile = getProfileById(id);

        existingProfile.setName(updatedProfile.getName());
        existingProfile.setRole(updatedProfile.getRole());

        existingProfile.setAvailableDays(updatedProfile.getAvailableDays());
        existingProfile.setPreferredStartTime(
                updatedProfile.getPreferredStartTime()
        );
        existingProfile.setPreferredEndTime(
                updatedProfile.getPreferredEndTime()
        );
        existingProfile.setMaximumDailyMinutes(
                updatedProfile.getMaximumDailyMinutes()
        );

        existingProfile.prepareForUpdate();

        return profileRepository.save(existingProfile);

    }

    private void validatePreferredTime(UserProfile profile){
        if(profile.getPreferredStartTime() != null
            && profile.getPreferredEndTime() != null
            && !profile.getPreferredStartTime().isBefore(profile.getPreferredEndTime())) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Preferred start time must be before end time");

        }
    }
}
