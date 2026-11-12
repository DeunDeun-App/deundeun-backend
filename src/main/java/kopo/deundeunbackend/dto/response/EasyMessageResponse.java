package kopo.deundeunbackend.dto.response;

import java.util.List;

public record EasyMessageResponse(String id, String disasterCode, String disasterName, List<Item> items,
                                  List<String> actions, String ttsText, boolean aiGenerated) {

    public record Item(String pictogram, String title, String description) {

    }
}
