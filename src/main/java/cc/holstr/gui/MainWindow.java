package cc.holstr.gui;

import cc.holstr.exception.ClassCompilationException;
import cc.holstr.gui.model.LineNumberingTextArea;
import cc.holstr.main.Runner;
import cc.holstr.main.UILTestManager;
import cc.holstr.main.model.UILTest;
import cc.holstr.main.model.UILTestResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Highlighter;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.TreeMap;

import static name.fraser.neil.plaintext.diff_match_patch.Diff;

/**
 * Created by Jason McElhenney on 1/18/18.
 * zudsniper on GitHub.
 */
public class MainWindow extends JFrame {

	private final Logger log = LoggerFactory.getLogger(this.getClass());

	private UILTestManager mgr;

	private TreeMap<String, UILTestResult.Outcome> testOutcomeDisplayMap;

	private String activeClassName;

	private JSplitPane main;
	private JSplitPane allAndInputViewPane;
	private JSplitPane classAndOutputPane;
	private JPanel classesListPanel;
	private JPanel landingPanel;
	private JPanel buttonPanel;

	private JTextArea expectedOutputViewer;
	private JTextArea actualOutputViewer;
	private JLabel expectedLabel;
	private JLabel actualLabel;
	private JTextArea classViewer;
	private JLabel classViewerStatus;
	private JTextArea classInputViewer;
	private JLabel classInputViewerLabel;
	private LineNumberingTextArea classViewerLNT;

	private JList classesList;

	private JButton reloadButton;
	private JButton testAllButton;
	private JButton openWorkingDirButton;

	private Font titleFont;
	private Font subtitleFont;
	private Font labelFont;
	private Font buttonFont;
	private Font textFont;
	private Font lineNumberFont;

	private Color mainGreen;
	private Color highlightGreen;
	private Color mainRed;
	private Color highlightRed;
	private Color expectedBorder;
	private Color actualBorder;
	private Color buttonText;
	private Color lineNumberBackground;
	private Color lineNumberText;

	private Action reloadAction;
	private Action runAction;
	private Action runAllAction;
	private Action debugAction;

	public MainWindow(UILTestManager mgr) {
		super("UIL Program Tester");

		this.mgr = mgr;
		buildGUIComponents();
		populateGUI();

		setVisible(true);
	}

