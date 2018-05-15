package net.earthcomputer.lightningtool;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.text.DefaultCaret;

import org.jdesktop.swingx.autocomplete.AutoCompleteDecorator;

import net.earthcomputer.lightningtool.FortuneManipulator.Ore;
import net.earthcomputer.lightningtool.MobList.BiomeType;

@SuppressWarnings("all")
public class MainFrame extends JFrame {

	private AbstractManipulator manipulator;
	private boolean initializing = true;

	private JPanel contentPane;
	private JTextField searchFromXTextField;
	private JTextField searchFromZTextField;
	private final ButtonGroup buttonGroup = new ButtonGroup();
	private JProgressBar progressBar;
	private JRadioButton rdbtnDependOnMoon;
	private JRadioButton rdbtnChargedCreepers;
	private JRadioButton rdbtnHorseTraps;
	private JTextField worldSeedTextField;
	private JLabel lblOutput;
	private JTextField viewDistanceTextField;
	private JTextField wantedChunkIterationTextField;
	private JCheckBox chckbxExact;
	private JTextField extraRandCallsTextField;
	private JPanel thunderOptionsPanel;
	private JPanel rainOptionsPanel;
	private JTextField thunderTimeTextField;
	private JTextField rainTimeTextField;
	private JCheckBox chckbxManipulateThunder;
	private JCheckBox chckbxManipulateRain;
	private final ButtonGroup buttonGroup_1 = new ButtonGroup();
	private JRadioButton rdbtnThunderTurningOn;
	private final ButtonGroup buttonGroup_2 = new ButtonGroup();
	private JRadioButton rdbtnRainTurningOn;
	private JTextArea outputTextArea;
	private JTextField thunderOffTimeTextField;
	private JTextField thunderOnTimeTextField;
	private JTextField rainOffTimeTextField;
	private JTextField rainOnTimeTextField;
	private JTextField messageSendTimeTextField;
	private JCheckBox chckbxThunderOffUsesExtraRand;
	private JCheckBox chckbxThunderOnUsesExtraRand;
	private JCheckBox chckbxRainOffUsesExtraRand;
	private JCheckBox chckbxRainOnUsesExtraRand;
	private JComboBox mobTypeComboBox;
	private JComboBox biomeTypeComboBox;
	private JTable spawnPriorityPlayerTable;
	private JTable mobChunksTable;
	private JTextField dropAmountTextField;
	private JPanel xpPanel;
	private JTextField xpDroppedTextField;
	private JComboBox oreComboBox;
	private JCheckBox chckbxDropAmountExact;
	private JCheckBox chckbxManipulateXp;
	private JCheckBox chckbxXpExact;
	private JTextField fortuneLevelTextField;
	private JComboBox advancerComboBox;
	private JTextField ironViewDistanceTextField;
	private JTextField playerXInChunkTextField;
	private JTextField playerZInChunkTextField;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					MainFrame frame = new MainFrame();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public MainFrame() {
		setResizable(false);
		setTitle("Lightning Machine Tool");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 540, 424);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(new BorderLayout(0, 0));
		setContentPane(contentPane);

		JPanel panel_2 = new JPanel();
		contentPane.add(panel_2, BorderLayout.NORTH);
		panel_2.setLayout(new BoxLayout(panel_2, BoxLayout.Y_AXIS));

		JPanel panel_3 = new JPanel();
		panel_2.add(panel_3);

		JLabel lblSeed = new JLabel("World Gen Seed:");
		panel_3.add(lblSeed);

		worldSeedTextField = new JTextField();
		panel_3.add(worldSeedTextField);
		worldSeedTextField.setColumns(10);
		worldSeedTextField.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void changedUpdate(DocumentEvent e) {
			}

			@Override
			public void insertUpdate(DocumentEvent e) {
				MobSpawnCalculator.recalculate(MainFrame.this, true, true);
			}

