const progressStatus =
    document.getElementById("progressStatus");

const progressContent =
    document.getElementById("progressContent");

const weekRange =
    document.getElementById("weekRange");

const weeklyRate =
    document.getElementById("weeklyRate");

const weeklyRateLabel =
    document.getElementById("weeklyRateLabel");

const weeklyProgressBar =
    document.getElementById("weeklyProgressBar");

const currentStreak =
    document.getElementById("currentStreak");

const completedSessions =
    document.getElementById("completedSessions");

const missedSessions =
    document.getElementById("missedSessions");

const trackedSessions =
    document.getElementById("trackedSessions");

const userId =
    localStorage.getItem("replanUserId");


if (!userId) {
    window.location.href = "/profile";
}

else {
    loadProgress();
}


async function loadProgress() {
    try {
        const response = await fetch(
            `/api/progress/${userId}`
        );

        if (!response.ok) {
            throw new Error(
                "Unable to load your progress"
            );
        }

        const progress =
            await response.json();

        showProgress(progress);

    } catch (error) {
        progressStatus.hidden = false;
        progressStatus.textContent =
            error.message;

        progressStatus.style.color =
            "#a93232";

        console.error(error);
    }
}


function showProgress(progress) {
    const rate = Math.round(
        progress.weeklyCompletionRate
    );

    progressStatus.hidden = true;
    progressContent.hidden = false;

    weekRange.textContent =
        `${formatDate(progress.weekStart)}
         – ${formatDate(progress.weekEnd)}`;

    weeklyRate.textContent =
        `${rate}%`;

    weeklyRateLabel.textContent =
        `${rate}%`;

    weeklyProgressBar.style.width =
        `${rate}%`;

    currentStreak.textContent =
        `${progress.currentStreak}
         ${progress.currentStreak === 1
            ? "session"
            : "sessions"}`;

    completedSessions.textContent =
        progress.completedSessions;

    missedSessions.textContent =
        progress.missedSessions;

    trackedSessions.textContent =
        `${progress.totalTrackedSessions}
         sessions were tracked this week.`;
}


function formatDate(dateText) {
    if (!dateText) {
        return "";
    }

    const [year, month, day] =
        dateText.split("-").map(Number);

    const date =
        new Date(year, month - 1, day);

    return date.toLocaleDateString(
        "en-IN",
        {
            day: "numeric",
            month: "short",
            year: "numeric"
        }
    );
}