	/**
	 * Initialise all GUI components and assemble GUI.
	 */
	private void buildGUIComponents() {
		main = new JSplitPane();
		main.setOrientation(JSplitPane.HORIZONTAL_SPLIT);
		main.setDividerSize(2);
		allAndInputViewPane = new JSplitPane();
		allAndInputViewPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
		allAndInputViewPane.setDividerSize(2);
		classAndOutputPane = new JSplitPane();
		classAndOutputPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
		classAndOutputPane.setDividerSize(2);
		classesListPanel = new JPanel(new BorderLayout());
		landingPanel = new JPanel();
		classesList = new JList();
		expectedOutputViewer = new JTextArea();
		actualOutputViewer = new JTextArea();

		//COLORS
		mainGreen = new Color(0,255,0);
		highlightGreen = new Color(106, 255, 163);
		mainRed = new Color(255,0,0);
		highlightRed = new Color(255, 95, 90);
		expectedBorder = new Color(200, 200, 200);
		actualBorder = new Color(81, 81, 81);
		buttonText = new Color(0, 0, 0);
		lineNumberBackground = new Color(240, 240, 240);
		lineNumberText = new Color(40, 40, 40);

		//FONTS
		titleFont = new Font("Helvetica",Font.BOLD, 26);
		subtitleFont = new Font("Helvetica",Font.ITALIC,20);
		labelFont = new Font("Helvetica",Font.BOLD,18);
		buttonFont = new Font("Helvetica",Font.ITALIC,14);
		textFont = new Font("Helvetica",Font.PLAIN,14);
		lineNumberFont = textFont;

		//CLASSESLIST
		JScrollPane classesListScrollPane = new JScrollPane(classesList);
		classesList.setLayoutOrientation(JList.VERTICAL);
		classesList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		classesListPanel.add(classesListScrollPane,BorderLayout.CENTER);

		//BUTTONS
		buttonPanel = new JPanel(new GridLayout(3,1));

		//reload
		reloadButton = new JButton("Reload");
		reloadButton.setFont(buttonFont);
		reloadButton.setForeground(buttonText);

		buttonPanel.add(reloadButton);

		//testAll
		testAllButton = new JButton("Run All");
		testAllButton.setFont(buttonFont);
		testAllButton.setForeground(buttonText);

		buttonPanel.add(testAllButton);

		//openWorkingDir
		openWorkingDirButton = new JButton("View Files");
		openWorkingDirButton.setFont(buttonFont);
		openWorkingDirButton.setForeground(buttonText);

		buttonPanel.add(openWorkingDirButton);

		classesListPanel.add(buttonPanel,BorderLayout.SOUTH);


		//LANDING PANEL
		JPanel titlesPanel = new JPanel(new GridLayout(2,1));
		JLabel title = new JLabel("CS UIL Program Tester", SwingConstants.CENTER);
		title.setFont(titleFont);
		JLabel subtitle = new JLabel(Runner.version + " by zudsniper", SwingConstants.CENTER);
		subtitle.setFont(subtitleFont);

		titlesPanel.add(title);
		titlesPanel.add(subtitle);

		landingPanel.add(titlesPanel);

		//CLASSVIEWPANEL
		JPanel inOutViewerPanel = new JPanel(new GridLayout(1,2,20,20));

		//expected/actual viewers
		expectedOutputViewer.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createLineBorder(expectedBorder),
				BorderFactory.createEmptyBorder(15, 15, 15, 15)));
		expectedOutputViewer.setFont(textFont);
		expectedOutputViewer.setEditable(false);
		expectedOutputViewer.setRows(10);

		actualOutputViewer.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createLineBorder(actualBorder),
				BorderFactory.createEmptyBorder(15, 15, 15, 15)));
		actualOutputViewer.setFont(textFont);
		actualOutputViewer.setEditable(false);
		actualOutputViewer.setRows(10);

		JScrollPane expectedScroll = new JScrollPane(expectedOutputViewer);
		JScrollPane actualScroll = new JScrollPane(actualOutputViewer);

		JPanel expectedPanel = new JPanel(new BorderLayout());
		JPanel actualPanel = new JPanel(new BorderLayout());

		expectedLabel = new JLabel("expected");
		expectedLabel.setFont(labelFont);
		actualLabel = new JLabel("actual");
		actualLabel.setFont(labelFont);

		expectedPanel.add(expectedLabel,BorderLayout.NORTH);
		expectedPanel.add(expectedScroll,BorderLayout.CENTER);
		actualPanel.add(actualLabel,BorderLayout.NORTH);
		actualPanel.add(actualScroll,BorderLayout.CENTER);

		inOutViewerPanel.add(expectedPanel);
		inOutViewerPanel.add(actualPanel);

		classAndOutputPane.setLeftComponent(inOutViewerPanel);

		//class viewer
		classViewer = new JTextArea();
		JScrollPane classViewerScroll = new JScrollPane(classViewer);
		classViewerLNT = new LineNumberingTextArea(classViewer);
		classViewerLNT.setFont(lineNumberFont);
		classViewerLNT.setBackground(lineNumberBackground);
		classViewerLNT.setForeground(lineNumberText);
		JPanel classViewerPanel = new JPanel(new BorderLayout());
		classViewerScroll.setRowHeaderView(classViewerLNT);

		classViewer.setEditable(false);
		classViewer.setColumns(10);

		classViewerStatus = new JLabel("Loading...");
		classViewerStatus.setFont(titleFont);

		classViewerPanel.add(classViewerStatus,BorderLayout.NORTH);
		classViewerPanel.add(classViewerScroll,BorderLayout.CENTER);
		classAndOutputPane.setRightComponent(classViewerPanel);

		//input viewer
		JPanel classInputViewerPanel = new JPanel(new BorderLayout());
		classInputViewerLabel = new JLabel("input");
		classInputViewerLabel.setFont(labelFont);

		classInputViewer = new JTextArea();
		JScrollPane classInputViewerScroll = new JScrollPane(classInputViewer);
		classInputViewer.setFont(textFont);
		classInputViewer.setRows(5);
		classInputViewer.setEditable(false);
		//classInputViewerScroll.setMinimumSize(new Dimension(487,60));
		classInputViewerPanel.add(classInputViewerLabel,BorderLayout.NORTH);
		classInputViewerPanel.add(classInputViewerScroll,BorderLayout.CENTER);

		allAndInputViewPane.setLeftComponent(classAndOutputPane);
		allAndInputViewPane.setRightComponent(classInputViewerPanel);

		//MAIN ADDITIONS
		main.setLeftComponent(classesListPanel);
		main.setRightComponent(landingPanel);

		add(main);

		//JFRAME OPTIONS
		inOutViewerPanel.setSize(new Dimension(580,210));
		classViewerPanel.setSize(new Dimension(580,270));
		setMinimumSize(new Dimension(614,400));
		setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		setSize(800,650);
		ImageIcon imageIcon = new ImageIcon(Objects.requireNonNull(getClass().getClassLoader().getResource("icon_512x512.png")));
		setIconImage(imageIcon.getImage());
	}

	/**
	 * add functionality to GUI
	 * adds customCellRenderer for test status colours
	 * adds all keyListeners & MouseListener
	 * adds button functionality
	 */
	private void populateGUI() {
		testOutcomeDisplayMap = new TreeMap<>();

		//list element coloring cellrenderer
		classesList.setCellRenderer(new DefaultListCellRenderer() {

			private boolean testExecuted = false;

			@Override
			public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
				Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
				String testName = (String) value;
				//coloring logic
				UILTestResult.Outcome outcome = testOutcomeDisplayMap.get(testName);

				if(isSelected && testExecuted) {
					setForeground(Color.BLACK);

				}

				if(outcome != null) {
					switch(outcome) {
						case PASS:
							setBackground(highlightGreen);
							break;
						case FAIL:
							setBackground(highlightRed);
							break;
						default:
							setBackground(Color.WHITE);
							break;
					}
				} else {
					testExecuted = true;
				}
				return c;
			}
		});

		populateList(false);

		//CLASSESLIST FUNC
		classesList.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				if(e.getClickCount() == 2) {
					testSelected((String) classesList.getSelectedValue());
				}
			}
		});
		classesList.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				if(e.getKeyCode() == KeyEvent.VK_ENTER) {
					testSelected((String) classesList.getSelectedValue());
				}
			}
		});


		createActions();
		//BUTTON FUNC

		//reload
		reloadButton.addActionListener(reloadAction);

		//testAll
		testAllButton.addActionListener(runAllAction);
		//openWorkingDir
		openWorkingDirButton.addActionListener(event -> {
			try {
				Desktop.getDesktop().open(new File(mgr.getPath()));
			} catch(IOException e) {
				error(e);
			}
		});

		//REAL SHORTCUTS
		main.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_R,InputEvent.CTRL_DOWN_MASK),
				"run");
		main.getActionMap().put("run",runAction);
		main.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_R, InputEvent.SHIFT_DOWN_MASK),
				"reload");
		main.getActionMap().put("reload",reloadAction);
		main.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_R, InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK),
				"runAll");
		main.getActionMap().put("runAll",runAllAction);

		if(Runner.DEBUG) {
			main.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_D,InputEvent.CTRL_DOWN_MASK),
					"debug");
			main.getActionMap().put("debug",debugAction);
		}

		//TEXTAREA LINE NUMBER DOCUMENT LISTENER
		classViewer.getDocument().addDocumentListener(new DocumentListener()
		{
			@Override
			public void insertUpdate(DocumentEvent documentEvent)
			{
				classViewerLNT.updateLineNumbers();
			}

			@Override
			public void removeUpdate(DocumentEvent documentEvent)
			{
				classViewerLNT.updateLineNumbers();
			}

			@Override
			public void changedUpdate(DocumentEvent documentEvent)
			{
				classViewerLNT.updateLineNumbers();
			}
		});

		//OLD SHORTCUT ADAPTER
		/*shortcutKeyAdapter = new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				super.keyPressed(e);
				if(e.getKeyCode() == KeyEvent.VK_R && e.isControlDown()) {
					if(activeClassName != null && !"".equals(activeClassName)) {
						log.info("reloading class " + activeClassName);
						testSelected(activeClassName);
					}
				}
			}
		};

		classViewer.addKeyListener(shortcutKeyAdapter);
		classesList.addKeyListener(shortcutKeyAdapter);
		allAndInputViewPane.addKeyListener(shortcutKeyAdapter);
		classInputViewer.addKeyListener(shortcutKeyAdapter);
		actualOutputViewer.addKeyListener(shortcutKeyAdapter);
		expectedOutputViewer.addKeyListener(shortcutKeyAdapter);*/
	}

	/**
	 * creates AbstractActions for all button/shortcut functionality
	 */
	public void createActions() {
		reloadAction = new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				populateList(true);
				classesList.grabFocus();
			}
		};

		runAction = new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if(activeClassName != null && !"".equals(activeClassName)) {
					log.info("reloading class " + activeClassName);
					testSelected(activeClassName);
				}
			}
		};

		runAllAction = new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					main.grabFocus();
					List<UILTestResult> results = mgr.testAll();
					results.forEach(s -> testExecuted(s));
					classesList.grabFocus();
				} catch (ClassCompilationException cce) {
					error(cce);
				}
			}
		};

		debugAction = new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				log.debug("inputTextArea dims: " + classInputViewer.getWidth() + "," + classInputViewer.getHeight());
				log.debug("classViewer dims: " + classViewer.getWidth() + "," + classViewer.getHeight());
				log.debug("OutputViewer HEIGHT: " + expectedOutputViewer.getHeight());
			}
		};
	}
	/**
	 * Fill lists with tests generated from manager
	 * @param override whether or not to override and reload tests
	 */
	public void populateList(boolean override) {
		if(mgr.getTestsList().size() < 1 || override) {
			testOutcomeDisplayMap.clear();
			try {
				mgr.populateTests();
			} catch(IOException e) {
				error(e);
			}
		}
		String[] classNames = mgr.getTestsList().stream().map(t -> t.getClassName()).toArray(String[]::new);
		classesList.setListData(classNames);
	}

	/**
	 * Run test and populate elements for the classViewer panel.
	 * @param testName name of the test to run
	 */
	private void testSelected(String testName) {
		activeClassName = testName;
		classViewerStatus.setText("Loading...");
		classViewerStatus.setForeground(Color.BLACK);

		main.setRightComponent(allAndInputViewPane);
//		displayPanel.remove(landingPanel);
//		displayPanel.add(allAndInputViewPane,BorderLayout.CENTER);

		UILTest test = mgr.getTest(testName);

		log.debug("TESTSELECTED test: " + test.shortToString());

		setTitle(testName + ".java");
		expectedLabel.setText("expected (" + test.getOutputName() + ")");
		if(test.getInputName()!=null) {
			classInputViewerLabel.setText("input (" + test.getInputName() + ")");
		} else {
			classInputViewerLabel.setText("input (none)");
		}

		SwingWorker<UILTestResult,Void> swingWorker = new SwingWorker<UILTestResult, Void>() {

			private UILTestResult result;

			@Override
			protected UILTestResult doInBackground() throws Exception {
				try {
					UILTest test = mgr.getTest(testName);
					classInputViewer.setText(test.getInput());
					expectedOutputViewer.setText(test.getOutput());
					classViewer.setText(test.getRawClassCode());
					result = mgr.test(test);
					return result;
				} catch(ClassCompilationException cce) {
					error(cce);
				}
				return null;
			}

			@Override
			public void done() {
				testExecuted(result);
			}
		};

		swingWorker.execute();
	}

	/**
	 * clean up and change labels after a test is executed.
	 * @param result the UILTestResult object to use for population
	 */
	private void testExecuted(UILTestResult result) {
		testOutcomeDisplayMap.put(result.getClassName(),result.getOutcome());
		actualOutputViewer.setText(result.getActualOutput());
		highlightByDiffs(result);
		classViewerStatus.setText(result.getClassName());
	}

	/**
	 * perform comparison highlighting using Google diff_match_patch for the actual and expected output JTextAreas
	 * @param result the UILTestResult object to use for comparison
	 */
	private void highlightByDiffs(UILTestResult result) {
		Highlighter.HighlightPainter deletionPainter = new DefaultHighlighter.DefaultHighlightPainter(highlightRed);
		Highlighter.HighlightPainter insertionPainter = new DefaultHighlighter.DefaultHighlightPainter(highlightGreen);

		Highlighter deletions = expectedOutputViewer.getHighlighter();
		Highlighter insertions = actualOutputViewer.getHighlighter();

		log.debug(result.getDifferences().toString());

		for(Diff diff : result.getDifferences()) {
			String text;
			switch(diff.operation) {
				case DELETE: {
					text = expectedOutputViewer.getText();
					int position = text.indexOf(diff.text);
					try {
						deletions.addHighlight(position, position+diff.text.length(), deletionPainter);
					} catch(BadLocationException e) {
						log.warn("Bad deletion highlight at " + position + "," + position+diff.text.length() + "!\ne: " + e.getMessage());
					}
				} break;
				case INSERT: {
					text = actualOutputViewer.getText();
					int position = text.indexOf(diff.text);
					try {
						insertions.addHighlight(position, position+diff.text.length(), insertionPainter);
					} catch(BadLocationException e) {
						log.warn("Bad insertion highlight at " + position + "," + position+diff.text.length() + "!\ne: " + e.getMessage());
					}
				} break;
			}
			/*if(!(diff.text.matches("\\s+") || diff.operation.equals(diff_match_patch.Operation.EQUAL))) {
				totalChanges++;
			}*/
		}

		if(result.getOutcome() == UILTestResult.Outcome.PASS) {
			classViewerStatus.setForeground(mainGreen);
		} else {
			classViewerStatus.setForeground(mainRed);
		}
	}

	/**
	 * log error and show user-friendly error message
	 * @param e
	 */
	private void error(Exception e) {
		JOptionPane.showMessageDialog(this,e.getMessage(),e.getClass().getSimpleName(),JOptionPane.ERROR_MESSAGE);
		log.error(e.getMessage());
		e.printStackTrace();
	}
}
