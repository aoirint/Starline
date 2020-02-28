package com.github.aoirint.starline;

import com.github.aoirint.starline.tree.StarlineTree;
import com.github.aoirint.starline.node.StarlineNode;
import com.github.aoirint.starline.tree.StarlineNodeTreeDelegate;
import com.github.aoirint.starline.tree.StarlineTreeCellRenderer;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.plaf.basic.BasicSplitPaneDivider;
import javax.swing.plaf.basic.BasicSplitPaneUI;
import javax.swing.tree.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.List;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class Starline extends JFrame implements StarlineNodeTreeDelegate, StarlinePopupMenuDelegate {
    public static final String APPLICATION_NAME = "Starline";
    public static final String APPLICATION_VERSION = "0.1.0";
    public static final int DOCUMENT_VERSION = 0;

    public static Logger logger = Logger.getLogger("Starline");

    public static void main(String[] args) {
        ConsoleHandler handler = new ConsoleHandler();
        handler.setLevel(Level.ALL);
        logger.addHandler(handler);
        logger.setLevel(Level.ALL);

        UIManager.getDefaults().put("SplitPane.border", BorderFactory.createEmptyBorder());
        UIManager.getDefaults().put("ScrollPane.border", BorderFactory.createEmptyBorder());
        UIManager.getDefaults().put("PopupMenu.border", BorderFactory.createEmptyBorder());

        Starline starline = new Starline();
        if (args.length > 1) {
            String filePathString = args[0];
            Path filePath = Paths.get(filePathString);

            starline.loadDocument(filePath);
        }

        starline.setVisible(true);
    }

    StarlineTree tree;
    JTextField titleTextField;
    JTextArea contentTextArea;
    JTextField searchTextField;
    boolean ignoreDocumentEvent;

    Color treeBackgroundColor;
    Color titleBorderColor;
    Color backgroundColor;
    Color foregroundColor;
    Color scrollBackgroundColor;
    Color scrollForegroundColor;
    Color caretColor;
    Font titleFont;
    Font contentFont;

    StarlineNode activatedNode;

    Path currentFilePath;
    Map savedDocument = null;
    boolean isModified;

    public Starline() {
        initLookAndFeel();

        setSize(1280, 720);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setBackground(backgroundColor);
        setForeground(foregroundColor);
        setLayout(new BorderLayout());
        setLocationRelativeTo(null);

        initMainView();
        initMenuBar();

        initDocument();

        selectFirstNode();
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowOpened(WindowEvent e) {
                titleTextField.requestFocus();
            }

            @Override
            public void windowClosing(WindowEvent e) {
                doExitFileMenuAction();
            }
        });

        addFileActions(getRootPane());
        addFileActionKeys(getRootPane(), JComponent.WHEN_IN_FOCUSED_WINDOW);

    }

    public void addFileActions(JComponent component) {
        ActionMap actionMap = component.getActionMap();

        actionMap.put("file.new", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                doNewFileMenuAction();
            }
        });
        actionMap.put("file.open", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                doOpenFileMenuAction();
            }
        });
        actionMap.put("file.save", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                doSaveFileMenuAction();
            }
        });
        actionMap.put("file.saveas", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                doSaveAsFileMenuAction();
            }
        });
        actionMap.put("file.exit", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                doExitFileMenuAction();
            }
        });
    }

    public void addFileActionKeys(JComponent component, int condition) {
        KeyStroke fileNewKey = KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.CTRL_DOWN_MASK);
        KeyStroke fileOpenKey = KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_DOWN_MASK);
        KeyStroke fileSaveKey = KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK);
        KeyStroke fileSaveAsKey = KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK);
        KeyStroke fileExitAsKey = KeyStroke.getKeyStroke(KeyEvent.VK_W, InputEvent.CTRL_DOWN_MASK);
        InputMap inputMap = component.getInputMap(condition);

        inputMap.put(fileNewKey, "file.new");
        inputMap.put(fileOpenKey, "file.open");
        inputMap.put(fileSaveKey, "file.save");
        inputMap.put(fileSaveAsKey, "file.saveas");
        inputMap.put(fileExitAsKey, "file.exit");
    }

    public boolean confirmModified() {
        if (isModified) {
            JOptionPane confirmPane = new JOptionPane("Current document is unsaved. Save before exit?", JOptionPane.WARNING_MESSAGE, JOptionPane.YES_NO_CANCEL_OPTION);
            JDialog confirmDialog = confirmPane.createDialog(Starline.this, null);

            confirmPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "input.escape");
            confirmPane.getActionMap().put("input.escape", new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    confirmPane.setValue(JOptionPane.CANCEL_OPTION);
                    confirmDialog.dispose();
                }
            });
            confirmDialog.setVisible(true);

            int result = (int) confirmPane.getValue();

            if (result == JOptionPane.CANCEL_OPTION) return false;
            if (result == JOptionPane.YES_OPTION) {
                boolean success = doSaveFileMenuAction();
                if (! success) return false;
            }

            return true;
        }

        return true;
    }

    public void initDocument() {
        ignoreDocumentEvent = true;

        StarlineNode rootNode = new StarlineNode();
        tree.setRootNode(rootNode);

        selectFirstNode();

        currentFilePath = null;
        savedDocument = rootNode.serialize();

        titleTextField.requestFocus();
        updateModifiedState();

        ignoreDocumentEvent = false;
    }

    public void selectFirstNode() {
        tree.setSelectionRow(0);
    }

    public String getRootTitle() {
        return tree.getRootNode().nodeData.title;
    }

    private void initLookAndFeel() {
        treeBackgroundColor = new Color(60, 60, 60);
        titleBorderColor = new Color(60, 60, 60);
        backgroundColor = new Color(40, 40, 40);
        foregroundColor = new Color(255, 255, 255);
        caretColor = new Color(255, 255, 255);
        scrollBackgroundColor = backgroundColor;
        scrollForegroundColor = new Color(120, 120, 120);

        titleFont = new Font(Font.SANS_SERIF, Font.PLAIN, 24);
        contentFont = new Font(Font.SANS_SERIF, Font.PLAIN, 12);
    }

    private void initTree() {
        StarlineTree tree = new StarlineTree();
        tree.setBackground(treeBackgroundColor);
        tree.setForeground(foregroundColor);

        StarlineTreeCellRenderer cellRenderer = new StarlineTreeCellRenderer();
        tree.setCellRenderer(cellRenderer);
        tree.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));

        tree.delegate = this;

        this.tree = tree;
    }

    private void initTitleTextField() {
        JTextField textField = new JTextField();

        textField.setBackground(backgroundColor);
        textField.setForeground(foregroundColor);
        textField.setCaretColor(caretColor);
        textField.setFont(titleFont);

        Border matteBorder = BorderFactory.createMatteBorder(0, 0, 2, 0, titleBorderColor);
        Border paddingBorder = BorderFactory.createEmptyBorder(8, 8, 8, 8);
        textField.setBorder(BorderFactory.createCompoundBorder(matteBorder, paddingBorder));

        LimitedTextDocument textDocument = new LimitedTextDocument(256);
        textField.setDocument(textDocument);

        textDocument.addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                changedUpdate(e);
            }
            @Override
            public void removeUpdate(DocumentEvent e) {
                changedUpdate(e);
            }
            @Override
            public void changedUpdate(DocumentEvent e) {
                onTitleTextUpdated();
            }
        });
        textField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent event) {
                if (event.getKeyCode() == KeyEvent.VK_ENTER) {
                    onEnteredInTitle();
                }
                if (event.isControlDown() && event.getKeyCode() == KeyEvent.VK_F) {
                    onInputSearchCommand();
                }
            }
        });

        this.titleTextField = textField;
    }

    private void initContentTextArea() {
        JTextArea textArea = new JTextArea();
        textArea.setBackground(backgroundColor);
        textArea.setForeground(foregroundColor);
        textArea.setCaretColor(caretColor);
        textArea.setFont(contentFont);
        textArea.setLineWrap(true);
        textArea.setTabSize(2);

        Border paddingBorder = BorderFactory.createEmptyBorder(8, 8, 8, 8);
        textArea.setBorder(paddingBorder);

        textArea.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                changedUpdate(e);
            }
            @Override
            public void removeUpdate(DocumentEvent e) {
                changedUpdate(e);
            }
            @Override
            public void changedUpdate(DocumentEvent e) {
                onContentTextUpdated();
            }
        });

        textArea.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent event) {
                if (event.isControlDown() && event.getKeyCode() == KeyEvent.VK_F) {
                    onInputSearchCommand();
                }
            }
        });

        this.contentTextArea = textArea;
    }

    private void initSearchTextField() {
        JTextField textField = new JTextField();

        textField.setBackground(backgroundColor);
        textField.setForeground(foregroundColor);
        textField.setCaretColor(caretColor);
        textField.setFont(titleFont);

        Border matteBorder = BorderFactory.createMatteBorder(2, 0, 0, 0, titleBorderColor);
        Border paddingBorder = BorderFactory.createEmptyBorder(8, 8, 8, 8);
        textField.setBorder(BorderFactory.createCompoundBorder(matteBorder, paddingBorder));

        textField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent event) {
                if (event.getKeyCode() == KeyEvent.VK_ENTER) {
                    onEnteredInSearch();
                }
                if (event.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    onInputEscapeInSearch();
                }
            }
        });

        this.searchTextField = textField;
    }

    private void initMainView() {
        initTree();
        initTitleTextField();
        initContentTextArea();
        initSearchTextField();

        assert tree != null;
        assert contentTextArea != null;
        assert titleTextField != null;

        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new BorderLayout());

        StarlineScrollPane scrollPaneTree = new StarlineScrollPane(tree);
        leftPanel.setMinimumSize(new Dimension(100, 0));
        leftPanel.add(scrollPaneTree);

        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(new BorderLayout());
        rightPanel.add(titleTextField, BorderLayout.NORTH);

        StarlineScrollPane scrollPaneTextArea = new StarlineScrollPane(contentTextArea);
        rightPanel.add(scrollPaneTextArea, BorderLayout.CENTER);

        rightPanel.add(searchTextField, BorderLayout.SOUTH);

        JSplitPane splitPane = new JSplitPane();
        BasicSplitPaneUI splitPaneUI = (BasicSplitPaneUI) splitPane.getUI();
        BasicSplitPaneDivider divider = splitPaneUI.getDivider();

        divider.setBackground(backgroundColor);
        divider.setBorder(null);
        splitPane.setBackground(backgroundColor);
        splitPane.setBorder(null);
        splitPane.setDividerSize(4);
        splitPane.setContinuousLayout(true);
        splitPane.setUI(new BasicSplitPaneUI()
        {
            @Override
            public BasicSplitPaneDivider createDefaultDivider()
            {
                return new BasicSplitPaneDivider(this)
                {
                    public void setBorder(Border b) {}
                    @Override
                    public void paint(Graphics g)
                    {
                        g.setColor(backgroundColor);
                        g.fillRect(0, 0, getSize().width, getSize().height);
                        super.paint(g);
                    }
                };
            }
        });

        splitPane.setDividerLocation(200);
        splitPane.setLeftComponent(leftPanel);
        splitPane.setRightComponent(rightPanel);

        add(splitPane);
    }

    private void initMenuBar() {
        StarlineMenuBar menuBar = new StarlineMenuBar();
        menuBar.delegate = new StarlineMenuBarDelegate() {
            @Override
            public void onNewFileMenu() {
                doNewFileMenuAction();
            }
            @Override
            public void onOpenFileMenu() {
                doOpenFileMenuAction();
            }
            @Override
            public void onSaveFileMenu() {
                doSaveFileMenuAction();
            }
            @Override
            public void onSaveAsFileMenu() {
                doSaveAsFileMenuAction();
            }
            @Override
            public void onExitFileMenu() {
                doExitFileMenuAction();
            }
        };

        setJMenuBar(menuBar);
    }


    boolean checkIfModified() {
        if (savedDocument != null) {
            StarlineNode rootNode = tree.getRootNode();
            Map serializedRootNode = rootNode.serialize();

            if (Objects.equals(savedDocument, serializedRootNode)) {
                return false;
            }
        }

        return true;
    }

    boolean saveDocument(Path filePath) {
        long ts = System.currentTimeMillis();

        DumperOptions dumperOptions = new DumperOptions();
        dumperOptions.setIndent(2);
        dumperOptions.setPrettyFlow(true);
        dumperOptions.setDefaultScalarStyle(DumperOptions.ScalarStyle.FOLDED);
        dumperOptions.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);

        Yaml yaml = new Yaml(dumperOptions);

        StarlineNode rootNode = tree.getRootNode();
        Map serializedRootNode = rootNode.serialize();

        Map document = new LinkedHashMap(); // keeping order
        document.put("fmt_version", DOCUMENT_VERSION);
        document.put("app_version", APPLICATION_VERSION);
        document.put("root", serializedRootNode);

        String yamlString = yaml.dump(document);

        try {
            Files.writeString(filePath, yamlString, StandardCharsets.UTF_8);
        } catch (IOException error) {
            error.printStackTrace();
            return false;
        }

        currentFilePath = filePath;
        savedDocument = serializedRootNode;

        updateModifiedState();

        long elapsedMillis = System.currentTimeMillis() - ts;
        double elapsedSec = (double)elapsedMillis / 1000d;
        logger.info(String.format("Document saved: %f s", elapsedSec));

        return true;
    }

    boolean loadDocument(Path filePath) {
        ignoreDocumentEvent = true;
        long ts = System.currentTimeMillis();

        initDocument();

        Yaml yaml = new Yaml();
        StarlineNode rootNode = tree.getRootNode();

        String yamlString;
        try {
            yamlString = Files.lines(filePath, StandardCharsets.UTF_8)
                    .collect(Collectors.joining(System.getProperty("line.separator")));
        } catch (IOException e) {
            e.printStackTrace();
            ignoreDocumentEvent = false;
            return false;
        }

        Map document = (Map) yaml.load(yamlString);
        Map serializedRootNode = (Map) document.get("root");
        rootNode.deserialize(serializedRootNode);

        titleTextField.setText(rootNode.nodeData.title);
        contentTextArea.setText(rootNode.nodeData.content);

        currentFilePath = filePath;
        savedDocument = serializedRootNode;

        tree.loadNodeStates();

        tree.updateUI();
        selectFirstNode();
        titleTextField.requestFocus();

        updateModifiedState();

        long elapsedMillis = System.currentTimeMillis() - ts;
        double elapsedSec = (double)elapsedMillis / 1000d;
        logger.info(String.format("Document loaded: %f s", elapsedSec));

        ignoreDocumentEvent = false;
        return true;
    }

    JFileChooser createFileDialog() {
        JFileChooser fileChooser = new JFileChooser();

        FileFilter fileFilter = new FileNameExtensionFilter("Starline Document (*.sldoc, *.yaml)", "sldoc", "yaml");
        fileChooser.addChoosableFileFilter(fileFilter);
        fileChooser.setFileFilter(fileFilter);

        return fileChooser;
    }

    Path showOpenFileDialog() {
        if (! confirmModified()) return null;

        JFileChooser fileChooser = createFileDialog();
        int result = fileChooser.showOpenDialog(Starline.this);
        if (result != JFileChooser.APPROVE_OPTION) {
            return null;
        }

        Path filePath = fileChooser.getSelectedFile().toPath();
        return filePath;
    }

    Path showSaveFileDialog() {
        JFileChooser fileChooser = createFileDialog();
        String rootTitle = getRootTitle();
        rootTitle = rootTitle.replace(".", "");

        final int limit = 32;
        if (limit < rootTitle.length())
            rootTitle = rootTitle.substring(0, limit);
        if (! rootTitle.isEmpty())
            rootTitle += ".sldoc";

        fileChooser.setSelectedFile(new File(rootTitle));

        int result = fileChooser.showSaveDialog(Starline.this);
        if (result != JFileChooser.APPROVE_OPTION) {
            return null;
        }

        Path filePath = fileChooser.getSelectedFile().toPath();
        {
            String fileName = filePath.getFileName().toString();
            if (fileName.endsWith(".sldoc")) {
            }
            else if (fileName.endsWith(".yaml")) {
            }
            else {
                fileName += ".sldoc";
                filePath = filePath.getParent().resolve(fileName);
            }
        }

        if (Files.exists(filePath)) {
            int confirmResult = JOptionPane.showConfirmDialog(Starline.this, String.format("%s already exists, overwrite?", filePath.getFileName()), "", JOptionPane.YES_NO_OPTION);
            if (confirmResult != JOptionPane.YES_OPTION) return null;
        }

        return filePath;
    }


    public void onEnteredInTitle() {
        contentTextArea.requestFocus();
    }

    public void onEnteredInSearch() {
        logger.info(searchTextField.getText());
    }

    public void onInputSearchCommand() {
        searchTextField.requestFocus();
    }

    public void onInputEscapeInSearch() {
        contentTextArea.requestFocus();
    }

    public void updateModifiedState() {
        boolean isModified = checkIfModified();

        StringBuilder fileInfo = new StringBuilder();
        String currentFileName = currentFilePath != null ? currentFilePath.getFileName().toString(): "New file";
        if (isModified) fileInfo.append('*');
        fileInfo.append(currentFileName);

        StringBuilder appInfo = new StringBuilder();
        appInfo.append(APPLICATION_NAME);

        String title = String.format("%s - %s", fileInfo.toString(), appInfo.toString());
        setTitle(title);

        this.isModified = isModified;
    }

    public void onTitleTextUpdated() {
        if (ignoreDocumentEvent) return;

        ignoreDocumentEvent = true;
        if (activatedNode != null) {
            activatedNode.nodeData.title = titleTextField.getText();
            tree.updateUI();

            updateModifiedState();
        }
        ignoreDocumentEvent = false;
    }

    public void onContentTextUpdated() {
        if (ignoreDocumentEvent) return;

        ignoreDocumentEvent = true;
        if (activatedNode != null) {
            activatedNode.nodeData.content = contentTextArea.getText();

            updateModifiedState();
        }
        ignoreDocumentEvent = false;
    }

    @Override
    public void onNodeActivated(StarlineTree tree, StarlineNode newActivatedNode, StarlineNode oldActivatedNode) {
        ignoreDocumentEvent = true;

        if (newActivatedNode == null) {
            if (oldActivatedNode != null) {
                tree.setSelectionPath(new TreePath(oldActivatedNode.getPath()));
                return;
            }
        }

        // logger.fine("selected " + newActivatedNode);
        if (newActivatedNode != null) {
            titleTextField.setText(newActivatedNode.nodeData.title);
            contentTextArea.setText(newActivatedNode.nodeData.content);
        }

        ignoreDocumentEvent = false;
        this.activatedNode = newActivatedNode;
    }

    @Override
    public void onNodeRightClicked(StarlineTree tree, TreePath cursorPath, StarlineNode cursorNode, MouseEvent event) {
        final List<StarlineNode> firstSelectedNodes = tree.getSelectedNodes();
        if (firstSelectedNodes.size() == 1) {
            tree.setSelectionPath(cursorPath);
        }

        final StarlineNode rootNode = tree.getRootNode();
        final List<StarlineNode> selectedNodes = tree.getSelectedNodes();

        StarlinePopupMenu popupMenu = new StarlinePopupMenu();
        popupMenu.cursorPath = cursorPath;
        popupMenu.cursorNode = cursorNode;

        popupMenu.addMenuItem.setEnabled(selectedNodes.size() == 1);
        popupMenu.deleteMenuItem.setEnabled(selectedNodes.size() > 0 && !selectedNodes.contains(rootNode));
        popupMenu.delegate = this;

        popupMenu.show(tree, event.getX(), event.getY());
    }


    public boolean doNewFileMenuAction() {
        if (! confirmModified()) return false;
        initDocument();
        return true;
    }
    public boolean doOpenFileMenuAction() {
        Path filePath = showOpenFileDialog();
        if (filePath == null) return false;
        return loadDocument(filePath);
    }

    public boolean doSaveFileMenuAction() {
        if (currentFilePath == null) return doSaveAsFileMenuAction();
        return saveDocument(currentFilePath);
    }

    public boolean doSaveAsFileMenuAction() {
        Path filePath = showSaveFileDialog();
        if (filePath == null) return false;
        return saveDocument(filePath);
    }

    public void doExitFileMenuAction() {
        if (! confirmModified()) return;
        System.exit(0);
    }

    @Override
    public void onAddPopupMenu(StarlinePopupMenu popupMenu) {
        final List<StarlineNode> selectedNodes = tree.getSelectedNodes();

        StarlineNode newNode = new StarlineNode();
        selectedNodes.get(0).add(newNode);
        tree.expandPath(popupMenu.cursorPath);
        tree.updateUI();

        tree.setSelectionPath(popupMenu.cursorPath.pathByAddingChild(newNode));
        updateModifiedState();
    }

    @Override
    public void onDeletePopupMenu(StarlinePopupMenu popupMenu) {
        final StarlineNode rootNode = tree.getRootNode();
        // final List<StarlineNode> selectedNodes = tree.getSelectedNodes();

        TreePath parentPath = popupMenu.cursorPath.getParentPath();

        TreePath[] selectionPaths = tree.getSelectionPaths();
        int count = 0;
        for (TreePath selectionPath: selectionPaths) {
            StarlineNode node = (StarlineNode) selectionPath.getLastPathComponent();

            count += 1;
            count += node.countDescendants();
        }

        int result = JOptionPane.showConfirmDialog(this, String.format("%d node/s will be deleted. OK?", count), null, JOptionPane.YES_NO_OPTION);
        if (result != JOptionPane.YES_OPTION) return;

        for (TreePath selectionPath: selectionPaths) {
            StarlineNode selectionNode = (StarlineNode) selectionPath.getLastPathComponent();
            if (selectionNode == rootNode) continue;

            selectionNode.removeFromParent();
        }
        tree.updateUI();

        DefaultMutableTreeNode nextSiblingNode = popupMenu.cursorNode.getNextSibling();
        TreePath nextSiblingPath = nextSiblingNode != null ? new TreePath(nextSiblingNode.getPath()) : null;
        DefaultMutableTreeNode prevSiblingNode = popupMenu.cursorNode.getPreviousSibling();
        TreePath prevSiblingPath = prevSiblingNode != null ? new TreePath(prevSiblingNode.getPath()) : null;

        if (prevSiblingPath != null) {
            tree.setSelectionPath(prevSiblingPath);
        }
        else if (nextSiblingPath != null) {
            tree.setSelectionPath(nextSiblingPath);
        }
        else if (parentPath != null) {
            tree.setSelectionPath(parentPath);
        }
        else {
            selectFirstNode();
        }

        updateModifiedState();
    }

}
