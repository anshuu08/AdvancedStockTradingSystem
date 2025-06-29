// Advanced Stock Trading Simulation System (Improved)
// Authored by: Anshuman Sinha

import java.util.*;
import java.util.concurrent.*;

class Stock {
    String stockName;
    double price;
    List<Double> priceHistory = new ArrayList<>();

    public Stock(String stockName, double price) {
        this.stockName = stockName;
        this.price = price;
        priceHistory.add(price);
    }

    public synchronized void updatePrice(double newPrice) {
        this.price = Math.max(1, newPrice);
        priceHistory.add(this.price);
    }

    public double calculateSMA(int period) {
        if (priceHistory.size() < period)
            return -1;
        double sum = 0;
        for (int i = priceHistory.size() - period; i < priceHistory.size(); i++) {
            sum += priceHistory.get(i);
        }
        return sum / period;
    }

    public double calculateEMA(int period) {
        if (priceHistory.size() < period)
            return -1;
        double k = 2.0 / (period + 1);
        double ema = priceHistory.get(priceHistory.size() - period);
        for (int i = priceHistory.size() - period + 1; i < priceHistory.size(); i++) {
            ema = priceHistory.get(i) * k + ema * (1 - k);
        }
        return ema;
    }
}

class User {
    String username;
    String password;
    double balance;
    double marginBalance;
    double debt;
    Map<String, Integer> portfolio = new HashMap<>();
    List<String> transactionHistory = new ArrayList<>();

    public User(String username, String password, double balance) {
        this.username = username;
        this.password = password;
        this.balance = balance;
        this.marginBalance = balance * 2;
        this.debt = 0;
    }

    public void addToPortfolio(String stockSymbol, int quantity) {
        portfolio.put(stockSymbol, portfolio.getOrDefault(stockSymbol, 0) + quantity);
    }

    public void removeFromPortfolio(String stockSymbol, int quantity) {
        int currentQuantity = portfolio.getOrDefault(stockSymbol, 0);
        if (currentQuantity > quantity) {
            portfolio.put(stockSymbol, currentQuantity - quantity);
        } else {
            portfolio.remove(stockSymbol);
        }
    }
}

class Order {
    String stockSymbol;
    double price;
    int quantity;
    String orderType; // buy/sell/short/cover

    public Order(String stockSymbol, double price, int quantity, String orderType) {
        this.stockSymbol = stockSymbol;
        this.price = price;
        this.quantity = quantity;
        this.orderType = orderType;
    }
}

public class AdvancedStockTradingSystem {
    static Scanner scanner = new Scanner(System.in);
    static Map<String, Stock> market = new ConcurrentHashMap<>();
    static Map<String, User> users = new ConcurrentHashMap<>();
    static PriorityQueue<Order> buyOrders = new PriorityQueue<>((a, b) -> Double.compare(b.price, a.price));
    static PriorityQueue<Order> sellOrders = new PriorityQueue<>(Comparator.comparingDouble(a -> a.price));
    static List<String> newsFeed = new ArrayList<>();
    static Timer marketTimer = new Timer();

    public static void main(String[] args) {
        initializeMarket();
        startMarketSimulation();

        while (true) {
            System.out.println("1. Register\n2. Login\n3. Exit");
            int choice = scanner.nextInt();
            if (choice == 1)
                registerUser();
            else if (choice == 2) {
                User user = loginUser();
                if (user != null)
                    displayUserMenu(user);
            } else
                break;
        }
    }

    static void initializeMarket() {
        market.put("AAPL", new Stock("AAPL", 150));
        market.put("TSLA", new Stock("TSLA", 800));
        market.put("GOOG", new Stock("GOOG", 2800));
        market.put("MSFT", new Stock("MSFT", 300));

        newsFeed.add("Apple announces new iPhone.");
        newsFeed.add("Tesla achieves record sales.");
        newsFeed.add("Google acquires AI startup.");
        newsFeed.add("Microsoft launches new Surface product.");
    }

    static void registerUser() {
        System.out.print("Username: ");
        String username = scanner.next();
        System.out.print("Password: ");
        String password = scanner.next();
        users.put(username, new User(username, password, 10000));
        System.out.println("Registered successfully!");
    }

    static User loginUser() {
        System.out.print("Username: ");
        String username = scanner.next();
        System.out.print("Password: ");
        String password = scanner.next();

        if (users.containsKey(username) && users.get(username).password.equals(password)) {
            System.out.println("Login successful!");
            return users.get(username);
        }
        System.out.println("Invalid credentials.");
        return null;
    }

    static void displayUserMenu(User user) {
        while (true) {
            System.out.println(
                    "\n1. Portfolio\n2. Market\n3. Buy\n4. Sell\n5. Add Funds\n6. Margin Trade\n7. Short Sell\n8. Limit Order\n9. News\n10. Analytics\n11. History\n12. Indicators\n13. Logout");
            int choice = scanner.nextInt();
            switch (choice) {
                case 1 -> viewPortfolio(user);
                case 2 -> viewMarket();
                case 3 -> buyStock(user);
                case 4 -> sellStock(user);
                case 5 -> addFunds(user);
                case 6 -> marginTrading(user);
                case 7 -> shortSelling(user);
                case 8 -> placeLimitOrder(user);
                case 9 -> newsFeed.forEach(System.out::println);
                case 10 -> displayAnalytics();
                case 11 -> user.transactionHistory.forEach(System.out::println);
                case 12 -> viewTechnicalIndicators(user);
                case 13 -> {
                    return;
                }
            }
        }
    }

