package au.com.shamal.nutrisnap;

import java.io.Serializable;

public class HistoryResultsData implements Serializable {

    private String email;
    private Float  calories;
    private String date;
    private String food;

    public HistoryResultsData(String email, Float calories, String date, String food) {
        this.email = email;
        this.calories = calories;
        this.date = date;
        this.food = food.substring(2);
    }

    public HistoryResultsData() {
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Float getCalories() {
        return calories;
    }

    public void setCalories(Float calories) {
        this.calories = calories;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getFood() {
        return food;
    }

    public void setFood(String food) {
        this.food = food;
    }
}
