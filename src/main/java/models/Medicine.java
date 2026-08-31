package models;

public class Medicine {
    private int id;
    private String name, brand, description, categoryName;
    private double price;
    private int stock;
    private boolean requiresRx;

    public Medicine() {}

    public Medicine(int id, String name, String brand, String categoryName,
                    double price, int stock, String description, boolean requiresRx) {
        this.id = id;
        this.name = name;
        this.brand = brand;
        this.categoryName = categoryName;
        this.price = price;
        this.stock = stock;
        this.description = description;
        this.requiresRx = requiresRx;
    }

    public int     getId()           { return id; }
    public String  getName()         { return name; }
    public String  getBrand()        { return brand; }
    public String  getCategoryName() { return categoryName; }
    public double  getPrice()        { return price; }
    public int     getStock()        { return stock; }
    public String  getDescription()  { return description; }
    public boolean isRequiresRx()    { return requiresRx; }

    public void setId(int id)               { this.id = id; }
    public void setName(String name)        { this.name = name; }
    public void setBrand(String brand)      { this.brand = brand; }
    public void setCategoryName(String c)   { this.categoryName = c; }
    public void setPrice(double price)      { this.price = price; }
    public void setStock(int stock)         { this.stock = stock; }
    public void setDescription(String d)    { this.description = d; }
    public void setRequiresRx(boolean r)    { this.requiresRx = r; }

    @Override
    public String toString() {
        return name + " (" + brand + ") — ₹" + price;
    }
}
