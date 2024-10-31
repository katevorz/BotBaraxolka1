package org.example;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;



public class BotBaraxolka extends TelegramLongPollingBot {

    private final List<Product> listings = new ArrayList<>();
    private final Map<Long, String> addingState = new HashMap<>(); // Хранит название товара
    private final Map<Long, String> priceState = new HashMap<>(); // Хранит цену товара
    private final Map<Long, String> descriptionState = new HashMap<>(); // Хранит описание товара
    private final Map<Long, String> phoneState = new HashMap<>(); // Хранит номер телефона
    private final Map<Long, String> photoState = new HashMap<>(); // Хранит ID фото
    private final List<Category> categories = new ArrayList<>();

    private final Map<Long, Category> categoryState = new HashMap<>();
    @Override
    public String getBotUsername() {
        return "BaraxolkaBotic"; // Замените на имя вашего бота
    }

    @Override
    public String getBotToken() {
        return "7591311099:AAHUxm40Jo-IBXovfw89bkIPlLPG9PBcuXY"; // Замените на токен вашего бота
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage()) {
            long chatId = update.getMessage().getChatId();

            if (update.getMessage().hasText()) {
                String messageText = update.getMessage().getText();

                // Проверяем состояние добавления товара
                if (addingState.containsKey(chatId)) {
                    if(categoryState.get(chatId) == null){
                        Category chosenCategory = findCategoryByName(messageText);
                        if(chosenCategory != null){
                            categoryState.put(chatId, chosenCategory);
                            sendResponse(chatId, "Вы выбрали: " + chosenCategory.getName() + ".Теперь введите название продукта: ");
                        }
                        //else {sendListings(chatId, "Неверная категория. Выберите правильную категорию: ");}
                    }else if
                    // Ожидаем название товара
                    (addingState.get(chatId).isEmpty()) {
                        addingState.put(chatId, messageText); // Сохраняем название товара
                        sendResponse(chatId, "Введите цену товара:");
                        priceState.put(chatId, null); // Инициализируем состояние цены
                    } else if (priceState.get(chatId) == null) {
                        // Ожидаем цену товара
                        try {
                            String price = /*String.parseDouble(*/messageText/*)*/;
                            priceState.put(chatId, price);
                            sendResponse(chatId, "Введите описание товара:");
                            descriptionState.put(chatId, ""); // Инициализируем состояние описания
                        } catch (NumberFormatException e) {
                            sendResponse(chatId, "Пожалуйста, введите корректную цену:");
                        }
                    } else if (descriptionState.get(chatId).isEmpty()) {
                        // Ожидаем описание товара
                        descriptionState.put(chatId, messageText);
                        sendResponse(chatId, "Введите номер телефона:");
                        phoneState.put(chatId, ""); // Инициализируем состояние номера телефона
                    } else if (phoneState.get(chatId).isEmpty()) {
                        // Ожидаем номер телефона
                        phoneState.put(chatId, messageText);
                        sendResponse(chatId, "Теперь отправьте фото товара:");
                    }
                } else {
                    // Обрабатываем команды
                    switch (messageText) {
                        case "/start":
                            sendWelcomeMessage(chatId);
                            break;
                        case "Добавить товар":
                            sendCategorySelection(chatId);
                            categoryState.put(chatId, null);
                            addingState.put(chatId, ""); // Устанавливаем состояние добавления
                           // sendResponse(chatId, "Введите название товара для добавления:");
                            break;
                        case "Показать все товары":
                            sendListings(chatId);
                            break;
                        default:
                            sendResponse(chatId, "Используйте кнопку 'Добавить товар' для добавления товара или 'Показать все товары' для просмотра.");
                            break;
                    }
                }
            } else if (update.getMessage().hasPhoto()) {
                // Если получено фото
                String productName = addingState.get(chatId);
                if (productName != null && !productName.isEmpty()) {
                    String photoId = update.getMessage().getPhoto().get(0).getFileId(); // Получаем ID фото
                    //System.out.println("Photo ID: " + photoId); // Логируем
                    // Добавляем товар с фото в список
                    listings.add(new Product(productName, priceState.get(chatId), descriptionState.get(chatId), phoneState.get(chatId), photoId));




                    // Сбрасываем состояния
                    addingState.remove(chatId);
                    priceState.remove(chatId);
                    descriptionState.remove(chatId);
                    phoneState.remove(chatId);
                    priceState.remove(chatId);


                    sendResponse(chatId, "Объявление добавлено: " + productName);
                    sendWelcomeMessage(chatId);
                }
            }
        }
    }

    private void sendWelcomeMessage(long chatId) {
        String welcomeText = "Добро пожаловать в Marketplace Bot! Используйте кнопки ниже для взаимодействия.";
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(welcomeText);

        // Создаем клавиатуру
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        KeyboardRow row = new KeyboardRow();
        row.add("Добавить товар");
        row.add("Показать все товары");
        List<KeyboardRow> keyboard = new ArrayList<>();
        keyboard.add(row);
        keyboardMarkup.setKeyboard(keyboard);

        message.setReplyMarkup(keyboardMarkup);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void sendCategorySelection(long chatId){
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText("Пожалуйста, выберите категорию: ");

        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        List<KeyboardRow> KeyboardRows = new ArrayList<>();

        for (Category category : categories){
            KeyboardRow row = new KeyboardRow();
            row.add(category.getName());
            KeyboardRows.add(row);
        }
        keyboardMarkup.setKeyboard(KeyboardRows);
        message.setReplyMarkup(keyboardMarkup);

        try{
            execute(message);
        } catch(TelegramApiException e){
            e.printStackTrace();
        }
    }

    private Category findCategoryByName(String name){
        for(Category category : categories){
            if (category.getName().equalsIgnoreCase(name)){
                return category;
            }
        }
        return null;
    }

    private void sendListings(long chatId) {
        if (listings.isEmpty()) {
            sendResponse(chatId, "Список объявлений пуст.");
            return;
        }

        for (Product product : listings) {
            sendPhoto(chatId, product);
        }
    }

    private void sendPhoto(long chatId, Product product) {
        SendPhoto photoMessage = new SendPhoto();
        photoMessage.setChatId(String.valueOf(chatId));

        // Используем InputFile для передачи ID фото
        photoMessage.setPhoto(new InputFile(product.getPhotoId()));

        String caption = "Товар: " + product.getName() + "\n" +
                "Описание: " + product.getDescription() + "\n" +
                "Цена: " + product.getPrice() + "\n" +
                "Телефон продавца: "+ product.getPhoneNumber();

        photoMessage.setCaption(caption);


        try {
            execute(photoMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void sendResponse(long chatId, String text) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(text);
        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
    public BotBaraxolka(){
        categories.add(new Category("Электроника"));
        categories.add(new Category("Одежда"));
        categories.add(new Category("Книги"));
    }

    public static class Product {
        private final String name;
        private final String photoId;
        private final String description;
        private final String price;
        private final String phoneNumber;
        private Category category;

        public Product(String name,String price, String description, String phoneNumber, String photoId) {
            //productName, priceState.get(chatId), descriptionState.get(chatId), phoneState.get(chatId), photoId
            this.name = name;
            this.photoId = photoId;
            this.description = description;
            this.price = price;
            this.phoneNumber = phoneNumber;
        }

        public String getName() {
            return name;
        }

        public String getPhotoId() {
            return photoId;
        }
        public String getDescription() {
            return description;
        }
        public String getPrice() {
            return price;
        }
        public String getPhoneNumber() {
            return phoneNumber;
        }
        public Category getCategory(){
            return category;
        }
    }

}

