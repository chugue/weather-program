package org.example.response;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class ResponseDTO {
    public Response response;

    public static class Response {
        public Body body;

        public static class Body {
            public Items items;

            public static class Items {
                @SerializedName("item")
                public List<Item> item;

                public static class Item {
                    public String baseDate;
                    public String baseTime;
                    public String category;
                    public String obsrValue;

                    @Override
                    public String toString() {
                        return "Item{" +
                                "baseDate='" + baseDate + '\'' +
                                ", baseTime='" + baseTime + '\'' +
                                ", category='" + category + '\'' +
                                ", obsrValue='" + obsrValue + '\'' +
                                '}';
                    }
                }
            }
        }
    }
}