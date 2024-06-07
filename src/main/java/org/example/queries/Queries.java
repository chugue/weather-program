package org.example.queries;

public class Queries {
    public static final String FIND_LEVEL1_LIST = "SELECT DISTINCT level1 FROM locations";
    public static final String FIND_LEVEL2_LIST = "SELECT DISTINCT level2 FROM locations where level1 = ?";
    public static final String FIND_LEVEL3_LIST = "SELECT DISTINCT level3 FROM locations where level1 = ? and level2 = ?";
    public static final String FIND_NX_NY = "SELECT nx, ny FROM locations where level1 = ? and level2 = ? and level3 = ?";
}
