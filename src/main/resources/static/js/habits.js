const habitList =
    document.getElementById("habitList");

const habitStatus =
    document.getElementById("habitStatus");

const userId =
    localStorage.getItem("replanUserId");


if (!userId) {
    window.location.href = "/profile";
}

else {
    loadHabits();
}


async function loadHabits() {
    try {
        habitStatus.textContent =
            "Loading your habits...";

        const response = await fetch(
            `/api/habits/user/${userId}`
        );

        if (!response.ok) {
            throw new Error(
                "Unable to load your habits"
            );
        }

        const habits = await response.json();

        renderHabits(habits);

    } catch (error) {
        habitStatus.textContent = error.message;
        habitStatus.style.color = "#a93232";

        console.error(error);
    }
}


function renderHabits(habits) {
    habitList.innerHTML = "";

    if (habits.length === 0) {
        habitStatus.textContent =
            "You have no habits yet. Create one through RePlan Chat.";

        return;
    }

    habitStatus.hidden = true;

    habits.forEach(habit => {
        const card =
            document.createElement("article");

        card.className = "habit-card";

        const details =
            document.createElement("div");

        const badge =
            document.createElement("span");

        badge.className =
            habit.status === "ACTIVE"
                ? "status-badge"
                : "status-badge paused-badge";

        badge.textContent = formatText(habit.status);

        const title =
            document.createElement("h2");

        title.textContent = habit.name;

        const description =
            document.createElement("p");

        description.textContent =
            habit.description
            || "No description provided.";

        const schedule =
            document.createElement("p");

        schedule.className = "habit-schedule";

        schedule.textContent =
            `${formatText(habit.frequencyType)}
             • ${habit.durationMinutes} minutes
             • ${formatTime(habit.preferredTime)}`;

        details.append(
            badge,
            title,
            description,
            schedule
        );

        card.appendChild(details);

        if (habit.status === "ACTIVE") {
            const actions =
                document.createElement("div");

            actions.className = "habit-actions";

            const pauseButton =
                document.createElement("button");

            pauseButton.type = "button";
            pauseButton.className = "miss-button";
            pauseButton.textContent = "Pause";

            pauseButton.addEventListener(
                "click",
                () => pauseHabit(
                    habit.id,
                    pauseButton
                )
            );

            actions.appendChild(pauseButton);
            card.appendChild(actions);
        }

        habitList.appendChild(card);
    });
}


async function pauseHabit(
    habitId,
    pauseButton
) {
    try {
        pauseButton.disabled = true;
        pauseButton.textContent = "Pausing...";

        const response = await fetch(
            `/api/habits/${habitId}/pause`,
            {
                method: "PATCH"
            }
        );

        if (!response.ok) {
            throw new Error(
                "Unable to pause this habit"
            );
        }

        await loadHabits();

    } catch (error) {
        pauseButton.disabled = false;
        pauseButton.textContent = "Pause";

        alert(error.message);
        console.error(error);
    }
}


function formatText(value) {
    if (!value) {
        return "";
    }

    return value
        .replaceAll("_", " ")
        .toLowerCase()
        .replace(
            /\b\w/g,
            letter => letter.toUpperCase()
        );
}


function formatTime(time) {
    if (!time) {
        return "No time";
    }

    const [hourText, minute] =
        time.split(":");

    const hour = Number(hourText);

    const suffix =
        hour >= 12 ? "PM" : "AM";

    const displayHour =
        hour % 12 || 12;

    return `${displayHour}:${minute} ${suffix}`;
}