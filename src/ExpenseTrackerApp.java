import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

/**
 * Console entry point for the Personal Expense and Budget Management System.
 * Presents a menu-driven interface backed by BudgetManager.
 */
public class ExpenseTrackerApp {
    private final BudgetManager manager = new BudgetManager();
    private final Scanner sc = new Scanner(System.in);

    public static void main(String[] args) {
        new ExpenseTrackerApp().run();
    }

    private void run() {
        System.out.println("=========================================");
        System.out.println(" Personal Expense and Budget Manager");
        System.out.println("=========================================");

        boolean running = true;
        while (running) {
            printMenu();
            String choice = sc.nextLine().trim();
            switch (choice) {
                case "1" -> addTransaction();
                case "2" -> editTransaction();
                case "3" -> deleteTransaction();
                case "4" -> listTransactions();
                case "5" -> setBudget();
                case "6" -> showSummary();
                case "7" -> showCategoryReport();
                case "8" -> searchByCategory();
                case "0" -> {
                    running = false;
                    System.out.println("Goodbye! Your data has been saved.");
                }
                default -> System.out.println("Invalid option, please try again.");
            }
        }
        sc.close();
    }

    private void printMenu() {
        System.out.println();
        System.out.println("---------------- MENU -------------------");
        System.out.println("1. Add Transaction");
        System.out.println("2. Edit Transaction");
        System.out.println("3. Delete Transaction");
        System.out.println("4. View All Transactions");
        System.out.println("5. Set Monthly Budget");
        System.out.println("6. View Summary (Income / Expense / Balance)");
        System.out.println("7. View Category-wise Expense Report");
        System.out.println("8. Search Transactions by Category");
        System.out.println("0. Exit");
        System.out.print("Enter choice: ");
    }

    // ---------- Menu actions ----------

    private void addTransaction() {
        TransactionType type = readType();
        System.out.print("Category (e.g. Food, Rent, Salary): ");
        String category = sc.nextLine().trim();
        double amount = readAmount("Amount: ");
        LocalDate date = readDate("Date (yyyy-mm-dd, blank = today): ");
        System.out.print("Description (optional): ");
        String description = sc.nextLine().trim();

        Transaction t = manager.addTransaction(type, category, amount, date, description);
        System.out.println("Added: " + t);
        warnIfOverBudget();
    }

    private void editTransaction() {
        listTransactions();
        int id = readInt("Enter ID of transaction to edit: ");
        Transaction existing = manager.findById(id);
        if (existing == null) {
            System.out.println("No transaction found with ID " + id);
            return;
        }
        System.out.println("Leave a field blank to keep its current value.");

        System.out.print("New type (INCOME/EXPENSE) [" + existing.getType() + "]: ");
        String typeInput = sc.nextLine().trim();
        TransactionType type = typeInput.isBlank() ? null : TransactionType.valueOf(typeInput.toUpperCase());

        System.out.print("New category [" + existing.getCategory() + "]: ");
        String category = sc.nextLine().trim();
        category = category.isBlank() ? null : category;

        System.out.print("New amount [" + existing.getAmount() + "]: ");
        String amountInput = sc.nextLine().trim();
        Double amount = amountInput.isBlank() ? null : Double.parseDouble(amountInput);

        System.out.print("New date (yyyy-mm-dd) [" + existing.getDate() + "]: ");
        String dateInput = sc.nextLine().trim();
        LocalDate date = dateInput.isBlank() ? null : LocalDate.parse(dateInput);

        System.out.print("New description [" + existing.getDescription() + "]: ");
        String descInput = sc.nextLine().trim();
        String description = descInput.isBlank() ? null : descInput;

        boolean ok = manager.editTransaction(id, type, category, amount, date, description);
        System.out.println(ok ? "Transaction updated." : "Update failed.");
        warnIfOverBudget();
    }

    private void deleteTransaction() {
        listTransactions();
        int id = readInt("Enter ID of transaction to delete: ");
        boolean ok = manager.deleteTransaction(id);
        System.out.println(ok ? "Transaction deleted." : "No transaction found with ID " + id);
    }

