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
    public String writeToString(Object object) throws IllegalAccessException {
        return jsonSerializer(object);
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
            return getJsonString(object);
        } catch (Exception e) {
            throw new JsonSerializationException(e.getMessage());
        }
    }

    private String dateToJson(Object object, Field field) throws IllegalAccessException {
        field.setAccessible(true);
        if (field.getType() == LocalTime.class) {
            if (field.isAnnotationPresent(DateFormat.class)) {
                return ((LocalTime) field.get(object)).format(DateTimeFormatter.ofPattern(field.getAnnotation(DateFormat.class).value()));
            } else {
                return ((LocalTime) field.get(object)).toString();
            }
        }
        if (field.getType() == LocalDateTime.class) {
            if (field.isAnnotationPresent(DateFormat.class)) {
                return ((LocalDateTime) field.get(object)).format(DateTimeFormatter.ofPattern(field.getAnnotation(DateFormat.class).value()));
            } else {
                return ((LocalDateTime) field.get(object)).toString();
            }
        }
        if (field.getType() == LocalDate.class) {
            if (field.isAnnotationPresent(DateFormat.class)) {
                return ((LocalDate) field.get(object)).format(DateTimeFormatter.ofPattern(field.getAnnotation(DateFormat.class).value()));
            } else {
                return ((LocalDate) field.get(object)).toString();
            }
        }
        throw new JsonSerializationException("can not serialize date field");
    }

    private String primitiveToJson(Object object, Field field) throws IllegalAccessException {
        return field.get(object).toString();
    }

    private String collectionToJson(Field field, Collection collection) throws IllegalAccessException {
        String res = "[";
        if (collection.isEmpty()) {
            return "[]";
        }
        if (LocalTime.class == collection.toArray()[0].getClass()) {
            if (field.isAnnotationPresent(DateFormat.class)) {
                res += collection.stream().map(elem -> ((LocalTime) elem).format(DateTimeFormatter.ofPattern(field.getAnnotation(DateFormat.class).value()))).collect(Collectors.joining(", "));
            } else {
                res += collection.stream().map(elem -> elem.toString()).collect(Collectors.joining(", "));
            }
        } else if (LocalDateTime.class == collection.toArray()[0].getClass()) {
            if (field.isAnnotationPresent(DateFormat.class)) {
                res += collection.stream().map(elem -> ((LocalDateTime) elem).format(DateTimeFormatter.ofPattern(field.getAnnotation(DateFormat.class).value()))).collect(Collectors.joining(", "));
            } else {
                res += collection.stream().map(elem -> elem.toString()).collect(Collectors.joining(", "));
            }
        } else if (LocalDate.class == collection.toArray()[0].getClass()) {
            if (field.isAnnotationPresent(DateFormat.class)) {
                res += collection.stream().map(elem -> ((LocalDate) elem).format(DateTimeFormatter.ofPattern(field.getAnnotation(DateFormat.class).value()))).collect(Collectors.joining(", "));
            } else {
                res += collection.stream().map(elem -> elem.toString()).collect(Collectors.joining(", "));
            }
        } else if (List.of(new Class[]{
                int.class, Integer.class, short.class, Short.class,
                long.class, Long.class, byte.class, Byte.class,
                float.class, Float.class, double.class, Double.class,
                char.class, Character.class, boolean.class, Boolean.class}).contains(collection.toArray()[0].getClass())) {
            res += collection.stream().map(elem -> elem.toString()).collect(Collectors.joining(", "));
        } else if (String.class == collection.toArray()[0].getClass()) {
            res += collection.stream().map(elem -> "\"" + elem + "\"").collect(Collectors.joining(", "));
        } else if (collection.toArray()[0].getClass().isAnnotationPresent(Exported.class)) {
           res += collection.stream().map(elem -> {
               try {
                   return getJsonString(elem);
               } catch (IllegalAccessException e) {
               }
               return "";
           }).collect(Collectors.joining(",\n"));
        } else if (List.of(List.class, Set.class).contains(collection.toArray()[0].getClass())) {
            res += collection.stream().map(elem -> {
                try {
                    return collectionToJson(field, collection);
                } catch (IllegalAccessException e) {}
                return "";
            }).collect(Collectors.joining(", "));
        } else if (collection.toArray()[0].getClass().isEnum()) {
            res += collection.stream().map(elem -> elem.toString()).collect(Collectors.joining(", "));
        } else {
            throw new JsonSerializationException("Unknown type of collection containment");
        }
        res += "]";
        return res;
    }

    private String getJsonString(Object object) throws IllegalAccessException {
        List<Field> fields = getSerializationFields(object);
        Map<String, String> jsonElementsMap = new HashMap<>();
        for (Field field : fields) {
            field.setAccessible(true);
            // Время.
            if (List.of(LocalTime.class, LocalDateTime.class, LocalDate.class).contains(field.getType())) {
                jsonElementsMap.put(getKey(field), dateToJson(object, field));
                continue;
            }
            // Примитивы.
            if (List.of(new Class[]{
                    int.class, Integer.class, short.class, Short.class,
                    long.class, Long.class, byte.class, Byte.class,
                    float.class, Float.class, double.class, Double.class,
                    char.class, Character.class, boolean.class, Boolean.class}).contains(field.getType())) {
                jsonElementsMap.put(getKey(field), field.get(object).toString());
                continue;
            }
            // String.
            if (field.getType() == String.class) {
                jsonElementsMap.put(getKey(field), "\"" + field.get(object).toString() + "\"");
                continue;
            }
            // Exported class.
            if (field.getType().isAnnotationPresent(Exported.class)) {
                jsonElementsMap.put(getKey(field), getJsonString(field.get(object)));
                continue;
            }
            // Коллекции.
            if (List.of(List.class, Set.class).contains(field.getType())) {
                jsonElementsMap.put(getKey(field), collectionToJson(field, (Collection) field.get(object)));
                continue;
            }
            // Перечесления.
            if (field.getType().isEnum()) {
                jsonElementsMap.put(getKey(field), field.get(object).toString());
                continue;
            }
            throw new JsonSerializationException("Unsupproted field type");
        }

        String jsonString = jsonElementsMap.entrySet()
                .stream()
                .map(entry -> "\"" + entry.getKey() + "\":" + (entry.getValue().contains("\"") ? entry.getValue().replaceAll("\"", "\\\"") : entry.getValue()))
                .collect(Collectors.joining(",\n"));
        return "\n{\n" + jsonString + "\n}";
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
        String value = "";
        if (field.isAnnotationPresent(PropertyName.class)) {
            value = field.getAnnotation(PropertyName.class).value();
        }
        return value.isEmpty() ? field.getName() : value;
    }

    private List<Field> getSerializationFields(Object object) throws IllegalAccessException {
        boolean ignoreNull = object.getClass().getAnnotation(Exported.class).nullHandling() == NullHandling.INCLUDE;
        List<Field> fields = Arrays.stream(object.getClass().getDeclaredFields()).toList();
        List<Field> res = new ArrayList<>();
        for (int i = 0; i < fields.size() - 1; i++) {
            fields.get(i).setAccessible(true);
            if (fields.get(i).isAnnotationPresent(Ignored.class)) {
                continue;
            }
            if (fields.get(i).get(object) == null && ignoreNull) {
                continue;
            }
            res.add(fields.get(i));
        }
        return res;
    }
}
