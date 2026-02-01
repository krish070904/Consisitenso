# ConsistEso - Consistency Enforcement System

**Philosophy**: You don't decide to be consistent. You wake up already inside a plan.

## 🧠 Core Concept

ConsistEso is not a productivity app. It's a **psychological trap system** designed to make consistency the path of least resistance. You're not forced—you're **boxed**.

### The Silent Trap

1. **You define rules once**
2. **The app rearranges your phone life**
3. **Laziness becomes inconvenient**
4. **Consistency becomes effortless**

---

## 🏗️ Architecture (Phase 1 - COMPLETE)

### **Data Layer**

#### Models
- **`Rule`** - IF-THEN-CONSEQUENCE law that governs behavior
- **`Execution`** - Every rule execution tracked for pattern detection
- **`TimeDebt`** - Cumulative punishment system (compounds, blocks rewards)
- **`SystemState`** - Global app state (boring mode, lockouts, rewards)
- **`BehaviorPattern`** - Detected failure patterns (escalation memory)
- **`Reward`** - Binary unlock system (music, YouTube, color mode)
- **`SkipToken`** - Earned after 7 perfect days

#### Database (Room)
- **Local-only** (no cloud, no sharing, no login)
- **Encrypted** storage
- **DAOs** for each entity with reactive Flow support
- **Type converters** for complex types

### **Repository Layer**

- **`RuleRepository`** - Rule CRUD, execution tracking, consequence application
- **`SystemStateRepository`** - Boring mode, lockouts, rewards, anti-cheat

### **Core Engine**

- **`RuleEngine`** - The Brain
  - Evaluates rules periodically
  - Applies consequences (time debt, app locks, boring mode)
  - Detects cheating patterns
  - Calculates daily rewards
  - Escalates punishment for repeated failures

### **UI Layer**

- **`MainViewModel`** - Manages app state, rules, executions, debt
- **`RuleViewModel`** - Rule creation and management
- **Brutal Minimal UI** - Dark theme, factual, no motivation

---

## ⚡ Core Systems

### 1️⃣ Time Debt Engine

```
Miss 30 min → Owe 45 min (1.5x multiplier)
Debt rolls over
Debt blocks rewards
Only execution clears debt
```

**Implementation**:
- Tracked in `TimeDebt` entity
- Transactions logged in `DebtTransaction`
- Multiplier increases with repeated failures
- Silent punishment increases debt multiplier

### 2️⃣ App Lockouts

```
Social/entertainment apps locked by rules
No error messages - just unavailable
Brain stops fighting after a few days
```

**Implementation**:
- Stored in `SystemState.lockedApps`
- Will be enforced via Accessibility Service (Phase 2)

### 3️⃣ Boring Mode

```
Phone goes grayscale
Only allowed: Notes, Study apps, Clock
No animations, no colors, no joy
```

**Levels**:
- **Level 1**: Grayscale only
- **Level 2**: + Music locked
- **Level 3**: + All entertainment locked

**Implementation**:
- Controlled by `SystemState.isBoringModeActive`
- UI adapts to show gray theme
- Full grayscale filter (Phase 2)

### 4️⃣ Escalation Memory

```
App stores:
- Your worst weeks
- Most common failures
- Exact excuses you repeat
```

**Implementation**:
- `BehaviorPattern` entity tracks patterns
- Failure hours/days analyzed
- Confidence scoring
- Used for predictive punishment

### 5️⃣ Rewards (Binary)

```
Done → Unlocked
Not done → Nonexistent
```

**Daily Rewards**:
- Music unlock
- YouTube unlock
- Color restoration

**Rare Rewards**:
- Skip token (7 perfect days)
- Silent praise
- Control mode

### 6️⃣ Anti-Cheat System

```
Detects:
- Time manipulation
- Pattern-based fake completion
- Rapid task marking
```

**Silent Punishments** (no warnings):
- Rewards unlock slower
- Time debt multiplier increases
- Boring mode lasts longer
- Rules become stricter

**Implementation**:
- Suspicion score calculated per execution
- Global suspicion score in `SystemState`
- Silent punishment multiplier applied

---

## 📊 Data Flow

