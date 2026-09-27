# Libuid

Libuid is a general-purpose fluid library for Paper, Bukkit and CraftEngine plugins. It provides a unified set of APIs for fluid types, fluid stacks, fluid components, tanks, recipe matching, item container bridging and persistence, allowing different plugins to share the same fluid data and transfer rules.

Libuid does not add any blocks or gameplay on its own. It provides the underlying API so that other plugins can build kegs, fluid storage blocks, fluid machines, fluid recipes and custom containers on top of it.

## Features

- `FluidType` — define fluid types with display name, color, texture, density, viscosity, temperature, light level and sounds.
- `FluidStack` — represent a fluid, an amount and component data.
- `FluidComponentMap` — store additional fluid properties such as temperature, quality or other plugin-defined data.
- `FluidTank` — standard single-slot fluid tank.
- `FluidHandler` — unified fluid storage and transfer interface.
- `FluidIngredient` — match by single fluid, fluid tag or candidate list.
- `FluidStackCodec` — encode a complete fluid stack into binary data suitable for NBT, PDC and other persistence systems.
- `FluidHandlerItem` — use an item as a fluid container.
- `PdcFluidHandlerItem` — use ItemStack PDC as a dynamic fluid tank.
- `VanillaBucketHandler` — built-in support for water, lava and milk buckets.
- CraftEngine `libuid:fluid_container` item setting — declare fluid container rules on custom items.
- `FluidUtil` — simulate or execute fluid transfers between tanks and item containers.

## Requirements

- Java 21
- Paper 1.21 or Folia, API version 1.21
- CraftEngine 26.9.1

Libuid runs as a Paper plugin and depends on CraftEngine. The loading order is declared in `paper-plugin.yml` so that Libuid loads before CraftEngine. Servers using Libuid must have CraftEngine installed.

Libuid declares `folia-supported: true`, so it loads and runs on Folia as well as Paper. Libuid performs no scheduling and keeps no main-thread state of its own, so its API can be called from any region thread. Item stacks must still be handled by the thread that owns them.

## Installation

1. Install Paper 1.21 or Folia.
2. Install CraftEngine 26.9.1.
3. Place the Libuid plugin jar into the server `plugins` directory.
4. Start the server.

Libuid does not ship any content packs that require separate configuration. Fluid types can be registered from plugin code or from CraftEngine configuration.

## Using Libuid in a Gradle project

After publishing Libuid to Maven Local, add the dependency:

```kotlin
repositories {
    mavenLocal()
    mavenCentral()
}

dependencies {
    compileOnly("dev.tako:libuid:1.0.1")
    compileOnly("io.papermc.paper:paper-api:1.21-R0.1-SNAPSHOT")
    compileOnly("net.momirealms:craft-engine-core:26.9.1")
    compileOnly("net.momirealms:craft-engine-bukkit:26.9.1")
}
```

For Maven projects:

```xml
<repositories>
    <repository>
        <id>local</id>
        <url>file://${user.home}/.m2/repository</url>
    </repository>
</repositories>

<dependency>
    <groupId>dev.tako</groupId>
    <artifactId>libuid</artifactId>
    <version>1.0.1</version>
    <scope>provided</scope>
</dependency>
```

Libuid is a server runtime plugin. Use `compileOnly` or `provided` scope and do not bundle Libuid inside your own plugin jar. Declare Libuid as a required dependency in your `plugin.yml` or `paper-plugin.yml`.

## Declaring the plugin dependency

In `paper-plugin.yml`:

```yaml
name: ExamplePlugin
version: '${version}'
main: example.plugin.ExamplePlugin
api-version: '1.21'
dependencies:
  server:
    Libuid:
      load: BEFORE
      required: true
      join-classpath: true
```

If your plugin also uses CraftEngine item definitions, declare CraftEngine as well:

```yaml
dependencies:
  server:
    Libuid:
      load: BEFORE
      required: true
      join-classpath: true
    CraftEngine:
      load: BEFORE
      required: true
      join-classpath: true
```

## Registering fluid types

Register fluid types during plugin startup. Make sure Libuid has loaded first.

```java
public final class ExampleFluids {
    public static final ResourceKey APPLE_JUICE_KEY = ResourceKey.of("example:apple_juice");
    public static FluidType APPLE_JUICE;

    public static void register() {
        APPLE_JUICE = FluidType.builder(APPLE_JUICE_KEY)
                .displayName("<lang:example.fluid.apple_juice>")
                .color(0xD63B2F)
                .texture("apple_juice")
                .density(1024)
                .viscosity(1200)
                .build();
        FluidRegistry.register(APPLE_JUICE);
    }
}
```

Each `ResourceKey` may only be registered once. Keys use the `namespace:path` format, for example `example:apple_juice`.

