package metrics;

import model.Process;
import java.util.List;

public class MetricsCalculator {
    
    public static double calculateAverageWaitingTime(List<Process> processes) {
        if (processes == null || processes.isEmpty()) return 0.0;
        double total = 0.0;
        for (Process p : processes) {
            total += p.getWaitingTime();
        }
        return total / processes.size();
    }
    
    public static double calculateAverageTurnaroundTime(List<Process> processes) {
        if (processes == null || processes.isEmpty()) return 0.0;
        double total = 0.0;
        for (Process p : processes) {
            total += p.getTurnaroundTime();
        }
        return total / processes.size();
    }
    
    public static double calculateAverageResponseTime(List<Process> processes) {
        if (processes == null || processes.isEmpty()) return 0.0;
        double total = 0.0;
        for (Process p : processes) {
            total += p.getResponseTime();
        }
        return total / processes.size();
    }
    
    public static String formatComparisonTable(double rrWT, double priorityWT,
                                                double rrTAT, double priorityTAT,
                                                double rrRT, double priorityRT) {
        StringBuilder sb = new StringBuilder();
        sb.append("╔══════════════════════╦══════════════╦══════════════╗\n");
        sb.append("║ Metric               ║ Round Robin  ║ Priority     ║\n");
        sb.append("╠══════════════════════╬══════════════╬══════════════╣\n");
        sb.append(String.format("║ Avg Waiting Time    ║ %10.4f ║ %10.4f ║\n", rrWT, priorityWT));
        sb.append(String.format("║ Avg Turnaround Time ║ %10.4f ║ %10.4f ║\n", rrTAT, priorityTAT));
        sb.append(String.format("║ Avg Response Time   ║ %10.4f ║ %10.4f ║\n", rrRT, priorityRT));
        sb.append("╚══════════════════════╩══════════════╩══════════════╝\n");
        return sb.toString();
    }
}