package controller;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
//import java.sql.Types;
import java.util.ArrayList;
//import java.util.HashSet;

public class Control
{
	private String database;
	private String username;
	private String password;
	private String url;

	private static final String MYSQL_DATABASE_DRIVER = "com.mysql.jdbc.Driver";
	private static String MYSQL_DATABASE_URL = "jdbc:mysql://?:3306/"; // server ip missing

	private Connection connection = null;
	
//	private static final int FIELD = 1;
//	private static final int TYPE = 2;
//	private static final int NULL = 3;
//	private static final int KEY = 4;
//	private static final int DEFAULT = 5;
//	private static final int EXTRA = 6;

//	set for keeping track of which data types require apostrophe for insertion
//	private HashSet<Integer> ApostropheSet = new HashSet<Integer>();

	public Control(String db, String username, String pw, String url)
	{
		setDatabase(db);
		setUsername(username);
		setPassword(pw);
		setURL(url);
	}

	// inner class to store table describe
	public class DescribeResults
	{
		public DescribeResults()
		{
			setField("");
			setType("");
			setNull("");
			setKey("");
			setDefault("");
			setExtra("");
		}

		private String Field;
		private String Type;
		private String Null;
		private String Key;
		private String Default;
		private String Extra;

		public String getField()
		{
			return Field;
		}

		public void setField(String field)
		{
			Field = field;
		}

		public String getType()
		{
			return Type;
		}

		public void setType(String type)
		{
			Type = type;
		}

		public String getNull()
		{
			return Null;
		}

		public void setNull(String null1)
		{
			Null = null1;
		}

		public String getKey()
		{
			return Key;
		}

		public void setKey(String key)
		{
			Key = key;
		}

		public String getDefault()
		{
			return Default;
		}

		public void setDefault(String default1)
		{
			Default = default1;
		}

		public String getExtra()
		{
			return Extra;
		}

		public void setExtra(String extra)
		{
			Extra = extra;
		}
	}

	public static void main(String[] args)
	{
		//
	}

	/**
	 * Login Dialog takes care of params
	protected void parseLoginFileAndAssign()
	{
		Properties prop = new Properties();
		InputStream input = null;

		try
		{
			input = new FileInputStream("~/workspace/MySQLWorkbench/src/login.properties");

			// load a properties file
			prop.load(input);

			// get the property values and assign to variables
			database = prop.getProperty("database");
			username = prop.getProperty("dbuser");
			password = prop.getProperty("dbpassword");

		} catch (IOException ex)
		{
			ex.printStackTrace();
		} finally
		{
			if (input != null)
			{
				try
				{
					input.close();
				} catch (IOException e)
				{
					e.printStackTrace();
				}
			}
		}
	}
	*/

	public void connectToMySQLDatabase()
	{
		MYSQL_DATABASE_URL = MYSQL_DATABASE_URL.replace("?", getURL());	
		try
		{
			Class.forName(MYSQL_DATABASE_DRIVER);
			connection = DriverManager.getConnection(MYSQL_DATABASE_URL + getDatabase() + "?autoReconnect=true",
					getUsername(), getPassword());
		} catch (ClassNotFoundException e)
		{
			e.printStackTrace();
		} catch (SQLException e)
		{
			e.printStackTrace();
		}
	}

	public void disconnectFromMySQLDatabase()
	{
		try
		{
			connection.close();
		} catch (SQLException e)
		{
			e.printStackTrace();
		}
	}

	public ArrayList<String> getListOfTables()
	{
		connectToMySQLDatabase();
		Statement st = null;
		ResultSet rs = null;
		ArrayList<String> tables = new ArrayList<String>();

		try
		{
			st = connection.createStatement();
			rs = st.executeQuery("SHOW TABLES");

			while (rs.next())
			{
				tables.add(rs.getString(1));
			}
		} catch (SQLException e)
		{
			e.printStackTrace();
		} finally 
		{
			disconnectFromMySQLDatabase();
		}
		return tables;
	}

	public ResultSet returnContentsOfTable(String table)
	{
		connectToMySQLDatabase();
		PreparedStatement st = null;
		ResultSet rs = null;

		try
		{
			st = connection.prepareStatement("SELECT * FROM " + table);
			rs = st.executeQuery();
		} catch (SQLException e)
		{
			e.printStackTrace();
		} finally 
		{
			//disconnectFromMySQLDatabase();
		}
		return rs;
	}

