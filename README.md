# CPU Scheduling Simulator: Round Robin vs Priority

## Team Members
- Islam Saad Abu-Deif Mohammed         - ID: 20240129
- Ammar Ahmed Abdallah Ahmed           - ID: 20240614
- Tariq Ahmed Hassan Morsi             - ID: 20240496
- Hossam Abd-ALaziz Abd-Alazim Ahmed   - ID: 20240292
- Anas Mohammed Fatah-Allah Mohammed   - ID: 20240170
- Ahmed Kamal Abd-Almohsen Abd-Alhalim - ID: 20240067

## 📋 Project Description
This project implements and compares two CPU scheduling algorithms:
- **Round Robin (RR)** with adjustable time quantum
- **Preemptive Priority Scheduling** (lower number = higher priority)

## 🚀 How to Run

### Prerequisites
- Java JDK 8 or higher
- NetBeans IDE (or any Java IDE)

### Steps
1. Open project in NetBeans
2. Right-click `gui/MainGUI.java`
3. Select "Run File"

## 📊 Features
- Add unlimited processes dynamically
- Input validation for all fields
- Round Robin with user-defined quantum
- Preemptive Priority Scheduling
- Gantt chart visualization for both algorithms
- Per-process metrics: WT, TAT, RT
- Average metrics calculation
- Side-by-side comparison with recommendations

## 🧪 Test Scenarios
See `report.html` for detailed test cases

## 📸 Screenshots
See `screenshots/` folder for execution screenshots

## ⚙️ Priority Rule
**Lower number = higher priority** (1 is highest, 10 is lowest)

## 🔧 Time Quantum
Default: 3 time units (user adjustable)

## 📈 Metrics Calculated
- **WT (Waiting Time)**: Time process spends in ready queue
- **TAT (Turnaround Time)**: Total time from arrival to completion
- **RT (Response Time)**: Time until first CPU access

## 🎯 Conclusion
- Round Robin provides better fairness and response time
- Priority Scheduling better for urgent/time-critical tasks
- Trade-off: Fairness vs. urgency