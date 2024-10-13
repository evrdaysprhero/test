package pojo;

public class OrderResponse {
    private String name;
    private Order order;
    private boolean success;
    private String message;

    public String getName() {
        return name;
    }

    public Order getOrder() {
        return order;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }
}
