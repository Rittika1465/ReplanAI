const todayHabitList =
    document.getElementById("todayHabitList");

const todayStatus =
    document.getElementById("todayStatus");

const dailyCompletion =
    document.getElementById("dailyCompletion");

const dailyProgress =
    document.getElementById("dailyProgress");

const userId =
    localStorage.getItem("replanUserId");


if (!userId) {
    window.location.href = "/profile";
}

else {
    loadTodayPlan();
}


async function loadTodayPlan() {
    try {
        todayStatus.hidden = false;
        todayStatus.style.color = "";
        todayStatus.textContent =
            "Loading today's plan...";

        const [
            habitResponse,
            logResponse,
            recoveryResponse
        ] = await Promise.all([
            fetch(
                `/api/habits/user/${userId}`
            ),

            fetch(
                `/api/habits/logs/user/${userId}`
            ),

            fetch(
                `/api/recovery/user/${userId}`
            )
        ]);

        if (!habitResponse.ok) {
            throw new Error(
                "Unable to load your habits"
            );
        }

        if (!logResponse.ok) {
            throw new Error(
                "Unable to load your habit logs"
            );
        }

        if (!recoveryResponse.ok) {
            throw new Error(
                "Unable to load recovery plans"
            );
        }

        const habits =
            await habitResponse.json();

        const logs =
            await logResponse.json();

        const recoveryPlans =
            await recoveryResponse.json();

        const todayName =
            getTodayDayName();

        const todayDate =
            getLocalDate();

        const todayHabits = habits.filter(habit =>
            habit.status === "ACTIVE"
            && habit.daysOfWeek
            && habit.daysOfWeek.includes(todayName)
        );

        const todayLogs = logs.filter(log =>
            log.scheduledDate === todayDate
        );

        // প্রথমে আজকের habits দেখাবে
        renderTodayHabits(
            todayHabits,
            todayLogs
        );

        // তারপর saved recovery plans দেখাবে
        renderRecoveryPlans(
            recoveryPlans,
            habits
        );

        updateDailyProgress(
            todayHabits,
            todayLogs
        );

    } catch (error) {
        todayStatus.hidden = false;
        todayStatus.textContent =
            error.message;

        todayStatus.style.color =
            "#a93232";

        console.error(error);
    }
}

function renderTodayHabits(
    habits,
    logs
) {
    todayHabitList.innerHTML = "";

    if (habits.length === 0) {
        todayStatus.hidden = false;

        todayStatus.textContent =
            "No habits are scheduled for today.";

        return;
    }

    todayStatus.hidden = true;

    habits.forEach(habit => {
        const existingLog = logs.find(log =>
            log.habitId === habit.id
        );

        const card =
            document.createElement("article");

        card.className = "habit-card";

        const information =
            document.createElement("div");

        const badge =
            document.createElement("span");

        badge.className = "status-badge";

        if (existingLog) {
            badge.textContent =
                formatText(existingLog.status);

            if (existingLog.status === "MISSED") {
                badge.classList.add("missed-badge");
            }

            else {
                badge.classList.add(
                    "completed-badge"
                );
            }
        }

        else {
            badge.textContent = "Scheduled";
        }

        const heading =
            document.createElement("h2");

        heading.textContent = habit.name;

        const schedule =
            document.createElement("p");

        schedule.textContent =
            `${formatTime(habit.preferredTime)}
             · ${habit.durationMinutes} minutes`;

        information.append(
            badge,
            heading,
            schedule
        );

        card.appendChild(information);

        if (!existingLog) {
            const actions =
                document.createElement("div");

            actions.className = "habit-actions";

            const timerButton =
                document.createElement("button");

            timerButton.type = "button";
            timerButton.className = "timer-button";
            timerButton.textContent = "Start Timer";

            timerButton.addEventListener(
                "click",
                () => startHabitTimer(habit)
            );

            const completeButton =
                document.createElement("button");

            completeButton.type = "button";
            completeButton.className =
                "complete-button";

            completeButton.textContent =
                "Complete";

            completeButton.addEventListener(
                "click",
                () => completeHabit(
                    habit,
                    completeButton,
                    actions
                )
            );

            const missButton =
                document.createElement("button");

            missButton.type = "button";
            missButton.className =
                "miss-button";

            missButton.textContent = "Miss";

            missButton.addEventListener(
                "click",
                () => missHabit(
                    habit,
                    missButton,
                    actions
                )
            );

            actions.append(
                timerButton,
                completeButton,
                missButton
            );

            card.appendChild(actions);
        }

        todayHabitList.appendChild(card);
    });
}


