# Libuid

Libuid 是面向 Paper、Bukkit 与 CraftEngine 插件的通用流体库。它提供统一的流体类型、流体堆、流体组件、储罐、配方匹配、物品容器桥接和持久化 API，让不同插件可以使用同一套流体数据和转移规则。

Libuid 本身不添加新的方块或玩法。它负责提供基础 API，具体插件可以使用这些 API 实现酒桶、储液方块、流体机器、流体配方和自定义容器。

## 功能

- `FluidType`：定义流体类型、显示名、颜色、纹理、密度、黏度、温度、亮度和声音。
- `FluidStack`：表示一种流体、数量和组件数据。
- `FluidComponentMap`：保存流体的附加属性，例如温度、品质或其他插件定义的数据。
- `FluidTank`：提供标准单槽流体储罐。
- `FluidHandler`：统一的流体存储与转移接口。
- `FluidIngredient`：支持单流体、流体标签和候选列表匹配。
- `FluidStackCodec`：将完整流体堆编码为二进制数据，适合 NBT、PDC 和其他持久化系统。
- `FluidHandlerItem`：将物品作为流体容器使用。
- `PdcFluidHandlerItem`：将 ItemStack PDC 作为动态流体储罐。
- `VanillaBucketHandler`：支持水桶、岩浆桶、奶桶和空桶。
- CraftEngine `libuid:fluid_container` item setting：声明自定义物品的流体容器规则。
- `FluidUtil`：在储罐与物品容器之间执行模拟或实际流体转移。

## 运行环境

- Java 21
- Paper 1.21 或 Folia，API 版本 1.21
- CraftEngine 26.9.1

Libuid 作为 Paper 插件运行，并依赖 CraftEngine。插件启动顺序由 `paper-plugin.yml` 声明为先于 CraftEngine 加载，因此使用 Libuid 的服务器必须安装 CraftEngine。

Libuid 声明了 `folia-supported: true`，因此可以在 Paper 与 Folia 上加载运行。Libuid 自身不进行任何调度，也不保存主线程状态，API 可以在任意区域线程直接调用；物品堆仍应由持有它的线程处理。

## 安装

1. 安装 Paper 1.21 或 Folia。
2. 安装 CraftEngine 26.9.1。
3. 将 Libuid 插件放入服务器的 `plugins` 目录。
4. 启动服务器。

Libuid 不包含需要单独配置的内容包。流体类型可以由插件代码注册，也可以由 CraftEngine 配置注册。

## 在 Gradle 项目中使用

Libuid 发布到 Maven Local 后，可以在开发环境中使用：

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

如果项目使用 Maven：

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

Libuid 是服务器运行时插件，因此依赖通常应使用 `compileOnly` 或 `provided`，不要把 Libuid 重复打包进自己的插件。自己的插件应在 `plugin.yml` 或 `paper-plugin.yml` 中声明 Libuid 为必需依赖。

## 声明插件依赖

Paper 插件可以在 `paper-plugin.yml` 中声明：

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

如果插件同时使用 CraftEngine 的物品定义，还应声明 CraftEngine：

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

## 注册流体类型

插件启动时可以注册自己的流体类型。注册前应确保 Libuid 已加载。

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

同一个 `ResourceKey` 不应重复注册。流体 key 使用 `namespace:path` 格式，例如 `example:apple_juice`。

## 创建和使用 FluidStack

```java
FluidType appleJuice = FluidRegistry.get("example:apple_juice")
        .orElseThrow();

FluidStack stack = FluidStack.of(appleJuice, 500);

int amount = stack.amount();
ResourceKey key = stack.fluidKey();
boolean empty = stack.isEmpty();
```

`FluidStack` 是不可变数据。修改数量时应创建新对象：

```java
FluidStack smaller = stack.copyWithAmount(250);
FluidStack larger = stack.grow(250);
FluidStack reduced = stack.shrink(100);
```

数量单位是 mB：

```java
int bucket = FluidStack.BUCKET_VOLUME;
int bottle = FluidStack.BOTTLE_VOLUME;
```

## 使用 FluidTank

```java
FluidTank tank = new FluidTank(4000);

tank.fill(FluidStack.of(appleJuice, 1000), FluidAction.EXECUTE);

FluidStack available = tank.fluidInTank(0);
int amount = tank.amount();
int capacity = tank.capacity();
```

`FluidTank` 是单槽储罐。它默认只允许相同流体和相同 components 的 `FluidStack` 合并。

### 模拟和执行

所有流体操作都使用 `FluidAction`：

