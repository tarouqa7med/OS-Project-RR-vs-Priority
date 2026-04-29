package gui;

import model.Process;
import scheduler.RoundRobinScheduler;
import scheduler.PriorityScheduler;
import metrics.MetricsCalculator;
import util.Validator;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class MainGUI extends JFrame {
    
    private List<Process> processes;
    private Validator validator;
    
    private JTextField txtId, txtArrival, txtBurst, txtPriority, txtQuantum;
    private DefaultTableModel tableModel;
    private JTextArea rrGanttArea, priorityGanttArea;
    private DefaultTableModel rrTableModel, priorityTableModel;
    private JTextArea comparisonArea;
    
    public MainGUI() {
        processes = new ArrayList<>();
        validator = new Validator();
        
        setTitle("CPU Scheduler - Round Robin vs Priority");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1300, 800);
        setLayout(new BorderLayout());
        
        // Top panel - Input
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBorder(BorderFactory.createTitledBorder("Add Processes"));
        
        JPanel inputRow = new JPanel(new FlowLayout());
        inputRow.add(new JLabel("ID:")); txtId = new JTextField(5); inputRow.add(txtId);
        inputRow.add(new JLabel("Arrival:")); txtArrival = new JTextField(5); inputRow.add(txtArrival);
        inputRow.add(new JLabel("Burst:")); txtBurst = new JTextField(5); inputRow.add(txtBurst);
        inputRow.add(new JLabel("Priority(1-10):")); txtPriority = new JTextField(5); inputRow.add(txtPriority);
        
        JButton btnAdd = new JButton("Add Process");
        inputRow.add(btnAdd);
        
        inputRow.add(new JLabel("Quantum:")); txtQuantum = new JTextField("3", 5); inputRow.add(txtQuantum);
        
        JButton btnRun = new JButton("RUN SIMULATION");
        btnRun.setBackground(new Color(59, 130, 246));
        btnRun.setForeground(Color.WHITE);
        inputRow.add(btnRun);
        
        JButton btnClear = new JButton("Clear All");
        inputRow.add(btnClear);
        
        topPanel.add(inputRow, BorderLayout.NORTH);
        
        String[] cols = {"ID", "Arrival", "Burst", "Priority"};
        tableModel = new DefaultTableModel(cols, 0);
        JTable processTable = new JTable(tableModel);
        topPanel.add(new JScrollPane(processTable), BorderLayout.CENTER);
        
        add(topPanel, BorderLayout.NORTH);
        
        // Center panel - Two sides
        JPanel centerPanel = new JPanel(new GridLayout(1, 2, 10, 10));
        
        JPanel rrPanel = new JPanel(new BorderLayout());
        rrPanel.setBorder(BorderFactory.createTitledBorder("ROUND ROBIN"));
        rrGanttArea = new JTextArea(5, 40);
        rrGanttArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        rrPanel.add(new JScrollPane(rrGanttArea), BorderLayout.NORTH);
        String[] resCols = {"ID", "WT", "TAT", "RT"};
        rrTableModel = new DefaultTableModel(resCols, 0);
        rrPanel.add(new JScrollPane(new JTable(rrTableModel)), BorderLayout.CENTER);
        
        JPanel priorityPanel = new JPanel(new BorderLayout());
        priorityPanel.setBorder(BorderFactory.createTitledBorder("PRIORITY (Preemptive)"));
        priorityGanttArea = new JTextArea(5, 40);
        priorityGanttArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        priorityPanel.add(new JScrollPane(priorityGanttArea), BorderLayout.NORTH);
        priorityTableModel = new DefaultTableModel(resCols, 0);
        priorityPanel.add(new JScrollPane(new JTable(priorityTableModel)), BorderLayout.CENTER);
        
        centerPanel.add(rrPanel);
        centerPanel.add(priorityPanel);
        add(centerPanel, BorderLayout.CENTER);
        
        // Bottom panel
        comparisonArea = new JTextArea(8, 80);
        comparisonArea.setEditable(false);
        comparisonArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane bottomScroll = new JScrollPane(comparisonArea);
        bottomScroll.setBorder(BorderFactory.createTitledBorder("COMPARISON SUMMARY"));
        add(bottomScroll, BorderLayout.SOUTH);
        
        // Button actions
        btnAdd.addActionListener(e -> addProcess());
        btnRun.addActionListener(e -> runSimulation());
        btnClear.addActionListener(e -> clearAll());
        
        setVisible(true);
    }
    
    private void addProcess() {
        try {
            String id = txtId.getText().trim();
            int arrival = Integer.parseInt(txtArrival.getText().trim());
            int burst = Integer.parseInt(txtBurst.getText().trim());
            int priority = Integer.parseInt(txtPriority.getText().trim());
            
            Process p = new Process(id, arrival, burst, priority);
            validator.clearErrors();
            
            if (validator.validateProcess(p)) {
                processes.add(p);
                tableModel.addRow(new Object[]{id, arrival, burst, priority});
                txtId.setText(""); txtArrival.setText(""); txtBurst.setText(""); txtPriority.setText("");
                JOptionPane.showMessageDialog(this, "Process " + id + " added!");
            } else {
                JOptionPane.showMessageDialog(this, validator.getFormattedErrors(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Enter valid numbers!", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void runSimulation() {
        if (processes.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Add at least one process first!");
            return;
        }
        
        int quantum;
        try {
            quantum = Integer.parseInt(txtQuantum.getText().trim());
            if (quantum <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Quantum must be positive integer!");
            return;
        }
        
        // Round Robin
        RoundRobinScheduler rr = new RoundRobinScheduler(quantum);
        rr.simulate(processes);
        rrGanttArea.setText(rr.getGanttChartString());
        rrTableModel.setRowCount(0);
        for (Process p : rr.getResults()) {
            rrTableModel.addRow(new Object[]{p.getId(), p.getWaitingTime(), p.getTurnaroundTime(), p.getResponseTime()});
        }
        
        // Priority
        PriorityScheduler ps = new PriorityScheduler();
        ps.simulate(processes);
        priorityGanttArea.setText(ps.getGanttChartString());
        priorityTableModel.setRowCount(0);
        for (Process p : ps.getResults()) {
            priorityTableModel.addRow(new Object[]{p.getId(), p.getWaitingTime(), p.getTurnaroundTime(), p.getResponseTime()});
        }
        
        // Comparison
        double rrWT = MetricsCalculator.calculateAverageWaitingTime(rr.getResults());
        double rrTAT = MetricsCalculator.calculateAverageTurnaroundTime(rr.getResults());
        double rrRT = MetricsCalculator.calculateAverageResponseTime(rr.getResults());
        double pWT = MetricsCalculator.calculateAverageWaitingTime(ps.getResults());
        double pTAT = MetricsCalculator.calculateAverageTurnaroundTime(ps.getResults());
        double pRT = MetricsCalculator.calculateAverageResponseTime(ps.getResults());
        
        StringBuilder comp = new StringBuilder();
        comp.append(MetricsCalculator.formatComparisonTable(rrWT, pWT, rrTAT, pTAT, rrRT, pRT));
        comp.append("\n\nANALYSIS:\n");
        if (rrWT < pWT) comp.append("✓ Round Robin has LOWER waiting time\n");
        else if (pWT < rrWT) comp.append("✓ Priority has LOWER waiting time\n");
        
        if (rrRT < pRT) comp.append("✓ Round Robin has FASTER response time\n");
        else if (pRT < rrRT) comp.append("✓ Priority has FASTER response time\n");
        
        comparisonArea.setText(comp.toString());
    }
    
    private void clearAll() {
        processes.clear();
        tableModel.setRowCount(0);
        rrTableModel.setRowCount(0);
        priorityTableModel.setRowCount(0);
        rrGanttArea.setText("");
        priorityGanttArea.setText("");
        comparisonArea.setText("");
    }
    
    public static void main(String[] args) {
        new MainGUI();
    }
}