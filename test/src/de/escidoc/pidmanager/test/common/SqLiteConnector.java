package de.escidoc.pidmanager.test.common;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.log4j.Logger;

/**
 * Connector to SQLite Databases.
 * 
 * @author SWA
 * 
 */
public class SqLiteConnector {

    private static Logger log = Logger.getLogger(SqLiteConnector.class);

    private Connection conn = null;

    private String sqlFileName = null;

    private String tableName = null;

    private String[] columNames = null;

    /**
     * Connector to the SQLite DB.
     * 
     * @param fileName
     *            Name of the SQLite DB file.
     * @param tableName
     *            Name of table.
     * @param colNames
     *            Name of cols.
     * @throws Exception
     *             Thrown if connection fails.
     */
    public SqLiteConnector(final String fileName, final String tableName,
        final String[] colNames) throws Exception {

        this.sqlFileName = fileName;
        this.tableName = tableName;
        this.columNames = colNames;

        Class.forName("org.sqlite.JDBC");
        this.conn = DriverManager.getConnection("jdbc:sqlite:" + sqlFileName);

        if (!checkTableExists(tableName)) {
            createTable();
        }
    }

    /**
     * DB request.
     * 
     * @param request
     * @return ResultSet for the request.
     * @throws SQLException
     */
    public ResultSet request(final String request) throws SQLException {

        ResultSet rs = null;

        if (this.conn.isClosed()) {
            this.conn =
                DriverManager.getConnection("jdbc:sqlite:" + sqlFileName);
        }

        try {
            Statement stat = conn.createStatement();

            // SELECT MAX(serial) FROM tableName - does not work :-(
            rs = stat.executeQuery(request);

        }
        catch (Exception e) {
            this.conn.close();
            throw new SQLException("DB Error, connection closed.", e.toString());
        }

        return (rs);
    }

    /**
     * Update DB entry.
     * 
     * @param request
     * @throws SQLException
     */
    public void update(final String request) throws SQLException {

        if (this.conn.isClosed()) {
            this.conn =
                DriverManager.getConnection("jdbc:sqlite:" + sqlFileName);
        }

        try {
            Statement stat = conn.createStatement();
            stat.executeUpdate(request);
        }
        catch (Exception e) {
            this.conn.close();
            throw new SQLException("DB Error, connection closed.", e.toString());
        }
    }

    /**
     * Check if SQLite DB contains table with name.
     * 
     * @param conn
     *            SQLite DB connection.
     * @param table
     *            Name of table.
     * @return true if table with name exists, false otherwise
     * @throws SQLException
     *             Thrown if DB operation fail.
     */
    private boolean checkTableExists(final String table) throws SQLException {

        DatabaseMetaData meta = this.conn.getMetaData();
        ResultSet rs =
            meta.getTables(null, null, table, new String[] { "TABLE" });

        String checkName = table.toLowerCase();

        while (rs.next()) {
            String foundTable = rs.getString("TABLE_NAME").trim().toLowerCase();
            if (foundTable.equals(checkName)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Creating table for serial number.
     * 
     * @param conn
     *            SQLite DB connection.
     * @throws SQLException
     *             Thrown if DB operation fail.
     */
    private void createTable() throws SQLException {

        Statement stat = this.conn.createStatement();

        String sqlQuery = "CREATE TABLE " + this.tableName + " (";

        for (int i = 0; i < this.columNames.length; i++) {

            sqlQuery += this.columNames[i];
            if ((i + 1) < this.columNames.length) {
                sqlQuery += ", ";
            }
        }
        sqlQuery += ");";

        stat.executeUpdate(sqlQuery);
        log.info("New table created: " + sqlQuery);
    }

}
