package eu.zhincore.chestnetworks.util;

import java.lang.reflect.Type;
import java.util.HashMap;
import com.google.common.collect.HashBasedTable;
import com.google.gson.*;

public class HashBasedTableSerializer implements JsonSerializer<HashBasedTable<Object, Object, Object>>,
    JsonDeserializer<HashBasedTable<Object, Object, Object>> {
  @Override
  public JsonElement serialize(HashBasedTable<Object, Object, Object> table, Type typeOfSrc,
      JsonSerializationContext context) {
    return context.serialize(table.rowMap());
  }

  @Override
  public HashBasedTable<Object, Object, Object> deserialize(JsonElement json, Type typeOfT,
      JsonDeserializationContext context) {
    HashMap<Object, HashMap<Object, Object>> map = context.deserialize(json, HashMap.class);
    var table = HashBasedTable.create();

    for (var row : map.entrySet()) {
      table.row(row.getKey()).putAll(row.getValue());
    }

    return table;
  }
}
