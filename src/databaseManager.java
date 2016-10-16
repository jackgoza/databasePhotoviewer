
import java.io.*;
import java.sql.*;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author jack
 */
public class databaseManager {

    private static Connection con;
    private static Statement stmt;

    private static void openConnection() {
	String url = "jdbc:mysql://kc-sce-appdb01.kc.umkc.edu/jgkp9?allowMultiQueries=true";
	String userID = "jgkp9";
	String password = "java";

	try {
	    Class.forName("com.mysql.jdbc.Driver");
	}
	catch (java.lang.ClassNotFoundException e) {
	    System.out.println(e);
	    System.exit(0);
	}

	try {
	    con = DriverManager.getConnection(url, userID, password);
	    con.setAutoCommit(false);
	    stmt = con.createStatement();
	}
	catch (SQLException s) {
	    System.err.println("Error opening connection");
	    s.printStackTrace();
	}
    }

    public void initializeTable() {
	try {

	    openConnection();

	    stmt.executeUpdate("DROP TABLE IF EXISTS Photos");

	    String createTable = "create table Photos "
		    + "(id INT NOT NULL AUTO_INCREMENT,"
		    + " pictnum INT NOT NULL, date VARCHAR(24),"
		    + " description VARCHAR(256), photo LONG VARBINARY NULL,"
		    + " PRIMARY KEY (id))";

	    stmt.executeUpdate(createTable);
	    cleanup();
	}
	catch (SQLException e) {
	    System.err.println("Error initing table");
	    e.printStackTrace();
	}

    }

    public int getTableSize() {
	try {
	    openConnection();

	    String sizeString = "SELECT COUNT(*) FROM Photos";

	    ResultSet rs;

	    rs = stmt.executeQuery(sizeString);
	    rs.next();

	    return (rs.getInt("COUNT(*)"));

	}
	catch (SQLException sizeE) {
	    System.err.println("Error retrieving size of table");
	    sizeE.printStackTrace();
	    return -1;
	}

    }

    public void addPhotoToDatabase(databasePhoto newPhoto) throws SQLException {

	openConnection();
	PreparedStatement addStatement = con.prepareStatement(
		"INSERT INTO photos (pictnum, date, description, photo) VALUES (?,?,?,?)"
	);

	addStatement.setInt(1, 1);
	addStatement.setString(2, newPhoto.date);
	addStatement.setString(3, newPhoto.description);
	ByteArrayInputStream bis = new ByteArrayInputStream(newPhoto.imageArray);
	addStatement.setBinaryStream(4, bis, (int) newPhoto.imageArray.length);

	addStatement.executeUpdate();
	addStatement.close();

	cleanup();

    }

    public databasePhoto getNewPhoto(int index) throws SQLException {
	openConnection();

	PreparedStatement getPhotoStatement = con.prepareStatement(
		"SELECT * FROM photos WHERE pictnum = ?"
	);

	getPhotoStatement.setInt(1, index);
	ResultSet rs;
	rs = getPhotoStatement.executeQuery();
	ResultSetMetaData rsmd = rs.getMetaData();
	databasePhoto newPhoto = new databasePhoto();

	newPhoto.photoNumber = rs.getInt("pictnum");
	newPhoto.date = rs.getString("date");
	newPhoto.description = rs.getString("description");
	newPhoto.imageArray = rs.getBytes("photo");

	cleanup();

	return newPhoto;

    }

    public void deletePhoto(int index) throws SQLException {
	openConnection();

	PreparedStatement deletePhotoStatement = con.prepareStatement(
		"DELETE FROM photos WHERE pictnum = ?"
	);

	deletePhotoStatement.setInt(1, index);

	deletePhotoStatement.executeUpdate();

	PreparedStatement deleteIndexFixer = con.prepareStatement(
		"UPDATE Photos SET pictnum=pictnum-1 WHERE pictnum > ?"
	);

	deletePhotoStatement.setInt(1, index);

	deletePhotoStatement.executeUpdate();

	cleanup();

    }

    public void cleanup() throws SQLException {

	stmt.close();
	con.close();
    }

}
