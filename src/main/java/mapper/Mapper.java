package mapper;

import ru.hse.homework4.*;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class Mapper implements ru.hse.homework4.Mapper {
    @Override
    public <T> T readFromString(Class<T> clazz, String input) {
        return null;
    }

    @Override
    public <T> T read(Class<T> clazz, InputStream inputStream) throws IOException {
        return null;
    }

    @Override
    public <T> T read(Class<T> clazz, File file) throws IOException {
        return null;
    }

    @Override
    public String writeToString(Object object) {
        return null;
    }

    @Override
    public void write(Object object, OutputStream outputStream) throws IOException {

    }

    @Override
    public void write(Object object, File file) throws IOException {

    }

    public String jsonSerializer(Object object) {
        try {
            checkIfSerializable(object);
            return null;
        } catch (Exception e) {
            throw new JsonSerializationException(e.getMessage());
        }
    }

    private String getJsonString(Object object) throws IllegalAccessException {
        List<Field> fields = getSerializationFields(object);
        Map<String, String> jsonElementsMap = new HashMap<>();
        for (Field field : fields) {
            if (field.getType() == LocalTime.class) {
                if (field.isAnnotationPresent(DateFormat.class)) {
                    jsonElementsMap.put(getKey(field), ((LocalTime)field.get(object)).format(DateTimeFormatter.ofPattern(field.getAnnotation(DateFormat.class).value())));
                }
                continue;
            }
            if (field.getType() == LocalDateTime.class) {
                if (field.isAnnotationPresent(DateFormat.class)) {
                    jsonElementsMap.put(getKey(field), ((LocalDateTime)field.get(object)).format(DateTimeFormatter.ofPattern(field.getAnnotation(DateFormat.class).value())));
                }
                continue;
            }
            if (field.getType() == LocalDate.class) {
                if (field.isAnnotationPresent(DateFormat.class)) {
                    jsonElementsMap.put(getKey(field), ((LocalDate)field.get(object)).format(DateTimeFormatter.ofPattern(field.getAnnotation(DateFormat.class).value())));
                }
                continue;
            }
            jsonElementsMap.put(getKey(field), (String) field.get(object));
        }


        String jsonString = jsonElementsMap.entrySet()
                .stream()
                .map(entry -> "\"" + entry.getKey() + "\":\"" + (entry.getValue().contains("\"") ? entry.getValue().replaceAll("\"", "\\\"") : entry.getValue())  + "\"")
                .collect(Collectors.joining(","));
        return "{" + jsonString + "}";
    }

    private void checkIfSerializable(Object object) {
        if (Objects.isNull(object)) {
            throw new JsonSerializationException("Can't serialize a null object");
        }

        Class<?> clazz = object.getClass();
        if (!clazz.isAnnotationPresent(Exported.class)) {
            throw new JsonSerializationException("The class " + clazz.getSimpleName() + " is not annotated with Exported");
        }
    }

    private String getKey(Field field) {
        String value = field.getAnnotation(PropertyName.class).value();
        return value.isEmpty() ? field.getName() : value;
    }

    private List<Field> getSerializationFields(Object object) throws IllegalAccessException {
        boolean ignoreNull = object.getClass().getAnnotation(Exported.class).nullHandling() == NullHandling.INCLUDE;
        List<Field> fields = Arrays.stream(object.getClass().getFields()).toList();
        for (int i = fields.size() - 1; i >= 0; i--) {
            fields.get(i).setAccessible(true);
            if (fields.get(i).isAnnotationPresent(Ignored.class)) {
                fields.remove(i);
            }
            if (fields.get(i).get(object) == null && ignoreNull) {
                fields.remove(i);
            }
        }
        return fields;
    }
}
