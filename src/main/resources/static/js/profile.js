const profileSetup =
    document.getElementById("profileSetup");

const profileSummary =
    document.getElementById("profileSummary");

const setupStep =
    document.getElementById("setupStep");

const setupQuestion =
    document.getElementById("setupQuestion");

const setupAnswer =
    document.getElementById("setupAnswer");

const setupNextButton =
    document.getElementById("setupNextButton");

const setupBackButton =
    document.getElementById("setupBackButton");

const profileStatus =
    document.getElementById("profileStatus");

const editProfileButton =
    document.getElementById("editProfileButton");


let currentStep = 0;

let profileData = {
    name: "",
    role: "STUDENT",
    availableDays: [],
    preferredStartTime: "",
    preferredEndTime: "",
    maximumDailyMinutes: 60
};


const questions = [
    "Hi! What should I call you?",
    "Are you a student or a working professional?",
    "On which days are you usually available?",
    "From what time are you usually free?",
    "Until what time are you usually free?",
    "How many minutes can you realistically give each day?"
];


setupNextButton.addEventListener("click", async () => {
    const isValid = saveCurrentAnswer();

    if (!isValid) {
        return;
    }

    if (currentStep === questions.length - 1) {
        await saveProfile();
        return;
    }

    currentStep++;
    renderStep();
});


setupBackButton.addEventListener("click", () => {
    if (currentStep === 0) {
        return;
    }

    currentStep--;
    renderStep();
});


editProfileButton.addEventListener("click", () => {
    profileSummary.hidden = true;
    profileSetup.hidden = false;

    currentStep = 0;
    renderStep();
});


function renderStep() {
    setupStep.textContent =
        `Step ${currentStep + 1} of ${questions.length}`;

    setupQuestion.textContent =
        questions[currentStep];

    setupBackButton.hidden =
        currentStep === 0;

    setupNextButton.textContent =
        currentStep === questions.length - 1
            ? "Save Profile"
            : "Next";

    if (currentStep === 0) {
        setupAnswer.innerHTML = `
            <input id="setupInput"
                   type="text"
                   placeholder="Enter your name"
                   value="${profileData.name}">
        `;
    }

    else if (currentStep === 1) {
        setupAnswer.innerHTML = `
            <select id="setupInput">
                <option value="STUDENT">
                    Student
                </option>

                <option value="WORKING_PROFESSIONAL">
                    Working Professional
                </option>
            </select>
        `;

        document.getElementById("setupInput").value =
            profileData.role;
    }



    else if (currentStep === 2) {
        const days = [
            ["MONDAY", "Mon"],
            ["TUESDAY", "Tue"],
            ["WEDNESDAY", "Wed"],
            ["THURSDAY", "Thu"],
            ["FRIDAY", "Fri"],
            ["SATURDAY", "Sat"],
            ["SUNDAY", "Sun"]
        ];

        setupAnswer.innerHTML = `
            <div class="setup-day-options">
                ${days.map(day => `
                    <label>
                        <input type="checkbox"
                               name="setupDays"
                               value="${day[0]}"
                               ${profileData.availableDays
                                   .includes(day[0])
                                   ? "checked"
                                   : ""}>
                        ${day[1]}
                    </label>
                `).join("")}
            </div>
        `;
    }

    else if (currentStep === 3) {
        setupAnswer.innerHTML = `
            <input id="setupInput"
                   type="time"
                   value="${profileData.preferredStartTime}">
        `;
    }

    else if (currentStep === 4) {
        setupAnswer.innerHTML = `
            <input id="setupInput"
                   type="time"
                   value="${profileData.preferredEndTime}">
        `;
    }

    else if (currentStep === 5) {
        setupAnswer.innerHTML = `
            <input id="setupInput"
                   type="number"
                   min="1"
                   max="1440"
                   placeholder="Example: 120"
                   value="${profileData.maximumDailyMinutes}">
        `;
    }
}


