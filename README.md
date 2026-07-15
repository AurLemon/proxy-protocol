# proxy-protocol

> [中文](README_zh.md)

Based on [PanSzelescik/proxy-protocol-support](https://github.com/PanSzelescik/proxy-protocol-support).

`proxy-protocol` is a NeoForge mod for Minecraft 1.21.1 that preserves clients’ real IPs when a Minecraft server is behind HAProxy/TCPShield/Nginx. The mod exposes the Proxy Protocol payload to plugins so downstream code can read the original `SocketAddress` instead of the proxy one.

For example you can use [TCPShield](https://tcpshield.com/ "TCPShield") or other software ([Nginx](https://nginx.org/en/docs/stream/ngx_stream_proxy_module.html#proxy_protocol "Nginx")) to forward traffic and hide your server's IP address. Without Proxy Protocol you only see the proxy's IP in console logs, but this mod reads the `PROXY` payload so plugins and downstream systems can display the client's real IP.

In our setup, we use a Tencent Lighthouse (LH) server as a reverse proxy in front of a Minecraft server running on our home network to hide the origin server’s real IP address. Players connect to the LH server, while the actual Minecraft server’s IP remains concealed behind it. With this mod, we can still obtain players’ real IP addresses (such as bukkit plugins that need to get player's IP address) even when the server is behind the reverse proxy.

## Layout

- `build.gradle`, `settings.gradle`, `gradlew`, `gradle.properties` (root) – aggregates the maintained NeoForge 1.21.1 build and keeps shared proxy settings via `proxy-properties.gradle`.
- `neoforge-1.21.1/` – maintained NeoForge 1.21.1 module (JDK 21) that produces the current jar/shadow artifact.
- `forge-1.16.5/`, `forge-1.18.2/`, `forge-1.20.1/` – legacy Forge modules kept only as historical references. They are no longer maintained and require non-JDK-21 environments (`1.16.5` needs JDK 8, `1.18.2`/`1.20.1` need JDK 17).

## Build

- Run `./gradlew buildAllTargets` from the repo root to build the maintained NeoForge 1.21.1 target; the root script automatically picks JDK 21 and can be overridden with `JAVA_HOME_21`, `JAVA_HOME_1_21`, or `JAVA_HOME`.
- To build only the maintained target, use `cd neoforge-1.21.1 && ./gradlew build`.
- The legacy Forge directories are intentionally excluded from the root aggregate build because they are no longer maintained and depend on older JDKs.
- Proxy configuration lives in `gradle.properties`; CI and the build script propagate those system props before any module runs, so you do not need to spray proxies in multiple files, and the runtime config always lives at `proxy-protocol.json`.

## Config

The mod looks for `proxy-protocol.json` in the same directory where the server is started (i.e., next to `mods/`, the vanilla `.jar`, and the modded launch wrapper). Drop the file there so the mod can read it on startup. The available options mirror `com.hydroline.proxy.protocol.shared.config.Config`.

- `enable-proxy-protocol` (boolean): controls whether the mod actively interprets Proxy Protocol payloads. Defaults to `false`.
- `proxy-protocol-whitelisted-ips` (array of CIDR strings): IP ranges allowed to handshake via Proxy Protocol. Two loopback entries are provided by default (`"127.0.0.1/32"` and `"::1/128"`).
- `whitelistTCPShieldServers` (boolean): if `true`, the built-in TCPShield CIDR lists merge with the static whitelist.
- `allow-direct-connections` (boolean): permits non-proxy direct connections even when the whitelist feature is enabled.
- `whitelist-mode` (boolean): when enabled, only the configured whitelist entries are allowed through the Proxy Protocol path.
- `debug-mode` (boolean): enables extra logging emitted during initialization and whitelist processing.

Example (replace the sample IP with your upstream proxy or leave an empty list if you do not have a fixed IP):

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

- The runtime configuration contract is intentionally unchanged: `proxy-protocol.json` from the older Forge builds can be reused 1:1 on NeoForge 1.21.1.
