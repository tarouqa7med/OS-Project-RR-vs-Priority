package gui;

import model.Process;
import model.GanttEvent;
import scheduler.RoundRobinScheduler;
import scheduler.PriorityScheduler;
import metrics.MetricsCalculator;
import util.Validator;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class MainGUI extends JFrame {
    
    private List<Process> processes;
    private Validator validator;
    
    private JTextField txtId, txtArrival, txtBurst, txtPriority, txtQuantum;
    private JTable processTable;
    private DefaultTableModel tableModel;
    private JTextArea rrGanttArea, priorityGanttArea;
    private JTable rrResultsTable, priorityResultsTable;
    private DefaultTableModel rrTableModel, priorityTableModel;
    private JTextArea comparisonArea;
    
    public MainGUI() {
        processes = new ArrayList<>();
        validator = new Validator();
        
        setTitle("CPU Scheduling Simulator - Round Robin vs Priority");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1300, 850);
        setLayout(new BorderLayout());
        
        createInputPanel();
        createCenterPanel();
        createBottomPanel();
        
        setVisible(true);
    }
    
    private void createInputPanel() {
        JPanel inputPanel = new JPanel(new BorderLayout());
        inputPanel.setBorder(BorderFactory.createTitledBorder("Input Panel"));
        
        JPanel fieldsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        fieldsPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        fieldsPanel.add(new JLabel("ID:"));
        txtId = new JTextField(5);
        fieldsPanel.add(txtId);
        
        fieldsPanel.add(new JLabel("Arrival:"));
        txtArrival = new JTextField(5);
        fieldsPanel.add(txtArrival);
        
        fieldsPanel.add(new JLabel("Burst:"));
        txtBurst = new JTextField(5);
        fieldsPanel.add(txtBurst);
        
        fieldsPanel.add(new JLabel("Priority (1-10):"));
        txtPriority = new JTextField(5);
        fieldsPanel.add(txtPriority);
        
        JButton btnAdd = new JButton("Add Process");
        btnAdd.setBackground(new Color(34, 197, 94));
        fieldsPanel.add(btnAdd);
        
        fieldsPanel.add(new JLabel("Quantum:"));
        txtQuantum = new JTextField("3", 5);
        fieldsPanel.add(txtQuantum);
        
        JButton btnRun = new JButton("RUN SIMULATION");
        btnRun.setBackground(new Color(59, 130, 246));
        btnRun.setForeground(Color.WHITE);
        btnRun.setFont(new Font("Segoe UI", Font.BOLD, 12));
        fieldsPanel.add(btnRun);
        
        JButton btnClear = new JButton("Clear All");
        btnClear.setBackground(new Color(239, 68, 68));
        btnClear.setForeground(Color.WHITE);
        fieldsPanel.add(btnClear);
        
        inputPanel.add(fieldsPanel, BorderLayout.NORTH);
        
        String[] columns = {"ID", "Arrival", "Burst", "Priority"};
        tableModel = new DefaultTableModel(columns, 0);
        processTable = new JTable(tableModel);
        processTable.setRowHeight(25);
        JScrollPane tableScroll = new JScrollPane(processTable);
        tableScroll.setBorder(BorderFactory.createTitledBorder("Added Processes"));
        tableScroll.setPreferredSize(new Dimension(800, 120));
        inputPanel.add(tableScroll, BorderLayout.CENTER);
        
        add(inputPanel, BorderLayout.NORTH);
        
        btnAdd.addActionListener(e -> addProcess());
        btnRun.addActionListener(e -> runSimulation());
        btnClear.addActionListener(e -> clearAll());
    }
    
    private void createCenterPanel() {
        JSplitPane mainSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        mainSplitPane.setResizeWeight(0.5);
        mainSplitPane.setDividerLocation(400);
        
        JPanel topPanel = new JPanel(new GridLayout(1, 2, 10, 10));
        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Round Robin Gantt
        JPanel rrPanel = new JPanel(new BorderLayout());
        rrPanel.setBorder(BorderFactory.createTitledBorder("ROUND ROBIN - Gantt Chart"));
        rrGanttArea = new JTextArea(5, 40);
        rrGanttArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        rrGanttArea.setEditable(false);
        rrGanttArea.setBackground(new Color(240, 255, 240));
        JScrollPane rrScroll = new JScrollPane(rrGanttArea);
        rrPanel.add(rrScroll, BorderLayout.CENTER);
        topPanel.add(rrPanel);
        
        // Priority Gantt
        JPanel priorityPanel = new JPanel(new BorderLayout());
        priorityPanel.setBorder(BorderFactory.createTitledBorder("PRIORITY - Gantt Chart"));
        priorityGanttArea = new JTextArea(5, 40);
        priorityGanttArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        priorityGanttArea.setEditable(false);
        priorityGanttArea.setBackground(new Color(255, 240, 240));
        JScrollPane priorityScroll = new JScrollPane(priorityGanttArea);
        priorityPanel.add(priorityScroll, BorderLayout.CENTER);
        topPanel.add(priorityPanel);
        
        mainSplitPane.setTopComponent(topPanel);
        
        // Metrics Tables
        JPanel bottomPanel = new JPanel(new GridLayout(1, 2, 10, 10));
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Round Robin Metrics
        JPanel rrMetricsPanel = new JPanel(new BorderLayout());
        rrMetricsPanel.setBorder(BorderFactory.createTitledBorder("ROUND ROBIN - Metrics"));
        String[] cols = {"ID", "WT", "TAT", "RT"};
        rrTableModel = new DefaultTableModel(cols, 0);
        rrResultsTable = new JTable(rrTableModel);
        rrResultsTable.setRowHeight(25);
        JScrollPane rrMetricScroll = new JScrollPane(rrResultsTable);
        rrMetricsPanel.add(rrMetricScroll, BorderLayout.CENTER);
        bottomPanel.add(rrMetricsPanel);
        
        // Priority Metrics
        JPanel priorityMetricsPanel = new JPanel(new BorderLayout());
        priorityMetricsPanel.setBorder(BorderFactory.createTitledBorder("PRIORITY - Metrics"));
        priorityTableModel = new DefaultTableModel(cols, 0);
        priorityResultsTable = new JTable(priorityTableModel);
        priorityResultsTable.setRowHeight(25);
        JScrollPane priorityMetricScroll = new JScrollPane(priorityResultsTable);
        priorityMetricsPanel.add(priorityMetricScroll, BorderLayout.CENTER);
        bottomPanel.add(priorityMetricsPanel);
        
        mainSplitPane.setBottomComponent(bottomPanel);
        add(mainSplitPane, BorderLayout.CENTER);
    }
    
    private void createBottomPanel() {
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBorder(BorderFactory.createTitledBorder("COMPARISON SUMMARY"));
        
        comparisonArea = new JTextArea(8, 80);
        comparisonArea.setEditable(false);
        comparisonArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        comparisonArea.setBackground(new Color(245, 248, 250));
        JScrollPane scroll = new JScrollPane(comparisonArea);
        bottomPanel.add(scroll, BorderLayout.CENTER);
        
        add(bottomPanel, BorderLayout.SOUTH);
    }
    
    private void addProcess() {
        try {
            String id = txtId.getText().trim();
            if (id.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Process ID cannot be empty!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            int arrival = Integer.parseInt(txtArrival.getText().trim());
            int burst = Integer.parseInt(txtBurst.getText().trim());
            int priority = Integer.parseInt(txtPriority.getText().trim());
            
            Process p = new Process(id, arrival, burst, priority);
            validator.clearErrors();
            
            if (validator.validateProcess(p)) {
                processes.add(p);
                tableModel.addRow(new Object[]{id, arrival, burst, priority});
                txtId.setText("");
                txtArrival.setText("");
                txtBurst.setText("");
                txtPriority.setText("");
                JOptionPane.showMessageDialog(this, "Process added!", "Success", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, validator.getFormattedErrors(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Please enter valid numbers", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private String formatGantt(List<GanttEvent> events) {
        if (events == null || events.isEmpty()) {
            return "No Gantt chart available. Run simulation first.";
        }
        StringBuilder sb = new StringBuilder();
        for (GanttEvent e : events) {
            sb.append(e.toString()).append(" ");
        }
        return sb.toString();
    }
    
    private String formatNumber(double value) {
        return String.format("%.2f", value);
    }
    
    private void runSimulation() {
        if (processes.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Add at least one process first!", "Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int quantum;
        try {
            quantum = Integer.parseInt(txtQuantum.getText().trim());
            if (quantum <= 0) {
                JOptionPane.showMessageDialog(this, "Quantum must be > 0", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid quantum value", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        
        try {
            // Round Robin
            RoundRobinScheduler rr = new RoundRobinScheduler(quantum);
            rr.simulate(processes);
            rrGanttArea.setText(formatGantt(rr.getGanttChart()));
            
            rrTableModel.setRowCount(0);
            for (Process p : rr.getResults()) {
                rrTableModel.addRow(new Object[]{
                    p.getId(),
                    formatNumber(p.getWaitingTime()),
                    formatNumber(p.getTurnaroundTime()),
                    formatNumber(p.getResponseTime())
                });
            }
            
            double rrWT = MetricsCalculator.calculateAverageWaitingTime(rr.getResults());
            double rrTAT = MetricsCalculator.calculateAverageTurnaroundTime(rr.getResults());
            double rrRT = MetricsCalculator.calculateAverageResponseTime(rr.getResults());
            
            // Priority
            PriorityScheduler ps = new PriorityScheduler();
            ps.simulate(processes);
            priorityGanttArea.setText(formatGantt(ps.getGanttChart()));
            
            priorityTableModel.setRowCount(0);
            for (Process p : ps.getResults()) {
                priorityTableModel.addRow(new Object[]{
                    p.getId(),
                    formatNumber(p.getWaitingTime()),
                    formatNumber(p.getTurnaroundTime()),
                    formatNumber(p.getResponseTime())
                });
            }
            
            double psWT = MetricsCalculator.calculateAverageWaitingTime(ps.getResults());
            double psTAT = MetricsCalculator.calculateAverageTurnaroundTime(ps.getResults());
            double psRT = MetricsCalculator.calculateAverageResponseTime(ps.getResults());
            
            // Comparison
            StringBuilder comp = new StringBuilder();
            comp.append("========== COMPARISON RESULTS ==========\n\n");
            comp.append(String.format("Round Robin - Avg WT: %.2f, Avg TAT: %.2f, Avg RT: %.2f\n", rrWT, rrTAT, rrRT));
            comp.append(String.format("Priority    - Avg WT: %.2f, Avg TAT: %.2f, Avg RT: %.2f\n\n", psWT, psTAT, psRT));
            
            if (psWT < rrWT) {
                comp.append("✓ Priority Scheduling has LOWER waiting time\n");
            } else {
                comp.append("✓ Round Robin has LOWER waiting time\n");
            }
            
            if (rrRT < psRT) {
                comp.append("✓ Round Robin has FASTER response time (better for interactivity)\n");
            } else {
                comp.append("✓ Priority Scheduling has FASTER response time\n");
            }
            
            comp.append("\nRecommendation:\n");
            if (psWT < rrWT && psRT < rrRT) {
                comp.append("  → Priority Scheduling is recommended for this workload\n");
            } else if (rrWT < psWT && rrRT < psRT) {
                comp.append("  → Round Robin is recommended for this workload\n");
            } else {
                comp.append("  → Use Priority for urgent tasks, Round Robin for fairness\n");
            }
            
            comparisonArea.setText(comp.toString());
            
        } finally {
            setCursor(Cursor.getDefaultCursor());
        }
    }
    
    private void clearAll() {
        processes.clear();
        validator.reset();
        tableModel.setRowCount(0);
        rrTableModel.setRowCount(0);
        priorityTableModel.setRowCount(0);
        rrGanttArea.setText("");
        priorityGanttArea.setText("");
        comparisonArea.setText("");
        txtId.setText("");
        txtArrival.setText("");
        txtBurst.setText("");
        txtPriority.setText("");
        txtQuantum.setText("3");
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MainGUI());
    }
}