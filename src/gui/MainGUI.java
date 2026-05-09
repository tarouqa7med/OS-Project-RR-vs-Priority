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
import java.util.HashMap;
import java.util.Map;

public class MainGUI extends JFrame {
    
    private List<Process> processes;
    private Validator validator;
    
    private JTextField txtId, txtArrival, txtBurst, txtPriority, txtQuantum;
    private JTable processTable;
    private DefaultTableModel tableModel;
    private GanttPanel rrGanttPanel, priorityGanttPanel;
    private JTable rrResultsTable, priorityResultsTable;
    private DefaultTableModel rrTableModel, priorityTableModel;
    private JTextArea comparisonArea;
    
    private List<GanttEvent> lastRrEvents;
    private List<GanttEvent> lastPriorityEvents;
    
    public MainGUI() {
        processes = new ArrayList<>();
        validator = new Validator();
        lastRrEvents = new ArrayList<>();
        lastPriorityEvents = new ArrayList<>();
        
        setTitle("CPU Scheduling Simulator - Round Robin vs Priority");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1400, 900);
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
        mainSplitPane.setDividerLocation(350);
        
        // Gantt Charts Section (Top)
        JPanel topPanel = new JPanel(new GridLayout(1, 2, 10, 10));
        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Round Robin Gantt
        JPanel rrContainer = new JPanel(new BorderLayout());
        rrContainer.setBorder(BorderFactory.createTitledBorder("ROUND ROBIN SCHEDULER"));
        rrGanttPanel = new GanttPanel();
        rrGanttPanel.setBackground(Color.WHITE);
        rrGanttPanel.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        JScrollPane rrScroll = new JScrollPane(rrGanttPanel);
        rrScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        rrScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        rrContainer.add(rrScroll, BorderLayout.CENTER);
        topPanel.add(rrContainer);
        
        // Priority Gantt
        JPanel priorityContainer = new JPanel(new BorderLayout());
        priorityContainer.setBorder(BorderFactory.createTitledBorder("PRIORITY SCHEDULER (Preemptive)"));
        priorityGanttPanel = new GanttPanel();
        priorityGanttPanel.setBackground(Color.WHITE);
        priorityGanttPanel.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        JScrollPane priorityScroll = new JScrollPane(priorityGanttPanel);
        priorityScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        priorityScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        priorityContainer.add(priorityScroll, BorderLayout.CENTER);
        topPanel.add(priorityContainer);
        
        mainSplitPane.setTopComponent(topPanel);
        
        // Metrics Tables Section (Bottom)
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
        
        comparisonArea = new JTextArea(10, 80);
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
            String quantumText = txtQuantum.getText().trim();
            