    static void marginTrading(User user) {
        System.out.print("Stock Symbol: ");
        String sym = scanner.next();
        System.out.print("Quantity: ");
        int qty = scanner.nextInt();
        Stock stock = market.get(sym);

        if (stock != null && user.marginBalance >= stock.price * qty) {
            double total = stock.price * qty;
            user.marginBalance -= total;
            user.debt += total;
            user.addToPortfolio(sym, qty);
            user.transactionHistory.add("Margin buy: " + qty + " of " + sym + " at $" + stock.price);
            System.out.println("Margin trade executed.");
        } else
            System.out.println("Error or insufficient margin.");
    }

    static void shortSelling(User user) {
        System.out.print("Stock Symbol: ");
        String sym = scanner.next();
        System.out.print("Quantity: ");
        int qty = scanner.nextInt();
        Stock stock = market.get(sym);

        if (stock != null) {
            double total = stock.price * qty;
            user.balance += total;
            user.debt += total;
            user.transactionHistory.add("Short sold: " + qty + " of " + sym + " at $" + stock.price);
            System.out.println("Short sell executed.");
        } else
            System.out.println("Stock not found.");
    }

    static void placeLimitOrder(User user) {
        System.out.print("Stock Symbol: ");
        String sym = scanner.next();
        System.out.print("Quantity: ");
        int qty = scanner.nextInt();
        System.out.print("Limit Price: ");
        double price = scanner.nextDouble();
        System.out.print("Order Type (buy/sell): ");
        String type = scanner.next();

        Order order = new Order(sym, price, qty, type);
        if (type.equals("buy"))
            buyOrders.add(order);
        else if (type.equals("sell"))
            sellOrders.add(order);
        System.out.println("Limit order placed.");
    }

    static void viewTechnicalIndicators(User user) {
        System.out.print("Stock Symbol: ");
        String sym = scanner.next();
        Stock stock = market.get(sym);
        if (stock != null) {
            System.out.print("SMA Period: ");
            int sp = scanner.nextInt();
            System.out.println("SMA: $" + stock.calculateSMA(sp));
            System.out.print("EMA Period: ");
            int ep = scanner.nextInt();
            System.out.println("EMA: $" + stock.calculateEMA(ep));
        } else
            System.out.println("Stock not found.");
    }

    static void viewPortfolio(User user) {
        System.out.println("\nPortfolio:");
        double total = 0;
        for (var e : user.portfolio.entrySet()) {
            Stock stock = market.get(e.getKey());
            double value = stock.price * e.getValue();
            total += value;
            System.out.println(e.getKey() + ": " + e.getValue() + " shares @ $" + stock.price);
        }
        System.out.println("Total: $" + total);
        System.out.println("Balance: $" + user.balance + ", Debt: $" + user.debt + ", Margin: $" + user.marginBalance);
    }

    static void viewMarket() {
        market.forEach((k, v) -> System.out.println(k + ": $" + v.price));
    }

    static void buyStock(User user) {
        System.out.print("Stock Symbol: ");
        String sym = scanner.next();
        System.out.print("Quantity: ");
        int qty = scanner.nextInt();
        Stock stock = market.get(sym);
        if (stock != null && user.balance >= stock.price * qty) {
            double total = stock.price * qty;
            user.balance -= total;
            user.addToPortfolio(sym, qty);
            user.transactionHistory.add("Buy: " + qty + " of " + sym + " at $" + stock.price);
            System.out.println("Buy executed.");
        } else
            System.out.println("Error or insufficient balance.");
    }

    static void sellStock(User user) {
        System.out.print("Stock Symbol: ");
        String sym = scanner.next();
        System.out.print("Quantity: ");
        int qty = scanner.nextInt();
        if (user.portfolio.getOrDefault(sym, 0) >= qty) {
            Stock stock = market.get(sym);
            double total = stock.price * qty;
            user.balance += total;
            user.removeFromPortfolio(sym, qty);
            user.transactionHistory.add("Sell: " + qty + " of " + sym + " at $" + stock.price);
            System.out.println("Sell executed.");
        } else
            System.out.println("Insufficient shares.");
    }

    static void addFunds(User user) {
        System.out.print("Amount: ");
        double amt = scanner.nextDouble();
        user.balance += amt;
        user.marginBalance += amt * 2;
        System.out.println("Funds added.");
    }

    static void displayAnalytics() {
        Stock top = null, worst = null;
        for (Stock s : market.values()) {
            if (top == null || s.price > top.price)
                top = s;
            if (worst == null || s.price < worst.price)
                worst = s;
        }
        System.out.println("Top: " + top.stockName + " ($" + top.price + ")");
        System.out.println("Worst: " + worst.stockName + " ($" + worst.price + ")");
    }

    static void matchOrders() {
        while (!buyOrders.isEmpty() && !sellOrders.isEmpty()) {
            Order buy = buyOrders.peek(), sell = sellOrders.peek();
            if (buy.price >= sell.price) {
                int qty = Math.min(buy.quantity, sell.quantity);
                System.out.println("Matched: " + qty + " shares of " + buy.stockSymbol + " at $" + sell.price);

                if (buy.quantity > qty) {
                    buy.quantity -= qty;
                    buyOrders.poll();
                    buyOrders.add(buy);
                } else
                    buyOrders.poll();

                if (sell.quantity > qty) {
                    sell.quantity -= qty;
                    sellOrders.poll();
                    sellOrders.add(sell);
                } else
                    sellOrders.poll();
            } else
                break;
        }
    }

    static void startMarketSimulation() {
        marketTimer.scheduleAtFixedRate(new TimerTask() {
            public void run() {
                Random rand = new Random();
                for (Stock stock : market.values()) {
                    double change = rand.nextDouble() * 10 - 5;
                    stock.updatePrice(stock.price + change);
                }
                matchOrders();
            }
        }, 0, 5000);
    }
}