async function completeHabit(
    habit,
    button,
    actions
) {
    const completedValueText = window.prompt(
        `How many ${habit.unit.toLowerCase()} did you complete?`,
        habit.targetValue
    );

    if (completedValueText === null) {
        return;
    }

    const completedValue =
        Number(completedValueText);

    if (
        Number.isNaN(completedValue)
        || completedValue < 0
    ) {
        alert("Please enter a valid value.");
        return;
    }

    const note = window.prompt(
        "Add a short note (optional):",
        ""
    );

    try {
        disableActions(actions);
        button.textContent = "Saving...";

        const response = await fetch(
            `/api/habits/${habit.id}/complete`,
            {
                method: "POST",

                headers: {
                    "Content-Type":
                        "application/json"
                },

                body: JSON.stringify({
                    scheduledDate:
                        getLocalDate(),

                    completedValue:
                        completedValue,

                    note: note || ""
                })
            }
        );

        if (!response.ok) {
            throw new Error(
                "Unable to mark this habit complete"
            );
        }

        await loadTodayPlan();

    } catch (error) {
        alert(error.message);
        await loadTodayPlan();

        console.error(error);
    }
}


async function missHabit(
    habit,
    button,
    actions
) {
    const missReason = window.prompt(
        `Why did you miss ${habit.name}?`
    );

    if (
        missReason === null
        || !missReason.trim()
    ) {
        return;
    }

    const note = window.prompt(
        "Add another note (optional):",
        ""
    );

    try {
        disableActions(actions);
        button.textContent = "RePlanning...";

        // // প্রথমে missed log save হবে
        const missResponse = await fetch(
            `/api/habits/${habit.id}/miss`,
            {
                method: "POST",

                headers: {
                    "Content-Type": "application/json"
                },

                body: JSON.stringify({
                    scheduledDate: getLocalDate(),
                    missReason: missReason.trim(),
                    note: note || ""
                })
            }
        );

        if (!missResponse.ok) {
            throw new Error(
                "Unable to mark this habit missed"
            );
        }

        // তারপর Recovery Engine suggestion বানাবে
        const recoveryResponse = await fetch(
            `/api/recovery/habits/${habit.id}/suggest`,
            {
                method: "POST",

                headers: {
                    "Content-Type": "application/json"
                },

                body: JSON.stringify({
                    missedDate: getLocalDate(),
                    reason: missReason.trim()
                })
            }
        );

        if (!recoveryResponse.ok) {
            throw new Error(
                "Habit was marked missed, but RePlan could not create a recovery suggestion"
            );
        }

        await recoveryResponse.json();
        await loadTodayPlan();

    } catch (error) {
        alert(error.message);
        await loadTodayPlan();

        console.error(error);
    }
}

function updateDailyProgress(
    habits,
    logs
) {
    if (habits.length === 0) {
        dailyCompletion.textContent = "0%";
        dailyProgress.style.width = "0%";
        return;
    }

    const habitIds =
        habits.map(habit => habit.id);

    const completedCount = logs.filter(log =>
        habitIds.includes(log.habitId)
        && log.status === "COMPLETED"
    ).length;

    const percentage = Math.round(
        completedCount / habits.length * 100
    );

    dailyCompletion.textContent =
        `${percentage}%`;

    dailyProgress.style.width =
        `${percentage}%`;
}


