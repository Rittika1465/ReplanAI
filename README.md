# RePlan AI

RePlan AI is a conversational habit planning and recovery application.  
Unlike a basic habit tracker that only records missed tasks, RePlan creates an alternative recovery plan based on the user’s schedule and miss reason.

## Demo
A deployed demonstration is available on request.

> The free hosting instance may take around 50–60 seconds to wake up after a period of inactivity.

## Key Features

- Conversational profile setup
- AI-assisted habit creation
- Structured habit proposals with user confirmation
- Daily habit planning
- Complete and missed-session tracking
- Adaptive recovery suggestions for missed habits
- Accept or reject recovery plans
- Weekly calendar
- Progress and completion-rate tracking
- Session-based streak calculation
- Focus timer
- Recent chat sessions stored in browser localStorage
- Harmful-habit safety filtering

## Core RePlan Flow

```text
Create Profile
      ↓
Describe Habit through Chat
      ↓
AI extracts habit details
      ↓
User confirms proposal
      ↓
Habit is saved in MongoDB
      ↓
Complete or miss the scheduled habit
      ↓
RePlan creates a recovery suggestion
      ↓
User accepts or rejects the new plan