function saveCurrentAnswer() {
    profileStatus.textContent =
        "RePlan will create your scheduling profile from these answers.";

    profileStatus.style.color = "#68738a";

    /*
     * Step 3: Available days
     */
    if (currentStep === 2) {
        const selectedDays = Array.from(
            document.querySelectorAll(
                'input[name="setupDays"]:checked'
            )
        ).map(checkbox => checkbox.value);

        if (selectedDays.length === 0) {
            showError(
                "Please select at least one available day."
            );

            return false;
        }

        profileData.availableDays = selectedDays;

        return true;
    }

    const input =
        document.getElementById("setupInput");

    if (!input) {
        showError("Unable to read this answer.");
        return false;
    }

    const value = input.value.trim();

    if (!value) {
        showError("Please answer this question.");
        return false;
    }

    /*
     * Step 1: Name
     */
    if (currentStep === 0) {
        profileData.name = value;
    }

    /*
     * Step 2: Role
     */
    else if (currentStep === 1) {
        profileData.role = value;
    }

    /*
     * Step 4: Start time
     */
    else if (currentStep === 3) {
        profileData.preferredStartTime = value;
    }

    /*
     * Step 5: End time
     */
    else if (currentStep === 4) {
        profileData.preferredEndTime = value;

        if (
            profileData.preferredStartTime
            >= profileData.preferredEndTime
        ) {
            showError(
                "End time must be after start time."
            );

            return false;
        }
    }

    /*
     * Step 6: Maximum daily minutes
     */
    else if (currentStep === 5) {
        profileData.maximumDailyMinutes =
            Number(value);

        if (
            profileData.maximumDailyMinutes < 1
            || profileData.maximumDailyMinutes > 1440
        ) {
            showError(
                "Daily time must be between 1 and 1440 minutes."
            );

            return false;
        }
    }

    return true;
}

async function saveProfile() {
    const savedUserId =
        localStorage.getItem("replanUserId");

    const url = savedUserId
        ? `/api/profile/${savedUserId}`
        : "/api/profile";

    const method =
        savedUserId ? "PUT" : "POST";

    try {
        setupNextButton.disabled = true;
        setupNextButton.textContent = "Saving...";

        const response = await fetch(url, {
            method: method,

            headers: {
                "Content-Type": "application/json"
            },

            body: JSON.stringify(profileData)
        });

        if (!response.ok) {
            const errorData = await response.json();

            throw new Error(
                errorData.message
                || "Unable to save profile"
            );
        }

        const savedProfile =
            await response.json();

        localStorage.setItem(
            "replanUserId",
            savedProfile.id
        );

        profileData = savedProfile;

        showProfileSummary();

    } catch (error) {
        showError(error.message);

        setupNextButton.disabled = false;
        setupNextButton.textContent = "Save Profile";
    }
}


async function loadSavedProfile() {
    const userId =
        localStorage.getItem("replanUserId");

    if (!userId) {
        renderStep();
        return;
    }

    try {
        const response = await fetch(
            `/api/profile/${userId}`
        );

        if (response.status === 404) {
            localStorage.removeItem("replanUserId");
            renderStep();
            return;
        }

        if (!response.ok) {
            throw new Error("Unable to load profile");
        }

        profileData = await response.json();

        showProfileSummary();

    } catch (error) {
        showError(error.message);
        renderStep();
    }
}


function showProfileSummary() {
    profileSetup.hidden = true;
    profileSummary.hidden = false;

    document.getElementById("summaryName")
        .textContent = profileData.name;

    document.getElementById("summaryRole")
        .textContent = formatText(profileData.role);


    document.getElementById("summaryDays")
        .textContent =
            profileData.availableDays
                .map(formatText)
                .join(", ");

    document.getElementById("summaryTime")
        .textContent =
            `${profileData.preferredStartTime}
             – ${profileData.preferredEndTime}`;

    document.getElementById("summaryMinutes")
        .textContent =
            `${profileData.maximumDailyMinutes} minutes`;
}


function formatText(value) {
    return value
        .replaceAll("_", " ")
        .toLowerCase()
        .replace(/\b\w/g, letter =>
            letter.toUpperCase()
        );
}


function showError(message) {
    profileStatus.textContent = message;
    profileStatus.style.color = "#a93232";
}


loadSavedProfile();