	public String[] columnNames(String table)
	{
		ArrayList<String> tables = new ArrayList<String>();
		try
		{
			ResultSet rs = returnContentsOfTable(table); //connection still open
			ResultSetMetaData rsm = rs.getMetaData();
			int columns = rsm.getColumnCount();
			for (int i = 1; i <= columns; i++)
			{
				tables.add(rsm.getColumnName(i));
			}
		} catch (SQLException e)
		{
			e.printStackTrace();
		}  finally 
		{
			disconnectFromMySQLDatabase();
		}
		return tables.toArray(new String[tables.size()]);
	}

	public boolean createTable(String existingTable, String newTable)
	{
		String sql = "CREATE TABLE " + newTable + " LIKE " + existingTable;
		return performDDLOperation(sql);
		
		/**
		connectToMySQLDatabase();
		ResultSet rs = null;
		PreparedStatement st = null;
		int executed = -1;
		try
		{
			st = connection.prepareStatement("DESCRIBE " + existingTable);
			rs = st.executeQuery();

			ResultSetMetaData rsm = rs.getMetaData();
			int columns = rsm.getColumnCount();
			DescribeResults[] dr = new DescribeResults[columns];

			int column = 0;
			while (rs.next())
			{
				dr[column] = new DescribeResults();

				if (rs.getString(FIELD) != null)
				{
					dr[column].setField(rs.getString(FIELD));
				}
				if (rs.getString(TYPE) != null)
				{
					dr[column].setType(rs.getString(TYPE));
				}
				if (rs.getString(NULL) != null)
				{
					dr[column].setNull(rs.getString(NULL));
				}
				if (rs.getString(KEY) != null)
				{
					dr[column].setKey(rs.getString(KEY));
				}
				if (rs.getString(DEFAULT) != null)
				{
					dr[column].setDefault(rs.getString(DEFAULT));
				}
				if (rs.getString(EXTRA) != null)
				{
					dr[column].setExtra(rs.getString(EXTRA));
				}

				column++;
			}

			StringBuffer sb = new StringBuffer();
			sb.append("CREATE TABLE " + newTable + " (");
			for (int i = 0; i < column; i++)
			{
				sb.append(dr[i].getField() + " " + dr[i].getType());
				if (dr[i].getNull().equals("NO"))
				{
					sb.append(" NOT NULL");
				}
				if (!dr[i].getDefault().equals(""))
				{
					sb.append(" DEFAULT " + dr[i].getDefault());
				}
				if (dr[i].getKey().equals("PRI"))
				{
					sb.append(", primary key (" + dr[i].getField() + ")");
				}
				if (i != column - 1)
				{
					sb.append(", ");
				}
			}
			sb.append(");");
			String sql = sb.toString();
			st = connection.prepareStatement(sql);
			executed = st.executeUpdate(sql);
		} catch (SQLException e)
		{
			e.printStackTrace();
		} finally 
		{
			disconnectFromMySQLDatabase();
		}
		
		if (executed == 0)
		{
			return true;
		}
		return false;
		*/
	}

	public boolean copyContents(String existingTable, String newTable)
	{
		String sql = "INSERT INTO " + newTable + " SELECT * FROM " + existingTable;
		return performDMLOperation(sql);

		/**
		connectToMySQLDatabase();
		DatabaseMetaData databaseMetaData;
		ResultSet rs = null;
		Statement st = null;
		int executed = -1;
		
		populateApostropheSet();
		
		try
		{
			st = connection.createStatement();
			databaseMetaData = connection.getMetaData();
			rs = databaseMetaData.getTables(getDatabase(), null, newTable, null);
			boolean exists = false;
			while (rs.next())
			{
				exists = newTable.equals(rs.getString(3));
			}

			if (!exists)
			{
				createTable(existingTable, newTable);
			}

			rs = returnContentsOfTable(existingTable);
			ResultSetMetaData rsm = rs.getMetaData();
			int columns = rsm.getColumnCount();
			while (rs.next())
			{
				StringBuffer sb = new StringBuffer();
				sb.append("INSERT INTO " + newTable + " VALUES (");
				for (int i = 1; i <= columns; i++)
				{
					if (ApostropheSet.contains(rsm.getColumnType(i)))
					{
						sb.append("'" + rs.getObject(i) + "', ");
					} else
					{
						sb.append(rs.getObject(i) + ", ");
					}
				}
				String sql = sb.substring(0, sb.length() - 2) + ")"; // get rid of trailing comma and space
				executed = st.executeUpdate(sql);
			}
		} catch (SQLException e)
		{
			e.printStackTrace();
		} finally 
		{
			disconnectFromMySQLDatabase();
		}
		
		if (executed == 0)
		{
			return true;
		}
		return false;
		*/
	}

