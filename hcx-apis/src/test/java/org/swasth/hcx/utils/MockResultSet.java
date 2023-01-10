package org.swasth.hcx.utils;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.mockito.Mockito.*;

public class MockResultSet {
    private final Map<String, Integer> columnIndices;
    private final Object[][] data;
    private int rowIndex;

    private MockResultSet(final String[] columnNames,
                          final Object[][] data) {
        // create a map of column name to column index
        this.columnIndices = IntStream.range(0, columnNames.length)
                .boxed()
                .collect(Collectors.toMap(
                        k -> columnNames[k],
                        Function.identity(),
                        (a, b) ->
                        { throw new RuntimeException("Duplicate column " + a); },
                        LinkedHashMap::new
                ));
        this.data = data;
        this.rowIndex = -1;
    }

    private ResultSet buildMock() throws SQLException {
        final var rs = mock(ResultSet.class);
        getNextMock(rs);
        getStringMock(rs);
        getLongMock(rs);
        getBooleanMock(rs);
        return rs;
    }

    private  ResultSet buildLongMock() {
        final  var rs = mock(ResultSet.class);
        return rs;
    }

    private ResultSet buildStringMock() throws SQLException {
        final var rs = mock(ResultSet.class);
        getNextMock(rs);
        getStringMock(rs);
        return rs;
    }

    private ResultSet buildEmptyMock() throws SQLException {
        final var rs = mock(ResultSet.class);
        getNextMock(rs);
        return rs;
    }

    private void getNextMock(ResultSet rs) throws SQLException {
        doAnswer(invocation -> {
            rowIndex++;
            return rowIndex < data.length;
        }).when(rs).next();
    }

    private void getStringMock(ResultSet rs) throws SQLException {
        doAnswer(invocation -> {
            final var columnName = invocation.getArgument(0, String.class);
            final var columnIndex = columnIndices.get(columnName);
            return data[rowIndex][columnIndex];
        }).when(rs).getString(anyString());
    }

    private void getIntMock(ResultSet rs) throws SQLException {
        doAnswer(invocation -> {
            final var columnName = invocation.getArgument(0, String.class);
            final var columnIndex = columnIndices.get(columnName);
            return data[rowIndex][columnIndex];
        }).when(rs).getInt(anyString());
    }

    private void getLongMock(ResultSet rs) throws SQLException {
        doAnswer(invocation -> {
            final var columnName = invocation.getArgument(0, String.class);
            final var columnIndex = columnIndices.get(columnName);
            return  (Long) data[rowIndex][columnIndex];
        }).when(rs).getLong(anyString());
    }

    private void getBooleanMock(ResultSet rs) throws SQLException {
        doAnswer(invocation -> {
            final var columnName = invocation.getArgument(0, String.class);
            final var columnIndex = columnIndices.get(columnName);
            return  (Boolean) data[rowIndex][columnIndex];
        }).when(rs).getBoolean(anyString());
    }

    /**
     * Creates the mock ResultSet.
     *
     * @param columnNames the names of the columns
     * @return a mocked ResultSet
     */
    public static ResultSet create(
            final String[] columnNames,
            final Object[][] data)
            throws SQLException {
        return new MockResultSet(columnNames, data).buildMock();
    }

    public static ResultSet createStringLongMock(
            final String[] columnNames,
            final Object[][] data)
            throws SQLException {
        return new MockResultSet(columnNames, data).buildLongMock();
    }

    public static ResultSet createStringMock(
            final String[] columnNames,
            final Object[][] data)
            throws SQLException {
        return new MockResultSet(columnNames, data).buildStringMock();
    }

    public static ResultSet createEmptyMock(
            final String[] columnNames,
            final Object[][] data)
            throws SQLException {
        return new MockResultSet(columnNames, data).buildEmptyMock();
    }
}