```java
int simulated = tank.fill(stack, FluidAction.SIMULATE);
int executed = tank.fill(stack, FluidAction.EXECUTE);
```

- `SIMULATE` 只检查可以转移多少，不改变状态。
- `EXECUTE` 才会真正修改储罐或物品。

交互逻辑应先模拟，再在确认物品和空间都足够后执行实际操作。

## 实现 FluidHandler

需要被其他容器操作的方块实体或自定义对象，可以实现 `FluidHandler`：

```java
public final class ExampleTank implements FluidHandler {
    private final FluidTank tank = new FluidTank(4000);

    @Override
    public int tanks() {
        return 1;
    }

    @Override
    public FluidStack fluidInTank(int tank) {
        return this.tank.fluidInTank(tank);
    }

    @Override
    public int tankCapacity(int tank) {
        return this.tank.tankCapacity(tank);
    }

    @Override
    public boolean isFluidValid(int tank, FluidStack stack) {
        return this.tank.isFluidValid(tank, stack);
    }

    @Override
    public int fill(FluidStack resource, FluidAction action) {
        return tank.fill(resource, action);
    }

    @Override
    public FluidStack drain(FluidStack resource, FluidAction action) {
        return tank.drain(resource, action);
    }

    @Override
    public FluidStack drain(int maxDrain, FluidAction action) {
        return tank.drain(maxDrain, action);
    }
}
```

自定义 handler 必须保证同一次同步调用中，合法请求的 `SIMULATE` 结果与紧接着的 `EXECUTE` 结果一致。否则 `FluidUtil` 会抛出 `IllegalStateException`，因为通用非事务接口无法安全补偿已经执行的转移。

## 使用 FluidUtil 转移流体

```java
FluidStack moved = FluidUtil.tryFluidTransfer(
        destinationTank,
        sourceTank,
        1000,
        FluidAction.SIMULATE
);
```

确认模拟结果后执行：

```java
FluidStack moved = FluidUtil.tryFluidTransfer(
        destinationTank,
        sourceTank,
        1000,
        FluidAction.EXECUTE
);
```

该方法会：

1. 模拟从 source 排出流体。
2. 模拟 destination 接收流体。
3. 计算双方都能接受的实际数量。
4. 在 `EXECUTE` 下依次执行排出和接收。
5. 返回实际转移的 `FluidStack`。

## 为 CraftEngine 物品声明动态容器

CE item setting 只保存物品类型的规则，当前物品实例中的流体则保存在 PDC 中。

示例：

```yaml
example:canteen:
  material: glass_bottle
  settings:
    "libuid:fluid_container":
      capacity: 4000
```

带白名单的容器：

```yaml
example:juice_canteen:
  material: glass_bottle
  settings:
    "libuid:fluid_container":
      capacity: 4000
      allowed_fluids:
        - example:apple_juice
```

也可以使用流体标签：

```yaml
example:drink_canteen:
  material: glass_bottle
  settings:
    "libuid:fluid_container":
      capacity: 4000
      allowed_fluids:
        - "#example:drinkable"
```

规则：

- `capacity` 必须是正整数，单位为 mB。
- 不写 `allowed_fluids` 时允许所有已注册流体。
- 同一个容器中的流体必须是相同流体，并且 components 必须一致。
- 容器支持部分填充和部分排出。
- 动态流体内容使用 PDC key `libuid:fluid_stack`。
- 空容器会删除该 PDC key。
- 无法解码或引用未知流体的数据会被保留，不会被当作空容器覆盖。

## 读取和写入物品流体数据

```java
ItemFluidDataReadResult stored = ItemFluidData.read(item);

if (stored.status() == ItemFluidDataReadResult.Status.INVALID) {
    return;
}

FluidStack fluid = stored.fluid();
```

状态含义：

- `EMPTY`：没有流体数据，可以填充。
- `PRESENT`：存在有效的流体数据。
- `INVALID`：存在但无法安全解析的数据，必须保留并拒绝覆盖。

写入数据：

```java
ItemFluidData.write(item, FluidStack.of(appleJuice, 1000));
ItemFluidData.write(item, FluidStack.EMPTY);
```

写入 `FluidStack.EMPTY` 会删除 PDC key。

## 使用动态物品容器

从 CE 物品中解析 handler：

```java
Optional<FluidHandlerItem> handler = FluidContainerRegistry.handlerFor(item);
```

向物品倒入流体：

```java
FluidActionResult result = FluidUtil.tryFillContainer(
        item,
        sourceTank,
        4000,
        FluidAction.EXECUTE
);

if (result.success()) {
    ItemStack updatedItem = result.result();
    FluidStack moved = result.moved();
}
```

