const calendarGrid =
    document.getElementById("calendarGrid");

const calendarStatus =
    document.getElementById("calendarStatus");

const calendarWeek =
    document.getElementById("calendarWeek");

const previousWeekButton =
    document.getElementById("previousWeek");

const currentWeekButton =
    document.getElementById("currentWeek");

const nextWeekButton =
    document.getElementById("nextWeek");

const userId =
    localStorage.getItem("replanUserId");

let habits = [];
let recoveryPlans = [];

let displayedWeekStart =
    getMonday(new Date());


if (!userId) {
    window.location.href = "/profile";
}

else {
    loadCalendar();
}


previousWeekButton.addEventListener(
    "click",
    () => {
        displayedWeekStart =
            addDays(displayedWeekStart, -7);

        renderCalendar();
    }
);


currentWeekButton.addEventListener(
    "click",
    () => {
        displayedWeekStart =
            getMonday(new Date());

        renderCalendar();
    }
);


nextWeekButton.addEventListener(
    "click",
    () => {
        displayedWeekStart =
            addDays(displayedWeekStart, 7);

        renderCalendar();
    }
);


async function loadCalendar() {
    try {
        const [
            habitResponse,
            recoveryResponse
        ] = await Promise.all([
            fetch(
                `/api/habits/user/${userId}`
            ),

            fetch(
                `/api/recovery/user/${userId}`
            )
        ]);

        if (!habitResponse.ok) {
            throw new Error(
                "Unable to load habits"
            );
        }

        if (!recoveryResponse.ok) {
            throw new Error(
                "Unable to load recovery plans"
            );
        }

        habits =
            await habitResponse.json();

        recoveryPlans =
            await recoveryResponse.json();

        renderCalendar();

    } catch (error) {
        calendarStatus.hidden = false;
        calendarStatus.textContent =
            error.message;

        calendarStatus.style.color =
            "#a93232";

        console.error(error);
    }
}


function renderCalendar() {
    calendarGrid.innerHTML = "";

    const weekEnd =
        addDays(displayedWeekStart, 6);

    calendarWeek.textContent =
        `${formatDate(displayedWeekStart)}
         – ${formatDate(weekEnd)}`;

    calendarStatus.hidden = true;

    for (let index = 0; index < 7; index++) {
        const date =
            addDays(displayedWeekStart, index);

        const day =
            createDayColumn(date);

        calendarGrid.appendChild(day);
    }
}


function createDayColumn(date) {
    const column =
        document.createElement("article");

    column.className = "calendar-day";

    if (isSameDate(date, new Date())) {
        column.classList.add("today");
    }

    const dayName =
        date.toLocaleDateString(
            "en-IN",
            {weekday: "short"}
        );

    const heading =
        document.createElement("h3");

    heading.textContent = dayName;

    const dateText =
        document.createElement("span");

    dateText.className = "calendar-date";

    dateText.textContent =
        formatDate(date);

    column.append(
        heading,
        dateText
    );

    const dateKey =
        toLocalDate(date);

    const fullDayName =
        getJavaDayName(date);

    const scheduledHabits = habits.filter(habit =>
        habit.status === "ACTIVE"
        && habit.daysOfWeek
        && habit.daysOfWeek.includes(fullDayName)
        && (
            !habit.startDate
            || habit.startDate <= dateKey
        )
    );

    scheduledHabits.forEach(habit => {
        column.appendChild(
            createHabitEvent(habit)
        );
    });

    const dayRecoveryPlans =
        recoveryPlans.filter(plan =>
            plan.status === "CONFIRMED"
            && plan.proposedDateTime
            && plan.proposedDateTime
                .substring(0, 10) === dateKey
        );

    dayRecoveryPlans.forEach(plan => {
        const habit = habits.find(
            item => item.id === plan.habitId
        );

        column.appendChild(
            createRecoveryEvent(
                plan,
                habit
            )
        );
    });

    if (
        scheduledHabits.length === 0
        && dayRecoveryPlans.length === 0
    ) {
        const empty =
            document.createElement("p");

        empty.className = "empty-note";
        empty.textContent = "No sessions";

        column.appendChild(empty);
    }

    return column;
}


function createHabitEvent(habit) {
    const event =
        document.createElement("div");

    event.className = "calendar-event";

    const name =
        document.createElement("strong");

    name.textContent = habit.name;

    const details =
        document.createElement("span");

    details.textContent =
        `${formatTime(habit.preferredTime)}
         · ${habit.durationMinutes} min`;

    event.append(
        name,
        details
    );

    return event;
}


function createRecoveryEvent(
    plan,
    habit
) {
    const event =
        document.createElement("div");

    event.className =
        "calendar-event recovery-event";

    const name =
        document.createElement("strong");

    name.textContent = habit
        ? `RePlan: ${habit.name}`
        : "Recovery session";

    const details =
        document.createElement("span");

    const target =
        plan.temporaryTarget != null
            ? plan.temporaryTarget
            : plan.originalTarget;

    details.textContent =
        `${formatDateTimeTime(
            plan.proposedDateTime
        )} · target ${target}`;

    event.append(
        name,
        details
    );

    return event;
}


function getMonday(date) {
    const result =
        new Date(
            date.getFullYear(),
            date.getMonth(),
            date.getDate()
        );

    const day =
        result.getDay();

    const difference =
        day === 0 ? -6 : 1 - day;

    result.setDate(
        result.getDate() + difference
    );

    return result;
}


function addDays(date, numberOfDays) {
    const result =
        new Date(date);

    result.setDate(
        result.getDate() + numberOfDays
    );

    return result;
}


function isSameDate(first, second) {
    return (
        first.getFullYear() === second.getFullYear()
        && first.getMonth() === second.getMonth()
        && first.getDate() === second.getDate()
    );
}


function toLocalDate(date) {
    const year =
        date.getFullYear();

    const month = String(
        date.getMonth() + 1
    ).padStart(2, "0");

    const day = String(
        date.getDate()
    ).padStart(2, "0");

    return `${year}-${month}-${day}`;
}


function getJavaDayName(date) {
    const days = [
        "SUNDAY",
        "MONDAY",
        "TUESDAY",
        "WEDNESDAY",
        "THURSDAY",
        "FRIDAY",
        "SATURDAY"
    ];

    return days[date.getDay()];
}


function formatDate(date) {
    return date.toLocaleDateString(
        "en-IN",
        {
            day: "numeric",
            month: "short",
            year: "numeric"
        }
    );
}


function formatTime(time) {
    if (!time) {
        return "No time";
    }

    const [hourText, minute] =
        time.split(":");

    const hour =
        Number(hourText);

    const suffix =
        hour >= 12 ? "PM" : "AM";

    return `${hour % 12 || 12}:${minute} ${suffix}`;
}


function formatDateTimeTime(dateTime) {
    if (!dateTime) {
        return "No time";
    }

    return formatTime(
        dateTime.substring(11, 16)
    );
}