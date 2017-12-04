package ViewModel;
import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JButton;
import javax.swing.JTextField;

import java.awt.Font;
import javax.swing.JPasswordField;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.awt.event.ActionEvent;
import javax.swing.JCheckBox;
import javax.swing.JSeparator;

public class LoginDialog
{

	private JFrame mframe;
	private JTextField mtextFieldDB;
	private JTextField mtextFieldUsername;
	private JPasswordField mpasswordField;
	private static final String MYSQL_DATABASE_DRIVER = "com.mysql.jdbc.Driver";
	private static String MYSQL_DATABASE_URL = "jdbc:mysql://?:3306/"; // db name missing jdbc:mysql://localhost:3306/
	protected static final int TIMEOUT = 60;
	private JTextField mtextFieldServerIP;
	
	/**
	 * Launch the application.
	 */
	public static void main(String[] args)
	{
		EventQueue.invokeLater(new Runnable()
		{
			public void run()
			{
				try
				{
					LoginDialog window = new LoginDialog();
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
	public LoginDialog()
	{
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize()
	{
		mframe = new JFrame();
		mframe.setBounds(420, 250, 450, 320);
		mframe.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		mframe.getContentPane().setLayout(null);

		JLabel lblWelcomeToMysql = new JLabel("Welcome to MySQL Workbench");
		lblWelcomeToMysql.setFont(new Font("Lucida Grande", Font.PLAIN, 15));
		lblWelcomeToMysql.setBounds(108, 17, 233, 16);
		mframe.getContentPane().add(lblWelcomeToMysql);

		JLabel lblDatabase = new JLabel("Database:");
		lblDatabase.setBounds(45, 62, 70, 25);
		mframe.getContentPane().add(lblDatabase);

		JLabel lblUsername = new JLabel("Username:");
		lblUsername.setBounds(45, 109, 70, 25);
		mframe.getContentPane().add(lblUsername);

		JLabel lblPassword = new JLabel("Password:");
		lblPassword.setBounds(45, 157, 70, 25);
		mframe.getContentPane().add(lblPassword);

		mtextFieldDB = new JTextField();
		mtextFieldDB.setBounds(167, 55, 233, 39);
		mframe.getContentPane().add(mtextFieldDB);
		mtextFieldDB.setColumns(10);

		mtextFieldUsername = new JTextField();
		mtextFieldUsername.setColumns(10);
		mtextFieldUsername.setBounds(167, 102, 233, 39);
		mframe.getContentPane().add(mtextFieldUsername);

		mpasswordField = new JPasswordField();
		mpasswordField.setBounds(167, 151, 233, 36);
		mframe.getContentPane().add(mpasswordField);
		
		JCheckBox chckbxSqlOnServer = new JCheckBox("SQL on Server");
		chckbxSqlOnServer.setFont(new Font("Lucida Grande", Font.PLAIN, 12));
		chckbxSqlOnServer.setBounds(35, 206, 117, 25);
		mframe.getContentPane().add(chckbxSqlOnServer);
		
		mtextFieldServerIP = new JTextField();
		mtextFieldServerIP.setBounds(167, 206, 233, 25);
		mframe.getContentPane().add(mtextFieldServerIP);
		mtextFieldServerIP.setColumns(10);
		
		JButton btnLogin = new JButton("Login");
		btnLogin.addActionListener(new ActionListener() 
		{
			public void actionPerformed(ActionEvent e) 
			{
				Connection connection = null;
				
				String database = mtextFieldDB.getText();
				String username = mtextFieldUsername.getText();
				String pw = mpasswordField.getText();
				String mySQLServerIP = null;
				
				if (database.isEmpty() || username.isEmpty() || pw.isEmpty())
				{
					JOptionPane.showMessageDialog(mframe, "Parameters cannot be empty.");
				}
				
				if (chckbxSqlOnServer.isSelected())
				{
					mySQLServerIP = mtextFieldServerIP.getText();
				} else
				{
					mySQLServerIP = "localhost";
				}
				
				try
				{
					Class.forName(MYSQL_DATABASE_DRIVER);
					MYSQL_DATABASE_URL = MYSQL_DATABASE_URL.replace("?", mySQLServerIP);
					
					connection = DriverManager.getConnection(MYSQL_DATABASE_URL + database + "?autoReconnect=true", username, pw);
					if (!connection.isClosed())
					{
						String[] params = {database, username, pw, mySQLServerIP}; 
						MySQLWorkbenchApp.main(params);
						mframe.dispose();
					}
					connection.close();
				} catch (SQLException sqle)
				{
					JOptionPane.showMessageDialog(mframe, "Invalid parameters. Try again.", "Error", JOptionPane.ERROR_MESSAGE);
				} catch (ClassNotFoundException cnfe)
				{
					JOptionPane.showMessageDialog(mframe, "Internal Error. Restart the application.", "Error", JOptionPane.ERROR_MESSAGE);					
				}
			}
		});
		btnLogin.setBounds(31, 263, 117, 29);
		mframe.getContentPane().add(btnLogin);

		JButton btnReset = new JButton("Reset");
		btnReset.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				mtextFieldDB.setText("");
				mtextFieldUsername.setText("");
				mpasswordField.setText("");
			}
		});
		btnReset.setBounds(163, 263, 117, 29);
		mframe.getContentPane().add(btnReset);

		JButton btnNewButton = new JButton("Exit");
		btnNewButton.addActionListener(new ActionListener() 
		{
			public void actionPerformed(ActionEvent e) 
			{
				int reply = JOptionPane.showConfirmDialog(null, "Do you want to exit ?", "Exit", JOptionPane.YES_NO_CANCEL_OPTION);
	            if (reply == JOptionPane.YES_OPTION)
	            {
	                System.exit(0);
	            }
			}
		});
		btnNewButton.setBounds(297, 263, 117, 29);
		mframe.getContentPane().add(btnNewButton);
		
		JSeparator separator = new JSeparator();
		separator.setBounds(6, 243, 438, 8);
		mframe.getContentPane().add(separator);
	}
}