			@Override
			public void removeUpdate(DocumentEvent e) {
				MobSpawnCalculator.recalculate(MainFrame.this, true, true);
			}
		});

		JPanel panel_1 = new JPanel();
		panel_2.add(panel_1);

		JLabel lblSearchFrom = new JLabel("Search from:");
		panel_1.add(lblSearchFrom);

		Component horizontalStrut = Box.createHorizontalStrut(20);
		panel_1.add(horizontalStrut);

		JLabel lblX = new JLabel("X:");
		panel_1.add(lblX);

		searchFromXTextField = new JTextField();
		searchFromXTextField.setText("0");
		panel_1.add(searchFromXTextField);
		searchFromXTextField.setColumns(10);
		searchFromXTextField.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void changedUpdate(DocumentEvent e) {
			}

			@Override
			public void insertUpdate(DocumentEvent e) {
				MobSpawnCalculator.recalculate(MainFrame.this, true, true);
			}

			@Override
			public void removeUpdate(DocumentEvent e) {
				MobSpawnCalculator.recalculate(MainFrame.this, true, true);
			}
		});

		Component horizontalStrut_1 = Box.createHorizontalStrut(20);
		panel_1.add(horizontalStrut_1);

		JLabel lblY = new JLabel("Z:");
		panel_1.add(lblY);

		searchFromZTextField = new JTextField();
		searchFromZTextField.setText("0");
		panel_1.add(searchFromZTextField);
		searchFromZTextField.setColumns(10);
		searchFromZTextField.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void changedUpdate(DocumentEvent e) {
			}

			@Override
			public void insertUpdate(DocumentEvent e) {
				MobSpawnCalculator.recalculate(MainFrame.this, true, true);
			}

			@Override
			public void removeUpdate(DocumentEvent e) {
				MobSpawnCalculator.recalculate(MainFrame.this, true, true);
			}
		});

		JPanel panel_8 = new JPanel();
		panel_2.add(panel_8);

		JLabel lblMaxExtraRand = new JLabel("Max extra rand calls:");
		panel_8.add(lblMaxExtraRand);

		extraRandCallsTextField = new JTextField();
		extraRandCallsTextField.setText("0");
		panel_8.add(extraRandCallsTextField);
		extraRandCallsTextField.setColumns(10);

		JLabel lblAdvancer = new JLabel("Advancer:");
		panel_8.add(lblAdvancer);

		advancerComboBox = new JComboBox();
		advancerComboBox.setModel(new DefaultComboBoxModel(RNGAdvancer.values()));
		panel_8.add(advancerComboBox);

		JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		contentPane.add(tabbedPane, BorderLayout.CENTER);

		JPanel lightningTab = new JPanel();
		tabbedPane.addTab("Lightning", null, lightningTab, null);
		lightningTab.setLayout(new BoxLayout(lightningTab, BoxLayout.Y_AXIS));

		rdbtnHorseTraps = new JRadioButton("Horse traps");
		rdbtnHorseTraps.setSelected(true);
		buttonGroup.add(rdbtnHorseTraps);
		lightningTab.add(rdbtnHorseTraps);

		rdbtnChargedCreepers = new JRadioButton("Charged creepers");
		buttonGroup.add(rdbtnChargedCreepers);
		lightningTab.add(rdbtnChargedCreepers);

		rdbtnDependOnMoon = new JRadioButton("Either (could depend on moon phase)");
		buttonGroup.add(rdbtnDependOnMoon);
		lightningTab.add(rdbtnDependOnMoon);

		JPanel panel_6 = new JPanel();
		FlowLayout flowLayout = (FlowLayout) panel_6.getLayout();
		flowLayout.setAlignment(FlowLayout.LEFT);
		flowLayout.setAlignOnBaseline(true);
		panel_6.setAlignmentX(Component.LEFT_ALIGNMENT);
		lightningTab.add(panel_6);

		JLabel lblViewDistance = new JLabel("View distance:");
		panel_6.add(lblViewDistance);

		viewDistanceTextField = new JTextField();
		viewDistanceTextField.setText("12");
		panel_6.add(viewDistanceTextField);
		viewDistanceTextField.setColumns(10);

		JButton btnSearch = new JButton("Search");
		btnSearch.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if (manipulator != null)
					manipulator.stop();
				manipulator = new LightningManipulator();
				manipulator.startSearch(MainFrame.this);
			}
		});

		JPanel panel_7 = new JPanel();
		FlowLayout flowLayout_1 = (FlowLayout) panel_7.getLayout();
		flowLayout_1.setAlignment(FlowLayout.LEFT);
		panel_7.setAlignmentX(0.0f);
		lightningTab.add(panel_7);

		JLabel lblWantedChunkIteration = new JLabel("Wanted chunk iteration:");
		panel_7.add(lblWantedChunkIteration);

		wantedChunkIterationTextField = new JTextField();
		wantedChunkIterationTextField.setText("0");
		wantedChunkIterationTextField.setColumns(10);
		panel_7.add(wantedChunkIterationTextField);

		chckbxExact = new JCheckBox("Exact");
		panel_7.add(chckbxExact);
		lightningTab.add(btnSearch);

		JPanel weatherTab = new JPanel();
		tabbedPane.addTab("Weather", null, weatherTab, null);
		weatherTab.setLayout(new BoxLayout(weatherTab, BoxLayout.Y_AXIS));

		chckbxManipulateThunder = new JCheckBox("Manipulate Thunder");
		chckbxManipulateThunder.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					setEnabled(thunderOptionsPanel, true);
				} else if (e.getStateChange() == ItemEvent.DESELECTED) {
					setEnabled(thunderOptionsPanel, false);
				}
			}
		});
		weatherTab.add(chckbxManipulateThunder);

		thunderOptionsPanel = new JPanel();
		thunderOptionsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		thunderOptionsPanel.setBorder(new LineBorder(UIManager.getColor("inactiveCaptionBorder")));
		weatherTab.add(thunderOptionsPanel);
		thunderOptionsPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 5));

		JLabel lblThunderTime = new JLabel("Thunder time:");
		thunderOptionsPanel.add(lblThunderTime);

		thunderTimeTextField = new JTextField();
		thunderTimeTextField.setAlignmentX(Component.LEFT_ALIGNMENT);
		thunderOptionsPanel.add(thunderTimeTextField);
		thunderTimeTextField.setColumns(10);

		rdbtnThunderTurningOn = new JRadioButton("Turning on");
		buttonGroup_1.add(rdbtnThunderTurningOn);
		rdbtnThunderTurningOn.setSelected(true);
		thunderOptionsPanel.add(rdbtnThunderTurningOn);

		JRadioButton rdbtnTurningOff = new JRadioButton("Turning off");
		buttonGroup_1.add(rdbtnTurningOff);
		thunderOptionsPanel.add(rdbtnTurningOff);

		chckbxManipulateRain = new JCheckBox("Manipulate Rain");
		chckbxManipulateRain.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					setEnabled(rainOptionsPanel, true);
				} else if (e.getStateChange() == ItemEvent.DESELECTED) {
					setEnabled(rainOptionsPanel, false);
				}
			}
		});
		weatherTab.add(chckbxManipulateRain);

		rainOptionsPanel = new JPanel();
		rainOptionsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		rainOptionsPanel.setBorder(new LineBorder(UIManager.getColor("inactiveCaptionBorder")));
		weatherTab.add(rainOptionsPanel);
		rainOptionsPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 5));

		JLabel lblRainTime = new JLabel("Rain time:");
		rainOptionsPanel.add(lblRainTime);

		rainTimeTextField = new JTextField();
		rainOptionsPanel.add(rainTimeTextField);
		rainTimeTextField.setColumns(10);

		rdbtnRainTurningOn = new JRadioButton("Turning on");
		buttonGroup_2.add(rdbtnRainTurningOn);
		rdbtnRainTurningOn.setSelected(true);
		rainOptionsPanel.add(rdbtnRainTurningOn);

		JRadioButton rdbtnTurningOff_1 = new JRadioButton("Turning off");
		buttonGroup_2.add(rdbtnTurningOff_1);
		rainOptionsPanel.add(rdbtnTurningOff_1);

		JButton btnSearch_1 = new JButton("Search");
		btnSearch_1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if (manipulator != null)
					manipulator.stop();
				manipulator = new WeatherManipulator();
				manipulator.startSearch(MainFrame.this);
			}
		});
		weatherTab.add(btnSearch_1);

		JPanel panel_4 = new JPanel();
		contentPane.add(panel_4, BorderLayout.SOUTH);
		panel_4.setLayout(new BoxLayout(panel_4, BoxLayout.Y_AXIS));

		lblOutput = new JLabel("Output appears here");
		lblOutput.setHorizontalAlignment(SwingConstants.CENTER);
		lblOutput.setAlignmentX(Component.CENTER_ALIGNMENT);
		panel_4.add(lblOutput);

		JPanel panel_5 = new JPanel();
		panel_4.add(panel_5);
		panel_5.setLayout(new BoxLayout(panel_5, BoxLayout.X_AXIS));

		progressBar = new JProgressBar();
		panel_5.add(progressBar);

		JButton btnAbort = new JButton("Stop");
		btnAbort.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if (manipulator != null)
					manipulator.stop();
				manipulator = null;
			}
		});
		panel_5.add(btnAbort);

		setEnabled(thunderOptionsPanel, false);
		setEnabled(rainOptionsPanel, false);

		JPanel schedulerTab = new JPanel();
		tabbedPane.addTab("Scheduler", null, schedulerTab, null);
		schedulerTab.setLayout(new BoxLayout(schedulerTab, BoxLayout.Y_AXIS));

		JPanel panel_13 = new JPanel();
		schedulerTab.add(panel_13);
		panel_13.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 5));

		JLabel lblThunderOfftime = new JLabel("Thunder off-time:");
		panel_13.add(lblThunderOfftime);

		thunderOffTimeTextField = new JTextField();
		panel_13.add(thunderOffTimeTextField);
		thunderOffTimeTextField.setColumns(10);

		chckbxThunderOffUsesExtraRand = new JCheckBox("Uses extra rand");
		chckbxThunderOffUsesExtraRand.setSelected(true);
		panel_13.add(chckbxThunderOffUsesExtraRand);

		JPanel panel_14 = new JPanel();
		FlowLayout flowLayout_2 = (FlowLayout) panel_14.getLayout();
		flowLayout_2.setAlignment(FlowLayout.LEFT);
		schedulerTab.add(panel_14);

		JLabel lblThunderOntime = new JLabel("Thunder on-time:");
		panel_14.add(lblThunderOntime);

		thunderOnTimeTextField = new JTextField();
		panel_14.add(thunderOnTimeTextField);
		thunderOnTimeTextField.setColumns(10);

		chckbxThunderOnUsesExtraRand = new JCheckBox("Uses extra rand");
		panel_14.add(chckbxThunderOnUsesExtraRand);

		JPanel panel_15 = new JPanel();
		FlowLayout flowLayout_3 = (FlowLayout) panel_15.getLayout();
		flowLayout_3.setAlignment(FlowLayout.LEFT);
		schedulerTab.add(panel_15);

		JLabel lblRainOfftime = new JLabel("Rain off-time:");
		panel_15.add(lblRainOfftime);

		rainOffTimeTextField = new JTextField();
		panel_15.add(rainOffTimeTextField);
		rainOffTimeTextField.setColumns(10);

		chckbxRainOffUsesExtraRand = new JCheckBox("Uses extra rand");
		chckbxRainOffUsesExtraRand.setSelected(true);
		panel_15.add(chckbxRainOffUsesExtraRand);

		JPanel panel_16 = new JPanel();
		FlowLayout flowLayout_4 = (FlowLayout) panel_16.getLayout();
		flowLayout_4.setAlignment(FlowLayout.LEFT);
		schedulerTab.add(panel_16);

		JLabel lblRainOntime = new JLabel("Rain on-time:");
		panel_16.add(lblRainOntime);

		rainOnTimeTextField = new JTextField();
		panel_16.add(rainOnTimeTextField);
		rainOnTimeTextField.setColumns(10);

		chckbxRainOnUsesExtraRand = new JCheckBox("Uses extra rand");
		panel_16.add(chckbxRainOnUsesExtraRand);

		JPanel panel_17 = new JPanel();
		FlowLayout flowLayout_5 = (FlowLayout) panel_17.getLayout();
		flowLayout_5.setAlignment(FlowLayout.LEFT);
		schedulerTab.add(panel_17);

		JLabel lblMessageSendTime = new JLabel("Signal delay:");
		panel_17.add(lblMessageSendTime);

		messageSendTimeTextField = new JTextField();
		messageSendTimeTextField.setText("16");
		panel_17.add(messageSendTimeTextField);
		messageSendTimeTextField.setColumns(10);

		JPanel panel_18 = new JPanel();
		FlowLayout flowLayout_6 = (FlowLayout) panel_18.getLayout();
		flowLayout_6.setAlignment(FlowLayout.LEFT);
		schedulerTab.add(panel_18);

		JButton btnCalculate = new JButton("Calculate");
		btnCalculate.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if (manipulator != null)
					manipulator.stop();
				manipulator = null;
				Scheduler scheduler = new Scheduler();
				if (scheduler.readFromFrame(MainFrame.this))
					scheduler.calcSchedule();
			}
		});
		panel_18.add(btnCalculate);

		JPanel mobSpawningTab = new JPanel();
		tabbedPane.addTab("Mobs", null, mobSpawningTab, null);
		mobSpawningTab.setLayout(new BoxLayout(mobSpawningTab, BoxLayout.X_AXIS));

		JTabbedPane tabbedPane_1 = new JTabbedPane(JTabbedPane.TOP);
		mobSpawningTab.add(tabbedPane_1);

		JPanel playerPositionsTab = new JPanel();
		tabbedPane_1.addTab("Player positions", null, playerPositionsTab, null);
		playerPositionsTab.setLayout(new BoxLayout(playerPositionsTab, BoxLayout.Y_AXIS));

		JPanel panel_25 = new JPanel();
		FlowLayout flowLayout_11 = (FlowLayout) panel_25.getLayout();
		flowLayout_11.setAlignment(FlowLayout.LEFT);
		playerPositionsTab.add(panel_25);

		JLabel lblPlayers = new JLabel("Players:");
		panel_25.add(lblPlayers);

		JPanel panel_24 = new JPanel();
		playerPositionsTab.add(panel_24);
		panel_24.setLayout(new BoxLayout(panel_24, BoxLayout.X_AXIS));

		JScrollPane scrollPane_2 = new JScrollPane();
		panel_24.add(scrollPane_2);

		spawnPriorityPlayerTable = new JTable() {
			@Override
			public Component prepareRenderer(TableCellRenderer renderer, int row, int col) {
				if (col == 0)
					return getTableHeader().getDefaultRenderer().getTableCellRendererComponent(this,
							getValueAt(row, col), false, false, row, col);
				else
					return super.prepareRenderer(renderer, row, col);
			}
		};
		spawnPriorityPlayerTable.setRowSelectionAllowed(false);
		spawnPriorityPlayerTable.setCellSelectionEnabled(true);
		spawnPriorityPlayerTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		spawnPriorityPlayerTable.setModel(
				new DefaultTableModel(new Object[][] { { null, null, null }, }, new String[] { "#", "X", "Z" }) {
					Class[] columnTypes = new Class[] { String.class, Integer.class, Integer.class };

					public Class getColumnClass(int columnIndex) {
						return columnTypes[columnIndex];
					}
				});
		spawnPriorityPlayerTable.getColumnModel().getColumn(0).setResizable(false);
		spawnPriorityPlayerTable.getColumnModel().getColumn(0).setPreferredWidth(15);
		spawnPriorityPlayerTable.getColumnModel().getColumn(1).setResizable(false);
		spawnPriorityPlayerTable.getColumnModel().getColumn(1).setPreferredWidth(150);
		spawnPriorityPlayerTable.getColumnModel().getColumn(2).setResizable(false);
		spawnPriorityPlayerTable.getColumnModel().getColumn(2).setPreferredWidth(150);
		spawnPriorityPlayerTable.setValueAt(1, 0, 0);
		spawnPriorityPlayerTable.getDefaultEditor(Integer.class).addCellEditorListener(new CellEditorListener() {
			@Override
			public void editingCanceled(ChangeEvent e) {
			}

			@Override
			public void editingStopped(ChangeEvent e) {
				if (!initializing) {
					if (manipulator != null)
						manipulator.stop();
					manipulator = null;
					MobSpawnCalculator.recalculate(MainFrame.this, true, false);
				}
			}
		});
		scrollPane_2.setViewportView(spawnPriorityPlayerTable);

		JPanel panel_26 = new JPanel();
		playerPositionsTab.add(panel_26);

		JButton btnAddPlayer = new JButton("Add Player");
		btnAddPlayer.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				((DefaultTableModel) spawnPriorityPlayerTable.getModel())
						.addRow(new Object[] { spawnPriorityPlayerTable.getRowCount() + 1, null, null });
			}
		});
		panel_26.add(btnAddPlayer);

		JButton btnRemovePlayer = new JButton("Remove Player");
		btnRemovePlayer.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if (spawnPriorityPlayerTable.getSelectedRow() != -1 && spawnPriorityPlayerTable.getRowCount() > 1) {
					((DefaultTableModel) spawnPriorityPlayerTable.getModel())
							.removeRow(spawnPriorityPlayerTable.getSelectedRow());
					for (int i = 0; i < spawnPriorityPlayerTable.getRowCount(); i++) {
						spawnPriorityPlayerTable.setValueAt(i + 1, i, 0);
					}
					if (manipulator != null)
						manipulator.stop();
					manipulator = null;
					MobSpawnCalculator.recalculate(MainFrame.this, true, false);
				}
			}
		});
		panel_26.add(btnRemovePlayer);

		JButton btnMoveUp = new JButton("Move Up");
		btnMoveUp.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				int row = spawnPriorityPlayerTable.getSelectedRow();
				if (row != -1 && row != 0) {
					DefaultTableModel model = (DefaultTableModel) spawnPriorityPlayerTable.getModel();
					Object x = model.getValueAt(row - 1, 1);
					Object y = model.getValueAt(row - 1, 2);
					model.removeRow(row - 1);
					model.insertRow(row, new Object[] { row + 1, x, y });
					model.setValueAt(row, row - 1, 0);
					if (manipulator != null)
						manipulator.stop();
					manipulator = null;
					MobSpawnCalculator.recalculate(MainFrame.this, true, false);
				}
			}
		});
		panel_26.add(btnMoveUp);

		JButton btnMoveDown = new JButton("Move Down");
		btnMoveDown.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				int row = spawnPriorityPlayerTable.getSelectedRow();
				if (row != -1 && row != spawnPriorityPlayerTable.getRowCount() - 1) {
					DefaultTableModel model = (DefaultTableModel) spawnPriorityPlayerTable.getModel();
					Object x = model.getValueAt(row + 1, 1);
					Object y = model.getValueAt(row + 1, 2);
					model.removeRow(row + 1);
					model.insertRow(row, new Object[] { row + 1, x, y });
					model.setValueAt(row + 2, row + 1, 0);
					if (manipulator != null)
						manipulator.stop();
					manipulator = null;
					MobSpawnCalculator.recalculate(MainFrame.this, true, false);
				}
			}
		});
		panel_26.add(btnMoveDown);

		JPanel mobSpawnSettingsTab = new JPanel();
		tabbedPane_1.addTab("Settings", null, mobSpawnSettingsTab, null);
		mobSpawnSettingsTab.setLayout(new BoxLayout(mobSpawnSettingsTab, BoxLayout.Y_AXIS));

		JPanel panel_20 = new JPanel();
		FlowLayout flowLayout_7 = (FlowLayout) panel_20.getLayout();
		flowLayout_7.setAlignment(FlowLayout.LEFT);
		mobSpawnSettingsTab.add(panel_20);

		JLabel lblMobToSpawn = new JLabel("Mob to spawn:");
		panel_20.add(lblMobToSpawn);

		mobTypeComboBox = new JComboBox(MobList.getValidMobsArray());
		mobTypeComboBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if (!initializing) {
					if (manipulator != null)
						manipulator.stop();
					manipulator = null;
					MobSpawnCalculator.recalculate(MainFrame.this, true, false);
				}
			}
		});
		mobTypeComboBox.setSelectedIndex(23);
		AutoCompleteDecorator.decorate(mobTypeComboBox);
		panel_20.add(mobTypeComboBox);

		JPanel panel_21 = new JPanel();
		FlowLayout flowLayout_8 = (FlowLayout) panel_21.getLayout();
		flowLayout_8.setAlignment(FlowLayout.LEFT);
		mobSpawnSettingsTab.add(panel_21);

		JLabel lblBiome = new JLabel("Biome type:");
		panel_21.add(lblBiome);

		biomeTypeComboBox = new JComboBox();
		biomeTypeComboBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if (!initializing) {
					if (manipulator != null)
						manipulator.stop();
					manipulator = null;
					MobSpawnCalculator.recalculate(MainFrame.this, true, false);
				}
			}
		});
		biomeTypeComboBox.setModel(new DefaultComboBoxModel(BiomeType.values()));
		panel_21.add(biomeTypeComboBox);

		JPanel mobChunksTab = new JPanel();
		tabbedPane_1.addTab("Chunks", null, mobChunksTab, null);
		mobChunksTab.setLayout(new BoxLayout(mobChunksTab, BoxLayout.Y_AXIS));

		JScrollPane scrollPane_1 = new JScrollPane();
		mobChunksTab.add(scrollPane_1);

		mobChunksTable = new JTable() {
			@Override
			public Component prepareRenderer(TableCellRenderer renderer, int row, int col) {
				if (col == 0) {
					return getTableHeader().getDefaultRenderer().getTableCellRendererComponent(this,
							getValueAt(row, col), false, false, row, col);
				} else if (col == 3 || col == 8) {
					Component c = super.prepareRenderer(renderer, row, col);
					if (mobTypeComboBox.getSelectedItem().equals(getValueAt(row, col))) {
						c.setFont(c.getFont().deriveFont(Font.BOLD | Font.ITALIC));
					}
					return c;
				} else {
					return super.prepareRenderer(renderer, row, col);
				}
			}
		};
		mobChunksTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		mobChunksTable.setCellSelectionEnabled(true);
		mobChunksTable.setRowSelectionAllowed(false);
		mobChunksTable.setColumnSelectionAllowed(false);
		mobChunksTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		mobChunksTable.setModel(new DefaultTableModel(
				new Object[][] { { null, null, null, null, null, null, null, null, null, null, null, null, null, null,
						null, null, null, null, null, null, null }, },
				new String[] { "#", "Chunk X", "Chunk Z", "Hostile", "Hostile Max Y", "Hostile X", "Hostile Y",
						"Hostile Z", "Passive", "Passive Max Y", "Passive X", "Passive Y", "Passive Z", "Bat Max Y",
						"Bat X", "Bat Y", "Bat Z", "Squid Max Y", "Squid X", "Squid Y", "Squid Z" }) {
			Class[] columnTypes = new Class[] { Object.class, Integer.class, Integer.class, Object.class, Integer.class,
					Integer.class, Integer.class, Integer.class, Object.class, Integer.class, Integer.class,
					Integer.class, Integer.class, Integer.class, Integer.class, Integer.class, Integer.class,
					Integer.class, Integer.class, Integer.class, Integer.class };

			public Class getColumnClass(int columnIndex) {
				return columnTypes[columnIndex];
			}

			boolean[] columnEditables = new boolean[] { false, false, false, false, true, false, false, false, false,
					true, false, false, false, true, false, false, false, true, false, false, false };

			public boolean isCellEditable(int row, int column) {
				return columnEditables[column];
			}
		});
		mobChunksTable.getColumnModel().getColumn(0).setResizable(false);
		mobChunksTable.getColumnModel().getColumn(0).setPreferredWidth(30);
		mobChunksTable.getColumnModel().getColumn(0).setMinWidth(30);
		mobChunksTable.getColumnModel().getColumn(1).setResizable(false);
		mobChunksTable.getColumnModel().getColumn(1).setMinWidth(75);
		mobChunksTable.getColumnModel().getColumn(2).setResizable(false);
		mobChunksTable.getColumnModel().getColumn(2).setMinWidth(75);
		mobChunksTable.getColumnModel().getColumn(3).setResizable(false);
		mobChunksTable.getColumnModel().getColumn(3).setPreferredWidth(150);
		mobChunksTable.getColumnModel().getColumn(3).setMinWidth(150);
		mobChunksTable.getColumnModel().getColumn(4).setResizable(false);
		mobChunksTable.getColumnModel().getColumn(4).setMinWidth(75);
		mobChunksTable.getColumnModel().getColumn(5).setResizable(false);
		mobChunksTable.getColumnModel().getColumn(5).setMinWidth(75);
		mobChunksTable.getColumnModel().getColumn(6).setResizable(false);
		mobChunksTable.getColumnModel().getColumn(6).setMinWidth(75);
		mobChunksTable.getColumnModel().getColumn(7).setResizable(false);
		mobChunksTable.getColumnModel().getColumn(7).setMinWidth(75);
		mobChunksTable.getColumnModel().getColumn(8).setResizable(false);
		mobChunksTable.getColumnModel().getColumn(8).setPreferredWidth(150);
		mobChunksTable.getColumnModel().getColumn(8).setMinWidth(150);
		mobChunksTable.getColumnModel().getColumn(9).setResizable(false);
		mobChunksTable.getColumnModel().getColumn(9).setPreferredWidth(80);
		mobChunksTable.getColumnModel().getColumn(9).setMinWidth(80);
		mobChunksTable.getColumnModel().getColumn(10).setResizable(false);
		mobChunksTable.getColumnModel().getColumn(10).setMinWidth(75);
		mobChunksTable.getColumnModel().getColumn(11).setResizable(false);
		mobChunksTable.getColumnModel().getColumn(11).setMinWidth(75);
		mobChunksTable.getColumnModel().getColumn(12).setResizable(false);
		mobChunksTable.getColumnModel().getColumn(12).setMinWidth(75);
		mobChunksTable.getColumnModel().getColumn(13).setResizable(false);
		mobChunksTable.getColumnModel().getColumn(13).setMinWidth(75);
		mobChunksTable.getColumnModel().getColumn(14).setResizable(false);
		mobChunksTable.getColumnModel().getColumn(14).setMinWidth(75);
		mobChunksTable.getColumnModel().getColumn(15).setResizable(false);
		mobChunksTable.getColumnModel().getColumn(15).setMinWidth(75);
		mobChunksTable.getColumnModel().getColumn(16).setResizable(false);
		mobChunksTable.getColumnModel().getColumn(16).setMinWidth(75);
		mobChunksTable.getColumnModel().getColumn(17).setResizable(false);
		mobChunksTable.getColumnModel().getColumn(17).setMinWidth(75);
		mobChunksTable.getColumnModel().getColumn(18).setResizable(false);
		mobChunksTable.getColumnModel().getColumn(18).setMinWidth(75);
		mobChunksTable.getColumnModel().getColumn(19).setResizable(false);
		mobChunksTable.getColumnModel().getColumn(19).setMinWidth(75);
		mobChunksTable.getColumnModel().getColumn(20).setResizable(false);
		mobChunksTable.getColumnModel().getColumn(20).setMinWidth(75);
		mobChunksTable.getDefaultEditor(Integer.class).addCellEditorListener(new CellEditorListener() {
			@Override
			public void editingCanceled(ChangeEvent e) {
			}

			@Override
			public void editingStopped(ChangeEvent e) {
				if (!initializing) {
					if (manipulator != null)
						manipulator.stop();
					manipulator = null;
					MobSpawnCalculator.recalculate(MainFrame.this, false, false);
				}
			}
		});
		((DefaultTableModel) mobChunksTable.getModel()).removeRow(0);
		scrollPane_1.setViewportView(mobChunksTable);

		JPanel ironTab = new JPanel();
		tabbedPane.addTab("Iron", null, ironTab, null);
		ironTab.setLayout(new BoxLayout(ironTab, BoxLayout.Y_AXIS));

		JPanel panel_27 = new JPanel();
		FlowLayout flowLayout_17 = (FlowLayout) panel_27.getLayout();
		flowLayout_17.setAlignment(FlowLayout.LEFT);
		ironTab.add(panel_27);

		JLabel lblViewDistance_1 = new JLabel("View distance:");
		panel_27.add(lblViewDistance_1);

		ironViewDistanceTextField = new JTextField();
		ironViewDistanceTextField.setText("12");
		panel_27.add(ironViewDistanceTextField);
		ironViewDistanceTextField.setColumns(10);

		JPanel panel_28 = new JPanel();
		FlowLayout flowLayout_18 = (FlowLayout) panel_28.getLayout();
		flowLayout_18.setAlignment(FlowLayout.LEFT);
		ironTab.add(panel_28);

		JLabel lblPlayerPosIn = new JLabel("Player pos in chunk:");
		panel_28.add(lblPlayerPosIn);

		Component horizontalStrut_3 = Box.createHorizontalStrut(20);
		panel_28.add(horizontalStrut_3);

		JLabel lblX_1 = new JLabel("X:");
		panel_28.add(lblX_1);

		playerXInChunkTextField = new JTextField();
		playerXInChunkTextField.setText("8");
		panel_28.add(playerXInChunkTextField);
		playerXInChunkTextField.setColumns(10);

		Component horizontalStrut_4 = Box.createHorizontalStrut(20);
		panel_28.add(horizontalStrut_4);

		JLabel lblZ = new JLabel("Z:");
		panel_28.add(lblZ);

		playerZInChunkTextField = new JTextField();
		playerZInChunkTextField.setText("8");
		panel_28.add(playerZInChunkTextField);
		playerZInChunkTextField.setColumns(10);

		JPanel panel_23 = new JPanel();
		FlowLayout flowLayout_16 = (FlowLayout) panel_23.getLayout();
		flowLayout_16.setAlignment(FlowLayout.LEFT);
		ironTab.add(panel_23);

		JButton btnCalculate_2 = new JButton("Calculate");
		btnCalculate_2.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if (manipulator != null)
					manipulator.stop();
				manipulator = new IronManipulator();
				manipulator.startSearch(MainFrame.this);
			}
		});
		panel_23.add(btnCalculate_2);

		JPanel fortuneTab = new JPanel();
		tabbedPane.addTab("Fortune", null, fortuneTab, null);
		fortuneTab.setLayout(new BoxLayout(fortuneTab, BoxLayout.Y_AXIS));

		JPanel panel = new JPanel();
		FlowLayout flowLayout_9 = (FlowLayout) panel.getLayout();
		flowLayout_9.setAlignment(FlowLayout.LEFT);
		fortuneTab.add(panel);

		JLabel lblOre = new JLabel("Ore:");
		panel.add(lblOre);

		oreComboBox = new JComboBox();
		oreComboBox.setModel(new DefaultComboBoxModel(Ore.values()));
		panel.add(oreComboBox);

		JPanel panel_9 = new JPanel();
		FlowLayout flowLayout_10 = (FlowLayout) panel_9.getLayout();
		flowLayout_10.setAlignment(FlowLayout.LEFT);
		fortuneTab.add(panel_9);

		JLabel lblAmount = new JLabel("Amount:");
		panel_9.add(lblAmount);

		dropAmountTextField = new JTextField();
		panel_9.add(dropAmountTextField);
		dropAmountTextField.setColumns(10);

		chckbxDropAmountExact = new JCheckBox("Exact");
		panel_9.add(chckbxDropAmountExact);

		JPanel panel_11 = new JPanel();
		FlowLayout flowLayout_12 = (FlowLayout) panel_11.getLayout();
		flowLayout_12.setAlignment(FlowLayout.LEFT);
		fortuneTab.add(panel_11);

		JPanel panel_10 = new JPanel();
		panel_11.add(panel_10);
		panel_10.setLayout(new BoxLayout(panel_10, BoxLayout.Y_AXIS));

		JPanel panel_12 = new JPanel();
		FlowLayout flowLayout_13 = (FlowLayout) panel_12.getLayout();
		flowLayout_13.setAlignment(FlowLayout.LEFT);
		panel_10.add(panel_12);

		chckbxManipulateXp = new JCheckBox("Manipulate XP");
		chckbxManipulateXp.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					setEnabled(xpPanel, true);
				} else if (e.getStateChange() == ItemEvent.DESELECTED) {
					setEnabled(xpPanel, false);
				}
			}
		});
		panel_12.add(chckbxManipulateXp);

		xpPanel = new JPanel();
		FlowLayout fl_xpPanel = (FlowLayout) xpPanel.getLayout();
		fl_xpPanel.setAlignment(FlowLayout.LEFT);
		panel_10.add(xpPanel);

		Component horizontalStrut_2 = Box.createHorizontalStrut(20);
		xpPanel.add(horizontalStrut_2);

		JLabel lblXpDropped = new JLabel("XP dropped:");
		xpPanel.add(lblXpDropped);

		xpDroppedTextField = new JTextField();
		xpPanel.add(xpDroppedTextField);
		xpDroppedTextField.setColumns(10);

		chckbxXpExact = new JCheckBox("Exact");
		xpPanel.add(chckbxXpExact);

		setEnabled(xpPanel, false);

		JPanel panel_22 = new JPanel();
		FlowLayout flowLayout_15 = (FlowLayout) panel_22.getLayout();
		flowLayout_15.setAlignment(FlowLayout.LEFT);
		fortuneTab.add(panel_22);

		JLabel lblFortuneLevel = new JLabel("Fortune level:");
		panel_22.add(lblFortuneLevel);

		fortuneLevelTextField = new JTextField();
		fortuneLevelTextField.setText("3");
		panel_22.add(fortuneLevelTextField);
		fortuneLevelTextField.setColumns(10);

		JPanel panel_19 = new JPanel();
		FlowLayout flowLayout_14 = (FlowLayout) panel_19.getLayout();
		flowLayout_14.setAlignment(FlowLayout.LEFT);
		fortuneTab.add(panel_19);

		JButton btnCalculate_1 = new JButton("Calculate");
		btnCalculate_1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if (manipulator != null)
					manipulator.stop();
				manipulator = new FortuneManipulator();
				manipulator.startSearch(MainFrame.this);
			}
		});
		panel_19.add(btnCalculate_1);

		JPanel outputTab = new JPanel();
		tabbedPane.addTab("Output", null, outputTab, null);
		outputTab.setLayout(new BoxLayout(outputTab, BoxLayout.X_AXIS));

		JScrollPane aboutTab = new JScrollPane();
		tabbedPane.addTab("About", null, aboutTab, null);

		JTextArea txtrAbout = new JTextArea();
		txtrAbout.setFocusable(false);
		txtrAbout.setWrapStyleWord(true);
		txtrAbout.setLineWrap(true);
		txtrAbout.setText(
				"Lightning machine tool by Earthcomputer\r\n(original tool by Xcom)\r\n\r\nLightning concept inspired by Amiga IZI\r\nLightning research by Earthcomputer, Xcom, 2No2Name, EDDxample, 0x53ee71ebe11e\r\n\r\nWeather research by Earthcomputer, 0x53ee71ebe11e, EDDxample, and independently by Xcom, 2No2Name\r\n\r\nMob spawning research by Xcom, 2No2Name, Earthcomputer\r\n\r\nFirst RNG manipulation iron farm by EDDxample\r\n\r\nLightning farm design by EDDxample, 0x53ee71ebe11e\r\nThanks to SciCraft for giving helpful pointers for various components");
		txtrAbout.setFont(UIManager.getFont("Label.font"));
		txtrAbout.setForeground(UIManager.getColor("Label.foreground"));
		txtrAbout.setBackground(UIManager.getColor("Label.background"));
		txtrAbout.setEditable(false);
		txtrAbout.setCaretPosition(0);
		aboutTab.setViewportView(txtrAbout);

		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		outputTab.add(scrollPane);

		outputTextArea = new JTextArea();
		scrollPane.setViewportView(outputTextArea);
		outputTextArea.setEditable(false);
		DefaultCaret caret = (DefaultCaret) outputTextArea.getCaret();
		caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);

		initializing = false;
	}

	private void setEnabled(Container container, boolean enabled) {
		container.setEnabled(enabled);
		for (Component child : container.getComponents()) {
			if (child instanceof Container) {
				setEnabled((Container) child, enabled);
			} else {
				child.setEnabled(enabled);
			}
		}
	}

	public JLabel getLblOutput() {
		return lblOutput;
	}

	public JTextField getSearchFromXTextField() {
		return searchFromXTextField;
	}

	public JTextField getSearchFromZTextField() {
		return searchFromZTextField;
	}

	public JTextField getWorldSeedTextField() {
		return worldSeedTextField;
	}

	public JProgressBar getProgressBar() {
		return progressBar;
	}

	public JTextField getViewDistanceTextField() {
		return viewDistanceTextField;
	}

	public JRadioButton getRdbtnHorseTraps() {
		return rdbtnHorseTraps;
	}

	public JRadioButton getRdbtnChargedCreepers() {
		return rdbtnChargedCreepers;
	}

	public JRadioButton getRdbtnDependOnMoon() {
		return rdbtnDependOnMoon;
	}

	public JTextField getWantedChunkIterationTextField() {
		return wantedChunkIterationTextField;
	}

	public JCheckBox getChckbxExact() {
		return chckbxExact;
	}

	public JTextField getExtraRandCallsTextField() {
		return extraRandCallsTextField;
	}

	public JCheckBox getChckbxManipulateThunder() {
		return chckbxManipulateThunder;
	}

	public JTextField getThunderTimeTextField() {
		return thunderTimeTextField;
	}

	public JCheckBox getChckbxManipulateRain() {
		return chckbxManipulateRain;
	}

	public JTextField getRainTimeTextField() {
		return rainTimeTextField;
	}

	public JRadioButton getRdbtnThunderTurningOn() {
		return rdbtnThunderTurningOn;
	}

	public JRadioButton getRdbtnRainTurningOn() {
		return rdbtnRainTurningOn;
	}

	public JTextArea getOutputTextArea() {
		return outputTextArea;
	}

	public JTextField getThunderOffTimeTextField() {
		return thunderOffTimeTextField;
	}

	public JTextField getThunderOnTimeTextField() {
		return thunderOnTimeTextField;
	}

	public JTextField getRainOffTimeTextField() {
		return rainOffTimeTextField;
	}

	public JTextField getRainOnTimeTextField() {
		return rainOnTimeTextField;
	}

	public JTextField getMessageSendTimeTextField() {
		return messageSendTimeTextField;
	}

	public JCheckBox getChckbxThunderOffUsesExtraRand() {
		return chckbxThunderOffUsesExtraRand;
	}

	public JCheckBox getChckbxThunderOnUsesExtraRand() {
		return chckbxThunderOnUsesExtraRand;
	}

	public JCheckBox getChckbxRainOffUsesExtraRand() {
		return chckbxRainOffUsesExtraRand;
	}

	public JCheckBox getChckbxRainOnUsesExtraRand() {
		return chckbxRainOnUsesExtraRand;
	}

	public JComboBox getMobTypeComboBox() {
		return mobTypeComboBox;
	}

	public JComboBox getBiomeTypeComboBox() {
		return biomeTypeComboBox;
	}

	public JTable getSpawnPriorityPlayerTable() {
		return spawnPriorityPlayerTable;
	}

	public JTable getMobChunksTable() {
		return mobChunksTable;
	}

	public JComboBox getOreComboBox() {
		return oreComboBox;
	}

	public JTextField getDropAmountTextField() {
		return dropAmountTextField;
	}

	public JCheckBox getChckbxDropAmountExact() {
		return chckbxDropAmountExact;
	}

	public JCheckBox getChckbxManipulateXp() {
		return chckbxManipulateXp;
	}

	public JTextField getXpDroppedTextField() {
		return xpDroppedTextField;
	}

	public JCheckBox getChckbxXpExact() {
		return chckbxXpExact;
	}

	public JTextField getFortuneLevelTextField() {
		return fortuneLevelTextField;
	}

	public JComboBox getAdvancerComboBox() {
		return advancerComboBox;
	}

	public JTextField getIronViewDistanceTextField() {
		return ironViewDistanceTextField;
	}

	public JTextField getPlayerXInChunkTextField() {
		return playerXInChunkTextField;
	}

	public JTextField getPlayerZInChunkTextField() {
		return playerZInChunkTextField;
	}
}
