const chatForm = document.getElementById("chatForm");
const messageInput = document.getElementById("messageInput");
const chatMessages = document.getElementById("chatMessages");
const newChatButton = document.getElementById("newChatButton");
const recentChats = document.getElementById("recentChats");

const USER_ID = localStorage.getItem("replanUserId");

if (!USER_ID) {
    alert("Please create your profile first.");

    window.location.href = "/profile";
}


const CHAT_STORAGE_KEY =
    `replanChatSessions_${USER_ID}`;

const ACTIVE_CHAT_KEY =
    `replanActiveChat_${USER_ID}`;

let chatSessions =
    readChatSessions();

let activeChatId =
    localStorage.getItem(ACTIVE_CHAT_KEY);

let conversationHistory = [];

initializeChatHistory();


chatForm.addEventListener("submit", async event => {
    // Form submit করলে page reload আটকায়
    event.preventDefault();

    const message = messageInput.value.trim();

    if (!message) {
        return;
    }

    // User-এর message screen-এ দেখায়
    addUserMessage(message);

    messageInput.value = "";
    messageInput.disabled = true;

    try {
        // Spring Boot backend-এ request পাঠাচ্ছে
        const response = await fetch("/api/chat", {
            method: "POST",

            headers: {
                "Content-Type": "application/json"
            },

            body: JSON.stringify({
                userId: USER_ID,
                message: message,
                history: conversationHistory
            })
        });

        if (!response.ok) {
            const errorBody =
                await response.text();

            console.error(
                "Backend error:",
                errorBody
            );

            throw new Error(
                "Unable to get AI response"
            );
        }

        // Backend-এর JSON response JavaScript object-এ বদলায়
        const data = await response.json();

        /*
         * Successful user এবং assistant messages
         * পরবর্তী request-এর জন্য memory-তে রাখা হচ্ছে।
         */
        conversationHistory.push(
            {
                role: "user",
                content: message
            },
            {
                role: "assistant",
                content: data.reply
            }
        );

        saveCurrentChat();

        // Groq-এর reply chat screen-এ দেখায়
        addAssistantMessage(data.reply);

        if (data.habit) {
            addHabitProposal(data.habit);
        }

    } catch (error) {
        addAssistantMessage(
            "Sorry, I could not process your request. Please try again."
        );

        console.error(error);

    } finally {
        messageInput.disabled = false;
        messageInput.focus();
    }
});

document
    .querySelectorAll(".quick-actions button")
    .forEach(button => {
        button.addEventListener("click", () => {
            const message = button.dataset.message;

            messageInput.value = message;
            messageInput.focus();
        });
    });

newChatButton.addEventListener(
    "click",
    startNewChat
);

function addUserMessage(message) {
    const row = document.createElement("div");

    row.className = "message-row user-row";

    row.innerHTML = `
        <div class="message-content">
            <strong>You</strong>
            <div class="message-bubble user-bubble"></div>
        </div>
    `;

    row.querySelector(".user-bubble").textContent = message;

    chatMessages.appendChild(row);
    chatMessages.scrollTop = chatMessages.scrollHeight;
}

function addAssistantMessage(message) {
    const row = document.createElement("div");

    row.className = "message-row";

    row.innerHTML = `
        <div class="message-avatar">R</div>

        <div class="message-content">
            <strong>RePlan AI</strong>
            <div class="message-bubble assistant-bubble"></div>
        </div>
    `;

    row.querySelector(".assistant-bubble").textContent = message;

    chatMessages.appendChild(row);
    chatMessages.scrollTop = chatMessages.scrollHeight;
}

function addHabitProposal(habit) {
    const card = document.createElement("div");

    card.className = "proposal-card";

    card.innerHTML = `
        <h3>Suggested habit</h3>

        <div class="proposal-details">
            <p><strong>Name:</strong> ${habit.name}</p>
            <p><strong>Frequency:</strong> ${habit.frequencyType}</p>
            <p><strong>Duration:</strong> ${habit.durationMinutes} minutes</p>
            <p><strong>Time:</strong> ${habit.preferredTime}</p>
        </div>

        <div class="proposal-actions">
            <button type="button"
                    class="confirm-button">
                Confirm
            </button>

            <button type="button"
                    class="change-button">
                Change
            </button>

            <button type="button"
                    class="cancel-button">
                Cancel
            </button>
        </div>
    `;

    /*
     * Confirm-এর database connection
     * আমরা পরের step-এ যোগ করব।
     */
    const confirmButton =
        card.querySelector(".confirm-button");

    confirmButton.addEventListener("click", async () => {
        confirmButton.disabled = true;
        confirmButton.textContent = "Saving...";

        try {
            /*
             * Confirmed AI proposal backend-এ পাঠাচ্ছে।
             */
            const response = await fetch("/api/chat/confirm", {
                method: "POST",

                headers: {
                    "Content-Type": "application/json"
                },

                body: JSON.stringify({
                    userId: USER_ID,
                    habit: habit
                })
            });

            if (!response.ok) {
                throw new Error("Habit could not be saved");
            }

            // MongoDB-তে save হওয়া Habit object
            const savedHabit = await response.json();

            const successMessage =
                `${savedHabit.name} has been added to your habits.`;

            /*
             * Button confirmation-টাও conversation history-তে
             * রাখা হচ্ছে, যাতে Groq জানে proposal already saved।
             */
            conversationHistory.push(
                {
                    role: "user",
                    content:
                        `I confirm the proposed ${savedHabit.name} habit.`
                },
                {
                    role: "assistant",
                    content: successMessage
                }
            );

            card.remove();

            addAssistantMessage(successMessage);

        } catch (error) {
            confirmButton.disabled = false;
            confirmButton.textContent = "Confirm";

            addAssistantMessage(
                "Sorry, I could not save the habit. Please try again."
            );

            console.error(error);
        }
    });

    card.querySelector(".cancel-button")
        .addEventListener("click", () => {
            card.remove();
            addAssistantMessage(
                "Okay, I cancelled this suggestion."
            );
        });

    card.querySelector(".change-button")
        .addEventListener("click", () => {
            messageInput.value =
                "Please change the suggested habit: ";

            messageInput.focus();
        });

    chatMessages.appendChild(card);
    chatMessages.scrollTop = chatMessages.scrollHeight;
}

