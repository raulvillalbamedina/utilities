package com.rvillalba.utilities;

import org.apache.commons.lang3.StringEscapeUtils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class ConvertPropertiesToJson {

    public static void convertJsonToProperties(String fileName) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            Map<String, Object> map = mapper.readValue(new File(fileName), new TypeReference<Map<String, Object>>() {
            });
            StringBuilder properties = new StringBuilder();
            addProperty(map, properties, null);
            createFile(properties.toString(),
                    "target/messages_" + fileName.split("locale-")[1].replace(".json", ".properties"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void createFile(String properties, String fileResult) throws UnsupportedEncodingException, FileNotFoundException, IOException {
        File tempFile = new File(fileResult);
        tempFile.getParentFile().mkdirs();
        Writer out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(tempFile), StandardCharsets.UTF_8));
        out.append(properties);
        out.flush();
        out.close();
    }

    private static String addProperty(Map<String, Object> map, StringBuilder properties, String keyConcat) {
        for (Entry<String, Object> entry : map.entrySet()) {
            if (entry.getValue() instanceof Map) {
                if (keyConcat == null) {
                    keyConcat = entry.getKey();
                } else {
                    keyConcat = keyConcat + "." + entry.getKey();
                }
                keyConcat = addProperty((Map) entry.getValue(), properties, keyConcat);
            } else {
                String key = null;
                if (keyConcat == null) {
                    key = entry.getKey();
                } else {
                    key = keyConcat + "." + entry.getKey();
                }
                String value = StringEscapeUtils.escapeJava((String) entry.getValue());
                value = value.replace("{{", "{");
                value = value.replace("}}", "}");
                System.out.println("Init property: " + keyConcat);
                System.out.println("Final key: " + key.trim() + " final value: " + value);
                properties.append(key.trim() + "=" + value + "\n");
            }
        }
        if (keyConcat != null) {
            List<String> keySplitted = Arrays.asList(keyConcat.split("\\."));
            if (keySplitted.size() > 1) {
                keyConcat = keyConcat.replace("." + keySplitted.get(keySplitted.size() - 1), "");
            } else {
                keyConcat = null;
            }
        }
        return keyConcat;
    }

    public static void convertPropertiesToJson(String fileName, String base) {
        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            String currentLine;
            Map<Object, Object> fileToMap = new HashMap<>();
            while ((currentLine = br.readLine()) != null) {
                if (!currentLine.startsWith("#")) {
                    currentLine = currentLine.replace(" = ", "=");
                    List<String> currentLineSplitted = Arrays.asList(currentLine.split("="));
                    String value = null;
                    if (currentLineSplitted.size() == 1) {
                        value = "";
                    } else {
                        value = currentLine.replace(currentLineSplitted.get(0) + "=", "");
                    }
                    value = value.replace("{", "{{");
                    value = value.replace("}", "}}");
                    addJson(currentLineSplitted.get(0), fileToMap, value);
                }
            }
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            String json = gson.toJson(fileToMap);
            createFile(json.toString().replace("\\\\", "\\").replace("\\:", ":").replace("\\!", "!").replace("\\#", "#"),
                    base + "target/locale-" + fileName.split("messages_")[1].replace(".properties", ".json"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void addJson(String key, Map<Object, Object> fileToMap, String value) {
        if (fileToMap.isEmpty()) {
            List<String> splitted = Arrays.asList(key.split("\\."));
            addNew(key, fileToMap, value, splitted);
        } else {
            List<String> splitted = Arrays.asList(key.split("\\."));
            if (fileToMap.containsKey(splitted.get(0))) {
                key = key.replace(splitted.get(0) + ".", "");
                if (fileToMap.get(splitted.get(0)) instanceof Map) {
                    addJson(key, (Map) fileToMap.get(splitted.get(0)), value);
                }
            } else {
                addNew(key, fileToMap, value, splitted);
            }
        }

    }

    private static void addNew(String key, Map<Object, Object> fileToMap, String value, List<String> splitted) {
        if (splitted.size() > 1) {
            fileToMap.put(splitted.get(0), new HashMap<>());
            key = key.replace(splitted.get(0) + ".", "");
            addJson(key, (Map) fileToMap.get(splitted.get(0)), value);
        } else {
            fileToMap.put(splitted.get(0).trim(), value);
        }
    }

}
