import java.io.*;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;

/**
 * Holds all transactions and the monthly budget, and provides
 * the core add/edit/delete/summary operations.
 * Also handles saving to and loading from simple CSV files so
 * data persists between runs.
 */
public class BudgetManager {
    private static final String TRANSACTIONS_FILE = "transactions.csv";
    private static final String BUDGET_FILE = "budget.txt";

    private final List<Transaction> transactions = new ArrayList<>();
    private int nextId = 1;
    private double monthlyBudget = 0.0;

    public BudgetManager() {
        loadTransactions();
        loadBudget();
    }

    // ---------- Budget ----------

    public void setMonthlyBudget(double budget) {
        this.monthlyBudget = budget;
        saveBudget();
    }

    public double getMonthlyBudget() {
        return monthlyBudget;
    }

    // ---------- Add / Edit / Delete ----------

    public Transaction addTransaction(TransactionType type, String category,
                                       double amount, LocalDate date, String description) {
        Transaction t = new Transaction(nextId++, type, category, amount, date, description);
        transactions.add(t);
        saveTransactions();
        return t;
    }

    public boolean editTransaction(int id, TransactionType type, String category,
                                    Double amount, LocalDate date, String description) {
        Transaction t = findById(id);
        if (t == null) return false;
        if (type != null) t.setType(type);
        if (category != null && !category.isBlank()) t.setCategory(category);
        if (amount != null) t.setAmount(amount);
        if (date != null) t.setDate(date);
        if (description != null) t.setDescription(description);
        saveTransactions();
        return true;
    }

    public boolean deleteTransaction(int id) {
        boolean removed = transactions.removeIf(t -> t.getId() == id);
        if (removed) saveTransactions();
        return removed;
    }

    public Transaction findById(int id) {
        for (Transaction t : transactions) {
            if (t.getId() == id) return t;
        }
        return null;
    }

    public List<Transaction> getAllTransactions() {
        return Collections.unmodifiableList(transactions);
    }

    // ---------- Summaries ----------

    public double getTotalIncome() {
        return transactions.stream()
                .filter(t -> t.getType() == TransactionType.INCOME)
                .mapToDouble(Transaction::getAmount)
                .sum();
    }

    public double getTotalExpense() {
        return transactions.stream()
                .filter(t -> t.getType() == TransactionType.EXPENSE)
                .mapToDouble(Transaction::getAmount)
                .sum();
    }

    public double getRemainingBudget() {
        return monthlyBudget - getTotalExpense();
    }

    /** Total expense for the current calendar month (used against the monthly budget). */
    public double getCurrentMonthExpense() {
        YearMonth now = YearMonth.now();
        return transactions.stream()
                .filter(t -> t.getType() == TransactionType.EXPENSE)
                .filter(t -> YearMonth.from(t.getDate()).equals(now))
                .mapToDouble(Transaction::getAmount)
                .sum();
    }

    public double getRemainingMonthlyBudget() {
        return monthlyBudget - getCurrentMonthExpense();
    }

    /** Category -> total expense amount, sorted by highest spend first. */
    public Map<String, Double> getExpenseByCategory() {
        Map<String, Double> totals = new TreeMap<>();
        for (Transaction t : transactions) {
            if (t.getType() == TransactionType.EXPENSE) {
                totals.merge(t.getCategory(), t.getAmount(), Double::sum);
            }
        }
        // Sort by amount descending
        List<Map.Entry<String, Double>> entries = new ArrayList<>(totals.entrySet());
        entries.sort((a, b) -> Double.compare(b.getValue(), a.getValue()));
        Map<String, Double> sorted = new LinkedHashMap<>();
        for (Map.Entry<String, Double> e : entries) sorted.put(e.getKey(), e.getValue());
        return sorted;
    }

    public List<Transaction> getTransactionsByCategory(String category) {
        List<Transaction> result = new ArrayList<>();
        for (Transaction t : transactions) {
            if (t.getCategory().equalsIgnoreCase(category)) result.add(t);
        }
        return result;
    }

    // ---------- Persistence ----------

    private void saveTransactions() {
        try (PrintWriter pw = new PrintWriter(new FileWriter(TRANSACTIONS_FILE))) {
            for (Transaction t : transactions) {
                pw.println(t.toCsv());
            }
        } catch (IOException e) {
            System.out.println("Warning: could not save transactions (" + e.getMessage() + ")");
        }
    }

    private void loadTransactions() {
        File file = new File(TRANSACTIONS_FILE);
        if (!file.exists()) return;
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            int maxId = 0;
            while ((line = br.readLine()) != null) {
                if (line.isBlank()) continue;
                Transaction t = Transaction.fromCsv(line);
                transactions.add(t);
                maxId = Math.max(maxId, t.getId());
            }
            nextId = maxId + 1;
        } catch (IOException e) {
            System.out.println("Warning: could not load transactions (" + e.getMessage() + ")");
        }
    }

    private void saveBudget() {
        try (PrintWriter pw = new PrintWriter(new FileWriter(BUDGET_FILE))) {
            pw.println(monthlyBudget);
        } catch (IOException e) {
            System.out.println("Warning: could not save budget (" + e.getMessage() + ")");
        }
    }

    private void loadBudget() {
        File file = new File(BUDGET_FILE);
        if (!file.exists()) return;
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line = br.readLine();
            if (line != null && !line.isBlank()) {
                monthlyBudget = Double.parseDouble(line.trim());
            }
        } catch (IOException e) {
            System.out.println("Warning: could not load budget (" + e.getMessage() + ")");
        }
    }
}
