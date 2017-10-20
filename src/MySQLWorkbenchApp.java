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
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JTextField;
import java.awt.event.ActionListener;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.awt.event.ActionEvent;
import javax.swing.JTextPane;

public class MySQLWorkbenchApp
{
	private static String[] params;
	private JFrame mframe;
	private Control ctrl;
	private String table;
	private JTextField newTableName;
	private String[] columnNamesGlobal;
	private Object[][] dataGlobal;
	JTextPane textAreaConsole;
	private boolean emptyPane = true;
	private boolean enteredValidTableName = false;
	public SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

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
		mframe.setBounds(100, 100, 640, 800);
		mframe.setTitle("MySQL Database Workbench");
		mframe.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		mframe.getContentPane().setLayout(null);
		
		JScrollPane consoleScrollPane = new JScrollPane();
		consoleScrollPane.setBounds(0, 619, 640, 73);
		mframe.getContentPane().add(consoleScrollPane);

		textAreaConsole = new JTextPane();
		textAreaConsole.setBounds(0, 619, 640, 73);
		textAreaConsole.setEditable(false);
		consoleScrollPane.setViewportView(textAreaConsole);

		JLabel lblListOfTables = new JLabel("List of Tables:");
		lblListOfTables.setBounds(6, 41, 89, 16);
		mframe.getContentPane().add(lblListOfTables);

		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(94, 77, 300, 80);
		mframe.getContentPane().add(scrollPane);

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
		lblDatabase.setBounds(6, 6, 169, 16);
		mframe.getContentPane().add(lblDatabase);

		DefaultListModel<String> tablesList = new DefaultListModel<String>();
		ArrayList<String> tables = ctrl.getListOfTables();
		for (String tab : tables)
		{
			tablesList.addElement(tab);
		}

		JList<String> listOfTables = new JList<String>(tablesList);
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
		scrollPane.setViewportView(listOfTables);

		JLabel lblTableCont = new JLabel("Contents of Table...");
		lblTableCont.setBounds(6, 299, 154, 16);
		mframe.getContentPane().add(lblTableCont);

		JScrollPane contentsScrollPane = new JScrollPane();
		contentsScrollPane.setBounds(94, 330, 477, 238);
		mframe.getContentPane().add(contentsScrollPane);

		//horizontally scrollable pane
		JTextPane textPane = new JTextPane()
		{
			@Override
			public boolean getScrollableTracksViewportWidth()
			{
				return getUI().getPreferredSize(this).width <= getParent().getSize().width;
			}
		};
		contentsScrollPane.setViewportView(textPane);
		textPane.setEditable(false);
		textPane.setBorder(BorderFactory.createLineBorder(Color.GREEN));

		JButton btnGetContent = new JButton("Get Content");
		btnGetContent.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				actionPerformedGetContent(textPane);
			}
		});
		btnGetContent.setBounds(406, 97, 117, 29);
		mframe.getContentPane().add(btnGetContent);

		newTableName = new JTextField("Enter new table name");
		newTableName.setBounds(94, 215, 169, 26);
		mframe.getContentPane().add(newTableName);
		newTableName.setColumns(10);

		JButton btnCreateTable = new JButton("Create Table");
		btnCreateTable.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				boolean createdTable = false;
				if (table != null && enteredValidTableName(newTableName))
				{
					ctrl.connectToMySQLDatabase();
					createdTable = createTable(table, newTableName);
					ctrl.disconnectFromMySQLDatabase();
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
		btnCreateTable.setBounds(277, 215, 117, 29);
		mframe.getContentPane().add(btnCreateTable);

		JLabel lblCopyTable = new JLabel("Copy Table...");
		lblCopyTable.setBounds(6, 220, 89, 16);
		mframe.getContentPane().add(lblCopyTable);

		//copy table contents to new table (create table if not exists)
		JButton buttonCopyContent = new JButton("Copy Content");
		buttonCopyContent.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				if (table != null && enteredValidTableName(newTableName))
				{
					ctrl.connectToMySQLDatabase();
					copyContents(table, newTableName);
					ctrl.disconnectFromMySQLDatabase();
					populateConsole("Success: Copied contents to table " + newTableName.getText());
				} else
				{
					JOptionPane.showMessageDialog(mframe,
							"Make sure you have selected a table, and entered a valid table name!", "Error",
							JOptionPane.ERROR_MESSAGE);
				}
			}
		});
		buttonCopyContent.setBounds(406, 215, 117, 29);
		mframe.getContentPane().add(buttonCopyContent);

		//console for stdout
		JLabel lblConsole = new JLabel("Console:");
		lblConsole.setBounds(6, 599, 78, 16);
		mframe.getContentPane().add(lblConsole);

		//update table list
		JButton btnRefresh = new JButton("Refresh");
		btnRefresh.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				listOfTables.setModel(refreshList(listOfTables));
				populateConsole("Success: Refreshed table list.");
			}
		});
		btnRefresh.setBounds(406, 128, 117, 29);
		mframe.getContentPane().add(btnRefresh);

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

	private void populateConsole(String text)
	{
		Date d = new Date();
		StyledDocument doc = textAreaConsole.getStyledDocument();
		Style style = textAreaConsole.addStyle("Color Style", null);

		try
		{
			doc.insertString(doc.getLength(), d.toString() + ": " + text + "\n", style);
		} catch (BadLocationException e)
		{
			e.printStackTrace();
		}
	}

	private void actionPerformedGetContent(JTextPane textPane)
	{
		if (table != null)
		{
			ctrl.connectToMySQLDatabase();

			ResultSet rs = ctrl.returnContentsOfTable(table);
			String[] columnNames = ctrl.columnNames(rs);

			try
			{
				int rowCount = rs.last() ? rs.getRow() : 0;
				rs.beforeFirst();
				ResultSetMetaData rsm = rs.getMetaData();
				int columns = rsm.getColumnCount();

				Object[][] data = new Object[rowCount][columns];

				int row = 0;

				while (rs.next())
				{
					for (int column = 1; column <= columns; column++)
					{
						data[row][column - 1] = rs.getObject(column);
					}
					row++;
				}

				transferTableInformation(columnNames, data);

				populatePane(textPane);
			} catch (SQLException e1)
			{
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		} else
		{
			JOptionPane.showMessageDialog(mframe, "Select a table...");
		}
		ctrl.disconnectFromMySQLDatabase();
		populateConsole("Success: Retrieved contents for table " + table);
	}

	private DefaultListModel<String> refreshList(JList<String> list)
	{
		ctrl.connectToMySQLDatabase();
		ArrayList<String> tablesRef = ctrl.getListOfTables();
		DefaultListModel<String> dlm = new DefaultListModel<String>();

		for (String tab : tablesRef)
		{
			dlm.addElement(tab);
		}
		list = new JList<String>(dlm);
		ctrl.disconnectFromMySQLDatabase();
		return dlm;
	}
}
