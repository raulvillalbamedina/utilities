package com.rvillalba.utilities;

import org.testng.annotations.Test;

@Test
public class ConvertPropertiesToJsonTest {

    @Test
    public void convertPropertiesToJson() {
        ConvertPropertiesToJson.convertPropertiesToJson("" + "src/main/resources/messages_en_US.properties", "");

    }

    @Test
    public void convertJsonToProperties() {
        ConvertPropertiesToJson.convertJsonToProperties("src/main/resources/locale-en_US.json");
    }

}
