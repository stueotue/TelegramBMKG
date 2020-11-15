import org.json.JSONObject;
import org.telegram.abilitybots.api.bot.AbilityBot;
import org.telegram.abilitybots.api.objects.Ability;
import static org.telegram.abilitybots.api.objects.Privacy.PUBLIC;
import static org.telegram.abilitybots.api.objects.Locality.ALL;
import javax.net.ssl.HttpsURLConnection;
import org.json.XML;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;

public class BMKGBot extends AbilityBot {

    public static String BOT_USERNAME = "stueotue_bot";
    public static String BOT_TOKEN = "1436447899:AAHVp-Hyzjj8oulNpR9YU5Bjgo4OLdFXuV0";

    public BMKGBot() {
        super(BOT_TOKEN, BOT_USERNAME);
    }

    @Override
    public int creatorId() {
        return 662317901;
    }

    public Ability sayHello() {
        return Ability
                .builder()
                .name("hello")
                .info("says hello world!")
                .locality(ALL)
                .privacy(PUBLIC)
                .action(ctx -> silent.send("Hello "+ctx.user().getFirstName(), ctx.chatId()))
                .build();
    }

    public Ability pullData() {
        return Ability
                .builder()
                .name("pull")
                .info("Pull the most recent earthquake data from BMKG")
                .locality(ALL)
                .privacy(PUBLIC)
                .action(ctx -> {
                    String responseData = httpsRequest("https://data.bmkg.go.id/autogempa.xml");
                    StringBuilder sb = new StringBuilder();
                    HashMap<String, String> pullData = xmlToJson(responseData);

                    String msg = displayData(pullData);
                    silent.send(msg, ctx.chatId());

                    try {
                        sendImage("https://data.bmkg.go.id/eqmap.gif", ctx.chatId());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                })
                .build();
    }

    private HashMap xmlToJson(String str) {

        HashMap<String, String> data = new HashMap<>();
        JSONObject xmlJsonObj = XML.toJSONObject(str);
        data.put("tgl", (String) xmlJsonObj.query("/Infogempa/gempa/Tanggal"));
        data.put("jam", (String) xmlJsonObj.query("/Infogempa/gempa/Jam"));
        data.put("coord", (String) xmlJsonObj.query("/Infogempa/gempa/point/coordinates"));
        data.put("mag", (String) xmlJsonObj.query("/Infogempa/gempa/Magnitude"));
        data.put("kedalaman", (String) xmlJsonObj.query("/Infogempa/gempa/Kedalaman"));
        data.put("wil1", (String) xmlJsonObj.query("/Infogempa/gempa/Wilayah1"));
        data.put("pot", (String) xmlJsonObj.query("/Infogempa/gempa/Potensi"));

        return data;
    }

    private String httpsRequest(String url) {
        URL bmkgUrl;
        StringBuilder content = new StringBuilder();
        try {
            bmkgUrl = new URL(url);
            HttpsURLConnection con = (HttpsURLConnection) bmkgUrl.openConnection();

            BufferedReader buffer = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String input;
            while((input = buffer.readLine()) != null)
                content.append(input);
            buffer.close();

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return content.toString();
    }
    public void sendImage(String url, long chatId) throws IOException {
        InputStream is = new URL(url).openStream();
        SendPhoto sendPhotoRequest = new SendPhoto();
        sendPhotoRequest.setChatId(Long.toString(chatId));
        sendPhotoRequest.setPhoto(new InputFile(is,"Test"));

        try {
            execute(sendPhotoRequest);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
    public String displayData(HashMap data) {
        return String.format("Tanggal : %s \n"+
                "Jam : %s \n" +
                "Magnitude : %s \n" +
                "Kedalaman : %s \n" +
                "Wilayah 1 : %s \n" +
                "Potensi   : %s \n", data.get("tgl"), data.get("jam"), data.get("mag"),
                                     data.get("kedalaman"), data.get("wil1"), data.get("pot"));
    }
}