从物品倒出流体：

```java
FluidActionResult result = FluidUtil.tryEmptyContainer(
        item,
        destinationTank,
        4000,
        FluidAction.EXECUTE
);

if (result.success()) {
    ItemStack updatedItem = result.result();
}
```

传入的物品可能是堆叠物品。Libuid 只操作 count 为 1 的副本，调用插件必须负责：

1. 从原物品堆中扣除一件。
2. 将 `result.result()` 放回玩家手中、背包或容器槽位。
3. 处理结果物品无法放回时的掉落或失败逻辑。

`SIMULATE` 成功时只返回预计转移量，返回的物品内容保持不变，不应写回原物品。

## 原版桶

Libuid 内置支持：

- `minecraft:bucket`
- `minecraft:water_bucket`
- `minecraft:lava_bucket`
- `minecraft:milk_bucket`

原版桶的转移单位固定为 1000 mB：

- 空桶只有在目标流体至少有 1000 mB 时才能装满。
- 满桶只有在目标至少能接收 1000 mB 时才能倒出。
- 原版桶不接受带 components 的流体，以避免把流体附加数据静默丢失。
- 当空间不足一桶时，操作失败，原桶保持不变。

## 方块实体与物品之间的流体同步

如果插件拥有一个"含储罐的方块"并且希望玩家拆除后流体跟随物品保留，可以把方块定义为不可堆叠的 CE 物品：

```yaml
example:canteen_block:
  settings:
    "libuid:fluid_container":
      capacity: 4000
```

放置方块时，从物品读取流体并恢复到方块实体：

```java
ItemFluidDataReadResult stored = ItemFluidData.read(item);
if (stored.status() == ItemFluidDataReadResult.Status.INVALID) {
    return;
}
blockEntity.tank().setFluid(stored.fluid());
```

拆除方块并生成掉落物品时，把方块实体的流体写入物品：

```java
ItemFluidData.write(item, blockEntity.tank().fluid());
```

交互时建议优先执行插件自己的特殊流体配方，未匹配时再调用 `FluidUtil` 的通用容器转移逻辑。

## 持久化建议

方块实体可以直接使用 `FluidStackCodec` 保存流体：

```java
byte[] encoded = FluidStackCodec.toBinary(stack);
FluidStack restored = FluidStackCodec.fromBinary(encoded);
```

保存时应保留完整的 `FluidStack`，不要只保存流体 ID 和数量，否则会丢失 components。读取未知流体或未知 component 时，应保留原始数据并禁止覆盖为空。

## 流体配方匹配

单流体匹配：

```java
FluidIngredient ingredient = FluidIngredient.single(ResourceKey.of("example:apple_juice"));
boolean matches = ingredient.test(stack);
```

标签匹配：

```java
FluidIngredient ingredient = FluidIngredient.tag(ResourceKey.of("example:drinkable"));
```

配置解析可以使用：

```java
FluidIngredient ingredient = FluidIngredient.parse(rawConfigValue);
```

`FluidIngredient` 匹配流体类型，不匹配数量。需要同时限制数量时使用 `SizedFluidIngredient`。

## 构建和测试

在 Libuid 源码目录执行：

```bash
gradle clean build
```

发布到 Maven Local：

```bash
gradle publishToMavenLocal
```

构建要求 Java 21，并会运行项目中的 JUnit 测试。

## 版本与兼容性

当前版本：`1.0.1`。

Libuid 在 `paper-plugin.yml` 中声明了 `folia-supported: true`，因此可以在 Paper 与 Folia 上加载运行。

Libuid 的流体数据以 `ResourceKey` 标识。插件升级时不要随意更改已经投入使用的流体 key，否则旧存档、物品 PDC 和配方将无法找到原来的流体。

CraftEngine item setting 使用 `libuid:fluid_container` 命名空间，避免与其他插件的 setting 冲突。

## 许可证与贡献

本项目采用 GNU General Public License v3.0（GPL-3.0）授权，完整许可证文本见仓库根目录的 [`LICENSE`](LICENSE) 文件。

欢迎通过 Pull Request 提交代码、文档和测试改进。提交前请尽量确认：

- 改动范围清晰，并说明预期行为；
- 新功能或行为变化包含对应测试；
- 不引入密钥、私有配置或无授权的第三方内容；
- 提交内容符合 GPL-3.0 及相关第三方许可要求。

问题、错误报告和功能建议请在 GitHub 的 Issue 中反馈。请提供复现步骤、相关日志和运行环境信息。