## Creating and using FluidStack

```java
FluidType appleJuice = FluidRegistry.get("example:apple_juice")
        .orElseThrow();

FluidStack stack = FluidStack.of(appleJuice, 500);

int amount = stack.amount();
ResourceKey key = stack.fluidKey();
boolean empty = stack.isEmpty();
```

`FluidStack` is immutable. Changing the amount creates a new instance:

```java
FluidStack smaller = stack.copyWithAmount(250);
FluidStack larger = stack.grow(250);
FluidStack reduced = stack.shrink(100);
```

Amount units are mB:

```java
int bucket = FluidStack.BUCKET_VOLUME;
int bottle = FluidStack.BOTTLE_VOLUME;
```

## Using FluidTank

```java
FluidTank tank = new FluidTank(4000);

tank.fill(FluidStack.of(appleJuice, 1000), FluidAction.EXECUTE);

FluidStack available = tank.fluidInTank(0);
int amount = tank.amount();
int capacity = tank.capacity();
```

`FluidTank` is a single-slot tank. It only allows merging stacks of the same fluid with identical components.

### Simulate and execute

All fluid operations use `FluidAction`:

```java
int simulated = tank.fill(stack, FluidAction.SIMULATE);
int executed = tank.fill(stack, FluidAction.EXECUTE);
```

- `SIMULATE` checks how much can be transferred without changing any state.
- `EXECUTE` actually modifies the tank or item.

Interaction logic should simulate first, then execute only after confirming that items and space are sufficient.

## Implementing FluidHandler

Block entities or custom objects that need to participate in fluid transfers can implement `FluidHandler`:

```java
public final class ExampleTank implements FluidHandler {
    private final FluidTank tank = new FluidTank(4000);

    @Override public int tanks() { return 1; }
    @Override public FluidStack fluidInTank(int tank) { return this.tank.fluidInTank(tank); }
    @Override public int tankCapacity(int tank) { return this.tank.tankCapacity(tank); }
    @Override public boolean isFluidValid(int tank, FluidStack stack) { return this.tank.isFluidValid(tank, stack); }
    @Override public int fill(FluidStack resource, FluidAction action) { return tank.fill(resource, action); }
    @Override public FluidStack drain(FluidStack resource, FluidAction action) { return tank.drain(resource, action); }
    @Override public FluidStack drain(int maxDrain, FluidAction action) { return tank.drain(maxDrain, action); }
}
```

Custom handlers must guarantee that within the same synchronous call, the result of a valid `SIMULATE` matches the immediately following `EXECUTE`. Otherwise `FluidUtil` throws `IllegalStateException`, because a non-transactional interface cannot safely compensate an already-executed transfer.

## Using FluidUtil to transfer fluids

```java
FluidStack moved = FluidUtil.tryFluidTransfer(
        destinationTank,
        sourceTank,
        1000,
        FluidAction.SIMULATE
);
```

After confirming the simulation result, execute:

```java
FluidStack moved = FluidUtil.tryFluidTransfer(
        destinationTank,
        sourceTank,
        1000,
        FluidAction.EXECUTE
);
```

This method:

1. Simulates draining from the source.
2. Simulates filling the destination.
3. Calculates the actual amount both sides can handle.
4. Under `EXECUTE`, drains and fills in sequence.
5. Returns the actually transferred `FluidStack`.

## Declaring a dynamic container via CraftEngine item settings

The CE item setting stores definition-level rules only. The actual fluid content of each item instance is stored in its PDC.

Example:

```yaml
example:canteen:
  material: glass_bottle
  settings:
    "libuid:fluid_container":
      capacity: 4000
```

With an allowed fluids whitelist:

```yaml
example:juice_canteen:
  material: glass_bottle
  settings:
    "libuid:fluid_container":
      capacity: 1000
      allowed_fluids:
        - "example:apple_juice"
        - "example:grape_juice"
```

Using fluid tags:

```yaml
example:drink_canteen:
  material: glass_bottle
  settings:
    "libuid:fluid_container":
      capacity: 1000
      allowed_fluids:
        - "#example:drinkable"
```

Rules:

- `capacity` must be a positive integer in mB.
- Omitting `allowed_fluids` allows all registered fluids.
- Fluids inside the same container must be the same type with identical components.
- Partial fill and partial drain are supported.
- Dynamic fluid content uses the PDC key `libuid:fluid_stack`.
- Empty containers have this PDC key removed.
- Data that cannot be decoded or references unknown fluids is preserved and never overwritten as empty.

## Reading and writing item fluid data

```java
ItemFluidDataReadResult stored = ItemFluidData.read(item);

if (stored.status() == ItemFluidDataReadResult.Status.INVALID) {
    return;
}

FluidStack fluid = stored.fluid();
```