	public boolean dropTable (String table)
	{
		connectToMySQLDatabase();
		Statement st = null;
		int executed = -1;
		
		try
		{
			st = connection.createStatement();
			String sql = "DROP TABLE " + table;
			executed = st.executeUpdate(sql);
		} catch (SQLException e)
		{
			e.printStackTrace();
		} finally 
		{
			disconnectFromMySQLDatabase();
		}
		
		if (executed == 0)
		{
			return true;
		}
		return false;
	}
	
	public int numberOfRows (ResultSet rs)
	{
		connectToMySQLDatabase();
		int countRow = -1;
		try
		{
			countRow = rs.last() ? rs.getRow() : 0;
			rs.beforeFirst();
		} catch (SQLException e)
		{
			e.printStackTrace();
		} finally
		{
			disconnectFromMySQLDatabase();
		}
		return countRow;
	}
	
	public Object[][] actionPerformedGetContent(String table)
	{
		ResultSet rs = null;
		try
		{
			rs = returnContentsOfTable(table); //connection still open
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
			return data;
		} catch (SQLException e)
		{
			e.printStackTrace();
		} finally
		{
			disconnectFromMySQLDatabase();
		}
		return null; //should not get to here
	}
	
	public boolean performDMLOperation(String operation)
	{
		connectToMySQLDatabase();
		Statement st = null;
		int executed = -1;
		
		try
		{
			st = connection.createStatement();
			executed = st.executeUpdate(operation);
		} catch (SQLException e)
		{
			e.printStackTrace();
		} finally
		{
			disconnectFromMySQLDatabase();
		}
		if (executed != 0)
		{
			return true;
		}
		return false;
	}

	public boolean performDDLOperation(String operation)
	{
		connectToMySQLDatabase();
		Statement st = null;
		int executed = -1;
		
		try
		{
			st = connection.createStatement();
			executed = st.executeUpdate(operation);
		} catch (SQLException e)
		{
			e.printStackTrace();
		} finally
		{
			disconnectFromMySQLDatabase();
		}
		if (executed == 0)
		{
			return true;
		}
		return false;
	}
	
	/**
	private void populateApostropheSet()
	{
		ApostropheSet.add(Types.CHAR);
		ApostropheSet.add(Types.VARCHAR);
		ApostropheSet.add(Types.DATE);
		ApostropheSet.add(Types.TIMESTAMP);
		ApostropheSet.add(Types.TIMESTAMP_WITH_TIMEZONE);
		ApostropheSet.add(Types.TIME);
		ApostropheSet.add(Types.TIME_WITH_TIMEZONE);
		ApostropheSet.add(Types.CLOB);
		ApostropheSet.add(Types.BLOB);
		ApostropheSet.add(Types.LONGNVARCHAR);
		ApostropheSet.add(Types.NCHAR);
	}
	*/
	
	protected String getUsername()
	{
		return new String(username);
	}

	public String getDatabase()
	{
		return new String(database);
	}

	public String getPassword()
	{
		return new String(password);
	}
	
	protected void setDatabase(String database)
	{
		this.database = database;
	}

	protected void setUsername(String username)
	{
		this.username = username;
	}

	protected void setPassword(String password)
	{
		this.password = password;
	}
	
	protected String getURL()
	{
		return new String(url);
	}
	
	protected void setURL(String url)
	{
		if (url == null)
		{
			this.url = "localhost";
		} else
		{
			this.url = url;
		}
	}
}
