package eu.zhincore.chestnetworks;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.command.CommandSender;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class Messenger {
  private static final String prefix = "§b[§2ChestNetworks§b]§r ";
  private JSONObject messages;
  private JSONParser parser = new JSONParser();

  public Messenger(Reader messagesRaw) throws IOException, ParseException {
    messages = (JSONObject) parser.parse(messagesRaw);
  }

  String[] getMessage(String key, String... params) {
    JSONArray jsonmessages = ((JSONArray) this.messages.get(key));
    List<String> messages = new ArrayList<>();
    // fill place-holders
    for (Object jsonmessage : jsonmessages) {
      String message = (String) jsonmessage;
      for (String param : params) {
        message = message.replaceFirst("%s", param);
      }
      messages.add(message);
    }
    return messages.toArray(String[]::new);
  }

  void send(String key, CommandSender target, String... params) {
    var message = getMessage(key, params);

    target.sendMessage(prefix + String.join("\n", message));
  }
}
