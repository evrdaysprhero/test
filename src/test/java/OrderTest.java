import io.qameta.allure.Step;
import io.qameta.allure.Story;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import pojo.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static io.restassured.RestAssured.given;

@Story("Создание заказа")
public class OrderTest {
    private String password;
    private String email;

    @Before
    public void setUp() {
        RestAssured.baseURI = "https://stellarburgers.nomoreparties.site/";

        String name = "sprhero" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        password = RandomStringUtils.randomNumeric(5);
        email = name + "@mailme.ru";

        //создать пользователя
        RegisterRequest registerRequest = new RegisterRequest(name, password, email);
        RegisterTest.postRegister(registerRequest);

    }

    @Step("Авторизоваться")
    public String authUser(String password, String email) {
        LoginRequest loginRequest = new LoginRequest(password, email);

        RegisterResponse registerResponse =  LoginTest
                .postLogin(loginRequest)
                .body()
                .as(RegisterResponse.class);

        return registerResponse.getAccessToken();
    }

    @Step("Вызов /api/orders с авторизацией")
    public static Response postOrders(OrderRequest order, String accessToken) {
        return given()
                .header("Content-type", "application/json")
                .header("authorization", accessToken)
                .body(order)
                .post("/api/orders");
    }

    @Step("Вызов /api/orders без авторизации")
    public static Response postOrders(OrderRequest order) {
        return given()
                .header("Content-type", "application/json")
                .body(order)
                .post("/api/orders");
    }

    @Step("Проверка кода ответа")
    public static void checkResponseCode(Response response, Integer expCode) {
        response.then().assertThat()
                .statusCode(expCode);
    }

    @Test
    @DisplayName("Заказ с авторизацией")
    public void makeOrderWithAuthSuccess() {

        //авторизация
        String accessToken = authUser(password, email);

        //создание заказа
        OrderRequest order = new OrderRequest(List.of("61c0c5a71d1f82001bdaaa73", "61c0c5a71d1f82001bdaaa6e", "61c0c5a71d1f82001bdaaa6c"));
        Response response = postOrders(order, accessToken);
        checkResponseCode(response, 200);

        //проверка
        OrderResponse orderResponse = response
                .body()
                .as(OrderResponse.class);
        Assert.assertTrue("Заказ не создан", orderResponse.isSuccess());

    }

    @Test
    @DisplayName("Заказ без авторизации")
    public void makeOrderNoAuthSuccess() {

        //создание заказа
        OrderRequest order = new OrderRequest(List.of("61c0c5a71d1f82001bdaaa73", "61c0c5a71d1f82001bdaaa6e", "61c0c5a71d1f82001bdaaa6c"));
        Response response = postOrders(order);
        checkResponseCode(response, 200);

        //проверка
        OrderResponse orderResponse = response
                .body()
                .as(OrderResponse.class);
        Assert.assertTrue("Заказ не создан", orderResponse.isSuccess());

    }

    @Test
    @DisplayName("Заказ без ингридиентов")
    public void makeOrderNoIngredintsFail() {

        //создание заказа
        OrderRequest order = new OrderRequest(null);
        Response response = postOrders(order);
        checkResponseCode(response, 400);

        //проверка
        OrderResponse orderResponse = response
                .body()
                .as(OrderResponse.class);
        Assert.assertEquals("Ingredient ids must be provided", orderResponse.getMessage());

    }

    @Test
    @DisplayName("Заказ с неверным хешем ингредиентов")
    public void makeOrderWrongIngredintsFail() {

        //создание заказа
        OrderRequest order = new OrderRequest(List.of("61c0c5a71d1f82001bdaaa73", "61c0c5a71d1f82001bdaaa6e", "0000"));
        Response response = postOrders(order);
        checkResponseCode(response, 500);

    }
}
