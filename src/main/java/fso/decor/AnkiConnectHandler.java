package fso.decor;

import org.apache.commons.text.StringEscapeUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;

public class AnkiConnectHandler {
    private static final HashMap<String, AnkiConnectHandler> map = new HashMap<>();
    private final String pdfHash;

    private String deckName = "";
    private String modelName = "";
    private final HttpClient client;

    private final String address = "http://127.0.0.1:8765";

    public static AnkiConnectHandler getInstance(String hash) {
        map.putIfAbsent(hash, new AnkiConnectHandler(hash));
        return map.get(hash);
    }

    public AnkiConnectHandler(String hash) {
        this.pdfHash = hash;
        client = HttpClient.newHttpClient();
    }

    public void addCard(String requestString) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI(address))
                    .headers("Content-Type", "text/plain;charset=UTF-8")
                    .POST(HttpRequest.BodyPublishers.ofString(requestString))
                    .build();
            client.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (URISyntaxException | IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public Set<String> getImageFileNames() {
        try {
            String requestString = String.format("{\n" +
                    "    \"action\": \"getMediaFilesNames\",\n" +
                    "    \"version\": 6,\n" +
                    "    \"params\": {\n" +
                    "        \"pattern\": \"%s\"\n" +
                    "    }\n" +
                    "}", pdfHash.substring(pdfHash.length() - 38) + "_*.jpg");

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI(address))
                    .headers("Content-Type", "text/plain;charset=UTF-8")
                    .POST(HttpRequest.BodyPublishers.ofString(requestString))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            JSONObject jo = new JSONObject(response.body());
            if (!jo.get("error").toString().equals("null")) {
                System.out.println("Something went wrong when getting files names");
                return new HashSet<>();
            }
            HashSet<String> ret = new HashSet<>();
            ret.addAll((Collection<? extends String>) jo.toMap().get("result"));
            return ret;
        } catch (URISyntaxException | InterruptedException | IOException e) {
            System.out.println("Something went wrong when getting file names");
            return new HashSet<>();
        }
    }

    private int getIdFromImageName(String filename) {
        int startIndex = filename.lastIndexOf("_") + 1;
        int endIndex = filename.lastIndexOf(".");
        String numberStr = filename.substring(startIndex, endIndex);
        int number = Integer.parseInt(numberStr);
        return number;
    }

    public void transferMedia(Set<Integer> idsToAdd) {
        try {
            Set<String> imagesInAnki = getImageFileNames();
            File baseFolder = GlobalConfig.getImageFolder();
            for (final File file : Objects.requireNonNull(baseFolder.listFiles())) {
                String shortName = file.getName().length() > 50 ?
                        file.getName().substring(file.getName().length() - 50) : file.getName();
                String absoluteName = file.getAbsolutePath();
                if (!imagesInAnki.contains(shortName) && !file.isDirectory() && absoluteName.contains(pdfHash) && absoluteName.contains(".jpg")) {
                    int id = getIdFromImageName(file.getName());
                    if (idsToAdd == null || !idsToAdd.contains(id))
                        continue;
                    String requestString = String.format("{\n" +
                            "    \"action\": \"storeMediaFile\",\n" +
                            "    \"version\": 6,\n" +
                            "    \"params\": {\n" +
                            "        \"filename\": \"%s\",\n" +
                            "        \"path\": \"%s\"\n" +
                            "    }\n" + "}", shortName, file.getAbsolutePath());
                    // taking only the last 50 as Anki can't handle the whole string

                    HttpRequest request = HttpRequest.newBuilder()
                            .uri(new URI(address))
                            .headers("Content-Type", "text/plain;charset=UTF-8")
                            .POST(HttpRequest.BodyPublishers.ofString(requestString))
                            .build();
                    client.send(request, HttpResponse.BodyHandlers.ofString());
                }
            }

        } catch (URISyntaxException | InterruptedException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void createDeckIfAbsent(String deckName) {
        this.deckName = deckName;
        try {
            String requestString = "{\n" +
                    "    \"action\": \"createDeck\",\n" +
                    "    \"version\": 6,\n" +
                    "    \"params\": {\n" +
                    "        \"deck\": \"" + deckName + "\"\n" +
                    "    }\n" +
                    "}";
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI(address))
                    .headers("Content-Type", "text/plain;charset=UTF-8")
                    .POST(HttpRequest.BodyPublishers.ofString(requestString))
                    .build();

            HttpResponse<String> response =
                    client.send(request, HttpResponse.BodyHandlers.ofString());
            JSONObject jo = new JSONObject(response.body());
            if (jo.get("error").toString().equals("null")) {
                System.out.println("Deck already existed or was successfully newly created.");
            }
        } catch (URISyntaxException | IOException | InterruptedException e) {
            System.out.printf("There was a problem creating the deck %s if it was absent.\n", deckName);
            throw new RuntimeException(e);
        }
    }
    public void createModelIfAbsent(String modelName) {
        this.modelName = modelName;
        try {
            String requestString = "{\n" +
                    "    \"action\": \"modelNames\",\n" +
                    "    \"version\": 6\n" +
                    "}";
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI(address))
                    .headers("Content-Type", "text/plain;charset=UTF-8")
                    .POST(HttpRequest.BodyPublishers.ofString(requestString))
                    .build();

            HttpResponse<String> response =
                    client.send(request, HttpResponse.BodyHandlers.ofString());
            JSONObject jo = new JSONObject(response.body());

            if (jo.get("error").toString().equals("null") && !jo.get("result").toString().equals("null")) {
                boolean isDeckPresent = Utils.JSONArrayContains((JSONArray) jo.get("result"), modelName);
                if (isDeckPresent) {
                    System.out.printf("%s Model is already present. Not creating it.\n", modelName);
                    return;
                }
            } else {
                System.out.println("There was an error fetching the names of existing Models.");
                return;
            }

            String cssContent = StringEscapeUtils.escapeJson(Utils.readAndEscape("./src/main/resources/styling.css"));
            String frontContent = StringEscapeUtils.escapeJson(Utils.readAndEscape("./src/main/resources/frontside.html"));
            String backContent = StringEscapeUtils.escapeJson(Utils.readAndEscape("./src/main/resources/backside.html"));
            requestString = String.format(Utils.readAndEscape("./src/main/resources/createModelRequest.txt"),
                    modelName, cssContent, modelName, frontContent, backContent);

            // requestString = StringEscapeUtils.escapeJson(requestString);

            request = HttpRequest.newBuilder()
                    .uri(new URI(address))
                    .headers("Content-Type", "text/plain;charset=UTF-8")
                    .POST(HttpRequest.BodyPublishers.ofString(requestString))
                    .build();

            response = client.send(request, HttpResponse.BodyHandlers.ofString());
            jo = new JSONObject(response.body());
            if (jo.get("error").toString().equals("null")) {
                System.out.println("Deck already existed or was successfully newly created.");
            }

        } catch (URISyntaxException | IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