function readChatSessions() {
    try {
        const saved =
            localStorage.getItem(
                CHAT_STORAGE_KEY
            );

        return saved
            ? JSON.parse(saved)
            : [];

    } catch (error) {
        console.error(error);
        return [];
    }
}


function initializeChatHistory() {
    const activeSession =
        chatSessions.find(
            session =>
                session.id === activeChatId
        );

    if (activeSession) {
        conversationHistory =
            [...activeSession.history];

        renderSavedConversation();
    }

    else {
        activeChatId = null;

        localStorage.removeItem(
            ACTIVE_CHAT_KEY
        );
    }

    renderRecentChats();
}


function saveCurrentChat() {
    if (conversationHistory.length === 0) {
        return;
    }

    if (!activeChatId) {
        activeChatId =
            `chat-${Date.now()}`;

        localStorage.setItem(
            ACTIVE_CHAT_KEY,
            activeChatId
        );
    }

    const firstUserMessage =
        conversationHistory.find(
            item => item.role === "user"
        );

    const title =
        firstUserMessage
            ? createChatTitle(
                firstUserMessage.content
            )
            : "New conversation";

    const session = {
        id: activeChatId,
        title: title,
        history: [
            ...conversationHistory
        ],
        updatedAt:
            new Date().toISOString()
    };

    const existingIndex =
        chatSessions.findIndex(
            item => item.id === activeChatId
        );

    if (existingIndex >= 0) {
        chatSessions[existingIndex] =
            session;
    }

    else {
        chatSessions.push(session);
    }

    chatSessions.sort(
        (first, second) =>
            new Date(second.updatedAt)
            - new Date(first.updatedAt)
    );

    localStorage.setItem(
        CHAT_STORAGE_KEY,
        JSON.stringify(chatSessions)
    );

    renderRecentChats();
}


function startNewChat() {
    activeChatId = null;
    conversationHistory = [];

    localStorage.removeItem(
        ACTIVE_CHAT_KEY
    );

    clearDisplayedConversation();
    renderRecentChats();

    messageInput.value = "";
    messageInput.focus();
}


function openSavedChat(chatId) {
    const session =
        chatSessions.find(
            item => item.id === chatId
        );

    if (!session) {
        return;
    }

    activeChatId = session.id;

    conversationHistory =
        [...session.history];

    localStorage.setItem(
        ACTIVE_CHAT_KEY,
        activeChatId
    );

    renderSavedConversation();
    renderRecentChats();
}


function renderSavedConversation() {
    clearDisplayedConversation();

    conversationHistory.forEach(
        message => {
            if (message.role === "user") {
                addUserMessage(
                    message.content
                );
            }

            else if (
                message.role === "assistant"
            ) {
                addAssistantMessage(
                    message.content
                );
            }
        }
    );
}


function clearDisplayedConversation() {
    chatMessages
        .querySelectorAll(
            ".message-row:not(.assistant-row), .proposal-card"
        )
        .forEach(element => {
            element.remove();
        });
}


function renderRecentChats() {
    recentChats.innerHTML = "";

    if (chatSessions.length === 0) {
        const empty =
            document.createElement("p");

        empty.className =
            "recent-chat-empty";

        empty.textContent =
            "No saved chats yet.";

        recentChats.appendChild(empty);
        return;
    }

    chatSessions.forEach(session => {
        const button =
            document.createElement("button");

        button.type = "button";
        button.className =
            "recent-chat-button";

        if (session.id === activeChatId) {
            button.classList.add("active");
        }

        button.textContent =
            session.title;

        button.addEventListener(
            "click",
            () => openSavedChat(
                session.id
            )
        );

        recentChats.appendChild(button);
    });
}


function createChatTitle(message) {
    const maximumLength = 28;

    if (message.length <= maximumLength) {
        return message;
    }

    return message.substring(
        0,
        maximumLength
    ) + "...";
}