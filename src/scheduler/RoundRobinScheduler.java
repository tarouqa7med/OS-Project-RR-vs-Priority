package scheduler;

import model.Process;
import model.GanttEvent;
import java.util.*;

public class RoundRobinScheduler {
    private int quantum;
    private List<Process> results;
    private List<GanttEvent> ganttChart;
    
    public RoundRobinScheduler(int quantum) {
        this.quantum = quantum;
        this.results = new ArrayList<>();
        this.ganttChart = new ArrayList<>();
    }
    
    public void simulate(List<Process> processes) {
        results.clear();
        ganttChart.clear();
        
        List<Process> remainingProcesses = new ArrayList<>();
        for (Process p : processes) {
            remainingProcesses.add(p.copy());
        }
        
        Queue<Process> readyQueue = new LinkedList<>();
        int currentTime = 0;
        int completed = 0;
        
        while (completed < remainingProcesses.size()) {
            for (Process p : remainingProcesses) {
                if (p.getArrivalTime() <= currentTime && !p.isFinished() && !readyQueue.contains(p)) {
                    readyQueue.add(p);
                }
            }
            
            if (readyQueue.isEmpty()) {
                Process next = remainingProcesses.stream()
                    .filter(p -> !p.isFinished())
                    .min(Comparator.comparingInt(Process::getArrivalTime))
                    .orElse(null);
                if (next != null) currentTime = next.getArrivalTime();
                continue;
            }
            
            Process current = readyQueue.poll();
            
            if (current.isFirstRun()) {
                current.setResponseTime(currentTime - current.getArrivalTime());
                current.setFirstRun(false);
            }
            
            int run = Math.min(quantum, current.getRemainingTime());
            ganttChart.add(new GanttEvent(current.getId(), currentTime, currentTime + run));
            current.execute(run);
            currentTime += run;
            
            if (current.isFinished()) {
                current.setCompletionTime(currentTime);
                current.calculateTurnaroundTime();
                current.calculateWaitingTime();
                completed++;
                results.add(current);
            } else {
                readyQueue.add(current);
            }
        }
        results.sort(Comparator.comparing(Process::getId));
    }
    
    public List<Process> getResults() { return results; }
    public List<GanttEvent> getGanttChart() { return ganttChart; }
    
    public String getGanttChartString() {
        StringBuilder sb = new StringBuilder();
        for (GanttEvent e : ganttChart) {
            sb.append(e.toString()).append(" ");
        }
        return sb.toString();
    }
}