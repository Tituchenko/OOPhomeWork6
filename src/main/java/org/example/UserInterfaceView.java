package org.example;


import java.io.IOException;
import java.util.Scanner;

public class UserInterfaceView {
    private Controller controller = new Controller();

    public void runInterface() {
        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.println("Введите 1 для получения погоды Accuweather;" +
                    "Введите 2 для получения погоды Yandex");
            String model = scanner.nextLine();


            System.out.println("Введите имя города: ");
            String city = scanner.nextLine();

            System.out.println("Введите 1 для получения погоды на сегодня;" +
                    "Введите 5 для прогноза на 5 дней; Для выхода введите 0:");

            String command = scanner.nextLine();

            //TODO* Сделать метод валидации пользовательского ввода

            if (command.equals("0")) break;

            try {
                controller.getWeather(model,command, city);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
