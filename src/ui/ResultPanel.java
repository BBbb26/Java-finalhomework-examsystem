package ui;

import dao.ExamRecordDao;
import model.ExamRecord;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.text.SimpleDateFormat;
import java.util.List;

/**
 * 考试结果面板，显示当前学生的历史考试记录。
 */
public class ResultPanel extends JPanel {
    private String username;
    private ExamRecordDao examRecordDao = new ExamRecordDao();
    private DefaultTableModel tableModel;
    private JLabel summaryLabel;

    public ResultPanel(String username) {
        this.username = username;
        initView();
        loadRecords();
    }

    private void initView() {
        setLayout(new BorderLayout(0, 16));
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(28, 32, 28, 32));

        JLabel titleLabel = new JLabel("考试结果");
        titleLabel.setFont(new Font("Microsoft YaHei", Font.BOLD, 22));
        add(titleLabel, BorderLayout.NORTH);

        summaryLabel = new JLabel("暂无考试记录");
        summaryLabel.setFont(new Font("Microsoft YaHei", Font.PLAIN, 15));
        add(summaryLabel, BorderLayout.SOUTH);

        String[] columns = {"考试时间", "科目", "总分", "题目数", "正确题数", "错误题数"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        JTable table = new JTable(tableModel);
        table.setRowHeight(30);
        add(new JScrollPane(table), BorderLayout.CENTER);
    }

    public void loadRecords() {
        tableModel.setRowCount(0);
        List<ExamRecord> records = examRecordDao.getRecordsByUsername(username);
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        for (ExamRecord record : records) {
            String time = record.getExamTime() == null ? "" : format.format(record.getExamTime());
            tableModel.addRow(new Object[]{
                    time,
                    record.getBankName(),
                    record.getTotalScore(),
                    record.getTotalCount(),
                    record.getCorrectCount(),
                    record.getWrongCount()
            });
        }

        if (records.size() == 0) {
            summaryLabel.setText("暂无考试记录");
        }
    }

    public void showLatestRecord(ExamRecord record) {
        summaryLabel.setText("最近一次考试：" + record.getBankName()
                + "，得分 " + record.getTotalScore()
                + "，正确 " + record.getCorrectCount()
                + " 题，错误 " + record.getWrongCount() + " 题。");
    }
}
