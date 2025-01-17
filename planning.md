# Planning For JSONSync
- Json examples
  - Rotation2D
  - Translation2D
  - Pose2D
- Conversions
  - Adding new Conversions
  - Using Conversions with Json
  - Using Conversions
- Type Adapters Example Code
  - Distance
  - Rotation2D
  - Translation2D
  - Pose2D

## Json examples

### Rotation2D

```json
{
    "radians" : 3.4
}
```

### Translation2D

```json
{
    "x" : 45,
    "y" : 543
}
```

### Pose 2D

```json
{

    "rotation" : {
        "radians" : 3.4
    },

    "translation" : {
        "x" : 45,
        "y" : 543
    }
}
```

## Conversions


### Adding new Conversions
```java
public class CustomConversions{
    public static LinearVelocity doubleToLinearVelocity(Double velocity){
        //Method implementation
    }

    public static Double linearVelocityToDouble(LinearVelocity velocity){
        //Method implementation
    }

    public static void registerConversions(){
        Conversions.register(
            CustomConversions::doubleToLinearVelocity, 
            LinearVelocity.class,
            Double.class
        );
        Conversions.register(
            CustomConversions::linearVelocityToDouble, 
            Double.class,
            LinearVelocity.class
        );
    }
}
```

### Using Conversions with JsonUsing Conversions with Json
```java
public class Constants{
    @JsonConversion(code=LinearVelocity.class, json=Double.class)
    public final LinearVelocity velocity = MetersPerSecond.of(1.0);
}
```

### Using Conversions
```java
public class SomeClass {

    public void someMethod(){
        //Some Code
        Double someDouble = Conversion.convert(MetersPerSecond.of(1.0), Double.class);
        //Some Code
    }

}
```


## Type Adapters Example Code

### Distance Adapter

```java
class MetersAdapter extends TypeAdapter<Distance> { 
   @Override 
   public Distance read(JsonReader reader) throws IOException { 
      Double value = reader.nextDouble();
      return Meters.of(value); 
   }  

   @Override 
   public void write(JsonWriter writer, Distance distance) throws IOException { 
      writer.value(distance.in(Meters));
   } 
}
```

### Rotation2D Adapter

```java
class Rotation2DAdapter extends TypeAdapter<Rotation2d> { 
   @Override 
   public Distance read(JsonReader reader) throws IOException {
        double value;
        reader.beginObject();
        while (reader.hasNext()) {
            String name = reader.nextName();
            if ("radians".equals(name)){
                value = reader.nextDouble();
            }
        }
        reader.endObject();
        return new Rotation2D(value);
   }  

   @Override 
   public void write(JsonWriter writer, Rotation2d rotation) throws IOException { 
        writer.beginObject();
        writer.name("radians").value(rotation.getX());
        writer.endObject();
   } 
}
```

### Translation2D Adapter

```java
class Translation2DAdapter extends TypeAdapter<Translation2d> {
   @Override 
   public Distance read(JsonReader reader) throws IOException { 
        int x;
        int y;
        reader.beginObject();
        while (reader.hasNext()) {
            String name = reader.nextName();
            switch (name) {
                case "x":
                    x = reader.nextDouble();
                    break;
                case "y":
                    y = reader.nextDouble();
                    break;
            }
        }
        reader.endObject();
        return new Translation2D(x, y);
   }  

   @Override 
   public void write(JsonWriter writer, Translation2d translation) throws IOException {
        writer.beginObject();
        writer.name("x").value(translation.getX());
        writer.name("y").value(translation.getY());
        writer.endObject();
   } 
}
```

### Pose2D Adapter

```java
class Pose2DAdapter extends TypeAdapter<Translation2d> {

    private Translation2DAdapter translationAdapter = new Translation2DAdapter();
    private Rotation2DAdapter rotationAdapter = new Rotation2DAdapter();


   @Override 
   public Distance read(JsonReader reader) throws IOException { 
        Translation2D translation;
        Rotation2D rotation;
        reader.beginObject();
        while (reader.hasNext()) {
            String name = reader.nextName();
            switch (name) {
                case "translation":
                    translation = translationAdapter.read(reader);
                    break;
                case "rotation":
                    rotation = rotationAdapter.read(reader);
                    break;
            }
        }
        reader.endObject();
        return new Pose2D(translation, rotation);
   }  

   @Override 
   public void write(JsonWriter writer, Pose2D pose) throws IOException {
        writer.beginObject();
        writer.name("translation").value(pose.getTranslation());
        writer.name("rotation").value(pose.getRotation());
        writer.endObject();
   } 
}
```