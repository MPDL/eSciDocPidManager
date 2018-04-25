package de.escidoc.pidmanager.test.common;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Vector;

/**
 * 
 * @author SWA
 * 
 */
public class CreatedHandles {

    private SqLiteConnector dbConnect = null;

    // private static final String SQL_FILENAME =
    // "forTestsCreatedHandles.sqlite";
    //
    // private static final String TABLE_NAME = "createdHandles";
    //
    // private static final String[] COLNAMES = new String[] { "timeStamp LONG",
    // "handle TEXT", "deleted INT" };

    private TestConfiguration conf;

    /**
     * 
     * @throws Exception
     */
    public CreatedHandles() throws Exception {

        this.conf = new TestConfiguration();
        this.dbConnect =
            new SqLiteConnector(this.conf.getDatabase(), this.conf.getTable(),
                this.conf.getColNames());
    }

    /**
     * 
     * @param handle
     * @throws Exception
     */
    public void add(final String handle) throws Exception {

        this.dbConnect.update("INSERT INTO " + this.conf.getTable()
            + " VALUES ('" + System.currentTimeMillis() + "', '" + handle
            + "', '0');");
    }

    /**
     * 
     * @param handle
     * @throws Exception
     */
    public void remove(final String handle) throws Exception {

        this.dbConnect.update("UPDATE " + this.conf.getTable()
            + " SET deleted='1' WHERE handle='" + handle + "';");
    }

    /**
     * 
     * @return
     * @throws SQLException
     */
    public Vector<String> getAllHandles() throws SQLException {

        Vector<String> list = new Vector<String>();
        ResultSet rs =
            this.dbConnect.request("SELECT " + this.conf.getColNames()[1]
                + " FROM " + this.conf.getTable() + " WHERE deleted='0';");

        while (rs.next()) {
            list.add(rs.getString(1));
        }

        return (list);
    }
}