    private void listTransactions() {
        List<Transaction> all = manager.getAllTransactions();
        System.out.println();
        if (all.isEmpty()) {
            System.out.println("No transactions recorded yet.");
            return;
        }
        System.out.println("ID   Type     Category        Amount        Date         Description");
        System.out.println("---------------------------------------------------------------------");
        for (Transaction t : all) {
            System.out.println(t);
        }
    }

    private void setBudget() {
        double budget = readAmount("Enter monthly budget amount: ");
        manager.setMonthlyBudget(budget);
        System.out.println("Monthly budget set to Rs." + budget);
    }

    private void showSummary() {
        double income = manager.getTotalIncome();
        double expense = manager.getTotalExpense();
        double balance = income - expense;

        System.out.println();
        System.out.println("============ SUMMARY ============");
        System.out.printf("Total Income      : Rs.%.2f%n", income);
        System.out.printf("Total Expense     : Rs.%.2f%n", expense);
        System.out.printf("Net Balance       : Rs.%.2f%n", balance);
        System.out.printf("Monthly Budget    : Rs.%.2f%n", manager.getMonthlyBudget());
        System.out.printf("Spent This Month  : Rs.%.2f%n", manager.getCurrentMonthExpense());
        double remaining = manager.getRemainingMonthlyBudget();
        if (remaining < 0) {
            System.out.printf("Budget Status     : OVER BUDGET by Rs.%.2f%n", -remaining);
        } else {
            System.out.printf("Budget Status     : Rs.%.2f remaining%n", remaining);
        }
    }

    private void showCategoryReport() {
        Map<String, Double> byCategory = manager.getExpenseByCategory();
        System.out.println();
        System.out.println("======= EXPENSE BY CATEGORY =======");
        if (byCategory.isEmpty()) {
            System.out.println("No expenses recorded yet.");
            return;
        }
        double total = manager.getTotalExpense();
        for (Map.Entry<String, Double> e : byCategory.entrySet()) {
            double pct = total == 0 ? 0 : (e.getValue() / total) * 100;
            System.out.printf("%-15s Rs.%-10.2f (%.1f%%)%n", e.getKey(), e.getValue(), pct);
        }
    }

    private void searchByCategory() {
        System.out.print("Enter category to search: ");
        String category = sc.nextLine().trim();
        List<Transaction> results = manager.getTransactionsByCategory(category);
        if (results.isEmpty()) {
            System.out.println("No transactions found in category: " + category);
            return;
        }
        System.out.println("Transactions in category \"" + category + "\":");
        for (Transaction t : results) {
            System.out.println(t);
        }
    }

    private void warnIfOverBudget() {
        if (manager.getMonthlyBudget() > 0 && manager.getRemainingMonthlyBudget() < 0) {
            System.out.printf("WARNING: You have exceeded your monthly budget by Rs.%.2f%n",
                    -manager.getRemainingMonthlyBudget());
        }
    }

    // ---------- Input helpers ----------

    private TransactionType readType() {
        while (true) {
            System.out.print("Type (1=Income, 2=Expense): ");
            String input = sc.nextLine().trim();
            if (input.equals("1")) return TransactionType.INCOME;
            if (input.equals("2")) return TransactionType.EXPENSE;
            System.out.println("Please enter 1 or 2.");
        }
    }

    private double readAmount(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = sc.nextLine().trim();
            try {
                double val = Double.parseDouble(input);
                if (val < 0) {
                    System.out.println("Amount cannot be negative.");
                    continue;
                }
                return val;
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid number.");
            }
        }
    }

    private int readInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = sc.nextLine().trim();
            try {
                return Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid integer.");
            }
        }
    }

    private LocalDate readDate(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = sc.nextLine().trim();
            if (input.isBlank()) return LocalDate.now();
            try {
                return LocalDate.parse(input);
            } catch (DateTimeParseException e) {
                System.out.println("Please use format yyyy-mm-dd.");
            }
        }
    }
}
