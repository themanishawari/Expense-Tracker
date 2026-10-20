import java.time.LocalDate;

/**
 * Represents a single financial transaction (income or expense).
 */
public class Transaction {
    private int id;
    private TransactionType type;
    private String category;
    private double amount;
    private LocalDate date;
    private String description;

    public Transaction(int id, TransactionType type, String category,
                        double amount, LocalDate date, String description) {
        this.id = id;
        this.type = type;
        this.category = category;
        this.amount = amount;
        this.date = date;
        this.description = description;
    }

    public int getId() {
        return id;
    }

    public TransactionType getType() {
        return type;
    }

    public void setType(TransactionType type) {
        this.type = type;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    /** Converts this transaction to a single CSV line for file storage. */
    public String toCsv() {
        // Escape commas in description by replacing them with semicolons
        String safeDescription = description == null ? "" : description.replace(",", ";");
        return id + "," + type + "," + category + "," + amount + "," + date + "," + safeDescription;
    }

    /** Parses a CSV line back into a Transaction object. */
    public static Transaction fromCsv(String line) {
        String[] parts = line.split(",", 6);
        int id = Integer.parseInt(parts[0]);
        TransactionType type = TransactionType.valueOf(parts[1]);
        String category = parts[2];
        double amount = Double.parseDouble(parts[3]);
        LocalDate date = LocalDate.parse(parts[4]);
        String description = parts.length > 5 ? parts[5] : "";
        return new Transaction(id, type, category, amount, date, description);
    }

    @Override
    public String toString() {
        return String.format("#%-4d %-8s %-15s Rs.%-10.2f %-12s %s",
                id, type, category, amount, date, description);
    }
}
