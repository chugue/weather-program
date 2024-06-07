package org.example.util;

import com.google.gson.Gson;
import org.example.response.ResponseDTO;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;

import static org.example.queries.Queries.*;

public class Search {
    private List<String> level1List = new ArrayList<String>();
    private List<String> level2List = new ArrayList<String>();
    private List<String> level3List = new ArrayList<String>();
    private String serviceKey = "%2BKowkX3tZBbVfxWVar7jObSgNfmLNbgRXoPVcI8%2F%2FkPIzcCrsNbdoe3pMB4ds%2BSF8%2B2PtDFZtc6I4DPI%2BhXaWQ%3D%3D";

    public ResponseDTO.Response.Body.Items.Item findWeather(String level1, String level2, String level3) throws IOException, SQLException {

        String todayDate = getTodayDate();
        String recentFullHour = getRecentFullHour();
        Map<String, String> nxny = getNxNy(level1, level2, level3);
        Gson gson = new Gson();

        StringBuilder urlBuilder = new StringBuilder("http://apis.data.go.kr/1360000/VilageFcstInfoService_2.0/getUltraSrtNcst"); /*URL*/
        urlBuilder.append("?" + URLEncoder.encode("serviceKey", "UTF-8") + "=" + serviceKey); /*Service Key*/
        urlBuilder.append("&" + URLEncoder.encode("pageNo", "UTF-8") + "=" + URLEncoder.encode("1", "UTF-8")); /*페이지번호*/
        urlBuilder.append("&" + URLEncoder.encode("numOfRows", "UTF-8") + "=" + URLEncoder.encode("10", "UTF-8")); /*한 페이지 결과 수*/
        urlBuilder.append("&" + URLEncoder.encode("dataType", "UTF-8") + "=" + URLEncoder.encode("JSON", "UTF-8")); /*요청자료형식(XML/JSON) Default: XML*/
        urlBuilder.append("&" + URLEncoder.encode("base_date", "UTF-8") + "=" + URLEncoder.encode(todayDate, "UTF-8")); /*‘21년 6월 28일 발표*/
        urlBuilder.append("&" + URLEncoder.encode("base_time", "UTF-8") + "=" + URLEncoder.encode(recentFullHour, "UTF-8")); /*06시 발표(정시단위) */
        urlBuilder.append("&" + URLEncoder.encode("nx", "UTF-8") + "=" + URLEncoder.encode(nxny.get("nx"), "UTF-8")); /*예보지점의 X 좌표값*/
        urlBuilder.append("&" + URLEncoder.encode("ny", "UTF-8") + "=" + URLEncoder.encode(nxny.get("ny"), "UTF-8")); /*예보지점의 Y 좌표값*/

        URL url = new URL(urlBuilder.toString());
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        conn.setRequestMethod("GET");
        conn.setRequestProperty("Content-type", "application/json");
        BufferedReader rd;

        if (conn.getResponseCode() >= 200 && conn.getResponseCode() <= 300) {
            rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        } else {
            rd = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
        }
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = rd.readLine()) != null) {
            sb.append(line);
        }
        rd.close();
        conn.disconnect();

        ResponseDTO responseDTO = gson.fromJson(String.valueOf(sb), ResponseDTO.class);
        ResponseDTO.Response.Body.Items items =  responseDTO.response.body.items;

        Optional<ResponseDTO.Response.Body.Items.Item> t1hItem = items.item.stream()
                .filter(item -> item.category.equals("T1H"))
                .findFirst();

        return t1hItem.get();
    }

    public void getLevel1List() throws SQLException {
        Connection connection = DatabaseUtil.getConnection();

        PreparedStatement ps =
                connection.prepareStatement(FIND_LEVEL1_LIST);
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            String level1 = rs.getString("level1");
            level1List.add(level1);
        }

        System.out.println(level1List.toString());
    }

    public void getLevel2List(String selectedValue) throws SQLException {
        Connection connection = DatabaseUtil.getConnection();

        if (!level1List.contains(selectedValue.trim())) {
            System.out.println("유효한 도시이름이 아닙니다.");
            System.out.println("다시 프로그램을 실행해 주세요");
            return;
        }

        PreparedStatement ps = connection.prepareStatement(FIND_LEVEL2_LIST);
        ps.setString(1, selectedValue.trim());
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            String level1 = rs.getString("level2");
            level2List.add(level1);
        }

        System.out.println(level2List.toString());
    }

    public void getLevel3List(String level1Selected, String level2Selected) throws SQLException {
        Connection connection = DatabaseUtil.getConnection();

        if (!level2List.contains(level2Selected.trim())) {
            System.out.println("유효한 '동'이름이 아닙니다.");
            System.out.println("다시 프로그램을 실행해 주세요");
            return;
        }

        PreparedStatement ps = connection.prepareStatement(FIND_LEVEL3_LIST);
        ps.setString(1, level1Selected.trim());
        ps.setString(2, level2Selected.trim());
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            String level1 = rs.getString("level3");
            level3List.add(level1);
        }

        System.out.println(level3List.toString());
    }



    private Map<String, String> getNxNy(String level1, String level2, String level3) throws SQLException {
        Connection connection = DatabaseUtil.getConnection();
        PreparedStatement ps = connection.prepareStatement(FIND_NX_NY);
        ps.setString(1, level1);
        ps.setString(2, level2);
        ps.setString(3, level3);

        ResultSet rs = ps.executeQuery();
        Map<String, String> nxny = new HashMap<>();
        while (rs.next()) {
            String nx = rs.getString("nx");
            String ny = rs.getString("ny");
            nxny.put("nx", nx);
            nxny.put("ny", ny);
        }

        rs.close();
        ps.close();
        connection.close();

        return nxny;
    }

    private String getTodayDate() {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd");
        Date now = new Date();
        return formatter.format(now);
    }

    private String getRecentFullHour(){
        SimpleDateFormat formatter = new SimpleDateFormat("HHmm");
        Calendar calendar = Calendar.getInstance();

        // 현재 시각을 기준으로 최근 정시 시각을 설정
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        // 최근 정시 시각을 Date 형식으로 변환 후 포맷팅
        Date recentFullHour = calendar.getTime();
        return formatter.format(recentFullHour);
    }
}
