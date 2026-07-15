# proxy-protocol

> [英文](README.md)

本项目基于 [PanSzelescik/proxy-protocol-support](https://github.com/PanSzelescik/proxy-protocol-support)。

**proxy-protocol** 现在以 **Minecraft 1.21.1 的 NeoForge 模组** 为主线维护，用于在服务器部署在 **HAProxy / TCPShield / Nginx** 等 TCP 反向代理之后，仍然保留并获取客户端的**真实 IP 地址**。该模组会解析 **Proxy Protocol** 数据包，并将其中的内容暴露给插件使用，使下游代码能够读取客户端原始的 `SocketAddress`，而不是代理服务器的地址。

例如，你可以使用 **[TCPShield](https://tcpshield.com/)** 或其他软件（如 **[Nginx](https://nginx.org/en/docs/stream/ngx_stream_proxy_module.html#proxy_protocol)**）来转发网络流量，从而隐藏服务器的真实 IP 地址。
在未启用 Proxy Protocol 的情况下，服务器控制台和插件中只能看到**代理服务器的 IP**；而通过解析并读取 Proxy Protocol 数据包，则可以正确获取并显示玩家的真实 IP 地址。

在我们的实际部署中，我们使用 **腾讯云 Lighthouse（LH）服务器** 作为反向代理，放置在运行于家庭网络中的 Minecraft 服务器前端，用于隐藏源服务器的真实 IP 地址。玩家连接到的是 LH 服务器，而真正的 Minecraft 服务器 IP 则被完全隐藏在其后。借助本模组，即使服务器位于反向代理之后，我们仍然可以获取玩家的真实 IP 地址（例如供需要玩家 IP 信息的 Bukkit 插件使用）。

## 架构布局

- 根目录：`build.gradle`/`settings.gradle`/`gradlew`/`gradle.properties` 只负责当前维护的 NeoForge 1.21.1 构建，并通过 `proxy-properties.gradle` 统一代理配置。
- `neoforge-1.21.1/`：当前维护主线（JDK 21），产出最新的 jar/shadow 产物。
- `forge-1.16.5/`、`forge-1.18.2/`、`forge-1.20.1/`：仅作为历史参考保留，**不再维护**；同时它们**不能使用 JDK 21**，其中 `1.16.5` 需要 JDK 8，`1.18.2`/`1.20.1` 需要 JDK 17。

## 构建方式

- 根目录的 `./gradlew buildAllTargets` 现在只构建当前维护的 `neoforge-1.21.1`，默认选择 JDK 21，也可以通过 `JAVA_HOME_21`、`JAVA_HOME_1_21` 或 `JAVA_HOME` 覆盖。
- 如果只想单独构建当前维护版本：进入 `neoforge-1.21.1` 目录执行 `./gradlew build`。
- 旧 Forge 目录已明确移出根聚合构建，原因是它们不再维护，且仍然依赖 JDK 8/JDK 17。
- 代理配置集中在根目录的 `gradle.properties`；CI 和子模块启动前都会设置这些 `systemProp.*`，无需在每个子工程重复写，运行时配置会保存为 `proxy-protocol.json`。

## 配置文件

本模组会在服务器启动目录（即 `mods/`、启动 `jar` 同级）查找 `proxy-protocol.json`，并根据 `com.hydroline.proxy.protocol.shared.config.Config` 中的字段初始化行为，放在运行目录即可自动被读取。

- `enable-proxy-protocol`（布尔）：是否启用 Proxy Protocol 解析，默认 `false`。
- `proxy-protocol-whitelisted-ips`（CIDR 数组）：允许通过 Proxy Protocol 的地址，默认包含 `127.0.0.1/32` 和 `::1/128`。
- `whitelistTCPShieldServers`（布尔）：开启后会把 TCPShield 官方白名单合并进去。
- `allow-direct-connections`（布尔）：即便启用了白名单，也允许直连玩家进入服务器。
- `whitelist-mode`（布尔）：开启后只能让白名单中的 IP 使用 Proxy Protocol。
- `debug-mode`（布尔）：打印初始化与白名单处理的调试日志。

示例（示例 IP 请替换为你自行控制的代理 IP 或在无固定 IP 时清空数组）：

```json
{
  "enable-proxy-protocol": true,
  "proxy-protocol-whitelisted-ips": ["192.0.2.1/32"],
  "whitelistTCPShieldServers": false,
  "allow-direct-connections": true,
  "whitelist-mode": false,
  "debug-mode": true
}
```

- 运行时配置契约保持不变：旧 Forge 版本可用的 `proxy-protocol.json` 可以 **1:1 原样复用** 到 NeoForge 1.21.1。
