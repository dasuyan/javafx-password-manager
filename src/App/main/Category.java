package App.main;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Category {

    private final SimpleStringProperty category;

    public Category(String category) {
        this.category = new SimpleStringProperty(category);
    }

    public String getCategory() {
        return this.category.get();
    }

    public StringProperty categoryProperty() {
        return this.category;
    }

    public void setCategory(String category) {
        this.category.set(category);
    }

    @Override
    public String toString() {
        return this.category.get();
    }

}