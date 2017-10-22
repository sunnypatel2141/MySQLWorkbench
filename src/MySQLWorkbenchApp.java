import java.awt.Color;
import java.awt.EventQueue;
import java.util.ArrayList;
import java.util.Date;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JTextField;
import java.awt.event.ActionListener;
import java.text.SimpleDateFormat;
import java.awt.event.ActionEvent;
import javax.swing.JTextPane;
import javax.swing.JTabbedPane;
import java.awt.Font;

public class MySQLWorkbenchApp
{
	private static String[] params;
	private JFrame mframe;
	private Control ctrl;
	private String table;
	private JTextField newTableName;
	private DefaultListModel<String> tablesList;
	private String[] columnNamesGlobal;
	private Object[][] dataGlobal;
	JTabbedPane tabbedPane;
	JTextPane paneForConsole, paneForOutput;
	private boolean emptyPane = true;
	private boolean enteredValidTableName = false;
	
	public SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

	private static final int RESULTS = 0;
	private static final int CONSOLE = 1;
	
	/**
	 * Launch the application.
	 */
	public static void main(String[] args)
	{
		if (args.length != 4)
		{
			System.exit(0);
		} else
		{
			params = args;
		}
		EventQueue.invokeLater(new Runnable()
		{
			public void run()
			{
				try
				{
					MySQLWorkbenchApp window = new MySQLWorkbenchApp();
					window.mframe.setVisible(true);
				} catch (Exception e)
				{
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public MySQLWorkbenchApp()
	{
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize()
	{
		mframe = new JFrame();
		mframe.setBounds(0, 0, 1280, 800);
		mframe.setTitle("MySQL Database Workbench");
		mframe.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		mframe.getContentPane().setLayout(null);

		JLabel lblListOfTables = new JLabel("List of Tables:");
		lblListOfTables.setBounds(831, 42, 89, 16);
		mframe.getContentPane().add(lblListOfTables);

		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(930, 40, 327, 347);
		mframe.getContentPane().add(scrollPane);
		
		paneForOutput = new JTextPane();
		JScrollPane jspOutput = new JScrollPane(paneForOutput);
		jspOutput.setBounds(232, 5, 0, 16);
		
		paneForConsole = new JTextPane();
		JScrollPane jspConsole = new JScrollPane(paneForConsole);
		jspConsole.setBounds(232, 5, 0, 16);
		
		tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		tabbedPane.setBounds(0, 542, 1280, 150);
		tabbedPane.add("Results", jspOutput);
		tabbedPane.add("Console", jspConsole);
		mframe.getContentPane().add(tabbedPane);
		
		// call Control model
		ctrl = new Control();
		populateConsole("Success: Started application.");
		
		//assign properties
		ctrl.setDatabase(params[0]);
		ctrl.setUsername(params[1]);
		ctrl.setPassword(params[2]);
		ctrl.setURL(params[3]);
		
		connectToDB();
		populateConsole("Success: Connected to database.");

		//populate JList with tables
		JLabel lblDatabase = new JLabel("Database: " + ctrl.getDatabase());
		lblDatabase.setFont(new Font("Lucida Grande", Font.PLAIN, 15));
		lblDatabase.setBounds(25, 6, 198, 29);
		mframe.getContentPane().add(lblDatabase);

		tablesList = new DefaultListModel<String>();
		ArrayList<String> tables = ctrl.getListOfTables();
		for (String tab : tables)
		{
			tablesList.addElement(tab);
		}
		
		JList<String> listOfTables = new JList<String>(tablesList);
		listOfTables.setFont(new Font("Lucida Grande", Font.PLAIN, 13));
		scrollPane.setViewportView(listOfTables);
		listOfTables.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
		
		ListSelectionListener listChanged = new ListSelectionListener()
		{
			@Override
			public void valueChanged(ListSelectionEvent evt)
			{
				if (!evt.getValueIsAdjusting())
				{
					//change table value to currently selected table
					selectedTable(listOfTables.getSelectedValue());
				}
			}
		};
		
		listOfTables.addListSelectionListener(listChanged);
	
		JButton btnGetContent = new JButton("Get Content");
		btnGetContent.setBounds(1123, 410, 117, 29);
		btnGetContent.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				actionPerformedGetContent(paneForOutput);
			}
		});
		mframe.getContentPane().add(btnGetContent);

		newTableName = new JTextField("Enter new table name");
		newTableName.setBounds(930, 451, 175, 26);
		mframe.getContentPane().add(newTableName);
		newTableName.setColumns(10);

		JButton btnCreateTable = new JButton("Create Table");
		btnCreateTable.setBounds(1123, 451, 117, 29);
		btnCreateTable.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				boolean createdTable = false;
				if (table != null && enteredValidTableName(newTableName))
				{
					createdTable = createTable(table, newTableName);
				} else
				{
					JOptionPane.showMessageDialog(mframe,
							"Make sure you have selected a table, and entered a valid table name!", "Inane error",
							JOptionPane.ERROR_MESSAGE);
				}
				if (!createdTable)
				{
					populateConsole("Error: Unable to create a new table.");
				} else
				{
					populateConsole("Success: Created table " + newTableName.getText());
				}
			}
		});
		mframe.getContentPane().add(btnCreateTable);

		JLabel lblCopyTable = new JLabel("Copy Table...");
		lblCopyTable.setBounds(844, 456, 89, 16);
		mframe.getContentPane().add(lblCopyTable);

		//copy table contents to new table (create table if not exists)
		JButton buttonCopyContent = new JButton("Copy Content");
		buttonCopyContent.setBounds(1123, 492, 117, 29);
		buttonCopyContent.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				if (table != null && enteredValidTableName(newTableName))
				{
					copyContents(table, newTableName);
					populateConsole("Success: Copied contents to table " + newTableName.getText());
				} else
				{
					JOptionPane.showMessageDialog(mframe,
							"Make sure you have selected a table, and entered a valid table name!", "Error",
							JOptionPane.ERROR_MESSAGE);
				}
			}
		});
		mframe.getContentPane().add(buttonCopyContent);
		
		//update table list
		JButton btnRefresh = new JButton("Refresh");
		btnRefresh.setBounds(994, 410, 117, 29);
		btnRefresh.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				listOfTables.setModel(refreshList());
				populateConsole("Success: Refreshed table list.");
			}
		});
		mframe.getContentPane().add(btnRefresh);
		
		JTextPane textPaneForEditor = new JTextPane();
		textPaneForEditor.setFont(new Font("Lucida Grande", Font.PLAIN, 16));
		
		JScrollPane scrollPaneForEditor = new JScrollPane(textPaneForEditor);
		scrollPaneForEditor.setBounds(25, 40, 769, 443);
		mframe.getContentPane().add(scrollPaneForEditor);
		
		JButton btnExecute = new JButton("Execute");
		btnExecute.setBounds(682, 492, 117, 29);
		btnExecute.addActionListener(new ActionListener() 
		{
			public void actionPerformed(ActionEvent e) 
			{
				parseEditorAndExecute(textPaneForEditor);
			}
		});
		mframe.getContentPane().add(btnExecute);
		
		JButton buttonDeleteTable = new JButton("Drop Table");
		buttonDeleteTable.addActionListener(new ActionListener() 
		{
			public void actionPerformed(ActionEvent e) 
			{
				if (table != null)
				{
					dropTable(table);
					populateConsole("Success: Dropped table " + table);
				}
			}
		});
		buttonDeleteTable.setBounds(994, 492, 117, 29);
		mframe.getContentPane().add(buttonDeleteTable);
		
		disconnectFromDB();
		populateConsole("Success: Disconnected from database.");
	}

	private boolean enteredValidTableName(JTextField text)
	{
		if (!text.getText().equals("Enter new table name") && !text.getText().contains(" "))
		{
			enteredValidTableName = true;
		}
		return enteredValidTableName;
	}

	public void selectedTable(String str)
	{
		table = str;
	}

	private void connectToDB()
	{
		ctrl.connectToMySQLDatabase();
	}

	private void disconnectFromDB()
	{
		ctrl.disconnectFromMySQLDatabase();
	}

	//copy table information to global variable for retrieving content
	private void transferTableInformation(String[] columnNames, Object[][] data)
	{
		columnNamesGlobal = columnNames.clone();
		dataGlobal = data.clone();
	}
	
	//call model create method
	private boolean createTable(String existingTable, JTextField newTable)
	{
		return ctrl.createTable(existingTable, newTable.getText());
	}

	private boolean copyContents(String existingTable, JTextField newTable)
	{
		return ctrl.copyContents(existingTable, newTable.getText());
	}

	private void actionPerformedGetContent(JTextPane textPane)
	{
		if (table != null)
		{
			String[] columnNames = ctrl.columnNames(table);
			Object[][] data = ctrl.actionPerformedGetContent(table);
			
			transferTableInformation(columnNames, data);
			populateConsole("Success: Retrieved contents for table " + table);
			populatePane(textPane);
		} else
		{
			JOptionPane.showMessageDialog(mframe, "Select a table...");
		}
	}
	
	private void actionPerformedGetContent(String table)
	{
		String[] columnNames = ctrl.columnNames(table);
		Object[][] data = ctrl.actionPerformedGetContent(table);
		
		transferTableInformation(columnNames, data);
		populateConsole("Success: Retrieved contents for table " + table);
		populatePane(paneForOutput);
	}

	private DefaultListModel<String> refreshList()
	{
		ArrayList<String> tablesRef = ctrl.getListOfTables();
		tablesList = new DefaultListModel<String>();

		for (String tab : tablesRef)
		{
			tablesList.addElement(tab);
		}
		return tablesList;
	}
	
	private void parseEditorAndExecute(JTextPane tp)
	{
		String[] lines = tp.getText().split("\n");
		int length = lines.length;
		
		for (int i = 0; i < length; i++)
		{
			String line = lines[i].toLowerCase().replace(";", "");
			if (line.contains("select") && !line.contains("create"))
			{
				String[] syntax = line.split(" ");
				String table = syntax[syntax.length - 1];
				if (tablesList.contains(table))
				{
					actionPerformedGetContent(table);
				} else
				{
					populateConsole("Error: Table " + table + " does not exist.");
				}
			} else if (line.contains("drop") || line.contains("create"))
			{
				boolean executed = ctrl.performDDLOperation(line);
				if (executed) 
				{
					populateConsole("Success: Executed `" + line + "`. Refresh table list to observe changes.");
					refreshList();
				} else
				{
					populateConsole("Error: Unable to execute `" + line + "`. Check if syntax is correct.");
				}
			} else if (line.contains("insert") || line.contains("update") || line.contains("delete"))
			{
				boolean executed = ctrl.performDMLOperation(line);
				if (executed) 
				{
					populateConsole("Success: Executed `" + line + "`.");
				} else
				{
					populateConsole("Error: Unable to execute `" + line + "`. Check if syntax is correct.");
				}
			} else
			{
				populateConsole("Error: Unknown operation. The application can only perform select DDL/DML operations.");
			}
		}
	}
	
	private void dropTable(String table) 
	{
		ctrl.dropTable(table);
	}
	
	public String toString()
	{
		StringBuffer aString = new StringBuffer();
		for (int row = 0; row < dataGlobal.length; row++)
		{
			for (int col = 0; col < dataGlobal[row].length; col++)
			{
				aString.append(dataGlobal[row][col] + "\t");
			}
			aString.append("\n");
		}
		return aString.toString();
	}

	public String printNice(String[] columnNames)
	{
		StringBuffer aString = new StringBuffer();
		for (int i = 0; i < columnNames.length; i++)
		{
			aString.append(columnNames[i] + "\t");
		}
		return aString.toString();
	}

	//populate text pane with table data
	private void populatePane(JTextPane textPane)
	{
		StyledDocument doc = textPane.getStyledDocument();
		Style style = textPane.addStyle("Color Style", null);
		StyleConstants.setForeground(style, Color.red);

		Style style_1 = textPane.addStyle("Color Style", null);

		String columns = printNice(columnNamesGlobal) + "\n";
		String data = toString();

		try
		{
			if (!emptyPane)
			{
				textPane.getDocument().remove(0, doc.getLength());
			}
			doc.insertString(doc.getLength(), columns, style);
			doc.insertString(doc.getLength(), data, style_1);
			emptyPane = false;
		} catch (BadLocationException e)
		{
			e.printStackTrace();
		}
		tabbedPane.setSelectedIndex(RESULTS);
	}

	private void populateConsole(String text)
	{
		Date d = new Date();
		StyledDocument doc = paneForConsole.getStyledDocument();
		Style style = paneForConsole.addStyle("Color Style", null);
		
		try
		{
			doc.insertString(doc.getLength(), d.toString() + ": " + text + "\n", style);
		} catch (BadLocationException e)
		{
			e.printStackTrace();
		}
		tabbedPane.setSelectedIndex(CONSOLE);
	}
}