function disableActions(actions) {
    actions
        .querySelectorAll("button")
        .forEach(button => {
            button.disabled = true;
        });
}


function getTodayDayName() {
    const days = [
        "SUNDAY",
        "MONDAY",
        "TUESDAY",
        "WEDNESDAY",
        "THURSDAY",
        "FRIDAY",
        "SATURDAY"
    ];

    return days[new Date().getDay()];
}


function getLocalDate() {
    const today = new Date();

    const year = today.getFullYear();

    const month = String(
        today.getMonth() + 1
    ).padStart(2, "0");

    const day = String(
        today.getDate()
    ).padStart(2, "0");

    return `${year}-${month}-${day}`;
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


function formatText(value) {
    return value
        .replaceAll("_", " ")
        .toLowerCase()
        .replace(
            /\b\w/g,
            letter => letter.toUpperCase()
        );
}

function showRecoveryProposal(
    habit,
    recoveryPlan
) {
    const card = document.createElement("article");

    card.className = "proposal-card";

    const targetText =
        recoveryPlan.temporaryTarget != null
            ? `${recoveryPlan.temporaryTarget} ${habit.unit}`
            : `${recoveryPlan.originalTarget} ${habit.unit}`;

    card.innerHTML = `
        <h3>RePlan recovery suggestion</h3>

        <div class="proposal-details">
            <p>
                <strong>Habit:</strong>
                ${habit.name}
            </p>

            <p>
                <strong>Strategy:</strong>
                ${formatText(recoveryPlan.recoveryType)}
            </p>

            <p>
                <strong>New time:</strong>
                ${formatDateTime(
                    recoveryPlan.proposedDateTime
                )}
            </p>

            <p>
                <strong>Recovery target:</strong>
                ${targetText}
            </p>
        </div>

        <div class="proposal-actions">
            <button type="button"
                    class="confirm-button">
                Accept RePlan
            </button>

            <button type="button"
                    class="cancel-button">
                Reject
            </button>
        </div>
    `;

    const confirmButton =
        card.querySelector(".confirm-button");

    const rejectButton =
        card.querySelector(".cancel-button");

    confirmButton.addEventListener(
        "click",
        async () => {
            confirmButton.disabled = true;
            confirmButton.textContent =
                "Confirming...";

            try {
                const response = await fetch(
                    `/api/recovery/${recoveryPlan.id}/confirm`,
                    {
                        method: "PATCH"
                    }
                );

                if (!response.ok) {
                    throw new Error(
                        "Unable to confirm recovery plan"
                    );
                }

                card.innerHTML = `
                    <h3>Recovery scheduled</h3>
                    <p>
                        ${habit.name} was moved to
                        ${formatDateTime(
                            recoveryPlan.proposedDateTime
                        )}.
                    </p>
                `;

            } catch (error) {
                confirmButton.disabled = false;
                confirmButton.textContent =
                    "Accept RePlan";

                alert(error.message);
            }
        }
    );

    rejectButton.addEventListener(
        "click",
        async () => {
            rejectButton.disabled = true;

            try {
                const response = await fetch(
                    `/api/recovery/${recoveryPlan.id}/reject`,
                    {
                        method: "PATCH"
                    }
                );

                if (!response.ok) {
                    throw new Error(
                        "Unable to reject recovery plan"
                    );
                }

                card.remove();

            } catch (error) {
                rejectButton.disabled = false;
                alert(error.message);
            }
        }
    );

    todayHabitList.appendChild(card);

    card.scrollIntoView({
        behavior: "smooth",
        block: "center"
    });
}


function formatDateTime(dateTime) {
    if (!dateTime) {
        return "No recovery time";
    }

    const date = new Date(dateTime);

    return date.toLocaleString(
        "en-IN",
        {
            weekday: "short",
            day: "numeric",
            month: "short",
            hour: "numeric",
            minute: "2-digit"
        }
    );
}


function renderRecoveryPlans(
    recoveryPlans,
    habits
) {
    const today = getLocalDate();

    const visiblePlans = recoveryPlans.filter(plan =>
        (
            plan.status === "PENDING"
            || plan.status === "CONFIRMED"
        )
        && plan.proposedDateTime
        && plan.proposedDateTime
            .substring(0, 10) >= today
    );

    visiblePlans.forEach(plan => {
        const habit = habits.find(
            item => item.id === plan.habitId
        );

        if (!habit) {
            return;
        }

        if (plan.status === "PENDING") {
            showRecoveryProposal(
                habit,
                plan
            );
        }

        else {
            showConfirmedRecovery(
                habit,
                plan
            );
        }
    });
}


function showConfirmedRecovery(
    habit,
    recoveryPlan
) {
    const card =
        document.createElement("article");

    card.className = "proposal-card";

    const target =
        recoveryPlan.temporaryTarget != null
            ? recoveryPlan.temporaryTarget
            : recoveryPlan.originalTarget;

    card.innerHTML = `
        <h3>Recovery scheduled</h3>

        <div class="proposal-details">
            <p>
                <strong>Habit:</strong>
                ${habit.name}
            </p>

            <p>
                <strong>New time:</strong>
                ${formatDateTime(
                    recoveryPlan.proposedDateTime
                )}
            </p>

            <p>
                <strong>Recovery target:</strong>
                ${target} ${habit.unit}
            </p>
        </div>
    `;

    todayHabitList.appendChild(card);
}

let activeTimerInterval = null;


function startHabitTimer(habit) {
    if (activeTimerInterval !== null) {
        alert("Another timer is already running.");
        return;
    }

    let remainingSeconds =
        habit.durationMinutes * 60;

    const overlay =
        document.createElement("div");

    overlay.className = "timer-overlay";

    overlay.innerHTML = `
        <section class="timer-panel">

            <p class="eyebrow">
                Focus session
            </p>

            <h2>${habit.name}</h2>

            <div class="timer-display">
                ${formatTimer(remainingSeconds)}
            </div>

            <p class="timer-note">
                Stay with this habit until the timer ends.
            </p>

            <div class="timer-actions">

                <button type="button"
                        class="timer-finish-button">
                    Finish now
                </button>

                <button type="button"
                        class="timer-cancel-button">
                    Cancel
                </button>

            </div>

        </section>
    `;

    document.body.appendChild(overlay);

    const display =
        overlay.querySelector(".timer-display");

    const finishButton =
        overlay.querySelector(
            ".timer-finish-button"
        );

    const cancelButton =
        overlay.querySelector(
            ".timer-cancel-button"
        );

    activeTimerInterval = setInterval(
        () => {
            remainingSeconds--;

            display.textContent =
                formatTimer(remainingSeconds);

            if (remainingSeconds <= 0) {
                stopHabitTimer(overlay);

                alert(
                    `${habit.name} focus session completed!`
                );

                loadTodayPlan();
            }
        },
        1000
    );

    finishButton.addEventListener(
        "click",
        () => {
            stopHabitTimer(overlay);

            alert(
                "Timer stopped. Mark the habit complete if you finished it."
            );
        }
    );

    cancelButton.addEventListener(
        "click",
        () => {
            stopHabitTimer(overlay);
        }
    );
}


function stopHabitTimer(overlay) {
    clearInterval(activeTimerInterval);

    activeTimerInterval = null;

    overlay.remove();
}


function formatTimer(totalSeconds) {
    const minutes = Math.floor(
        totalSeconds / 60
    );

    const seconds =
        totalSeconds % 60;

    return `${String(minutes).padStart(2, "0")}:${String(seconds).padStart(2, "0")}`;
}