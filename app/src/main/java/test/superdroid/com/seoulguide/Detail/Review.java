package test.superdroid.com.seoulguide.Detail;

public class Review {
    private String writer;
    private String info;
    private String date;
    private String point;

    public Review(String writer, String info, String date, String point) {
        this.writer = writer;
        this.info = info;
        this.date = date;
        this.point = point;
    }

    public String getWriter() {
        return writer;
    }

    public void setWriter(String writer) {
        this.writer = writer;
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getPoint() {
        return point;
    }

    public void setPoint(String point) {
        this.point = point;
    }
}