Status values:

- `EMPTY` — no fluid data; the container can be filled.
- `PRESENT` — valid fluid data exists.
- `INVALID` — data exists but cannot be safely decoded; it must be preserved and never overwritten.

Writing data:

```java
ItemFluidData.write(item, FluidStack.of(appleJuice, 1000));
ItemFluidData.write(item, FluidStack.EMPTY);
```

Writing `FluidStack.EMPTY` removes the PDC key.

## Using dynamic item containers

Resolve a handler from a CE item:

```java
Optional<FluidHandlerItem> handler = FluidContainerRegistry.handlerFor(item);
```

Fill an item from a tank:

```java
FluidActionResult result = FluidUtil.tryFillContainer(
        item, sourceTank, 4000, FluidAction.EXECUTE);

if (result.success()) {
    ItemStack updatedItem = result.result();
    FluidStack moved = result.moved();
}
```

Empty an item into a tank:

```java
FluidActionResult result = FluidUtil.tryEmptyContainer(
        item, destinationTank, 4000, FluidAction.EXECUTE);

if (result.success()) {
    ItemStack updatedItem = result.result();
}
```

The input item may be a stack. Libuid only operates on a count-1 copy. The calling plugin is responsible for:

1. Removing one item from the original stack.
2. Placing `result.result()` back into the player's hand, inventory or container slot.
3. Handling drop or failure logic when the result item cannot be placed.

A successful `SIMULATE` only returns the expected transfer amount. The returned item is unchanged and must not be written back.

## Vanilla buckets

Built-in support for `minecraft:bucket`, `minecraft:water_bucket`, `minecraft:lava_bucket` and `minecraft:milk_bucket`.

Vanilla bucket transfers are fixed at 1000 mB:

- An empty bucket can only be filled when the source has at least 1000 mB.
- A full bucket can only be emptied when the destination can accept at least 1000 mB.
- Vanilla buckets do not accept fluids with components, to avoid silently dropping attached data.
- When there is not enough space for a full bucket, the operation fails and the bucket is unchanged.

## Syncing fluid between block entities and items

Define the block as a non-stackable CE item with the `libuid:fluid_container` setting. When placing the block, read fluid from the item and restore it to the block entity:

```java
ItemFluidDataReadResult stored = ItemFluidData.read(item);
if (stored.status() == ItemFluidDataReadResult.Status.INVALID) return;
blockEntity.tank().setFluid(stored.fluid());
```

When breaking the block and creating a drop item:

```java
ItemFluidData.write(item, blockEntity.tank().fluid());
```

Check plugin-specific fluid recipes first during interaction and fall back to `FluidUtil` when no recipe matches.

## Persistence

Block entities can use `FluidStackCodec` directly:

```java
byte[] encoded = FluidStackCodec.toBinary(stack);
FluidStack restored = FluidStackCodec.fromBinary(encoded);
```

Always save the complete `FluidStack`. Saving only ID and amount loses components. When reading unknown fluids or components, preserve the original data and do not overwrite as empty.

## Fluid recipe matching

```java
FluidIngredient ingredient = FluidIngredient.single(ResourceKey.of("example:apple_juice"));
boolean matches = ingredient.test(stack);

FluidIngredient tagIngredient = FluidIngredient.tag(ResourceKey.of("example:drinkable"));

SizedFluidIngredient sized = new SizedFluidIngredient(ingredient, 1000);
boolean enough = sized.test(stack);
```

`FluidIngredient` matches by fluid type, not by amount. Use `SizedFluidIngredient` when an amount threshold is required.

## Building and testing

```bash
gradle clean build
gradle publishToMavenLocal
```

Building requires Java 21 and runs the JUnit tests included in the project.

## Version and compatibility

Current version: `1.0.1`.

Libuid declares `folia-supported: true` in `paper-plugin.yml`, so it loads and runs on both Paper and Folia.

Libuid identifies fluid data by `ResourceKey`. Do not change fluid keys already in use when upgrading, or existing saves, item PDC and recipes will fail to find their fluids.

The CraftEngine item setting uses the `libuid:fluid_container` namespace to avoid conflicts with other plugins.

## License and contributing

This project is licensed under the GNU General Public License v3.0 (GPL-3.0). The full license text is in the [`LICENSE`](LICENSE) file at the repository root.

Contributions via Pull Request are welcome. Before submitting, please ensure:

- The scope of the change is clear and the expected behavior is described.
- New features or behavior changes include corresponding tests.
- No secrets, private configuration or unauthorized third-party content is introduced.
- The submission complies with GPL-3.0 and applicable third-party licenses.

Bug reports, issues and feature suggestions can be filed in GitHub Issues. Please include reproduction steps, relevant logs and environment details.