            if (quantumText.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Time quantum cannot be empty!", 
                    "Invalid Quantum", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            quantum = Integer.parseInt(quantumText);
            
            if (quantum <= 0) {
                JOptionPane.showMessageDialog(this, "ERROR: Time quantum must be greater than zero (got: " + quantum + ")", 
                    "Invalid Quantum", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            if (quantum > 50) {
                int confirm = JOptionPane.showConfirmDialog(this, 
                    "WARNING: Time quantum is very large (" + quantum + ").\nThis may affect performance. Continue anyway?",
                    "Large Quantum Warning", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                if (confirm != JOptionPane.YES_OPTION) {
                    return;
                }
            }
            
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Please enter a valid integer for Time Quantum", 
                "Input Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        
        try {
            // Round Robin
            RoundRobinScheduler rr = new RoundRobinScheduler(quantum);
            rr.simulate(processes);
            lastRrEvents = rr.getGanttChart();
            rrGanttPanel.setGanttEvents(lastRrEvents);
            
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
            lastPriorityEvents = ps.getGanttChart();
            priorityGanttPanel.setGanttEvents(lastPriorityEvents);
            
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
        comparisonArea.setText("");
        
        lastRrEvents.clear();
        lastPriorityEvents.clear();
        rrGanttPanel.setGanttEvents(lastRrEvents);
        priorityGanttPanel.setGanttEvents(lastPriorityEvents);
        
        txtId.setText("");
        txtArrival.setText("");
        txtBurst.setText("");
        txtPriority.setText("");
        txtQuantum.setText("3");
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MainGUI());
    }
    
    // ===== INNER CLASS: Graphical Gantt Chart Panel =====
    class GanttPanel extends JPanel {
        private List<GanttEvent> events;
        private final Color[] colors = {
            new Color(66, 133, 244),  // Blue
            new Color(234, 67, 53),   // Red
            new Color(52, 168, 83),   // Green
            new Color(251, 188, 5),   // Yellow
            new Color(155, 66, 245),  // Purple
            new Color(255, 87, 34),   // Orange
            new Color(46, 125, 50),   // Dark Green
            new Color(194, 24, 91),   // Pink
            new Color(96, 125, 139),  // Blue Grey
            new Color(121, 85, 72)    // Brown
        };
        
        public GanttPanel() {
            events = new ArrayList<>();
            setBackground(Color.WHITE);
            setPreferredSize(new Dimension(800, 100));
        }
        
        public void setGanttEvents(List<GanttEvent> events) {
            this.events = events;
            repaint();
            revalidate();
        }
        
        @Override
        public Dimension getPreferredSize() {
            if (events == null || events.isEmpty()) {
                return new Dimension(800, 100);
            }
            
            int maxTime = 0;
            for (GanttEvent e : events) {
                if (e.getEndTime() > maxTime) maxTime = e.getEndTime();
            }
            
            int width = Math.max(800, maxTime * 45);
            return new Dimension(width, 100);
        }
        
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            if (events == null || events.isEmpty()) {
                g2d.setColor(Color.GRAY);
                g2d.setFont(new Font("Segoe UI", Font.ITALIC, 14));
                g2d.drawString("No Gantt chart available. Run simulation first.", 20, 50);
                return;
            }
            
            int y = 20;
            int height = 50;
            
            int maxTime = 0;
            for (GanttEvent e : events) {
                if (e.getEndTime() > maxTime) maxTime = e.getEndTime();
            }
            if (maxTime == 0) maxTime = 1;
            
            double scale = (double) (getWidth() - 80) / maxTime;
            
            Map<String, Color> processColors = new HashMap<>();
            int colorIndex = 0;
            int x = 40;
            
            for (GanttEvent e : events) {
                if (!processColors.containsKey(e.getProcessId())) {
                    processColors.put(e.getProcessId(), colors[colorIndex % colors.length]);
                    colorIndex++;
                }
                
                int duration = e.getEndTime() - e.getStartTime();
                int blockWidth = (int) (duration * scale);
                if (blockWidth < 3) blockWidth = 3;
                
                // Draw colored rectangle
                g2d.setColor(processColors.get(e.getProcessId()));
                g2d.fillRect(x, y, blockWidth, height);
                g2d.setColor(Color.BLACK);
                g2d.drawRect(x, y, blockWidth, height);
                
                // Draw process ID inside block
                g2d.setColor(Color.BLACK);
                g2d.setFont(new Font("Arial", Font.BOLD, 11));
                String text = e.getProcessId();
                int textWidth = g2d.getFontMetrics().stringWidth(text);
                if (blockWidth > textWidth + 6) {
                    g2d.drawString(text, x + (blockWidth - textWidth) / 2, y + height / 2 + 4);
                }
                
                // Draw duration if space permits
                if (blockWidth > 35) {
                    g2d.setFont(new Font("Arial", Font.PLAIN, 9));
                    String durText = duration + "";
                    int durWidth = g2d.getFontMetrics().stringWidth(durText);
                    if (blockWidth > durWidth + 10) {
                        g2d.setColor(Color.WHITE);
                        g2d.drawString(durText, x + (blockWidth - durWidth) / 2, y + height - 8);
                        g2d.setColor(Color.BLACK);
                    }
                }
                
                x += blockWidth;
            }
            
            // Draw time markers
            g2d.setColor(Color.GRAY);
            g2d.setFont(new Font("Arial", Font.PLAIN, 9));
            for (int t = 0; t <= maxTime; t += Math.max(1, maxTime / 10)) {
                int markerX = 40 + (int) (t * scale);
                if (markerX <= getWidth() - 20) {
                    g2d.drawLine(markerX, y + height, markerX, y + height + 5);
                    g2d.drawString(String.valueOf(t), markerX - 4, y + height + 18);
                }
            }
            
            // Draw axis label
            g2d.drawString("Time →", 10, y + height + 15);
        }
    }
}