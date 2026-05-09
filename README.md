# MinecraftFisher

Framework that speeds up your bukkit plugin development

## Features

- Automatic registration of listeners, commands, configuration etc.
- Management of singletons via `@Hook` annotation
- `plugin.yml` and `config.yml` generation

## Instalation

### Maven

Repository
```xml
<repository>
    <id>Minecraft-Fisher</id>
    <url>https://maven.pkg.github.com/Kari3600/MinecraftFisher</url>
</repository>
```

Dependency
```xml
<dependency>
    <groupId>com.kari3600.mc</groupId>
    <artifactId>minecraftfisher</artifactId>
</dependency>
```

Annotation processor
```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-compiler-plugin</artifactId>
    <configuration>
        <annotationProcessorPaths>
            <dependency>
                <groupId>com.kari3600.mc</groupId>
                <artifactId>minecraftfisher</artifactId>
            </dependency>
        </annotationProcessorPaths>
    </configuration>
</plugin>
```

## Usage

Instead of extending `JavaPlugin`, extend `FisherPlugin<JavaPlugin>` and add constructor calling super - all the features remain the same!

Annotate it with `@AutoPlugin` to generate proper `plugin.yml`

```java
@AutoPlugin(
        name = "MyPlugin",
        version = "1.0.0",
        author = "Kari3600",
        depend = {"Vault"},
        softDepend = {"WorldEdit", "WorldGuard"}
)
public class MyPlugin extends FisherPlugin<JavaPlugin> {
    @Override
    public void onLoad() {
        System.out.println("Plugin loaded");
    }
    
    @Override
    public void onEnable() {
        System.out.println("Plugin enabled");
    }
    
    @Override
    public void onDisable() {
        System.out.println("Plugin disabled");
    }
    
    public MyPlugin(JavaPlugin plugin) {
        super(plugin);
    }
}
```

### Auto annotations

Instead of registering listeners manually, you can just annotate the class with `@AutoListener`

```java
@AutoListener
public class MyListener implements Listener {
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        event.getPlayer().sendMessage("Welcome!");
    }
}
```

To automatically register command, just annotate executor with `@AutoCommand`

```java
@AutoCommand(
        name = "ping",
        description = "try it out!",
        permission = "myplugin.command.ping",
        aliases = {"pping", "pinging"}
)
public class PingCommand implements CommandExecutor {
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        sender.sendMessage("Pong!");
    }
}
```

To automatically register `ConfigurationSerializable` class, annotate it with `@AutoSerializable`

```java
@AutoSerializable
public class BlockPosition implements ConfigurationSerializable {
    private final int x, y, z;
    
    public BlockPosition(Map<String, Object> map) {
        this.x = (int) map.get("x");
        this.y = (int) map.get("y");
        this.z = (int) map.get("z");
    }
    
    @Override
    public Map<String, Object> serialize() {
        return Map.of(
                "x", this.x,
                "y", this.y,
                "z", this.z
        );
    }
}
```

You can also automatically call a function with configuration value every time the configuration is reloaded, just annotate it with `@AutoConfiguration`

```java
public class MessageManager {
    private String privateMessageFormat;

    @AutoConfiguration(path = "format.message.private")
    public void setPrivateMessageFormat(String privateMessageFormat) {
        this.privateMessageFormat = privateMessageFormat;
    }
    
    public String formatPrivateMessage(String sender, String receiver, String message) {
        return String.format(privateMessageFormat, sender, receiver, message);
    }
}
```

### Dependency Injection

Instead of using static instance getter for every dependency, you can just add annotated parameter to the constructor and `FishContainer` will provide it automatically

```java
public class ShopManager {
    private final EconomyManager economyManager;

    public ShopManager(@Hook EconomyManager economyManager) {
        this.economyManager = economyManager;
    }

    public void buy(Player player) {
        boolean success = economyManager.withdraw(player, 100);
        if (success) {
            player.getInventory().addItem(new ItemStack(Material.GOLDEN_APPLE));
            player.sendMessage("Item purchased successfully.");
        } else {
            player.sendMessage("Not enough money!");
        }
    }
}
```

All listeners and commands are already registered in DI and you can use `@Hook` annotated parameters for them too

If for some reason you have to instantiate class with DI, use
```java
FishContainer.get().instantiate(ShopManager.class);
```
To statically get already instantiated class, use
```java
FishContainer.get().getFish(ShopManager.class);
```