```
User creates Rule
    ↓
RuleEngine evaluates (periodic)
    ↓
Execution created (PENDING)
    ↓
User completes OR deadline passes
    ↓
Consequences applied (if missed)
    ↓
Time Debt accrued
    ↓
Rewards locked
    ↓
Boring Mode activated
    ↓
Patterns detected
    ↓
Escalation applied
```

---

## 🎯 Phase 1 Status: ✅ COMPLETE

### What's Built:

✅ **Complete data models** (8 entities)  
✅ **Room database** with DAOs  
✅ **Repository layer** with business logic  
✅ **Rule Engine** (core brain)  
✅ **ViewModels** with reactive state  
✅ **Brutal minimal UI** (dark theme, factual)  
✅ **Time debt system**  
✅ **Execution tracking**  
✅ **Pattern detection**  
✅ **Reward system**  
✅ **Anti-cheat detection**  

### What Works:

- Create rules (IF-THEN-CONSEQUENCE)
- Track executions
- Calculate time debt
- Detect cheating patterns
- Award/revoke rewards
- Boring mode UI adaptation
- Execution graph visualization

---

## 🚀 Next Phases

### **Phase 2: Enforcement** 🔒

- [ ] Accessibility Service (app blocking)
- [ ] WorkManager (background monitoring)
- [ ] Grayscale filter implementation
- [ ] Notification system (subtle, factual)
- [ ] Boot receiver (auto-start)

### **Phase 3: Advanced Features** 🧠

- [ ] Rule edit window (Sunday 6-7 PM only)
- [ ] Uninstall resistance (Device Admin)
- [ ] Observer mode (see apps, can't open)
- [ ] Failure report system
- [ ] Pattern-based predictions
- [ ] Escalation automation

### **Phase 4: Polish** ✨

- [ ] Onboarding flow
- [ ] Rule creation wizard
- [ ] Execution graph improvements
- [ ] Silent praise system
- [ ] Control mode rewards
- [ ] Data export (after X days clean)

---

## 🛠️ Tech Stack

- **Language**: Kotlin 1.9.20
- **UI**: Jetpack Compose + Material 3
- **Database**: Room 2.6.1 (local, encrypted)
- **Architecture**: MVVM + Clean Architecture
- **Async**: Coroutines + Flow
- **Background**: WorkManager 2.9.0
- **Min SDK**: 24 (Android 7.0)
- **Target SDK**: 34 (Android 14)

---

## 📱 How to Build

1. **Open in Android Studio**
2. **Sync Gradle** (dependencies will download)
3. **Run on device/emulator**

```bash
./gradlew assembleDebug
```

---

## 🧪 Testing the System

### Create a Test Rule:

```kotlin
viewModel.createRule(
    name = "Morning Routine",
    description = "Complete before 9 AM",
    triggerType = TriggerType.TIME,
    triggerTime = LocalTime.of(7, 0),
    triggerDays = listOf(DayOfWeek.MONDAY, DayOfWeek.TUESDAY),
    actionType = ActionType.HABIT,
    actionDescription = "Exercise for 30 minutes",
    estimatedDurationMinutes = 30,
    consequenceType = ConsequenceType.TIME_DEBT,
    timeDebtMultiplier = 1.5f
)
```

### Test Execution Flow:

1. Rule triggers → Execution created (PENDING)
2. Complete → Debt cleared, rewards unlocked
3. Miss → Debt accrued, rewards locked, boring mode activated

---

## 🎨 UI Philosophy

**Minimal. Brutal. Factual.**

- No motivation
- No gamification
- No streaks with flames
- Just clean, brutal lines
- Gaps visible forever
- Clean weeks feel elite

**Colors**:
- **Completed**: Green (#4CAF50)
- **Missed**: Red (#FF5252)
- **Debt**: Red (#FF5252)
- **Boring Mode**: Gray (#757575)

---

## 🔐 Privacy

- **No login**
- **No cloud**
- **No sharing**
- **No screenshots** (will be blocked)
- **Local-only** encrypted storage
- **Data export** only after X days clean

---

## 💀 The Hard Truth

If you use this app:

✅ You won't feel motivated  
✅ You'll feel **directed**  
✅ One day you'll realize: "Consistency stopped being a decision"

---

## 📄 License

This project is a demonstration of advanced Android architecture and psychological design patterns.

---

**Built with precision. Designed for discipline. Engineered for consistency.**
