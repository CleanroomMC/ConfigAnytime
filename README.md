## ConfigAnytime

#### Allows Forge configurations to be setup at any point in time. Especially for developers that use Forge's configuration system during coremod/tweaker's loading stage.


### Dev Usage:

- Add CleanroomMC's repository and depend on ConfigAnytime's maven entry:
```groovy
repositories {
    maven {
        url 'https://maven.cleanroommc.com'
    }
}

dependencies {
    implementation 'com.cleanroommc:configanytime:1.0'
}
```

- Example API Usage:

```java
@Config(modid = "configanytime")
public class ConfigClass {
    
    public static boolean configBooleanProperty = true;
    public static int configIntProperty = 42;
    
    public static final InnerClass INNER_CLASS = new InnerClass(); // Inner access via this member, this is processed automatically by ConfigManager
    
    public static class InnerClass {
        
        public boolean innerProperty = false; // Must be non-static, referenced via the member field in the outer class
        
    }
    
    // Static initializers go after the properties!
    // This will run automatically when you retrieve any properties from this config class
    static {
        ConfigAnytime.register(ConfigClass.class);
    }
    
}
```