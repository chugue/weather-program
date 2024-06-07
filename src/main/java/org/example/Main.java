package org.example;

import org.example.util.DataToMySQL;
import org.example.response.ResponseDTO;
import org.example.util.Search;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws SQLException, IOException {
        // MySQL 데이터 베이스 생성
        DataToMySQL data = new DataToMySQL();
        Scanner sc = new Scanner(System.in);
        Search search = new Search();
        data.insertDataToSql();

        System.out.println("👉👉👉 궁금하신 '도시'를 선택해주세요 👈👈👈");
        search.getLevel1List();
        System.out.print("입력 : ");
        String level1 = sc.nextLine().trim();
        System.out.println("'" + level1 + "'" + "을 선택하셨습니다.");
        System.out.println("-------------------------------------");

        System.out.println("👉👉👉 궁금하신 '구'를 선택해주세요 👈👈👈");
        search.getLevel2List(level1);
        System.out.print("입력 : ");
        String level2 = sc.nextLine().trim();
        System.out.println("'" + level2 + "'" + "을 선택하셨습니다.");
        System.out.println("-------------------------------------");

        System.out.println("👉👉👉 궁금하신 '동'을 선택해주세요 👈👈👈");
        search.getLevel3List(level1, level2);
        System.out.print("입력 : ");
        String level3 = sc.nextLine().trim();
        System.out.println("'" + level3 + "'" + "을 선택하셨습니다.");
        System.out.println("-------------------------------------");



        ResponseDTO.Response.Body.Items.Item item = search.findWeather(level1, level2, level3);

        System.out.println("👉👉👉 요청하신 지역은 '"+ level1 + " "+ level2 + " " + level3 + "'이며, 이 지역의 온도는 " + item.obsrValue + " 입니다. 👈👈👈");
    }
}