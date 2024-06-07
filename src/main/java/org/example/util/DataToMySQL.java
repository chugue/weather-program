package org.example.util;

import org.apache.poi.openxml4j.util.ZipSecureFile;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.sql.*;
import java.util.stream.Collectors;

public class DataToMySQL {
    private static final String INSERT_SQL = "INSERT INTO locations (region_code, level1, level2, level3, nx, ny) VALUES (?, ?, ?, ?, ?, ?)";
    private static final String SCHEMA_FILE_PATH = "/schema.sql";


    public DataToMySQL() {}

    public void insertDataToSql() {
        ZipSecureFile.setMinInflateRatio(0.001);
        String excelFilePath = "src/main/resources/location_data.xlsx";

        try (FileInputStream fis = new FileInputStream(excelFilePath);
            Workbook workbook = new XSSFWorkbook(fis);){

            Connection connection = DatabaseUtil.getConnection();
            Sheet sheet = workbook.getSheetAt(0);
            PreparedStatement preparedStatement = connection.prepareStatement(INSERT_SQL);

            // 테이블 존재 여부 확인
            if (!isTableExists(connection, "locations")) {
            } else {
                return;
            }

            // 테이블 생성
            try (Statement stmt = connection.createStatement()) {
                String schemaSQL = readSchemaSQL();
                stmt.execute(schemaSQL);
            }

            for (Row row : sheet) {
                if (row.getRowNum() == 0) {
                    continue;
                }
                // 각 셀의 값을 읽어 변수에 저장
                String region_code = getCellStringValue(row.getCell(1)); // 행정구역코드
                String level1 = getCellStringValue(row.getCell(2)); // 1단계
                String level2 = getCellStringValue(row.getCell(3)); // 2단계
                String level3 = getCellStringValue(row.getCell(4)); // 3단계
                String nx = getCellStringValue(row.getCell(5)); // 위도
                String ny = getCellStringValue(row.getCell(6)); // 경도

                // 준비된 구문에 변수 값을 설정
                preparedStatement.setString(1, region_code);
                preparedStatement.setString(2, level1);
                preparedStatement.setString(3, level2);
                preparedStatement.setString(4, level3);
                preparedStatement.setString(5, nx);
                preparedStatement.setString(6, ny);
                // 배치에 추가
                preparedStatement.addBatch();
            }
            // 배치 실행
            preparedStatement.executeBatch();
            // 데이터 삽입 완료 메시지 출력
            System.out.println("Data has been inserted successfully.");

        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private String getCellStringValue(Cell cell) {
        if (cell == null) {
            return "";
        }
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                return String.valueOf(cell.getNumericCellValue());
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                return cell.getCellFormula();
            default:
                return "";
        }
    }


    private String readSchemaSQL() throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream(SCHEMA_FILE_PATH)))) {
            return reader.lines().collect(Collectors.joining("\n"));
        }
    }

    private boolean isTableExists(Connection connection, String tableName) throws SQLException {
        DatabaseMetaData metaData = connection.getMetaData();
        try (ResultSet resultSet = metaData.getTables(null, null, tableName, new String[] {"TABLE"})) {
            return resultSet.next();
        }
    }
}
