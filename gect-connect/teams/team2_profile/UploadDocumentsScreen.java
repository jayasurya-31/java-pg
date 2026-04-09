package teams.team2_profile;

import core.User;
import shared.*;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;
import java.util.Map;

/**
 * GUI screen for uploading user documents.
 */
public class UploadDocumentsScreen extends JPanel {
    private final ProfileService profileService;
    private final JPanel documentsPanel;

    public UploadDocumentsScreen(ProfileService profileService) {
        this.profileService = profileService;
        setLayout(new BorderLayout());
        UIUtils.stylePanel(this);

        JLabel title = new JLabel("  My Documents");
        title.setFont(new Font(UIUtils.FONT_FAMILY, Font.BOLD, UIUtils.HEADING_M));
        title.setPreferredSize(new Dimension(0, 50));
        title.setOpaque(true);
        title.setBackground(Color.WHITE);
        add(title, BorderLayout.NORTH);

        documentsPanel = new JPanel();
        documentsPanel.setLayout(new BoxLayout(documentsPanel, BoxLayout.Y_AXIS));
        UIUtils.stylePanel(documentsPanel);

        JScrollPane scroll = new JScrollPane(documentsPanel);
        scroll.setBorder(null);
        add(scroll, BorderLayout.CENTER);

        JButton uploadBtn = UIUtils.createRoundedButton("Upload New Document", UIUtils.PRIMARY_COLOR, Color.WHITE);
        uploadBtn.setPreferredSize(new Dimension(200, 50));
        uploadBtn.addActionListener(e -> uploadDocument());
        add(uploadBtn, BorderLayout.SOUTH);
    }

    public void refreshDocuments() {
        User currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser == null) return;

        new Thread(() -> {
            Map<String, String> docs = profileService.getDocuments();
            SwingUtilities.invokeLater(() -> {
                documentsPanel.removeAll();
                if (docs.isEmpty()) {
                    documentsPanel.add(new JLabel("No documents uploaded yet.", JLabel.CENTER));
                } else {
                    for (Map.Entry<String, String> entry : docs.entrySet()) {
                        documentsPanel.add(createDocumentCard(entry.getKey(), entry.getValue()));
                    }
                }
                documentsPanel.revalidate();
                documentsPanel.repaint();
            });
        }).start();
    }

    private void uploadDocument() {
        JFileChooser jfc = new JFileChooser();
        jfc.setFileFilter(new FileNameExtensionFilter("Documents", "pdf", "docx", "doc", "jpg", "png"));
        if (jfc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File selectedFile = jfc.getSelectedFile();
            if (profileService.uploadDocument(selectedFile.getName(), selectedFile.getAbsolutePath())) {
                UIUtils.showToast("Document Uploaded!", this);
                refreshDocuments();
            }
        }
    }

    private JPanel createDocumentCard(String name, String path) {
        JPanel card = new JPanel(new BorderLayout(15, 0));
        card.setBackground(Color.WHITE);
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));
        card.setPreferredSize(new Dimension(0, 70));
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, UIUtils.BORDER_COLOR),
            BorderFactory.createEmptyBorder(10, 20, 10, 20)
        ));

        JLabel info = new JLabel(name);
        info.setFont(new Font(UIUtils.FONT_FAMILY, Font.BOLD, 16));
        info.setIcon(Icons.get("document_icon", 40));
        info.setIconTextGap(15);
        
        JButton removeBtn = UIUtils.createRoundedButton("Delete", Color.WHITE, Color.RED);
        removeBtn.setPreferredSize(new Dimension(100, 40));
        removeBtn.addActionListener(e -> {
            if (JOptionPane.showConfirmDialog(this, "Remove " + name + "?", "Confirm", 0) == 0) {
                if (profileService.deleteDocument(name)) {
                    refreshDocuments();
                }
            }
        });

        card.add(info, BorderLayout.CENTER);
        card.add(removeBtn, BorderLayout.EAST);
        return card;
    }
}
